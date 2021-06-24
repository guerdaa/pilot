package com.tsellami.pilot.utils

import org.junit.Assert
import org.junit.Test

class UtilsTest {

    @Test
    fun `Assert that checkIcao will return false if the length of the entry is different than 4`() {
        val result = Utils.checkIcao("12345")

        Assert.assertFalse(result)
    }

    @Test
    fun `Assert that checkIcao will return false if the length of the entry does not start with ED`() {
        val result = Utils.checkIcao("QE__")

        Assert.assertFalse(result)
    }

    @Test
    fun `Assert that checkIcao will return true if the length of the entry starts with ED and its length == 4`() {
        val result = Utils.checkIcao("EDDT")

        Assert.assertTrue(result)
    }
}