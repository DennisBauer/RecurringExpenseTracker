package de.dbauer.expensetracker.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocalizedNumberParsingTest {
    private fun en(input: String) = parseLocalizedFloat(input, decimalSeparator = '.', groupSeparator = ',')

    private fun de(input: String) = parseLocalizedFloat(input, decimalSeparator = ',', groupSeparator = '.')

    private fun ar(input: String) =
        parseLocalizedFloat(input, decimalSeparator = '\u066B', groupSeparator = '\u066C')

    private fun fr(input: String) = parseLocalizedFloat(input, decimalSeparator = ',', groupSeparator = ' ')

    @Test
    fun parsesPlainNumbers() {
        assertEquals(12f, en("12"))
        assertEquals(12.99f, en("12.99"))
        assertEquals(12.99f, de("12,99"))
        assertEquals(12.99f, en(" 12.99 "))
    }

    @Test
    fun parsesGroupedNumbers() {
        assertEquals(1234.56f, en("1,234.56"))
        assertEquals(1234.56f, de("1.234,56"))
        assertEquals(1234567f, en("1,234,567"))
        assertEquals(1234567f, de("1.234.567"))
        assertEquals(1234.56f, fr("1 234,56"))
        assertEquals(1234f, de("1.234"))
    }

    @Test
    fun acceptsOtherSeparatorAsDecimalWhenNotGrouping() {
        assertEquals(12.99f, de("12.99"))
        assertEquals(12.99f, en("12,99"))
        assertEquals(0.5f, de(".5"))
    }

    @Test
    fun rejectsInvalidInput() {
        assertNull(en(""))
        assertNull(en("   "))
        assertNull(en("abc"))
        assertNull(de("1,234.56"))
        assertNull(en("1.234,56"))
        assertNull(de("1.23.4"))
    }

    @Test
    fun rejectsMalformedGroupingBeforeDecimalSeparator() {
        assertNull(en("12,34.56"))
        assertNull(de("1..234,56"))
        assertNull(en("1,2345.6"))
        assertNull(en(",234.5"))
    }

    @Test
    fun parsesIndianGrouping() {
        assertEquals(123456f, en("1,23,456"))
        assertEquals(123456f, en("1,23,456.00"))
        assertEquals(12345678f, en("1,23,45,678"))
        assertNull(en("1,23,456,789"))
    }

    @Test
    fun parsesArabicIndicDigits() {
        assertEquals(1234.5f, ar("\u0661\u066C\u0662\u0663\u0664\u066B\u0665\u0660"))
        assertEquals(12.5f, ar("\u200F\u0661\u0662\u066B\u0665"))
        assertEquals(42f, en("\u06F4\u06F2"))
    }

    @Test
    fun keepsLeadingSignOutOfGrouping() {
        assertEquals(-123456.78f, en("-123,456.78"))
        assertEquals(123456.78f, en("+123,456.78"))
        assertEquals(-123456f, en("-123,456"))
        assertEquals(123456f, en("+123,456"))
        assertEquals(-1234.5f, de("-1.234,5"))
        assertEquals(-12.99f, en("-12.99"))
        assertNull(en("-"))
        assertNull(en("--1"))
    }

    @Test
    fun handlesMissingGroupSeparator() {
        assertEquals(1234.5f, parseLocalizedFloat("1234,5", decimalSeparator = ',', groupSeparator = null))
    }
}
