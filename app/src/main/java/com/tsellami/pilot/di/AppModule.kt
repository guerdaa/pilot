package com.tsellami.pilot.di

import android.app.Application
import androidx.room.Room
import com.tsellami.pilot.data.PilotDatabase
import com.tsellami.pilot.data.airport.AirportDao
import com.tsellami.pilot.data.metar.MetarDao
import com.tsellami.pilot.data.query.QueryDao
import com.tsellami.pilot.network.airport.AirportApi
import com.tsellami.pilot.network.metar.MetarApi
import com.tsellami.pilot.repository.AirportRepository
import com.tsellami.pilot.repository.MetarDataRepository
import com.tsellami.pilot.repository.api.IAirportRepository
import com.tsellami.pilot.repository.api.IMetarDataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Singleton
    @Provides
    fun provideAirportApi(): AirportApi {
        val retrofit = Retrofit.Builder().baseUrl(AirportApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
        return retrofit.create(AirportApi::class.java)
    }

    @Singleton
    @Provides
    fun provideMetarApi(): MetarApi {
        val retrofit = Retrofit.Builder().baseUrl(MetarApi.BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create()).build()
        return retrofit.create(MetarApi::class.java)
    }

    @Singleton
    @Provides
    fun provideAirportDatabase(app: Application): PilotDatabase =
        Room.databaseBuilder(app, PilotDatabase::class.java, AIRPORT_DATABASE)
            .fallbackToDestructiveMigration()
            .build()

    @Singleton
    @Provides
    fun provideAirportDao(database: PilotDatabase): AirportDao =
        database.airportDao()

    @Singleton
    @Provides
    fun provideMetarDao(database: PilotDatabase): MetarDao =
        database.metarDao()

    @Singleton
    @Provides
    fun provideQueryDao(database: PilotDatabase): QueryDao =
        database.queryDao()

    @Singleton
    @Provides
    fun provideAirportRepository(
        airportDao: AirportDao,
        airportApi: AirportApi,
        queryDao: QueryDao
    ): IAirportRepository = AirportRepository(airportDao, airportApi, queryDao)

   @Singleton
   @Provides
   fun provideMetarDataRepository(
       metarDao: MetarDao,
       metarApi: MetarApi
   ): IMetarDataRepository = MetarDataRepository(metarDao, metarApi)

    companion object {
        private const val AIRPORT_DATABASE = "airport_database"
    }
}