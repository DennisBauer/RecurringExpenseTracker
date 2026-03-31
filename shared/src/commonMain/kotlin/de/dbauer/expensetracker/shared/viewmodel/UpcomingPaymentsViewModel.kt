package de.dbauer.expensetracker.shared.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.dbauer.expensetracker.shared.data.CurrencyValue
import de.dbauer.expensetracker.shared.data.RecurringExpenseData
import de.dbauer.expensetracker.shared.data.UpcomingPaymentData
import de.dbauer.expensetracker.shared.getDefaultCurrencyCode
import de.dbauer.expensetracker.shared.model.DateTimeCalculator
import de.dbauer.expensetracker.shared.model.IExchangeRateProvider
import de.dbauer.expensetracker.shared.model.database.IExpenseRepository
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.shared.model.getSystemCurrencyCode
import de.dbauer.expensetracker.shared.toCurrencyString
import de.dbauer.expensetracker.shared.toLocaleString
import de.dbauer.expensetracker.shared.toMonthYearStringUTC
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
import kotlinx.datetime.minus
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

sealed class UpcomingPayment {
    abstract val month: String
    abstract val paymentsSum: String

    data class MonthHeader(
        override val month: String,
        override val paymentsSum: String,
        val isPastSection: Boolean = false,
    ) : UpcomingPayment()

    data class PaymentItem(
        override val month: String,
        override val paymentsSum: String,
        val payment: UpcomingPaymentData,
    ) : UpcomingPayment()

    data class PaidDivider(
        override val month: String,
        override val paymentsSum: String,
        val isPastSection: Boolean = false,
    ) : UpcomingPayment()

    data class UpcomingDivider(
        override val month: String,
        override val paymentsSum: String,
    ) : UpcomingPayment()
}

