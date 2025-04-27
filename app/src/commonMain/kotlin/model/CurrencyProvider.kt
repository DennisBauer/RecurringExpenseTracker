package model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import org.jetbrains.compose.resources.ExperimentalResourceApi
import recurringexpensetracker.app.generated.resources.Res

@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class Currency(
    val symbol: String,
    val name: String,
    @SerialName("symbol_native")
    val symbolNative: String,
    @SerialName("decimal_digits")
    val decimalDigits: Int,
    val rounding: Int,
    val code: String,
    @SerialName("name_plural")
    val namePlural: String,
    val type: String,
    val countries: List<String>,
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
