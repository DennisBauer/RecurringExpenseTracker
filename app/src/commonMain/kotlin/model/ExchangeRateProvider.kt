package model

import data.CurrencyValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import recurringexpensetracker.app.generated.resources.Res

@Serializable
private data class ExchangeRates(val data: Map<String, Float>, val updateTime: String)

class ExchangeRateProvider {
    private val mutex = Mutex()

    private var exchangeRates: ExchangeRates? = null

    suspend fun exchangeCurrencyValue(
        from: CurrencyValue,
        toCurrencyCode: String,
    ): CurrencyValue? {
        if (from.currencyCode == toCurrencyCode) return from

        val exchangeRates =
            mutex.withLock {
                exchangeRates ?: retrieveExchangeRates().apply {
                    exchangeRates = this
                }
            }
        exchangeRates.data[from.currencyCode]?.let { exchangeRateSource ->
            exchangeRates.data[toCurrencyCode]?.let { exchangeRateTarget ->
                return CurrencyValue(from.value / exchangeRateSource * exchangeRateTarget, toCurrencyCode, true)
            }
        }
        return null
    }

    suspend fun getLastUpdateInfo(): String {
        val exchangeRates =
            mutex.withLock {
                exchangeRates ?: retrieveExchangeRates().apply {
                    exchangeRates = this
                }
            }
        return exchangeRates.updateTime
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun retrieveExchangeRates(): ExchangeRates =
        withContext(Dispatchers.IO) {
            val currenciesFile = Res.readBytes("files/exchange_rates.json")
            return@withContext Json.decodeFromString<ExchangeRates>(currenciesFile.decodeToString())
        }
}
