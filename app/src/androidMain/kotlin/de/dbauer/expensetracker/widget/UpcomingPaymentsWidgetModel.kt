package de.dbauer.expensetracker.widget

import androidx.compose.runtime.mutableStateListOf
import data.CurrencyValue
import data.UpcomingPaymentData
import getDefaultCurrencyCode
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import model.DateTimeCalculator
import model.database.IExpenseRepository
import model.database.RecurrenceDatabase
import model.database.RecurringExpense
import model.datastore.IUserPreferencesRepository
import toLocaleString
import ui.customizations.ExpenseColor
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
                        color = ExpenseColor.fromInt(expense.color),
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
