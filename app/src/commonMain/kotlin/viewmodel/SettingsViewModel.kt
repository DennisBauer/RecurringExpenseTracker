package viewmodel

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import model.Currency
import model.CurrencyProvider
import model.ExchangeRateProvider
import model.database.UserPreferencesRepository
import ui.DefaultTab
import ui.ThemeMode

class SettingsViewModel(
    private val currencyProvider: CurrencyProvider,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val exchangeRateProvider: ExchangeRateProvider,
) : ViewModel() {
    var showThemeSelectionDialog by mutableStateOf(false)
        private set
    var selectedThemeMode = userPreferencesRepository.themeMode.get().map { ThemeMode.fromInt(it) }
        private set
    var showDefaultTabSelectionDialog by mutableStateOf(false)
        private set
    var selectedDefaultTab = userPreferencesRepository.defaultTab.get().map { DefaultTab.fromInt(it) }
        private set
    private val _availableCurrencies = mutableStateListOf<Currency>()
    val availableCurrencies: List<Currency>
        get() = _availableCurrencies
    var selectedCurrencyName: String by mutableStateOf("")
        private set
    var selectedCurrencyCode: String by mutableStateOf("")
        private set
    var exchangeRateLastUpdate by mutableStateOf("--")
        private set
    val showConvertedCurrency = userPreferencesRepository.showConvertedCurrency
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
            _availableCurrencies.addAll(currenciesList.sortedBy { it.name })

            exchangeRateLastUpdate = exchangeRateProvider.getLastUpdateInfo()
            userPreferencesRepository.defaultCurrency.get().collect { defaultCurrency ->
                currenciesList.firstOrNull { it.code == defaultCurrency }?.let { currency ->
                    selectedCurrencyName = "${currency.name} (${currency.symbol})"
                    selectedCurrencyCode = currency.code
                } ?: run {
                    selectedCurrencyName = ""
                    selectedCurrencyCode = ""
                }
            }
        }
    }

    fun onClickThemeSelection() {
        showThemeSelectionDialog = true
    }

    fun onDismissThemeSelectionDialog() {
        showThemeSelectionDialog = false
    }

    fun onSelectTheme(themeMode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.themeMode.save(themeMode.value)
            onDismissThemeSelectionDialog()
        }
    }

    fun onClickDefaultTabSelection() {
        showDefaultTabSelectionDialog = true
    }

    fun onDismissDefaultTabSelectionDialog() {
        showDefaultTabSelectionDialog = false
    }

    fun onSelectDefaultTab(defaultTab: DefaultTab) {
        viewModelScope.launch {
            userPreferencesRepository.defaultTab.save(defaultTab.value)
            onDismissDefaultTabSelectionDialog()
        }
    }

    fun onCurrencySelected(currency: Currency?) {
        viewModelScope.launch {
            userPreferencesRepository.defaultCurrency.save(currency?.code ?: "")
        }
    }

    fun onShowConvertedCurrency(convertExpenses: Boolean) {
        viewModelScope.launch {
            showConvertedCurrency.save(convertExpenses)
        }
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
