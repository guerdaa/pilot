package com.tsellami.pilot.data.metar

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "metar_table")
data class MetarData(
    @PrimaryKey val icao: String,
    val decodedData: String,
    val rawData: String,
    val retrievingTime: Long = System.currentTimeMillis()
) {
    fun convertDataToList(): List<String> {
        return decodedData.split("\n").drop(2).dropLast(1)
    }
}
