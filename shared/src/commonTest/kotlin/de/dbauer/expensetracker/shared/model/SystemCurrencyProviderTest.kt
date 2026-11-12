package de.dbauer.expensetracker.shared.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SystemCurrencyProviderTest {
    private fun currency(
        code: String,
        countries: List<String>,
        type: String = "fiat",
    ) = Currency(
        symbol = code,
        name = code,
        symbolNative = code,
        decimalDigits = 2,
        rounding = 0,
        code = code,
        namePlural = code,
        type = type,
        countries = countries,
    )

    private val currencies =
        listOf(
            currency("BTC", listOf("DE"), type = "crypto"),
            currency("EUR", listOf("DE", "FR")),
            currency("USD", listOf("US")),
        )

    @Test
    fun findsFiatCurrencyForRegion() {
        assertEquals("EUR", currencies.currencyCodeForRegion("DE"))
        assertEquals("EUR", currencies.currencyCodeForRegion("fr"))
        assertEquals("USD", currencies.currencyCodeForRegion("US"))
    }

    @Test
    fun returnsNullForUnknownRegion() {
        assertNull(currencies.currencyCodeForRegion("XX"))
    }
}
