package com.tsellami.pilot.data.airport

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "airport")
data class Airport(
    @PrimaryKey val icao: String,
    val name: String,
    val favorite: Boolean = false
)