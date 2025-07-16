package de.dbauer.expensetracker.model

import de.dbauer.expensetracker.data.CurrencyValue

class FakeExchangeRateProvider : IExchangeRateProvider {
    override suspend fun exchangeCurrencyValue(
        from: CurrencyValue,
        toCurrencyCode: String,
    ): CurrencyValue? {
        return from
    }

    override suspend fun getLastUpdateInfo(): String {
        return "2025-05-16 17:25:16"
    }
}
