package de.dbauer.expensetracker.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.dbauer.expensetracker.data.CurrencyValue
import de.dbauer.expensetracker.data.RecurringExpenseData
import de.dbauer.expensetracker.data.UpcomingPaymentData
import de.dbauer.expensetracker.getDefaultCurrencyCode
import de.dbauer.expensetracker.model.DateTimeCalculator
import de.dbauer.expensetracker.model.IExchangeRateProvider
import de.dbauer.expensetracker.model.database.IExpenseRepository
import de.dbauer.expensetracker.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.model.getSystemCurrencyCode
import de.dbauer.expensetracker.toCurrencyString
import de.dbauer.expensetracker.toLocaleString
import de.dbauer.expensetracker.toMonthYearStringUTC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

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
            expenseRepository.getRecurringExpenseById(expenseId)?.let { recurringExpense ->
                onItemClicked(recurringExpense)
            }
        }
    }

    private suspend fun onDatabaseUpdated(recurringExpenses: List<RecurringExpenseData>) {
        _upcomingPaymentsData.clear()
        val from =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        _upcomingPaymentsData.addAll(
            createUpcomingPaymentData(
                recurringExpenses = recurringExpenses,
                from = from,
                until = from.plus(DatePeriod(years = 10)),
            ),
        )
    }

    suspend fun createUpcomingPaymentData(
        recurringExpenses: List<RecurringExpenseData>,
        from: LocalDate,
        until: LocalDate,
    ): List<UpcomingPayment> =
        withContext(Dispatchers.IO) {
            var yearMonthIterator = LocalDate(from.year, from.month, 1)
            val yearMonthUntil = LocalDate(until.year, until.month, 1)
            if (yearMonthIterator >= yearMonthUntil) return@withContext emptyList()

            val localUpcomingPaymentsData = mutableListOf<UpcomingPayment>()
            do {
                val paymentsThisMonth = mutableListOf<UpcomingPaymentData>()
                var paymentsSumThisMonth = 0f
                var atLeastOneWasExchanged = false
                recurringExpenses.forEach { expense ->
                    var nextPaymentDay = expense.getNextPaymentDayAfter(yearMonthIterator) ?: return@forEach
                    while (nextPaymentDay < from) {
                        nextPaymentDay =
                            expense.getNextPaymentDayAfter(nextPaymentDay.plus(1, DateTimeUnit.DAY))
                                ?: return@forEach
                    }
                    while (nextPaymentDay.isSameMonth(yearMonthIterator)) {
                        val nextPaymentRemainingDays =
                            DateTimeCalculator.getDaysFromUntil(
                                from = from,
                                until = nextPaymentDay,
                            )
                        val nextPaymentDate = nextPaymentDay.atStartOfDayIn(TimeZone.UTC).toLocaleString()
                        paymentsThisMonth.add(
                            UpcomingPaymentData(
                                id = expense.id,
                                name = expense.name,
                                price = expense.price,
                                nextPaymentRemainingDays = nextPaymentRemainingDays,
                                nextPaymentDate = nextPaymentDate,
                                tags = expense.tags,
                            ),
                        )

                        paymentsSumThisMonth += expense.price.exchangeToDefaultCurrency()?.value ?: 0f
                        if (expense.price.currencyCode != defaultCurrency.getDefaultCurrencyCode()) {
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
                    localUpcomingPaymentsData.add(
                        UpcomingPayment(yearMonthIterator.toMonthYearStringUTC(), paymentsSum, null),
                    )

                    paymentsThisMonth.forEach {
                        localUpcomingPaymentsData.add(
                            UpcomingPayment(
                                yearMonthIterator.toMonthYearStringUTC(),
                                paymentsSum,
                                it,
                            ),
                        )
                    }
                }
                yearMonthIterator = yearMonthIterator.plus(DatePeriod(months = 1))
            } while (yearMonthIterator.monthsUntil(yearMonthUntil) > 0)
            return@withContext localUpcomingPaymentsData
        }

    private suspend fun CurrencyValue.exchangeToDefaultCurrency(): CurrencyValue? {
        return exchangeRateProvider.exchangeCurrencyValue(this, getDefaultCurrencyCode())
    }

    private suspend fun getDefaultCurrencyCode(): String {
        return defaultCurrency.first().ifBlank { getSystemCurrencyCode() }
    }

    private fun LocalDate.isSameMonth(other: LocalDate): Boolean {
        return year == other.year && month == other.month
    }
}
