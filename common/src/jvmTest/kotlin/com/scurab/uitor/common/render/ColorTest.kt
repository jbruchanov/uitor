package com.scurab.uitor.common.render

import junit.framework.Assert.assertEquals
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(JUnitParamsRunner::class)
internal class ColorTest {

    @Test
    @Parameters(
        value = [
            "000, #FF000000",
            "F000, #FF000000",
            "111, #FF111111",
            "F123, #FF112233",
            "112233, #FF112233",
            "AA112233, #AA112233"
        ]
    )
    fun testColor(value: String, expected: String) {
        assertEquals(expected, value.toColor().htmlARGB)
    }
}