package de.dbauer.expensetracker.model

import de.dbauer.expensetracker.data.CurrencyValue

interface IExchangeRateProvider {
    suspend fun exchangeCurrencyValue(
        from: CurrencyValue,
        toCurrencyCode: String,
    ): CurrencyValue?

    suspend fun getLastUpdateInfo(): String
}
