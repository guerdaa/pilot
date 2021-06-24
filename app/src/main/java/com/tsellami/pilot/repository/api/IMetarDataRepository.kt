package com.tsellami.pilot.repository.api

import com.tsellami.pilot.data.metar.MetarData

interface IMetarDataRepository {

    suspend fun retrieveMetarData(icao: String): MetarData

    suspend fun retrieveMetarDataRemotely(icao: String): MetarData

    suspend fun deleteMetarData(icao: String)

    suspend fun deleteOldMetarData()

    suspend fun insertMetarData(metarData: MetarData)

    suspend fun updateOutdatedFavoriteMetarData()
}