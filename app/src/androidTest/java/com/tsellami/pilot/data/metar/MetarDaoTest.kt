package com.tsellami.pilot.data.metar

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.tsellami.pilot.data.DaoTest
import com.tsellami.pilot.data.airport.Airport
import com.tsellami.pilot.data.airport.AirportDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class MetarDaoTest: DaoTest() {

    private lateinit var metarDao: MetarDao
    private lateinit var airportDao: AirportDao

    @Before
    override fun setUp() {
        super.setUp()
        metarDao = pilotDatabase.metarDao()
        airportDao = pilotDatabase.airportDao()
    }

    @Test
    fun testGetMetarDataByIcao_afterInsertingOneElement() {
        runBlocking {
            val metarData = MetarData(ICAO, DECODED_DATA, RAW_DATA)
            metarDao.insertMetarData(metarData)

            val retrieved = metarDao.getMetarDataByIcao(ICAO)

            Assert.assertEquals(metarData, retrieved)
        }
    }

    @Test
    fun testDeleteMetarData_afterInsertingOneElement() {
        runBlocking {
            val metarData = MetarData(ICAO, DECODED_DATA, RAW_DATA)
            metarDao.insertMetarData(metarData)
            metarDao.deleteMetarData(ICAO)

            val retrieved = metarDao.getMetarDataByIcao(ICAO)

            Assert.assertNull(retrieved)
        }
    }

    @Test
    fun testUpdateMetarData_afterInsertingOneElement() {
        runBlocking {
            val metarData = MetarData(ICAO, DECODED_DATA, RAW_DATA)
            metarDao.insertMetarData(metarData)
            val updatedMetarData = metarData.copy(decodedData = "UPDATED_DECODED_DATA")

            metarDao.updateMetarData(updatedMetarData)

            val retrieved = metarDao.getMetarDataByIcao(ICAO)
            Assert.assertEquals(retrieved, updatedMetarData)
        }
    }

    @Test
    fun testDeleteAllMetarData_afterInsertingMultipleElements() {
        runBlocking {
            val metarData1 = MetarData("ICAO1", DECODED_DATA, RAW_DATA)
            val metarData2 = MetarData("ICAO2", DECODED_DATA, RAW_DATA)
            metarDao.insertMetarData(metarData1)
            metarDao.insertMetarData(metarData2)

            metarDao.deleteAllMetarData()

            val retrieved1 = metarDao.getMetarDataByIcao("ICAO1")
            Assert.assertNull(retrieved1)
            val retrieved2 = metarDao.getMetarDataByIcao("ICAO2")
            Assert.assertNull(retrieved2)
        }
    }

    @Test
    fun testDeleteInvalidMetarData() {
        runBlocking {
            val favoriteAirport = Airport("ICAO1", "NAME", true)
            val airport = Airport("ICAO2", "NAME", false)
            airportDao.insertAirport(favoriteAirport)
            airportDao.insertAirport(airport)
            val metarData1 = MetarData("ICAO1", DECODED_DATA, RAW_DATA, 1L)
            val metarData2 = MetarData("ICAO2", DECODED_DATA, RAW_DATA, 1L)
            metarDao.insertMetarData(metarData1)
            metarDao.insertMetarData(metarData2)

            metarDao.deleteInvalidMetarData(2L)

            val retrievedMetarData1 = metarDao.getMetarDataByIcao("ICAO1")
            val retrievedMetarData2 = metarDao.getMetarDataByIcao("ICAO2")
            Assert.assertEquals(metarData1, retrievedMetarData1)
            Assert.assertNull(retrievedMetarData2)
        }
    }

    @Test
    fun testGetOutdatedFavoriteMetarData() {
        runBlocking {
            val favoriteAirport1 = Airport("ICAO1", "NAME", true)
            val favoriteAirport2 = Airport("ICAO3", "NAME", true)
            val airport = Airport("ICAO2", "NAME", false)
            airportDao.insertAirport(favoriteAirport1)
            airportDao.insertAirport(favoriteAirport2)
            airportDao.insertAirport(airport)
            val metarData1 = MetarData("ICAO1", DECODED_DATA, RAW_DATA, 1L)
            val metarData2 = MetarData("ICAO2", DECODED_DATA, RAW_DATA, 1L)
            val metarData3 = MetarData("ICAO3", DECODED_DATA, RAW_DATA, 3L)
            metarDao.insertMetarData(metarData1)
            metarDao.insertMetarData(metarData2)
            metarDao.insertMetarData(metarData3)

            val retrieved = metarDao.getOutdatedFavoriteMetarData(2L)

            Assert.assertEquals(retrieved, listOf(metarData1))
        }
    }

    companion object {
        private const val DECODED_DATA = "DECODED_DATA"
        private const val RAW_DATA = "RAW_DATA"
        private const val ICAO = "ICAO"
    }

}