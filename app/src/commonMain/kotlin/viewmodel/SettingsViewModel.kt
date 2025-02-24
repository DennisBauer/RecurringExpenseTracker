package viewmodel

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import model.Currency
import model.CurrencyProvider
import model.ExchangeRateProvider
import model.database.UserPreferencesRepository

class SettingsViewModel(
    private val currencyProvider: CurrencyProvider,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val exchangeRateProvider: ExchangeRateProvider,
) : ViewModel() {
    private val _availableCurrencies = MutableStateFlow<List<Currency>>(emptyList())
    val availableCurrencies: StateFlow<List<Currency>>
        get() = _availableCurrencies
    var selectedCurrencyName: String by mutableStateOf("")
        private set
    var showCurrencySelectionDialog by mutableStateOf(false)
        private set
    var showCurrencyInfoDialog by mutableStateOf(false)
        private set
    var exchangeRateLastUpdate by mutableStateOf("--")
        private set
    val upcomingPaymentNotification = userPreferencesRepository.upcomingPaymentNotification
    var upcomingPaymentNotificationTime = userPreferencesRepository.upcomingPaymentNotificationTime
    var upcomingPaymentNotificationTimePickerDialog by mutableStateOf(false)
        private set
    var upcomingPaymentNotificationDaysAdvance = userPreferencesRepository.upcomingPaymentNotificationDaysAdvance
    var upcomingPaymentNotificationDaysAdvanceDialog by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            val currenciesList = currencyProvider.retrieveCurrencies()
            _availableCurrencies.emit(currenciesList)

            exchangeRateLastUpdate = exchangeRateProvider.getLastUpdateInfo()

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

    fun onCurrencyInfo() {
        showCurrencyInfoDialog = true
    }

    fun onDismissCurrencyInfoDialog() {
        showCurrencyInfoDialog = false
    }

    fun onUpcomingPaymentNotification(enabled: Boolean) {
        viewModelScope.launch {
            upcomingPaymentNotification.save(enabled)
        }
    }

    fun onUpcomingPaymentNotificationTimeSelection() {
        upcomingPaymentNotificationTimePickerDialog = true
    }

    fun onDismissUpcomingPaymentNotificationTimePickerDialog() {
        upcomingPaymentNotificationTimePickerDialog = false
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun onConfirmUpcomingPaymentNotificationTimePickerDialog(timePickerState: TimePickerState) {
        viewModelScope.launch {
            userPreferencesRepository.upcomingPaymentNotificationTime.save(
                timePickerState.toMinutes(),
            )
        }
        upcomingPaymentNotificationTimePickerDialog = false
    }

    fun onUpcomingPaymentNotificationDaysAdvanceSelection() {
        upcomingPaymentNotificationDaysAdvanceDialog = true
    }

    fun onDismissUpcomingPaymentNotificationDaysAdvanceDialog() {
        upcomingPaymentNotificationDaysAdvanceDialog = false
    }

    fun onConfirmUpcomingPaymentNotificationDaysAdvanceDialog(days: Int) {
        viewModelScope.launch {
            userPreferencesRepository.upcomingPaymentNotificationDaysAdvance.save(days)
        }
        upcomingPaymentNotificationDaysAdvanceDialog = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun TimePickerState.toMinutes(): Int {
    return (hour * 60) + minute
}
