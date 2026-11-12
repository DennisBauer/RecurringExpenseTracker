package de.dbauer.expensetracker.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocalizedNumberParsingTest {
    private fun en(input: String) = parseLocalizedFloat(input, decimalSeparator = '.', groupSeparator = ',')

    private fun de(input: String) = parseLocalizedFloat(input, decimalSeparator = ',', groupSeparator = '.')

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
    fun handlesMissingGroupSeparator() {
        assertEquals(1234.5f, parseLocalizedFloat("1234,5", decimalSeparator = ',', groupSeparator = null))
    }
}
