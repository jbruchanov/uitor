package com.scurab.uitor.common.render

import junit.framework.Assert.assertEquals
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
class ColorTest {

    @Test
    @Parameters(method = "testValues")
    fun testColor(value: String, expected: String) {
        assertEquals(expected, value.toColor().htmlARGB)
    }

    companion object {
        @JvmStatic
        fun testValues(): Array<Array<String>> {
            return arrayOf(
                arrayOf("000", "#FF000000"),
                arrayOf("000", "#FF000000"),
                arrayOf("F000", "#FF000000"),
                arrayOf("111", "#FF111111"),
                arrayOf("F123", "#FF112233"),
                arrayOf("112233", "#FF112233"),
                arrayOf("AA112233", "#AA112233"),
                arrayOf("rgba(255, 0, 255, 0.5)", "#80FF00FF"),
                arrayOf("rgb(200, 127, 0)", "#FFC87F00")
            )
        }
    }
}