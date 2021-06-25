package com.tsellami.pilot

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsellami.pilot.repository.api.IMetarDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val metarDataRepository: IMetarDataRepository
): ViewModel(){

    private val channel = Channel<OnlineUpdatingEvent>()
    val event = channel.receiveAsFlow()

    fun updateCacheOnNetworkAvailable() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.let {
            it.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    try {
                        viewModelScope.launch {
                            metarDataRepository.updateOutdatedFavoriteMetarData()
                            metarDataRepository.deleteOldMetarData()
                        }
                    } catch (e: Exception) {
                        viewModelScope.launch {
                            channel.send(OnlineUpdatingEvent.UpdatingFailed)
                        }
                    }
                }
            })
        }
    }

    sealed class OnlineUpdatingEvent {
        object UpdatingFailed: OnlineUpdatingEvent()
    }

}