package com.tsellami.pilot.repository

import android.util.Log
import com.tsellami.pilot.data.airport.Airport
import com.tsellami.pilot.data.airport.AirportDao
import com.tsellami.pilot.data.metar.MetarDao
import com.tsellami.pilot.data.metar.MetarData
import com.tsellami.pilot.data.query.QueryDao
import com.tsellami.pilot.data.query.QueryData
import com.tsellami.pilot.network.airport.AirportApi
import com.tsellami.pilot.network.airport.AirportDto
import com.tsellami.pilot.network.metar.MetarApi
import com.tsellami.pilot.utils.Utils
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AirportRepository @Inject constructor(
    private val airportDao: AirportDao,
    private val airportApi: AirportApi,
    private val metarDao: MetarDao,
    private val metarApi: MetarApi,
    private val queryDao: QueryDao
) {

    private suspend fun insertNewAirport(newEntry: Airport): Boolean {
        val airport = airportDao.getAirportByIcao(newEntry.icao)
        airport?.let {
            return false
        }
        airportDao.insertAirport(newEntry)
        return true
    }

    suspend fun retrieveAirportByIcao(icao: String): Airport {
        val airport = airportDao.getAirportByIcao(icao)
        return if (airport == null) {
            Log.d("AirportRepository", "retrieved from API")
            val newEntry = airportApi.getAirport(icao, 1).items.first().toAirport()
            insertNewAirport(newEntry)
            newEntry
        } else {
            Log.d("AirportRepository", "retrieved from DAO")
            airport
        }
    }

    suspend fun retrieveAirportByName(name: String): Airport? {
        val retrievedQuery = getAirportNameByOldQuery(name)
        var query = name
        if (retrievedQuery != null && retrievedQuery.airportName == null) {
            Log.d("airportrepository", "it is null")
                return null
        } else if (retrievedQuery?.airportName != null) {
            query = retrievedQuery.airportName
        }
        val localAirports = airportDao.getAirportByAirportName(query)
        if (localAirports.isNotEmpty()) {
            localAirports.first().let {
                Log.d("AirportRepository", "retrieveAirportByName from DAO")
                return it
            }
        } else {
            Log.d("AirportRepository", "retrieveAirportByName from API")
            val retrievedAirports = airportApi.getAirport(name, 1).items
            return if (retrievedAirports.isNotEmpty()) {
                retrieveGermanyAirport(retrievedAirports, name)
            } else {
                insertQuery(name, null)
                null
            }
        }
    }

    private suspend fun retrieveGermanyAirport(airports: List<AirportDto>, name: String): Airport? {
        val airport = airports.first().toAirport()
        return if (Utils.checkIcao(airport.icao)) {
            insertQuery(name, airport.name)
            insertNewAirport(airport)
            airport
        }
        else {
            insertQuery(name, null)
            null
        }
    }

    private suspend fun insertQuery(query: String, airportName: String?) {
        queryDao.insertQuery(QueryData(query, airportName))
    }

    private suspend fun getAirportNameByOldQuery(query: String): QueryData? {
        val queryData = queryDao.getAirportNameByQuery(query)
        queryData?.let {
            return it
        }
        return null
    }

    suspend fun retrieveMetarData(icao: String): MetarData {
        val localMetarData = metarDao.getMetarDataByIcao(icao)
        localMetarData?.let {
            Log.d("AirportRepository", "MetarData retrieved from DAO")
            return MetarData(icao, it.decodedData, it.rawData)
        }
        return retrieveMetarDataRemotely(icao)
    }

    suspend fun retrieveMetarDataRemotely(icao: String): MetarData {
        val metarDecoded = metarApi.getDecodedMetarData(icao)
        val metarRaw = metarApi.getRawMetarData(icao)
        Log.d("AirportRepository", "MetarData retrieved from API")
        insertMetarData(MetarData(icao, metarDecoded, metarRaw))
        return  MetarData(icao, metarDecoded, metarRaw)
    }

    suspend fun deleteMetarData(icao: String) {
        metarDao.deleteMetarData(icao)
    }

    suspend fun deleteOldMetarData() {
        metarDao.deleteInvalidMetarData(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1))
    }

    private suspend fun insertMetarData(metarData: MetarData) {
        metarDao.insertMetarData(metarData)
    }

    suspend fun retrieveFavoriteAirports(): List<Airport> {
        return airportDao.getFavorites()
    }

    suspend fun editFavoriteAirport(airport: Airport) {
        airportDao.update(airport)
    }

    suspend fun updateOutdatedFavoriteMetarData() {
        val outdatedMetar = metarDao.getOutdatedFavoriteMetarData(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1))
        outdatedMetar.forEach {
            Log.d("∞∞∞∞∞∞∞∞", it.icao)
            retrieveMetarDataRemotely(it.icao)
        }
    }
}