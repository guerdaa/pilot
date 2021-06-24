package com.tsellami.pilot.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsellami.pilot.data.airport.Airport
import com.tsellami.pilot.repository.api.IAirportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val airportRepository: IAirportRepository
) : ViewModel() {

    private val channel = Channel<FavoriteEvents>()
    val favoriteEvent = channel.receiveAsFlow()

    init {
        retrieveFavoriteAirports()
    }

    private fun retrieveFavoriteAirports() {
        viewModelScope.launch {
            channel.send(FavoriteEvents.Loading)
            val favorites = airportRepository.retrieveFavoriteAirports()
            if (favorites.isEmpty())
                channel.send(FavoriteEvents.Empty)
            else
                channel.send(FavoriteEvents.Successful(favorites))
        }
    }

    sealed class FavoriteEvents {
        object Loading: FavoriteEvents()
        object Empty: FavoriteEvents()
        data class Successful(val favorites: List<Airport>): FavoriteEvents()
    }
}