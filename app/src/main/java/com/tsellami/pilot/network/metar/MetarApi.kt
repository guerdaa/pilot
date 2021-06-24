package com.tsellami.pilot.network.metar

import retrofit2.http.GET
import retrofit2.http.Path

interface MetarApi {

    @GET("decoded/{icao}.TXT")
    suspend fun getDecodedMetarData(@Path("icao") icao: String): String

    @GET("stations/{icao}.TXT")
    suspend fun getRawMetarData(@Path("icao") icao: String): String

    companion object {
        const val BASE_URL = "https://tgftp.nws.noaa.gov/data/observations/metar/"
    }
}