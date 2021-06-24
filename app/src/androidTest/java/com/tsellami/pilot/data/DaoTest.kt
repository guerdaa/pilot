package com.tsellami.pilot.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
open class DaoTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    protected lateinit var pilotDatabase: PilotDatabase

    @Before
    open fun setUp() {
        pilotDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PilotDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        pilotDatabase.close()
    }
}