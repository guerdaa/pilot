package com.tsellami.pilot.repository

import com.tsellami.pilot.data.metar.MetarDao
import com.tsellami.pilot.data.metar.MetarData
import com.tsellami.pilot.network.metar.MetarApi
import com.tsellami.pilot.repository.api.IMetarDataRepository
import java.util.concurrent.TimeUnit

class MetarDataRepository(
    private val metarDao: MetarDao,
    private val metarApi: MetarApi
) : IMetarDataRepository {

    override suspend fun retrieveMetarData(icao: String): MetarData {
        val localMetarData = metarDao.getMetarDataByIcao(icao)
        localMetarData?.let {
            return it
        }
        return retrieveMetarDataRemotely(icao)
    }

    override suspend fun retrieveMetarDataRemotely(icao: String): MetarData {
        val metarDecoded = metarApi.getDecodedMetarData(icao)
        val metarRaw = metarApi.getRawMetarData(icao)
        insertMetarData(MetarData(icao, metarDecoded, metarRaw))
        return MetarData(icao, metarDecoded, metarRaw)
    }

    override suspend fun deleteMetarData(icao: String) {
        metarDao.deleteMetarData(icao)
    }

    override suspend fun deleteOldMetarData() {
        metarDao.deleteInvalidMetarData(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1))
    }

    override suspend fun insertMetarData(metarData: MetarData) {
        metarDao.insertMetarData(metarData)
    }

    override suspend fun updateOutdatedFavoriteMetarData() {
        val outdatedMetar = metarDao.getOutdatedFavoriteMetarData(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1))
        outdatedMetar.forEach {
            retrieveMetarDataRemotely(it.icao)
        }
    }
}