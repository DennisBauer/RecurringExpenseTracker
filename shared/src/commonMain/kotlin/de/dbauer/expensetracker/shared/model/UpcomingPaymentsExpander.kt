package de.dbauer.expensetracker.shared.model

import de.dbauer.expensetracker.shared.data.RecurringExpenseData
import de.dbauer.expensetracker.shared.data.UpcomingPaymentData
import de.dbauer.expensetracker.shared.model.database.IExpenseRepository
import de.dbauer.expensetracker.shared.toLocaleString
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus

object UpcomingPaymentsExpander {
    fun expandAutoAdvance(
        expense: RecurringExpenseData,
        yearMonthIterator: LocalDate,
        from: LocalDate,
        payments: MutableList<UpcomingPaymentData>,
    ) {
        var nextPaymentDay = expense.getNextPaymentDayAfter(yearMonthIterator) ?: return
        while (nextPaymentDay < from) {
            nextPaymentDay =
                expense.getNextPaymentDayAfter(nextPaymentDay.plus(1, DateTimeUnit.DAY))
                    ?: return
        }
        while (nextPaymentDay.isSameMonth(yearMonthIterator)) {
            val nextPaymentRemainingDays =
                DateTimeCalculator.getDaysFromUntil(from = from, until = nextPaymentDay)
            val nextPaymentDate = nextPaymentDay.atStartOfDayIn(TimeZone.UTC).toLocaleString()
            val paymentDateEpoch = nextPaymentDay.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            payments.add(
                UpcomingPaymentData(
                    id = expense.id,
                    name = expense.name,
                    price = expense.price,
                    nextPaymentRemainingDays = nextPaymentRemainingDays,
                    nextPaymentDate = nextPaymentDate,
                    tags = expense.tags,
                    requiresConfirmation = false,
                    isPaid = false,
                    paymentDateEpoch = paymentDateEpoch,
                ),
            )
            nextPaymentDay =
                expense.getNextPaymentDayAfter(nextPaymentDay.plus(1, DateTimeUnit.DAY))
                    ?: return
        }
    }

    fun expandManualConfirmation(
        expense: RecurringExpenseData,
        yearMonthIterator: LocalDate,
        from: LocalDate,
        paidDates: Set<Long>,
        unpaidPayments: MutableList<UpcomingPaymentData>,
        paidPayments: MutableList<UpcomingPaymentData>,
    ) {
        var nextPaymentDay = expense.getNextPaymentDayAfter(yearMonthIterator) ?: return

        while (nextPaymentDay.isSameMonth(yearMonthIterator)) {
            val paymentDateEpoch = nextPaymentDay.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            val isPaid = paidDates.contains(paymentDateEpoch)
            val nextPaymentRemainingDays =
                DateTimeCalculator.getDaysFromUntil(from = from, until = nextPaymentDay)
            val nextPaymentDate = nextPaymentDay.atStartOfDayIn(TimeZone.UTC).toLocaleString()

            val paymentData =
                UpcomingPaymentData(
                    id = expense.id,
                    name = expense.name,
                    price = expense.price,
                    nextPaymentRemainingDays = nextPaymentRemainingDays,
                    nextPaymentDate = nextPaymentDate,
                    tags = expense.tags,
                    requiresConfirmation = true,
                    isPaid = isPaid,
                    paymentDateEpoch = paymentDateEpoch,
                )

            if (isPaid) {
                paidPayments.add(paymentData)
            } else {
                if (nextPaymentRemainingDays >= 0 || yearMonthIterator.isSameMonth(from)) {
                    unpaidPayments.add(paymentData)
                }
            }

            nextPaymentDay =
                expense.getNextPaymentDayAfter(nextPaymentDay.plus(1, DateTimeUnit.DAY))
                    ?: return
        }
    }

