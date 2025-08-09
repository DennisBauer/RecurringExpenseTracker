package de.dbauer.expensetracker.widget

import androidx.compose.runtime.mutableStateListOf
import de.dbauer.expensetracker.data.CurrencyValue
import de.dbauer.expensetracker.data.UpcomingPaymentData
import de.dbauer.expensetracker.getDefaultCurrencyCode
import de.dbauer.expensetracker.model.DateTimeCalculator
import de.dbauer.expensetracker.model.database.IExpenseRepository
import de.dbauer.expensetracker.model.database.RecurrenceDatabase
import de.dbauer.expensetracker.model.database.RecurringExpense
import de.dbauer.expensetracker.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.toLocaleString
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.Instant

class UpcomingPaymentsWidgetModel(
    private val expenseRepository: IExpenseRepository,
    userPreferencesRepository: IUserPreferencesRepository,
) {
    private val _upcomingPaymentsData = mutableStateListOf<UpcomingPaymentData>()
    val upcomingPaymentsData: List<UpcomingPaymentData>
        get() = _upcomingPaymentsData

    private val defaultCurrency = userPreferencesRepository.defaultCurrency.get()

    suspend fun init() {
        expenseRepository.allRecurringExpensesByPrice.collect { recurringExpenses ->
            onDatabaseUpdated(recurringExpenses)
        }
    }

    private suspend fun onDatabaseUpdated(recurringExpenses: List<RecurringExpense>) {
        _upcomingPaymentsData.clear()
        recurringExpenses.forEach { expense ->
            expense.firstPayment?.let { Instant.fromEpochMilliseconds(it) }?.let { firstPayment ->
                val nextPaymentDay =
                    getNextPaymentDay(firstPayment, expense.everyXRecurrence!!, expense.recurrence!!)
                val nextPaymentRemainingDays = DateTimeCalculator.getDaysFromNowUntil(nextPaymentDay)
                val nextPaymentDate = nextPaymentDay.atStartOfDayIn(TimeZone.UTC).toLocaleString()
                _upcomingPaymentsData.add(
                    UpcomingPaymentData(
                        id = expense.id,
                        name = expense.name!!,
                        price =
                            CurrencyValue(
                                expense.price!!,
                                expense.currencyCode.ifBlank { defaultCurrency.getDefaultCurrencyCode() },
                            ),
                        nextPaymentRemainingDays = nextPaymentRemainingDays,
                        nextPaymentDate = nextPaymentDate,
                    ),
                )
            }
        }
        _upcomingPaymentsData.sortBy { it.nextPaymentRemainingDays }
    }

    private fun getNextPaymentDay(
        firstPayment: Instant,
        everyXRecurrence: Int,
        recurrence: Int,
    ): LocalDate {
        return DateTimeCalculator.getDayOfNextOccurrenceFromNow(
            from = firstPayment,
            everyXRecurrence = everyXRecurrence,
            recurrence =
                when (recurrence) {
                    RecurrenceDatabase.Daily.value -> DateTimeUnit.DAY
                    RecurrenceDatabase.Weekly.value -> DateTimeUnit.WEEK
                    RecurrenceDatabase.Monthly.value -> DateTimeUnit.MONTH
                    RecurrenceDatabase.Yearly.value -> DateTimeUnit.YEAR
                    else -> DateTimeUnit.MONTH
                },
        )
    }
}
