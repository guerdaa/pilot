package com.tsellami.pilot.data.query

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "query_table")
data class QueryData(
    @PrimaryKey val search: String,
    val airportName: String?
)