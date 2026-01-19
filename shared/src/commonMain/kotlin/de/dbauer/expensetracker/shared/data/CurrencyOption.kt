package de.dbauer.expensetracker.shared.data

data class CurrencyOption(val currencyCode: String, val currencyName: String) {
    companion object {
        val INVALID = CurrencyOption("", "")
    }
}
