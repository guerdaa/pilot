package com.tsellami.pilot.repository

import com.tsellami.pilot.data.metar.MetarDao
import com.tsellami.pilot.data.metar.MetarData
import com.tsellami.pilot.network.metar.MetarApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class MetarDataRepositoryTest {

    private val metarDao: MetarDao = mockk(relaxed = true)
    private val metarApi: MetarApi = mockk(relaxed = true)
    private lateinit var metarDataRepository: MetarDataRepository

    @Before
    fun setUp() {
        metarDataRepository = MetarDataRepository(metarDao, metarApi)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Assert that retrieving metar data will be performed locally if it is already cached`() {
        runBlocking {
            val metarData = MetarData(ICAO, DECODED_DATA, RAW_DATA)
            coEvery { metarDao.getMetarDataByIcao(ICAO) } returns metarData

            val retrievedMetarData = metarDataRepository.retrieveMetarData(ICAO)

            Assert.assertEquals(metarData, retrievedMetarData)
            coVerify(exactly = 0) { metarApi.getDecodedMetarData(any()) }
            coVerify(exactly = 0) { metarApi.getRawMetarData(any()) }
            coVerify(exactly = 0) { metarDao.insertMetarData(any()) }
        }
    }

    @Test
    fun `Assert that retrieving metar data will be performed remotely if it is not cached`() {
        runBlocking {
            val metarData = MetarData(ICAO, DECODED_DATA, RAW_DATA)
            coEvery { metarDao.getMetarDataByIcao(ICAO) } returns null
            coEvery { metarApi.getRawMetarData(ICAO) } returns RAW_DATA
            coEvery { metarApi.getDecodedMetarData(ICAO) } returns DECODED_DATA

            val retrievedMetarData = metarDataRepository.retrieveMetarData(ICAO)

            Assert.assertEquals(metarData.decodedData, retrievedMetarData.decodedData)
            Assert.assertEquals(metarData.rawData, retrievedMetarData.rawData)
            coVerify(exactly = 1) { metarApi.getDecodedMetarData(any()) }
            coVerify(exactly = 1) { metarApi.getRawMetarData(any()) }
            coVerify(exactly = 1) { metarDao.insertMetarData(any()) }
        }
    }

    @Test
    fun `Assert that deleteMetarData will invoke DAO`() {
        runBlocking {
            metarDataRepository.deleteMetarData(ICAO)

            coVerify { metarDao.deleteMetarData(ICAO) }
        }
    }

    @Test
    fun `Assert that deleteOldMetarData will delete old Data`() {
        runBlocking {
            metarDataRepository.deleteOldMetarData()

            coVerify { metarDao.deleteInvalidMetarData(any()) }
        }
    }

    @Test
    fun `Assert that insertMetarData will insert a new MetarData entry`() {
        runBlocking {
            val metarData = MetarData(ICAO, DECODED_DATA, RAW_DATA)

            metarDataRepository.insertMetarData(metarData)

            coVerify { metarDao.insertMetarData(metarData) }
        }
    }

    companion object {
        private const val ICAO = "ICAO"
        private const val DECODED_DATA = "DECODED DATA"
        private const val RAW_DATA = "RAW DATA"
    }
}