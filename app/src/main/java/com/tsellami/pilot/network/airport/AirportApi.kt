package com.tsellami.pilot.network.airport

import com.tsellami.pilot.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface AirportApi {

    @Headers(
        "x-rapidapi-key: ${BuildConfig.RAPID_API_KEY}",
        "x-rapidapi-host: ${BuildConfig.RAPID_API_HOST}",
        "useQueryString: true"
    )
    @GET("term")
    suspend fun getAirport(
        @Query("q") query: String,
        @Query("limit") limit: Int
    ): AirportResponse

    companion object {
        const val BASE_URL = "https://aerodatabox.p.rapidapi.com/airports/search/"
    }
}