class UpcomingPaymentsViewModel(
    private val expenseRepository: IExpenseRepository,
    private val exchangeRateProvider: IExchangeRateProvider,
    userPreferencesRepository: IUserPreferencesRepository,
) : ViewModel() {
    private val _upcomingPaymentsData = mutableStateListOf<UpcomingPayment>()
    val upcomingPaymentsData: List<UpcomingPayment>
        get() = _upcomingPaymentsData

    var upcomingStartIndex: Int = 0
        private set

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

    fun markAsPaid(
        expenseId: Int,
        paymentDateEpoch: Long,
    ) {
        viewModelScope.launch {
            expenseRepository.markAsPaid(expenseId, paymentDateEpoch)
            refreshData()
        }
    }

    fun markAsUnpaid(
        expenseId: Int,
        paymentDateEpoch: Long,
    ) {
        viewModelScope.launch {
            expenseRepository.markAsUnpaid(expenseId, paymentDateEpoch)
            refreshData()
        }
    }

    private suspend fun refreshData() {
        val recurringExpenses = expenseRepository.allRecurringExpensesByPrice.first()
        onDatabaseUpdated(recurringExpenses)
    }

    private suspend fun onDatabaseUpdated(recurringExpenses: List<RecurringExpenseData>) {
        val from =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        val newData =
            createUpcomingPaymentData(
                recurringExpenses = recurringExpenses,
                from = from,
                until = from.plus(DatePeriod(years = 10)),
                pastMonths = PAST_MONTHS_COUNT,
            )
        upcomingStartIndex =
            newData
                .indexOfFirst { it is UpcomingPayment.UpcomingDivider }
                .let { if (it == -1) 0 else it }
        applyListUpdate(newData)
    }

    private fun applyListUpdate(newData: List<UpcomingPayment>) {
        for (i in newData.indices) {
            if (i < _upcomingPaymentsData.size) {
                if (_upcomingPaymentsData[i] != newData[i]) {
                    _upcomingPaymentsData[i] = newData[i]
                }
            } else {
                _upcomingPaymentsData.add(newData[i])
            }
        }
        while (_upcomingPaymentsData.size > newData.size) {
            _upcomingPaymentsData.removeAt(_upcomingPaymentsData.size - 1)
        }
    }

    suspend fun createUpcomingPaymentData(
        recurringExpenses: List<RecurringExpenseData>,
        from: LocalDate,
        until: LocalDate,
        pastMonths: Int = 0,
    ): List<UpcomingPayment> =
        withContext(Dispatchers.IO) {
            val currentMonthStart = LocalDate(from.year, from.month, 1)
            val pastFrom =
                if (pastMonths > 0) {
                    currentMonthStart.minus(DatePeriod(months = pastMonths))
                } else {
                    currentMonthStart
                }

            var yearMonthIterator = LocalDate(pastFrom.year, pastFrom.month, 1)
            val yearMonthUntil = LocalDate(until.year, until.month, 1)
            if (yearMonthIterator >= yearMonthUntil) return@withContext emptyList()

            // Pre-fetch payment records for manual-confirmation expenses
            val paymentRecordsByExpenseId = mutableMapOf<Int, Set<Long>>()
            recurringExpenses.filter { it.requireManualConfirmation }.forEach { expense ->
                val records = expenseRepository.getPaymentRecordsForExpense(expense.id)
                paymentRecordsByExpenseId[expense.id] = records.toSet()
            }

            // Phase 1: Collect all items per month, classified as above/below the divider
            data class MonthGroup(
                val monthStr: String,
                val aboveUnpaid: MutableList<UpcomingPaymentData> = mutableListOf(),
                val abovePaid: MutableList<UpcomingPaymentData> = mutableListOf(),
                val belowUnpaid: MutableList<UpcomingPaymentData> = mutableListOf(),
                val belowPaid: MutableList<UpcomingPaymentData> = mutableListOf(),
            )

            val monthGroups = mutableListOf<MonthGroup>()

            do {
                val isPastMonth = yearMonthIterator < currentMonthStart
                val unpaidPaymentsThisMonth = mutableListOf<UpcomingPaymentData>()
                val paidPaymentsThisMonth = mutableListOf<UpcomingPaymentData>()
                recurringExpenses.forEach { expense ->
                    val paidDates = paymentRecordsByExpenseId[expense.id] ?: emptySet()

                    if (isPastMonth) {
                        processPastMonthExpense(
                            expense = expense,
                            yearMonthIterator = yearMonthIterator,
                            from = from,
                            paidDates = paidDates,
                            unpaidPayments = unpaidPaymentsThisMonth,
                            paidPayments = paidPaymentsThisMonth,
                        )
                    } else if (expense.requireManualConfirmation) {
                        processManualConfirmationExpense(
                            expense = expense,
                            yearMonthIterator = yearMonthIterator,
                            from = from,
                            paidDates = paidDates,
                            unpaidPayments = unpaidPaymentsThisMonth,
                            paidPayments = paidPaymentsThisMonth,
                        )
                    } else {
                        processAutoAdvanceExpense(
                            expense = expense,
                            yearMonthIterator = yearMonthIterator,
                            from = from,
                            payments = unpaidPaymentsThisMonth,
                        )
                    }
                }

                if (unpaidPaymentsThisMonth.isNotEmpty() || paidPaymentsThisMonth.isNotEmpty()) {
                    unpaidPaymentsThisMonth.sortBy { it.nextPaymentRemainingDays }
                    paidPaymentsThisMonth.sortBy { it.nextPaymentRemainingDays }

                    val monthStr = yearMonthIterator.toMonthYearStringUTC()
                    val group = MonthGroup(monthStr)

                    // Classify each item as above or below the UpcomingDivider.
                    // Above: past date AND (auto-advance OR manually paid)
                    // Below: everything else (unpaid items, future items, paid future items)
                    fun isAboveDivider(item: UpcomingPaymentData): Boolean {
                        if (pastMonths <= 0) return false
                        if (item.nextPaymentRemainingDays >= 0) return false
                        return !item.requiresConfirmation || item.isPaid
                    }

                    unpaidPaymentsThisMonth.forEach { item ->
                        if (isAboveDivider(item)) {
                            group.aboveUnpaid.add(item)
                        } else {
                            group.belowUnpaid.add(item)
                        }
                    }
                    paidPaymentsThisMonth.forEach { item ->
                        if (isAboveDivider(item)) {
                            group.abovePaid.add(item)
                        } else {
                            group.belowPaid.add(item)
                        }
                    }

                    monthGroups.add(group)
                }

                yearMonthIterator = yearMonthIterator.plus(DatePeriod(months = 1))
            } while (yearMonthIterator.monthsUntil(yearMonthUntil) > 0)

            // Phase 2: Assemble the final list
            val localUpcomingPaymentsData = mutableListOf<UpcomingPayment>()

            // Above section (past/done items)
            for (group in monthGroups) {
                if (group.aboveUnpaid.isEmpty() && group.abovePaid.isEmpty()) continue
                val paymentsSum =
                    computeSumString(group.aboveUnpaid, recurringExpenses)
                addMonthSection(
                    result = localUpcomingPaymentsData,
                    monthStr = group.monthStr,
                    paymentsSum = paymentsSum,
                    unpaidItems = group.aboveUnpaid,
                    paidItems = group.abovePaid,
                    isPastSection = true,
                )
            }

            // Single UpcomingDivider (present when pastMonths > 0 and above section has items)
            if (pastMonths > 0 && localUpcomingPaymentsData.isNotEmpty()) {
                val firstBelowGroup =
                    monthGroups.firstOrNull {
                        it.belowUnpaid.isNotEmpty() || it.belowPaid.isNotEmpty()
                    }
                val dividerMonth = firstBelowGroup?.monthStr ?: ""
                val dividerSum =
                    if (firstBelowGroup != null) {
                        computeSumString(firstBelowGroup.belowUnpaid, recurringExpenses)
                    } else {
                        ""
                    }
                localUpcomingPaymentsData.add(
                    UpcomingPayment.UpcomingDivider(dividerMonth, dividerSum),
                )
            }

            // Below section (upcoming/actionable items)
            for (group in monthGroups) {
                if (group.belowUnpaid.isEmpty() && group.belowPaid.isEmpty()) continue
                val paymentsSum =
                    computeSumString(group.belowUnpaid, recurringExpenses)
                addMonthSection(
                    result = localUpcomingPaymentsData,
                    monthStr = group.monthStr,
                    paymentsSum = paymentsSum,
                    unpaidItems = group.belowUnpaid,
                    paidItems = group.belowPaid,
                    isPastSection = false,
                )
            }

            return@withContext localUpcomingPaymentsData
        }

    private fun addMonthSection(
        result: MutableList<UpcomingPayment>,
        monthStr: String,
        paymentsSum: String,
        unpaidItems: List<UpcomingPaymentData>,
        paidItems: List<UpcomingPaymentData>,
        isPastSection: Boolean,
    ) {
        result.add(
            UpcomingPayment.MonthHeader(monthStr, paymentsSum, isPastSection = isPastSection),
        )
        unpaidItems.forEach {
            result.add(UpcomingPayment.PaymentItem(monthStr, paymentsSum, it))
        }
        if (paidItems.isNotEmpty()) {
            result.add(
                UpcomingPayment.PaidDivider(monthStr, paymentsSum, isPastSection = isPastSection),
            )
            paidItems.forEach {
                result.add(UpcomingPayment.PaymentItem(monthStr, paymentsSum, it))
            }
        }
    }

    private suspend fun computeSumString(
        unpaidItems: List<UpcomingPaymentData>,
        recurringExpenses: List<RecurringExpenseData>,
    ): String {
        var sum = 0f
        var atLeastOneWasExchanged = false
        unpaidItems.forEach { payment ->
            val expense = recurringExpenses.firstOrNull { it.id == payment.id }
            if (expense != null) {
                sum += expense.price.exchangeToDefaultCurrency()?.value ?: 0f
                if (expense.price.currencyCode != defaultCurrency.getDefaultCurrencyCode()) {
                    atLeastOneWasExchanged = true
                }
            }
        }
        val currencyPrefix = if (atLeastOneWasExchanged) "~" else ""
        return currencyPrefix + sum.toCurrencyString(defaultCurrency.first())
    }

    private fun processAutoAdvanceExpense(
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

    private fun processManualConfirmationExpense(
        expense: RecurringExpenseData,
        yearMonthIterator: LocalDate,
        from: LocalDate,
        paidDates: Set<Long>,
        unpaidPayments: MutableList<UpcomingPaymentData>,
        paidPayments: MutableList<UpcomingPaymentData>,
    ) {
        // For manual confirmation expenses, we need to find all occurrences in this month,
        // including past ones that haven't been paid yet (overdue).
        // Start from the first of the month (or earlier for overdue items in the current month)
        val monthStart = yearMonthIterator

        var nextPaymentDay = expense.getNextPaymentDayAfter(monthStart) ?: return

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
                // Only show future unpaid items, or overdue (past) unpaid items in the current month
                if (nextPaymentRemainingDays >= 0 || yearMonthIterator.isSameMonth(from)) {
                    unpaidPayments.add(paymentData)
                }
            }

            nextPaymentDay =
                expense.getNextPaymentDayAfter(nextPaymentDay.plus(1, DateTimeUnit.DAY))
                    ?: return
        }
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

    private fun processPastMonthExpense(
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

    companion object {
        const val PAST_MONTHS_COUNT = 3
    }
}
