package model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import recurringexpensetracker.app.generated.resources.Res

@Serializable
data class Currency(
    val symbol: String,
    val name: String,
    val symbol_native: String,
    val decimal_digits: Int,
    val rounding: Int,
    val code: String,
    val name_plural: String,
    val type: String,
)

@Serializable
private data class CurrencyWrapper(val data: Map<String, Currency>)

class CurrencyProvider {
    @OptIn(ExperimentalResourceApi::class)
    suspend fun retrieveCurrencies(): List<Currency> =
        withContext(Dispatchers.IO) {
            val currenciesFile = Res.readBytes("files/currencies.json")
            return@withContext Json
                .decodeFromString<CurrencyWrapper>(currenciesFile.decodeToString())
                .data.values
                .toList()
        }
}
