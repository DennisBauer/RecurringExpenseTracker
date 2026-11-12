package de.dbauer.expensetracker.shared.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.dbauer.expensetracker.shared.data.RecurringExpenseData
import de.dbauer.expensetracker.shared.data.UpcomingPaymentData
import de.dbauer.expensetracker.shared.ioDispatcher
import de.dbauer.expensetracker.shared.model.IExchangeRateProvider
import de.dbauer.expensetracker.shared.model.UpcomingPaymentsExpander
import de.dbauer.expensetracker.shared.model.database.IExpenseRepository
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.shared.model.getSystemCurrencyCode
import de.dbauer.expensetracker.shared.toCurrencyString
import de.dbauer.expensetracker.shared.toMonthYearStringUTC
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
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

    /** True until the list was built for the first time. */
    var isLoading by mutableStateOf(true)
        private set

    private val defaultCurrency = userPreferencesRepository.defaultCurrency.get()
    private val horizonMonthsFlow = userPreferencesRepository.upcomingPaymentHorizonMonths.get()

    init {
        viewModelScope.launch {
            combine(
                expenseRepository.allRecurringExpensesByPrice,
                horizonMonthsFlow,
            ) { expenses, horizonMonths -> expenses to horizonMonths }
                .collect { (recurringExpenses, horizonMonths) ->
                    onDatabaseUpdated(recurringExpenses, horizonMonths)
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
        val horizonMonths = horizonMonthsFlow.first()
        onDatabaseUpdated(recurringExpenses, horizonMonths)
    }

    private suspend fun onDatabaseUpdated(
        recurringExpenses: List<RecurringExpenseData>,
        horizonMonths: Int,
    ) {
        val from =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        val newData =
            createUpcomingPaymentData(
                recurringExpenses = recurringExpenses,
                from = from,
                until = from.plus(DatePeriod(months = horizonMonths)),
                pastMonths = PAST_MONTHS_COUNT,
            )
        upcomingStartIndex =
            newData
                .indexOfFirst { it is UpcomingPayment.UpcomingDivider }
                .let { if (it == -1) 0 else it }
        applyListUpdate(newData)
        isLoading = false
    }

    private fun applyListUpdate(newData: List<UpcomingPayment>) {
        val commonSize = minOf(_upcomingPaymentsData.size, newData.size)
        for (i in 0 until commonSize) {
            if (_upcomingPaymentsData[i] != newData[i]) {
                _upcomingPaymentsData[i] = newData[i]
            }
        }
        if (newData.size > commonSize) {
            _upcomingPaymentsData.addAll(newData.subList(commonSize, newData.size))
        } else if (_upcomingPaymentsData.size > commonSize) {
            _upcomingPaymentsData.removeRange(commonSize, _upcomingPaymentsData.size)
        }
    }

    suspend fun createUpcomingPaymentData(
        recurringExpenses: List<RecurringExpenseData>,
        from: LocalDate,
        until: LocalDate,
        pastMonths: Int = 0,
    ): List<UpcomingPayment> =
        withContext(ioDispatcher) {
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
            val schedule =
                UpcomingPaymentsExpander.computePaymentSchedule(
                    recurringExpenses = recurringExpenses,
                    rangeStart = yearMonthIterator,
                    rangeEndExclusive = yearMonthUntil,
                )

            do {
                val isPastMonth = yearMonthIterator < from
                val unpaidPaymentsThisMonth = mutableListOf<UpcomingPaymentData>()
                val paidPaymentsThisMonth = mutableListOf<UpcomingPaymentData>()
                recurringExpenses.forEach { expense ->
                    val paidDates = paymentRecordsByExpenseId[expense.id] ?: emptySet()

                    if (isPastMonth) {
                        UpcomingPaymentsExpander.expandPastMonth(
                            expense = expense,
                            schedule = schedule,
                            yearMonthIterator = yearMonthIterator,
                            from = from,
                            paidDates = paidDates,
                            unpaidPayments = unpaidPaymentsThisMonth,
                            paidPayments = paidPaymentsThisMonth,
                        )
                    } else if (expense.requireManualConfirmation) {
                        UpcomingPaymentsExpander.expandManualConfirmation(
                            expense = expense,
                            schedule = schedule,
                            yearMonthIterator = yearMonthIterator,
                            from = from,
                            paidDates = paidDates,
                            unpaidPayments = unpaidPaymentsThisMonth,
                            paidPayments = paidPaymentsThisMonth,
                        )
                    } else {
                        UpcomingPaymentsExpander.expandAutoAdvance(
                            expense = expense,
                            schedule = schedule,
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
            val sumCalculator =
                MonthSumCalculator(
                    expensesById = recurringExpenses.associateBy { it.id },
                    defaultCurrencyCode = getDefaultCurrencyCode(),
                    displayCurrencyCode = defaultCurrency.first(),
                )
            val localUpcomingPaymentsData = mutableListOf<UpcomingPayment>()

            // Above section (past/done items)
            for (group in monthGroups) {
                if (group.aboveUnpaid.isEmpty() && group.abovePaid.isEmpty()) continue
                val paymentsSum =
                    sumCalculator.computeSumString(group.aboveUnpaid)
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
                        sumCalculator.computeSumString(firstBelowGroup.belowUnpaid)
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
                    sumCalculator.computeSumString(group.belowUnpaid)
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

    /**
     * Computes the month sums of one list build. Reads the currency settings only once and
     * exchanges the price of every expense only once, instead of once per payment.
     *
     * @param defaultCurrencyCode the currency to exchange into
     * @param displayCurrencyCode the currency code used to format the sum, may be blank
     */
    private inner class MonthSumCalculator(
        private val expensesById: Map<Int, RecurringExpenseData>,
        private val defaultCurrencyCode: String,
        private val displayCurrencyCode: String,
    ) {
        private val exchangedPriceByExpenseId = mutableMapOf<Int, Float>()

        suspend fun computeSumString(unpaidItems: List<UpcomingPaymentData>): String {
            var sum = 0f
            var atLeastOneWasExchanged = false
            unpaidItems.forEach { payment ->
                val expense = expensesById[payment.id]
                if (expense != null) {
                    sum +=
                        exchangedPriceByExpenseId.getOrPut(expense.id) {
                            exchangeRateProvider
                                .exchangeCurrencyValue(expense.price, defaultCurrencyCode)
                                ?.value ?: 0f
                        }
                    if (expense.price.currencyCode != defaultCurrencyCode) {
                        atLeastOneWasExchanged = true
                    }
                }
            }
            val currencyPrefix = if (atLeastOneWasExchanged) "~" else ""
            return currencyPrefix + sum.toCurrencyString(displayCurrencyCode)
        }
    }

    private suspend fun getDefaultCurrencyCode(): String {
        return defaultCurrency.first().ifBlank { getSystemCurrencyCode() }
    }

    companion object {
        const val PAST_MONTHS_COUNT = 3
    }
}
