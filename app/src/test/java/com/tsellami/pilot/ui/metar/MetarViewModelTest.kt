package com.tsellami.pilot.ui.metar

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.tsellami.pilot.MainCoroutineRule
import com.tsellami.pilot.R
import com.tsellami.pilot.data.airport.Airport
import com.tsellami.pilot.data.metar.MetarData
import com.tsellami.pilot.repository.api.IAirportRepository
import com.tsellami.pilot.repository.api.IMetarDataRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MetarViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val airportRepository: IAirportRepository = mockk(relaxed = true)
    private val metarDataRepository: IMetarDataRepository = mockk(relaxed = true)
    private lateinit var metarViewModel: MetarViewModel

    @Before
    fun setUp() {
        metarViewModel = MetarViewModel(airportRepository, metarDataRepository)
    }

    @Test
    fun `Assert that when MetarViewModel is initialized from BottomNavigation, NotStarted is sent`() {
        runBlocking {
            metarViewModel.setInitialData("")

            Assert.assertTrue(metarViewModel.queryEvent.first() is MetarViewModel.QueryEvent.NotStarted)
        }
    }

    @Test
    fun `Assert that when MetarViewModel is initialized from FavoriteFragment and a corresponding airport exists, metarData will be retrieved`() {
        runBlocking {
            val airport = Airport("EDDF", "Frankfurt")
            val metarData = MetarData("EDDF", "DECODED", "RAW")
            coEvery { airportRepository.retrieveAirportByIcao(any()) } returns airport
            coEvery { metarDataRepository.retrieveMetarData("EDDF") } returns metarData

            metarViewModel.setInitialData("EDDF")

            Assert.assertTrue(metarViewModel.queryEvent.first() is MetarViewModel.QueryEvent.Loading)
            coVerify { metarDataRepository.retrieveMetarData("EDDF") }
            coVerify { airportRepository.retrieveAirportByIcao("EDDF") }
            delay(1)
            val flow = metarViewModel.queryEvent.first()
            Assert.assertTrue(flow is MetarViewModel.QueryEvent.Retrieved)
            Assert.assertEquals((flow as MetarViewModel.QueryEvent.Retrieved).metarData, metarData)
        }
    }

    @Test
    fun `Assert that when MetarViewModel is initialized from FavoriteFragment and a corresponding airport exists, metarData will be retrieved by name`() {
        runBlocking {
            val airport = Airport("EDDF", "Frankfurt")
            val metarData = MetarData("EDDF", "DECODED", "RAW")
            coEvery { airportRepository.retrieveAirportByName(any()) } returns airport
            coEvery { metarDataRepository.retrieveMetarData("EDDF") } returns metarData

            metarViewModel.setInitialData("Frankfurt")

            Assert.assertTrue(metarViewModel.queryEvent.first() is MetarViewModel.QueryEvent.Loading)
            coVerify { metarDataRepository.retrieveMetarData("EDDF") }
            coVerify { airportRepository.retrieveAirportByName("Frankfurt") }
            delay(1)
            val flow = metarViewModel.queryEvent.first()
            Assert.assertTrue(flow is MetarViewModel.QueryEvent.Retrieved)
            Assert.assertEquals((flow as MetarViewModel.QueryEvent.Retrieved).metarData, metarData)
        }
    }

    @Test
    fun `Assert that when MetarViewModel is initialized from FavoriteFragment and a corresponding airport does not exist, NotFound will be send`() {
        runBlocking {
            coEvery { airportRepository.retrieveAirportByIcao(any()) } returns null

            metarViewModel.setInitialData("EDDF")

            Assert.assertTrue(metarViewModel.queryEvent.first() is MetarViewModel.QueryEvent.Loading)
            coVerify { airportRepository.retrieveAirportByIcao("EDDF") }
            delay(1)
            Assert.assertTrue(metarViewModel.queryEvent.first() is MetarViewModel.QueryEvent.NotFound)
        }
    }

    @Test
    fun `Assert that when MetarViewModel is initialized from FavoriteFragment and an error occurs, Error will be send`() {
        runBlocking {
            metarViewModel.setInitialData("EDDF")

            Assert.assertTrue(metarViewModel.queryEvent.first() is MetarViewModel.QueryEvent.Loading)
            coVerify { airportRepository.retrieveAirportByIcao("EDDF") }
            delay(1)
            Assert.assertTrue(metarViewModel.queryEvent.first() is MetarViewModel.QueryEvent.Error)
        }
    }

    @Test
    fun `Assert that refreshMetarDataManually will delete the corresponding cached MetarData`() {
        runBlocking {
            initAirportLiveData()

            metarViewModel.refreshMetarDataManually()

            delay(1)
            Assert.assertTrue(metarViewModel.queryEvent.first() is MetarViewModel.QueryEvent.Loading)
            coVerify { metarDataRepository.deleteMetarData(any()) }
            coVerify { metarDataRepository.retrieveMetarDataRemotely(any()) }
        }
    }

    @Test
    fun `Assert editFavorite will update favorites`() {
        runBlocking {
            initAirportLiveData()

            metarViewModel.editFavorite()

            coVerify { airportRepository.editFavoriteAirport(any()) }
        }
    }

    @Test
    fun `Assert that setFavoriteImage will return the uncolored icon when it is not favorite`() {
        runBlocking {
            initAirportLiveData()

            val icon = metarViewModel.setFavoriteImage()

            Assert.assertEquals(icon, R.drawable.ic_unfavorite)
        }
    }

    @Test
    fun `Assert that setFavoriteImage will return the uncolored icon when it is favorite`() {
        runBlocking {
            initAirportLiveData(true)

            val icon = metarViewModel.setFavoriteImage()

            Assert.assertEquals(icon, R.drawable.ic_favorite)
        }
    }

    private suspend fun initAirportLiveData(favorite: Boolean = false) {
        val airport = Airport("EDDF", "Frankfurt", favorite)
        val metarData = MetarData("EDDF", "DECODED", "RAW")
        coEvery { airportRepository.retrieveAirportByIcao(any()) } returns airport
        coEvery { metarDataRepository.retrieveMetarData("EDDF") } returns metarData
        metarViewModel.setInitialData("EDDF")
        metarViewModel.queryEvent.first()
        delay(1)
        metarViewModel.queryEvent.first()
        coEvery { airportRepository.retrieveAirportByIcao("EDDF") } returns Airport("EDDF", "FR")
    }
}