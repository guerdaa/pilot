package com.tsellami.pilot.repository

import android.util.Log
import com.tsellami.pilot.data.airport.Airport
import com.tsellami.pilot.data.airport.AirportDao
import com.tsellami.pilot.data.query.QueryDao
import com.tsellami.pilot.data.query.QueryData
import com.tsellami.pilot.network.airport.AirportApi
import com.tsellami.pilot.network.airport.AirportDto
import com.tsellami.pilot.repository.api.IAirportRepository
import com.tsellami.pilot.utils.Utils
import javax.inject.Inject

class AirportRepository(
    private val airportDao: AirportDao,
    private val airportApi: AirportApi,
    private val queryDao: QueryDao
): IAirportRepository {

    override suspend fun insertNewAirport(newEntry: Airport): Boolean {
        val airport = airportDao.getAirportByIcao(newEntry.icao)
        airport?.let {
            return false
        }
        airportDao.insertAirport(newEntry)
        return true
    }

    override suspend fun retrieveAirportByIcao(icao: String): Airport {
        val airport = airportDao.getAirportByIcao(icao)
        return if (airport == null) {
            val newEntry = airportApi.getAirport(icao, 1).items.first().toAirport()
            insertNewAirport(newEntry)
            newEntry
        } else {
            airport
        }
    }

    override suspend fun retrieveAirportByName(name: String): Airport? {
        var query = name
        val retrievedQuery = getAirportNameByOldQuery(query)
        if (retrievedQuery != null && retrievedQuery.airportName == null) {
            return null
        } else if (retrievedQuery?.airportName != null) {
            query = retrievedQuery.airportName
        }
        return retrieveAirportByNameLocallyOrRemotely(query)
    }

    override suspend fun retrieveAirportByNameLocallyOrRemotely(query: String): Airport? {
        val localAirports = airportDao.getAirportByAirportName(query)
        return if (localAirports.isNotEmpty()) {
            localAirports.first()
        } else {
            val retrievedAirports = airportApi.getAirport(query, 1).items
            if (retrievedAirports.isNotEmpty()) {
                retrieveGermanyAirport(retrievedAirports, query)
            } else {
                insertQuery(query, null)
                null
            }
        }
    }

    override suspend fun retrieveGermanyAirport(airports: List<AirportDto>, name: String): Airport? {
        val airport = airports.first().toAirport()
        return if (Utils.checkIcao(airport.icao)) {
            insertQuery(name, airport.name)
            insertNewAirport(airport)
            airport
        } else {
            insertQuery(name, null)
            null
        }
    }

    override suspend fun retrieveFavoriteAirports(): List<Airport> {
        return airportDao.getFavorites()
    }

    override suspend fun editFavoriteAirport(airport: Airport) {
        airportDao.update(airport)
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
}