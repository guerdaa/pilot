package com.tsellami.pilot.data.metar

import androidx.room.*
import com.tsellami.pilot.data.airport.Airport
import java.util.concurrent.Flow

@Dao
interface MetarDao {

    @Query("SELECT * FROM metar_table WHERE icao = UPPER(:icao)")
    suspend fun getMetarDataByIcao(icao: String): MetarData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetarData(metarData: MetarData)

    @Update
    suspend fun updateMetarData(metarData: MetarData)

    @Query("DELETE FROM metar_table WHERE icao = :icao")
    suspend fun deleteMetarData(icao: String)

    @Query("DELETE FROM metar_table")
    suspend fun deleteAllMetarData()

    @Query("DELETE FROM metar_table WHERE retrievingTime < :timestamp")
    suspend fun deleteInvalidMetarData(timestamp: Long)

    @Query("SELECT * FROM airport INNER JOIN metar_table ON airport.icao = metar_table.icao WHERE favorite = 1 AND retrievingTime < :timestamp")
    suspend fun getOutdatedFavoriteMetarData(timestamp: Long): List<MetarData>
}