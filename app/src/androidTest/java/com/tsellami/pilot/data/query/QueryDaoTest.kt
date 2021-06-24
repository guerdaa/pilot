package com.tsellami.pilot.data.query

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.tsellami.pilot.data.DaoTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class QueryDaoTest: DaoTest() {

    private lateinit var dao: QueryDao

    @Before
    override fun setUp() {
        super.setUp()
        dao = pilotDatabase.queryDao()
    }

    @Test
    fun testGetAirportNameByQuery_afterInsertingOneElement() {
        runBlocking {
            val query = QueryData("frankf", "Frankfurt")
            dao.insertQuery(query)

            val retrieved = dao.getAirportNameByQuery("fRanKf")

            Assert.assertEquals(retrieved, query)
        }
    }
}