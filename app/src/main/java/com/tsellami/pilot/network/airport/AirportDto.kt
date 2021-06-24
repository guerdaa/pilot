package com.tsellami.pilot.network.airport

import com.tsellami.pilot.data.airport.Airport

data class AirportDto(
    val icao: String,
    val name: String
) {
    fun toAirport() = Airport(icao, name)
}