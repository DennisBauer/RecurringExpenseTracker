package viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import model.database.UserPreferencesRepository
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

@OptIn(ExperimentalResourceApi::class)
class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val _availableCurrencies = MutableStateFlow<List<Currency>>(emptyList())
    val availableCurrencies: StateFlow<List<Currency>>
        get() = _availableCurrencies
    var selectedCurrencyName: String by mutableStateOf("")
        private set
    var showCurrencySelectionDialog by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            val currenciesFile = Res.readBytes("files/currencies.json")
            val currencies = Json.decodeFromString<CurrencyWrapper>(currenciesFile.decodeToString())
            val currenciesList = currencies.data.values.toList()
            _availableCurrencies.emit(currenciesList)

            userPreferencesRepository.defaultCurrency.get().collect { defaultCurrency ->
                currenciesList.firstOrNull { it.code == defaultCurrency }?.let { currency ->
                    selectedCurrencyName = "${currency.name} (${currency.symbol})"
                }
            }
        }
    }

    fun onSelectCurrency() {
        showCurrencySelectionDialog = true
    }

    fun onDismissCurrencySelectionDialog() {
        showCurrencySelectionDialog = false
    }

    fun onCurrencySelected(currency: Currency) {
        showCurrencySelectionDialog = false
        viewModelScope.launch {
            userPreferencesRepository.defaultCurrency.save(currency.code)
        }
    }
}
