package com.tsellami.pilot.data.airport

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Dao
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.tsellami.pilot.data.DaoTest
import com.tsellami.pilot.data.PilotDatabase
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class AirportDaoTest: DaoTest() {

    private lateinit var dao: AirportDao

    @Before
    override fun setUp() {
        super.setUp()
        dao = pilotDatabase.airportDao()
    }

    @Test
    fun testGetAirportByIcao_afterInsertingOneElement() {
        runBlocking {
            val airport = Airport("ICAO", "NAME")
            dao.insertAirport(airport)

            val retrieved = dao.getAirportByIcao("ICAO")

            Assert.assertEquals(airport, retrieved)
        }
    }

    @Test
    fun testGetAirportByAirportName_afterInsertingTwoElements() {
        runBlocking {
            val airport1 = Airport("ICAO1", "NAME1")
            val airport2 = Airport("ICAO2", "NAME1")
            dao.insertAirport(airport1)
            dao.insertAirport(airport2)

            val retrieved = dao.getAirportByAirportName("NAME1")

            Assert.assertEquals(listOf(airport1, airport2), retrieved)
        }
    }

    @Test
    fun testGetFavorites_afterInsertingElements() {
        runBlocking {
            val airport1 = Airport("ICAO1", "NAME1", true)
            val airport2 = Airport("ICAO2", "NAME1", false)
            dao.insertAirport(airport1)
            dao.insertAirport(airport2)

            val retrieved = dao.getFavorites()

            Assert.assertEquals(listOf(airport1), retrieved)
        }
    }

    @Test
    fun testUpdate_afterInsertingElements() {
        runBlocking {
            val airport1 = Airport("ICAO1", "NAME1", true)
            dao.insertAirport(airport1)
            val updatedAirport = airport1.copy(name = "UpdatedName")

            dao.update(updatedAirport)
            val retrieved = dao.getAirportByIcao("ICAO1")

            Assert.assertEquals(updatedAirport, retrieved)
        }
    }
}