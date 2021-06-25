package com.tsellami.pilot.ui.favorite

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.tsellami.pilot.MainCoroutineRule
import com.tsellami.pilot.data.airport.Airport
import com.tsellami.pilot.repository.api.IAirportRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class FavoriteViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val airportRepository: IAirportRepository = mockk(relaxed = true)
    private lateinit var favoriteViewModel: FavoriteViewModel

    @Test
    fun `Assert that FavoriteEvents Empty is sent when there is no favorites`() {
        runBlocking {
            coEvery { airportRepository.retrieveFavoriteAirports() } returns emptyList()

            favoriteViewModel = FavoriteViewModel(airportRepository)

            Assert.assertTrue(favoriteViewModel.favoriteEvent.first() is FavoriteViewModel.FavoriteEvents.Loading)
            delay(1)
            Assert.assertTrue(favoriteViewModel.favoriteEvent.first() is FavoriteViewModel.FavoriteEvents.Empty)
        }
    }

    @Test
    fun `Assert that FavoriteEvents Empty is sent when there are stored favorites`() {
        runBlocking {
            val favorites = listOf(Airport(ICAO, NAME))
            coEvery { airportRepository.retrieveFavoriteAirports() } returns favorites

            favoriteViewModel = FavoriteViewModel(airportRepository)

            Assert.assertTrue(favoriteViewModel.favoriteEvent.first() is FavoriteViewModel.FavoriteEvents.Loading)

            delay(1)
            val flow = favoriteViewModel.favoriteEvent.first()

            Assert.assertTrue(flow is FavoriteViewModel.FavoriteEvents.Successful)
            Assert.assertEquals((flow as FavoriteViewModel.FavoriteEvents.Successful).favorites, favorites)
        }
    }

    companion object {
        private const val ICAO = "ICAO"
        private const val NAME = "NAME"
    }
}