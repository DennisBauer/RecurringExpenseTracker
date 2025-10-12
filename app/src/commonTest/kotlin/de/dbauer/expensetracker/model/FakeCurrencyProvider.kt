package de.dbauer.expensetracker.model

class FakeCurrencyProvider : CurrencyProvider() {
    override suspend fun retrieveCurrencies(): List<Currency> {
        return listOf(
            Currency(
                symbol = "$",
                name = "US Dollar",
                symbolNative = "$",
                decimalDigits = 2,
                rounding = 0,
                code = "USD",
                namePlural = "US dollars",
                type = "fiat",
                countries = listOf("US"),
            ),
            Currency(
                symbol = "€",
                name = "Euro",
                symbolNative = "€",
                decimalDigits = 2,
                rounding = 0,
                code = "EUR",
                namePlural = "euros",
                type = "fiat",
                countries = listOf("DE", "FR", "IT"),
            ),
            Currency(
                symbol = "£",
                name = "British Pound",
                symbolNative = "£",
                decimalDigits = 2,
                rounding = 0,
                code = "GBP",
                namePlural = "British pounds",
                type = "fiat",
                countries = listOf("GB"),
            ),
        )
    }
}
