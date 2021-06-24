package com.tsellami.pilot.data.query

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QueryDao {

    @Query("SELECT * FROM query_table WHERE Upper(search) = Upper(:search)")
    suspend fun getAirportNameByQuery(search: String): QueryData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuery(queryData: QueryData)
}