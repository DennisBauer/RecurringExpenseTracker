package de.dbauer.expensetracker.shared.model

import de.dbauer.expensetracker.shared.data.CurrencyValue

interface IExchangeRateProvider {
    suspend fun exchangeCurrencyValue(
        from: CurrencyValue,
        toCurrencyCode: String,
    ): CurrencyValue?

    suspend fun getLastUpdateInfo(): String
}