    fun expandPastMonth(
        expense: RecurringExpenseData,
        yearMonthIterator: LocalDate,
        from: LocalDate,
        paidDates: Set<Long>,
        unpaidPayments: MutableList<UpcomingPaymentData>,
        paidPayments: MutableList<UpcomingPaymentData>,
    ) {
        var nextPaymentDay = expense.getNextPaymentDayAfter(yearMonthIterator) ?: return

        while (nextPaymentDay.isSameMonth(yearMonthIterator)) {
            val paymentDateEpoch = nextPaymentDay.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            val isPaid =
                if (expense.requireManualConfirmation) {
                    paidDates.contains(paymentDateEpoch)
                } else {
                    false
                }
            val nextPaymentRemainingDays =
                DateTimeCalculator.getDaysFromUntil(from = from, until = nextPaymentDay)
            val nextPaymentDate = nextPaymentDay.atStartOfDayIn(TimeZone.UTC).toLocaleString()

            val paymentData =
                UpcomingPaymentData(
                    id = expense.id,
                    name = expense.name,
                    price = expense.price,
                    nextPaymentRemainingDays = nextPaymentRemainingDays,
                    nextPaymentDate = nextPaymentDate,
                    tags = expense.tags,
                    requiresConfirmation = expense.requireManualConfirmation,
                    isPaid = isPaid,
                    paymentDateEpoch = paymentDateEpoch,
                )

            if (isPaid) {
                paidPayments.add(paymentData)
            } else {
                unpaidPayments.add(paymentData)
            }

            nextPaymentDay =
                expense.getNextPaymentDayAfter(nextPaymentDay.plus(1, DateTimeUnit.DAY))
                    ?: return
        }
    }

    /**
     * Builds a flat, future-only list of upcoming payments using the same projection rules as the
     * Upcoming screen (auto-advance + manual-confirmation with current-month overdue handling).
     * No past months are included.
     */
    suspend fun collectFuturePayments(
        expenseRepository: IExpenseRepository,
        recurringExpenses: List<RecurringExpenseData>,
        from: LocalDate,
        until: LocalDate,
    ): List<UpcomingPaymentData> {
        val currentMonthStart = LocalDate(from.year, from.month, 1)
        var yearMonthIterator = currentMonthStart
        val yearMonthUntil = LocalDate(until.year, until.month, 1)
        if (yearMonthIterator >= yearMonthUntil) return emptyList()

        val paidByExpense = mutableMapOf<Int, Set<Long>>()
        recurringExpenses.filter { it.requireManualConfirmation }.forEach { expense ->
            paidByExpense[expense.id] = expenseRepository.getPaymentRecordsForExpense(expense.id).toSet()
        }

        val result = mutableListOf<UpcomingPaymentData>()
        do {
            val unpaidThisMonth = mutableListOf<UpcomingPaymentData>()
            val paidThisMonth = mutableListOf<UpcomingPaymentData>()
            recurringExpenses.forEach { expense ->
                val paidDates = paidByExpense[expense.id] ?: emptySet()
                if (expense.requireManualConfirmation) {
                    expandManualConfirmation(
                        expense = expense,
                        yearMonthIterator = yearMonthIterator,
                        from = from,
                        paidDates = paidDates,
                        unpaidPayments = unpaidThisMonth,
                        paidPayments = paidThisMonth,
                    )
                } else {
                    expandAutoAdvance(
                        expense = expense,
                        yearMonthIterator = yearMonthIterator,
                        from = from,
                        payments = unpaidThisMonth,
                    )
                }
            }
            unpaidThisMonth.sortBy { it.nextPaymentRemainingDays }
            paidThisMonth.sortBy { it.nextPaymentRemainingDays }
            result.addAll(unpaidThisMonth)
            result.addAll(paidThisMonth)

            yearMonthIterator = yearMonthIterator.plus(DatePeriod(months = 1))
        } while (yearMonthIterator.monthsUntil(yearMonthUntil) > 0)

        return result
    }

    private fun LocalDate.isSameMonth(other: LocalDate): Boolean {
        return year == other.year && month == other.month
    }
}
