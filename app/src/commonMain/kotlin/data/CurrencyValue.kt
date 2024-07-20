package data

import toCurrencyString

data class CurrencyValue(val value: Float, val currencyCode: String) {
    fun toCurrencyString(): String {
        return value.toCurrencyString(currencyCode)
    }
}
