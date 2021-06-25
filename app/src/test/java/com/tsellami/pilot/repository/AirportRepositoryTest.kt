package com.tsellami.pilot.repository

import com.tsellami.pilot.data.airport.Airport
import com.tsellami.pilot.data.airport.AirportDao
import com.tsellami.pilot.data.query.QueryDao
import com.tsellami.pilot.data.query.QueryData
import com.tsellami.pilot.network.airport.AirportApi
import com.tsellami.pilot.network.airport.AirportDto
import com.tsellami.pilot.network.airport.AirportResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AirportRepositoryTest {

    private val airportDao: AirportDao = mockk(relaxed = true)
    private val airportApi: AirportApi = mockk(relaxed = true)
    private val queryDao: QueryDao = mockk(relaxed = true)
    private lateinit var airportRepository: AirportRepository

    @Before
    fun setUp() {
        airportRepository = AirportRepository(
            airportDao, airportApi, queryDao
        )
    }

    @Test
    fun `Assert that inserting an already added airport will fail`() {
        runBlocking {
            val airport = Airport(ICAO, NAME)
            coEvery { airportDao.getAirportByIcao(any()) } returns airport

            val isInserted = airportRepository.insertNewAirport(airport)

            Assert.assertEquals(isInserted, false)
        }
    }

    @Test
    fun `Assert that inserting an airport will succeed if it is not already added`() = runBlocking {
            val airport = Airport(ICAO, NAME)
            coEvery { airportDao.getAirportByIcao(any()) } returns null

            val isInserted = airportRepository.insertNewAirport(airport)

            Assert.assertEquals(isInserted, true)
    }

    @Test
    fun `Assert that searching an airport by icao will be retrieved locally if an entry for the provided icao is already cached`() {
        runBlocking {
            val airport = Airport(ICAO, NAME)
            coEvery { airportDao.getAirportByIcao(ICAO) } returns airport

            val retrievedAirport = airportRepository.retrieveAirportByIcao(ICAO)

            Assert.assertEquals(retrievedAirport, airport)
        }
    }

    @Test
    fun `Assert that searching an airport by icao will be retrieved remotely if no entry for the provided icao is cached`() {
        runBlocking {
            val airport = AirportDto(ICAO, NAME)
            val airports = AirportResponse(items = listOf(airport))
            coEvery { airportDao.getAirportByIcao(ICAO) } returns null
            coEvery { airportApi.getAirport(ICAO, 1) } returns airports

            val retrievedAirport = airportRepository.retrieveAirportByIcao(ICAO)

            coVerify { airportDao.getAirportByIcao(ICAO) }
            coVerify { airportDao.insertAirport(any()) }
            Assert.assertEquals(retrievedAirport, airports.items.first().toAirport())
        }
    }

    @Test
    fun `Assert that retrieving airport by name will return null in case a query is already performed by this name and no airport is found`() {
        runBlocking {
            coEvery { queryDao.getAirportNameByQuery(QUERY) } returns QueryData(QUERY, null)

            val airport = airportRepository.retrieveAirportByName(QUERY)

            coVerify(exactly = 0) { airportDao.getAirportByAirportName(NAME) }
            coVerify(exactly = 0) { airportApi.getAirport(any(), any()) }
            coVerify(exactly = 0) { queryDao.insertQuery(any()) }
            coVerify(exactly = 0) { airportDao.getAirportByIcao(any()) }
            Assert.assertNull(airport)
        }
    }

    @Test
    fun `Assert that retrieving airport by name will succeed locally in case a query is already performed by this name and an airport found`() {
        runBlocking {
            val airport = Airport(ICAO, NAME)
            coEvery { queryDao.getAirportNameByQuery(QUERY) } returns QueryData(QUERY, NAME)
            coEvery { airportDao.getAirportByAirportName(NAME) } returns listOf(airport)

            val retrievedAirport = airportRepository.retrieveAirportByName(QUERY)

            coVerify { airportDao.getAirportByAirportName(NAME) }
            coVerify(exactly = 0) { airportApi.getAirport(any(), any()) }
            coVerify(exactly = 0) { queryDao.insertQuery(any()) }
            coVerify(exactly = 0) { airportDao.getAirportByIcao(any()) }
            Assert.assertEquals(retrievedAirport, airport)
        }
    }

    @Test
    fun `Assert that retrieving airport by name will be remotely and return null in case no airport found`() {
        runBlocking {
            coEvery { queryDao.getAirportNameByQuery(QUERY) } returns QueryData(QUERY, NAME)
            coEvery { airportDao.getAirportByAirportName(NAME) } returns emptyList()
            coEvery { airportApi.getAirport(NAME, 1).items } returns emptyList()

            val retrievedAirport = airportRepository.retrieveAirportByName(QUERY)

            coVerify { airportDao.getAirportByAirportName(NAME) }
            coVerify(exactly = 1) { airportApi.getAirport(NAME, 1) }
            coVerify(exactly = 1) { queryDao.insertQuery(any()) }
            coVerify(exactly = 0) { airportDao.getAirportByIcao(any()) }
            Assert.assertNull(retrievedAirport)
        }
    }

    @Test
    fun `Assert that retrieving airport by name will be remotely and return null if the found airport is not in germany`() {
        runBlocking {
            val airport = AirportDto(ICAO, NAME)
            coEvery { queryDao.getAirportNameByQuery(QUERY) } returns QueryData(QUERY, NAME)
            coEvery { airportDao.getAirportByAirportName(NAME) } returns emptyList()
            coEvery { airportApi.getAirport(NAME, 1).items } returns listOf(airport)

            val retrievedAirport = airportRepository.retrieveAirportByName(QUERY)

            coVerify { airportDao.getAirportByAirportName(NAME) }
            coVerify(exactly = 1) { airportApi.getAirport(NAME, 1) }
            coVerify(exactly = 1) { queryDao.insertQuery(any()) }
            coVerify(exactly = 0) { airportDao.getAirportByIcao(any()) }
            Assert.assertNull(retrievedAirport)
        }
    }

    @Test
    fun `Assert that retrieving airport by name will succeed remotely if the found airport is in germany`() {
        runBlocking {
            val airportDto = AirportDto(GERMAN_ICAO, NAME)
            coEvery { queryDao.getAirportNameByQuery(QUERY) } returns QueryData(QUERY, NAME)
            coEvery { airportDao.getAirportByAirportName(NAME) } returns emptyList()
            coEvery { airportApi.getAirport(NAME, 1).items } returns listOf(airportDto)
            coEvery { airportDao.getAirportByIcao(GERMAN_ICAO) } returns airportDto.toAirport()

            val retrievedAirport = airportRepository.retrieveAirportByName(QUERY)

            coVerify { airportDao.getAirportByAirportName(NAME) }
            coVerify(exactly = 1) { airportApi.getAirport(NAME, 1) }
            coVerify(exactly = 1) { queryDao.insertQuery(any()) }
            coVerify(exactly = 1) { airportDao.getAirportByIcao(any()) }
            Assert.assertEquals(retrievedAirport, airportDto.toAirport())
        }
    }

    @Test
    fun `Assert that retrieveFavoriteAirports will retrieve favorites from DAO`() {
        runBlocking {
            val favorites = listOf(Airport(ICAO, NAME))
            coEvery { airportDao.getFavorites() } returns favorites

            val retrievedFavorites = airportRepository.retrieveFavoriteAirports()

            coVerify { airportDao.getFavorites() }
            Assert.assertEquals(favorites, retrievedFavorites)
        }
    }

    @Test
    fun `Assert that editFavoriteAirport will invoke AirportDao`() {
        runBlocking {
            val airport: Airport = mockk(relaxed = true)

            airportRepository.editFavoriteAirport(airport)

            coVerify { airportDao.update(airport) }
        }
    }

    companion object {
        private const val ICAO = "icao"
        private const val NAME = "AIRPORT_NAME"
        private const val QUERY = "QUERY"
        private const val GERMAN_ICAO = "ED__"
    }
}