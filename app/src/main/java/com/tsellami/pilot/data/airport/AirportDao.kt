package com.tsellami.pilot.data.airport

import androidx.room.*

@Dao
interface AirportDao {

    @Query("SELECT * FROM airport WHERE icao = UPPER(:icao)")
    suspend fun getAirportByIcao(icao: String): Airport?

    @Query("SELECT * FROM airport WHERE name LIKE '%' || :name || '%'")
    suspend fun getAirportByAirportName(name: String): List<Airport>

    @Query("SELECT * FROM airport WHERE favorite = 1")
    suspend fun getFavorites(): List<Airport>

    @Update
    suspend fun update(airport: Airport)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAirport(airport: Airport)
}