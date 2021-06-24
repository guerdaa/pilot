package com.tsellami.pilot.data.metar

import org.junit.Assert
import org.junit.Test

class MetarDataTest {

    @Test
    fun `Assert that data is correctly converted to a list`() {
        val metarData = MetarData("", DECODED_DATA, "")

        val dataList = metarData.convertDataToList()

        Assert.assertEquals(dataList.size, 3)
    }

    companion object {
        private const val DECODED_DATA = "Null\nNull\nFirst\nSecond\nThird\n"
    }
}