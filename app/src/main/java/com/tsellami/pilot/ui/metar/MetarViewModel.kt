package com.tsellami.pilot.ui.metar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsellami.pilot.R
import com.tsellami.pilot.data.airport.Airport
import com.tsellami.pilot.data.metar.MetarData
import com.tsellami.pilot.repository.AirportRepository
import com.tsellami.pilot.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class MetarViewModel @Inject constructor(
    private val airportRepository: AirportRepository
) : ViewModel() {

    private val _airport = MutableLiveData<Airport?>()
    val airport: LiveData<Airport?>
        get() = _airport

    val query = MutableLiveData<String>()

    private val channel = Channel<QueryEvent>()
    val queryEvent = channel.receiveAsFlow()

    suspend fun setInitialData(icao: String) {
        if (icao.isEmpty()) {
            channel.send(QueryEvent.NotStarted)
        } else {
            retrieveMetarData(icao)
        }
    }

    private suspend fun retrieveAirportByIcao(icao: String) {
        _airport.value = airportRepository.retrieveAirportByIcao(icao)
    }

    private suspend fun retrieveAirportByName(name: String) {
        _airport.value = airportRepository.retrieveAirportByName(name)

    }

    private suspend fun retrieveAirport(query: String) {
        if (Utils.checkIcao(query)) {
            retrieveAirportByIcao(query)
        } else {
            retrieveAirportByName(query)
        }
    }

    fun retrieveMetarData(query: String) {
        viewModelScope.launch {
            try {
                channel.send(QueryEvent.Loading)
                retrieveAirport(query)
                if (_airport.value != null) {
                    _airport.value?.icao?.let {
                        val data = airportRepository.retrieveMetarData(it)
                        channel.send(QueryEvent.Retrieved(data))
                    }
                } else {
                    channel.send(QueryEvent.NotFound)
                }
            } catch (e: Exception) {
                channel.send(QueryEvent.Error)
            }

        }
    }

    fun refreshMetarDataManually() {
        viewModelScope.launch {
            channel.send(QueryEvent.Loading)
            airportRepository.deleteMetarData(airport.value!!.icao)
            val data = airportRepository.retrieveMetarDataRemotely(airport.value!!.icao)
            channel.send(QueryEvent.Retrieved(data))
        }
    }

    fun editFavorite() {
        viewModelScope.launch {
            _airport.value?.let {
                _airport.value = it.copy(favorite = !it.favorite)
                airportRepository.editFavoriteAirport(_airport.value!!)
            }
        }
    }

    fun setFavoriteImage(): Int {
        _airport.value?.let {
            return if (it.favorite)
                R.drawable.ic_favorite
            else
                R.drawable.ic_unfavorite
        }
        return R.drawable.ic_unfavorite
    }

    suspend fun updateOutdatedFavorites() {
        try {
            airportRepository.updateOutdatedFavoriteMetarData()
        } catch (e: Exception) {

        }
    }

    sealed class QueryEvent {
        object Loading: QueryEvent()
        object NotFound: QueryEvent()
        object NotStarted: QueryEvent()
        object Error: QueryEvent()
        data class Retrieved(val metarData: MetarData): QueryEvent()
    }
}