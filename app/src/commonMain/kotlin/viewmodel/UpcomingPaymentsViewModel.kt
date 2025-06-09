package viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.CurrencyValue
import data.RecurringExpenseData
import data.UpcomingPaymentData
import getDefaultCurrencyCode
import getNextPaymentDays
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import model.IExchangeRateProvider
import model.database.IExpenseRepository
import model.database.RecurringExpense
import model.datastore.IUserPreferencesRepository
import model.getSystemCurrencyCode
import toCurrencyString
import toLocaleString
import toMonthYearStringUTC
import ui.customizations.ExpenseColor

data class UpcomingPayment(val month: String, val paymentsSum: String, val payment: UpcomingPaymentData?)

class UpcomingPaymentsViewModel(
    private val expenseRepository: IExpenseRepository,
    private val exchangeRateProvider: IExchangeRateProvider,
    userPreferencesRepository: IUserPreferencesRepository,
) : ViewModel() {
    private val _upcomingPaymentsData = mutableStateListOf<UpcomingPayment>()
    val upcomingPaymentsData: List<UpcomingPayment>
        get() = _upcomingPaymentsData

    private val defaultCurrency = userPreferencesRepository.defaultCurrency.get()

    init {
        viewModelScope.launch {
            expenseRepository.allRecurringExpensesByPrice.collect { recurringExpenses ->
                onDatabaseUpdated(recurringExpenses)
            }
        }
    }

    fun onExpenseWithIdClicked(
        expenseId: Int,
        onItemClicked: (RecurringExpenseData) -> Unit,
    ) {
        viewModelScope.launch {
            expenseRepository.getRecurringExpenseById(expenseId)?.let {
                val recurringExpenseData = it.toFrontendType(defaultCurrency.getDefaultCurrencyCode())
                onItemClicked(recurringExpenseData)
            }
        }
    }

    private suspend fun onDatabaseUpdated(recurringExpenses: List<RecurringExpense>) =
        withContext(Dispatchers.IO) {
            _upcomingPaymentsData.clear()
            val now =
                Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
            var yearMonth = LocalDate(now.year, now.monthNumber, 1)
            val yearMonthUntil = yearMonth.plus(DatePeriod(years = 10))
            do {
                val paymentsThisMonth = mutableListOf<UpcomingPaymentData>()
                var paymentsSumThisMonth = 0f
                var atLeastOneWasExchanged = false
                recurringExpenses.forEach { expense ->
                    var nextPaymentDay = expense.getNextPaymentDayAfter(yearMonth) ?: return@forEach
                    while (nextPaymentDay < now) {
                        nextPaymentDay =
                            expense.getNextPaymentDayAfter(nextPaymentDay.plus(1, DateTimeUnit.DAY))
                                ?: return@forEach
                    }
                    while (nextPaymentDay.isSameMonth(yearMonth)) {
                        val nextPaymentRemainingDays = nextPaymentDay.getNextPaymentDays()
                        val nextPaymentDate = nextPaymentDay.atStartOfDayIn(TimeZone.UTC).toLocaleString()
                        val currencyValue =
                            CurrencyValue(
                                expense.price!!,
                                expense.currencyCode.ifBlank { defaultCurrency.getDefaultCurrencyCode() },
                            )
                        paymentsThisMonth.add(
                            UpcomingPaymentData(
                                id = expense.id,
                                name = expense.name!!,
                                price = currencyValue,
                                nextPaymentRemainingDays = nextPaymentRemainingDays,
                                nextPaymentDate = nextPaymentDate,
                                color = ExpenseColor.fromInt(expense.color),
                            ),
                        )

                        paymentsSumThisMonth += currencyValue.exchangeToDefaultCurrency()?.value ?: 0f
                        val currencyCode =
                            expense.currencyCode.ifBlank { defaultCurrency.getDefaultCurrencyCode() }
                        if (currencyCode != defaultCurrency.getDefaultCurrencyCode()) {
                            atLeastOneWasExchanged = true
                        }
                        nextPaymentDay =
                            expense.getNextPaymentDayAfter(nextPaymentDay.plus(1, DateTimeUnit.DAY))
                                ?: return@forEach
                    }
                }
                if (paymentsThisMonth.isNotEmpty()) {
                    paymentsThisMonth.sortBy { it.nextPaymentRemainingDays }

                    val currencyPrefix = if (atLeastOneWasExchanged) "~" else ""
                    val paymentsSum =
                        currencyPrefix + paymentsSumThisMonth.toCurrencyString(defaultCurrency.first())

                    // Header element for month
                    _upcomingPaymentsData.add(UpcomingPayment(yearMonth.toMonthYearStringUTC(), paymentsSum, null))

                    paymentsThisMonth.forEach {
                        _upcomingPaymentsData.add(
                            UpcomingPayment(
                                yearMonth.toMonthYearStringUTC(),
                                paymentsSum,
                                it,
                            ),
                        )
                    }
                }
                yearMonth = yearMonth.plus(DatePeriod(months = 1))
            } while (yearMonth.monthsUntil(yearMonthUntil) > 0)
        }

    private suspend fun CurrencyValue.exchangeToDefaultCurrency(): CurrencyValue? {
        return exchangeRateProvider.exchangeCurrencyValue(this, getDefaultCurrencyCode())
    }

    private suspend fun getDefaultCurrencyCode(): String {
        return defaultCurrency.first().ifBlank { getSystemCurrencyCode() }
    }

    private fun LocalDate.isSameMonth(other: LocalDate): Boolean {
        return year == other.year && monthNumber == other.monthNumber
    }
}
