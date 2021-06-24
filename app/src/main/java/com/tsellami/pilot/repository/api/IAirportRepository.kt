package com.tsellami.pilot.repository.api

import com.tsellami.pilot.data.airport.Airport
import com.tsellami.pilot.network.airport.AirportDto

interface IAirportRepository {

    suspend fun insertNewAirport(newEntry: Airport): Boolean

    suspend fun retrieveAirportByIcao(icao: String): Airport?

    suspend fun retrieveAirportByName(name: String): Airport?

    suspend fun retrieveAirportByNameLocallyOrRemotely(query: String): Airport?

    suspend fun retrieveGermanyAirport(airports: List<AirportDto>, name: String): Airport?

    suspend fun retrieveFavoriteAirports(): List<Airport>

    suspend fun editFavoriteAirport(airport: Airport)
}