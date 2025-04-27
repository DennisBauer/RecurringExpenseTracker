package model

import data.CurrencyValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import recurringexpensetracker.app.generated.resources.Res

@Serializable
data class ExchangeRates(
    val meta: Meta,
    val data: Map<String, Rate>,
    val updateTime: String,
)

@Serializable
data class Meta(
    @SerialName("last_updated_at")
    val lastUpdatedAt: String,
)

@Serializable
data class Rate(
    val code: String,
    val value: Float,
)

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
        exchangeRates.data[from.currencyCode]?.value?.let { exchangeRateSource ->
            exchangeRates.data[toCurrencyCode]?.value?.let { exchangeRateTarget ->
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
        return exchangeRates.meta.lastUpdatedAt
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun retrieveExchangeRates(): ExchangeRates =
        withContext(Dispatchers.IO) {
            val currenciesFile = Res.readBytes("files/exchange_rates.json")
            return@withContext Json.decodeFromString<ExchangeRates>(currenciesFile.decodeToString())
        }
}
