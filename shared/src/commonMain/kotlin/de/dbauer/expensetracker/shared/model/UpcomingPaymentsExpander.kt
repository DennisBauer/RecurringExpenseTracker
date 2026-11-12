package de.dbauer.expensetracker.shared.model

import de.dbauer.expensetracker.shared.data.RecurringExpenseData
import de.dbauer.expensetracker.shared.data.UpcomingPaymentData
import de.dbauer.expensetracker.shared.model.database.IExpenseRepository
import de.dbauer.expensetracker.shared.toLocaleString
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus

/**
 * Payment days of a set of expenses, computed once per expense and grouped by month, so the
 * month-by-month expansion does not have to walk each expense's recurrence again for every month.
 * Create it with [UpcomingPaymentsExpander.computePaymentSchedule] and use it for one expansion run.
 */
class PaymentSchedule internal constructor(
    private val daysByExpenseId: Map<Int, Map<Int, List<LocalDate>>>,
) {
    private val dateLabels = mutableMapOf<LocalDate, String>()

    /** Payment days of [expense] in the month of [yearMonth], in ascending order. */
    fun daysOf(
        expense: RecurringExpenseData,
        yearMonth: LocalDate,
    ): List<LocalDate> {
        return daysByExpenseId[expense.id]?.get(yearMonth.monthKey()) ?: emptyList()
    }

    /** Localized date string of [day]. Formats every distinct day only once. */
    fun dateLabelOf(day: LocalDate): String {
        return dateLabels.getOrPut(day) { day.atStartOfDayIn(TimeZone.UTC).toLocaleString() }
    }
}

private fun LocalDate.monthKey(): Int = year * 12 + month.ordinal

object UpcomingPaymentsExpander {
    /**
     * Computes the payment days of all [recurringExpenses] inside
     * [[rangeStart], [rangeEndExclusive]).
     */
    fun computePaymentSchedule(
        recurringExpenses: List<RecurringExpenseData>,
        rangeStart: LocalDate,
        rangeEndExclusive: LocalDate,
    ): PaymentSchedule {
        return PaymentSchedule(
            recurringExpenses.associate { expense ->
                expense.id to
                    expense
                        .getPaymentDaysBetween(rangeStart, rangeEndExclusive)
                        .groupBy { it.monthKey() }
            },
        )
    }

    fun expandAutoAdvance(
        expense: RecurringExpenseData,
        schedule: PaymentSchedule,
        yearMonthIterator: LocalDate,
        from: LocalDate,
        payments: MutableList<UpcomingPaymentData>,
    ) {
        for (nextPaymentDay in schedule.daysOf(expense, yearMonthIterator)) {
            if (nextPaymentDay < from) continue
            val nextPaymentRemainingDays =
                DateTimeCalculator.getDaysFromUntil(from = from, until = nextPaymentDay)
            val nextPaymentDate = schedule.dateLabelOf(nextPaymentDay)
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
        }
    }

    fun expandManualConfirmation(
        expense: RecurringExpenseData,
        schedule: PaymentSchedule,
        yearMonthIterator: LocalDate,
        from: LocalDate,
        paidDates: Set<Long>,
        unpaidPayments: MutableList<UpcomingPaymentData>,
        paidPayments: MutableList<UpcomingPaymentData>,
    ) {
        for (nextPaymentDay in schedule.daysOf(expense, yearMonthIterator)) {
            val paymentDateEpoch = nextPaymentDay.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            val isPaid = paidDates.contains(paymentDateEpoch)
            val nextPaymentRemainingDays =
                DateTimeCalculator.getDaysFromUntil(from = from, until = nextPaymentDay)
            val nextPaymentDate = schedule.dateLabelOf(nextPaymentDay)

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
        }
    }

    fun expandPastMonth(
        expense: RecurringExpenseData,
        schedule: PaymentSchedule,
        yearMonthIterator: LocalDate,
        from: LocalDate,
        paidDates: Set<Long>,
        unpaidPayments: MutableList<UpcomingPaymentData>,
        paidPayments: MutableList<UpcomingPaymentData>,
    ) {
        for (nextPaymentDay in schedule.daysOf(expense, yearMonthIterator)) {
            val paymentDateEpoch = nextPaymentDay.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            val isPaid =
                if (expense.requireManualConfirmation) {
                    paidDates.contains(paymentDateEpoch)
                } else {
                    false
                }
            val nextPaymentRemainingDays =
                DateTimeCalculator.getDaysFromUntil(from = from, until = nextPaymentDay)
            val nextPaymentDate = schedule.dateLabelOf(nextPaymentDay)

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
        val activeExpenses = recurringExpenses.filter { it.archivedDate == null }
        val currentMonthStart = LocalDate(from.year, from.month, 1)
        var yearMonthIterator = currentMonthStart
        val yearMonthUntil = LocalDate(until.year, until.month, 1)
        if (yearMonthIterator >= yearMonthUntil) return emptyList()

        val paidByExpense = mutableMapOf<Int, Set<Long>>()
        activeExpenses.filter { it.requireManualConfirmation }.forEach { expense ->
            paidByExpense[expense.id] = expenseRepository.getPaymentRecordsForExpense(expense.id).toSet()
        }

        val schedule = computePaymentSchedule(activeExpenses, yearMonthIterator, yearMonthUntil)

        val result = mutableListOf<UpcomingPaymentData>()
        do {
            val unpaidThisMonth = mutableListOf<UpcomingPaymentData>()
            val paidThisMonth = mutableListOf<UpcomingPaymentData>()
            activeExpenses.forEach { expense ->
                val paidDates = paidByExpense[expense.id] ?: emptySet()
                if (expense.requireManualConfirmation) {
                    expandManualConfirmation(
                        expense = expense,
                        schedule = schedule,
                        yearMonthIterator = yearMonthIterator,
                        from = from,
                        paidDates = paidDates,
                        unpaidPayments = unpaidThisMonth,
                        paidPayments = paidThisMonth,
                    )
                } else {
                    expandAutoAdvance(
                        expense = expense,
                        schedule = schedule,
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
