package model

import data.CurrencyValue

interface IExchangeRateProvider {
    suspend fun exchangeCurrencyValue(
        from: CurrencyValue,
        toCurrencyCode: String,
    ): CurrencyValue?

    suspend fun getLastUpdateInfo(): String
}
