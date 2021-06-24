package com.tsellami.pilot.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tsellami.pilot.data.airport.Airport
import com.tsellami.pilot.data.airport.AirportDao
import com.tsellami.pilot.data.metar.MetarDao
import com.tsellami.pilot.data.metar.MetarData
import com.tsellami.pilot.data.query.QueryDao
import com.tsellami.pilot.data.query.QueryData

@Database(entities = [Airport::class, MetarData::class, QueryData::class], version = 5)
abstract class PilotDatabase: RoomDatabase() {

    abstract fun airportDao(): AirportDao

    abstract fun metarDao(): MetarDao

    abstract fun queryDao(): QueryDao
}
