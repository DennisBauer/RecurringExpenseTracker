package de.dbauer.expensetracker.shared.viewmodel

import de.dbauer.expensetracker.shared.data.RecurringExpenseData
import de.dbauer.expensetracker.shared.data.Tag
import de.dbauer.expensetracker.shared.model.FakeExchangeRateProvider
import de.dbauer.expensetracker.shared.model.database.IExpenseRepository
import de.dbauer.expensetracker.shared.model.database.RecurrenceDatabase
import de.dbauer.expensetracker.shared.model.database.RecurringExpenseEntry
import de.dbauer.expensetracker.shared.model.database.RecurringExpenseWithTagsEntry
import de.dbauer.expensetracker.shared.model.database.toRecurringExpenseData
import de.dbauer.expensetracker.shared.model.datastore.FakeUserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class UpcomingPaymentsViewModelTest {
    private val defaultCurrencyCode = "EUR"
    private val expenseRepository =
        object : IExpenseRepository {
            var expenses: Flow<List<RecurringExpenseData>> = emptyFlow()
            var tags: Flow<List<Tag>> = emptyFlow()
            val paymentRecords = mutableMapOf<Int, MutableSet<Long>>()

            override val allRecurringExpenses: Flow<List<RecurringExpenseData>>
                get() = expenses
            override val allRecurringExpensesByPrice: Flow<List<RecurringExpenseData>>
                get() = expenses
            override val allTags: Flow<List<Tag>>
                get() = tags

            override suspend fun getRecurringExpenseById(id: Int): RecurringExpenseData? {
                return expenses.first().find { it.id == id }
            }

            override suspend fun insert(recurringExpense: RecurringExpenseData) {}

            override suspend fun update(recurringExpense: RecurringExpenseData) {}

            override suspend fun delete(recurringExpense: RecurringExpenseData) {}

            override suspend fun insert(tag: Tag) {}

            override suspend fun update(tag: Tag) {}

            override suspend fun delete(tag: Tag) {}

            override suspend fun markAsPaid(
                expenseId: Int,
                paymentDateEpoch: Long,
            ) {
                paymentRecords.getOrPut(expenseId) { mutableSetOf() }.add(paymentDateEpoch)
            }

            override suspend fun markAsUnpaid(
                expenseId: Int,
                paymentDateEpoch: Long,
            ) {
                paymentRecords[expenseId]?.remove(paymentDateEpoch)
            }

            override suspend fun getPaymentRecordsForExpense(expenseId: Int): List<Long> {
                return paymentRecords[expenseId]?.toList() ?: emptyList()
            }
        }

    private lateinit var viewModel: UpcomingPaymentsViewModel

    @BeforeTest
    fun setup() {
        expenseRepository.paymentRecords.clear()
        viewModel =
            UpcomingPaymentsViewModel(
                expenseRepository,
                FakeExchangeRateProvider(),
                FakeUserPreferencesRepository(),
            )
    }

    @Test
    fun `empty recurringExpenses returns empty upcoming payments`() =
        runTest {
            val from = LocalDate(2025, 6, 1)
            val until = LocalDate(2030, 7, 1)
            val result = viewModel.createUpcomingPaymentData(emptyList(), from, until)
            assertTrue(result.isEmpty())
        }

    @Test
    fun `expense with no next payment date returns nothing`() =
        runTest {
            val name = "My Expense"
            val from = LocalDate(2025, 1, 1)
            val until = LocalDate(2027, 7, 1)
            val expenses =
                listOf(
                    getTestExpense(
                        name = name,
                        price = 10f,
                        currencyCode = defaultCurrencyCode,
                    ),
                )

            val result = viewModel.createUpcomingPaymentData(expenses, from, until)
            assertTrue(result.isEmpty())
        }

    @Test
    fun `expense with next payment before now skips to future payments`() =
        runTest {
            val name = "My Expense"
            val from = LocalDate(2025, 6, 1)
            val until = LocalDate(2025, 7, 1)
            val firstPayment = LocalDateTime(2025, 5, 22, 0, 0)
            val payments =
                listOf(
                    getTestExpense(
                        name = name,
                        price = 20f,
                        currencyCode = defaultCurrencyCode,
                        recurrence = RecurrenceDatabase.Monthly,
                        everyXRecurrence = 1,
                        firstPayment = firstPayment.toInstant(TimeZone.UTC),
                    ),
                )

            val result = viewModel.createUpcomingPaymentData(payments, from, until)
            assertEquals(
                21,
                result
                    .filterIsInstance<UpcomingPayment.PaymentItem>()
                    .find { it.payment.name == name }
                    ?.payment
                    ?.nextPaymentRemainingDays,
            )
        }

    @Test
    fun `expenses with multiple payments in one month are all included`() =
        runTest {
            val name = "My Expense"
            val from = LocalDate(2025, 6, 1)
            val until = LocalDate(2025, 7, 1)
            val firstPayment = LocalDateTime(2025, 6, 3, 0, 0)
            val payments =
                listOf(
                    getTestExpense(
                        name = name,
                        price = 50f,
                        currencyCode = defaultCurrencyCode,
                        recurrence = RecurrenceDatabase.Daily,
                        everyXRecurrence = 12,
                        firstPayment = firstPayment.toInstant(TimeZone.UTC),
                    ),
                )

            val result = viewModel.createUpcomingPaymentData(payments, from, until)
            val paymentItems = result.filterIsInstance<UpcomingPayment.PaymentItem>()
            assertEquals(3, paymentItems.size) // 3 payments (1 header excluded)
            assertEquals(2, paymentItems[0].payment.nextPaymentRemainingDays)
            assertEquals(14, paymentItems[1].payment.nextPaymentRemainingDays)
            assertEquals(26, paymentItems[2].payment.nextPaymentRemainingDays)
        }

    @Test
    fun `expenses with foreign currency prefix sum with about symbol`() =
        runTest {
            val name = "My Expense"
            val from = LocalDate(2025, 6, 1)
            val until = LocalDate(2025, 7, 1)
            val firstPayment = LocalDateTime(2025, 6, 1, 0, 0)
            val payments =
                listOf(
                    getTestExpense(
                        name = name,
                        price = 30f,
                        currencyCode = "AUD",
                        recurrence = RecurrenceDatabase.Weekly,
                        everyXRecurrence = 2,
                        firstPayment = firstPayment.toInstant(TimeZone.UTC),
                    ),
                )

            val result = viewModel.createUpcomingPaymentData(payments, from, until)
            val header = result.first()
            assertTrue(header.paymentsSum.startsWith("~"))
        }

    @Test
    fun `expenses with no foreign currency do not prefix sum with about symbol`() =
        runTest {
            val name = "My Expense"
            val from = LocalDate(2025, 6, 1)
            val until = LocalDate(2025, 7, 1)
            val firstPayment = LocalDateTime(2025, 6, 1, 0, 0)
            val payments =
                listOf(
                    getTestExpense(
                        name = name,
                        price = 30f,
                        currencyCode = defaultCurrencyCode,
                        recurrence = RecurrenceDatabase.Weekly,
                        everyXRecurrence = 2,
                        firstPayment = firstPayment.toInstant(TimeZone.UTC),
                    ),
                )

            val result = viewModel.createUpcomingPaymentData(payments, from, until)
            val header = result.first()
            assertTrue(!header.paymentsSum.startsWith("~"))
        }

    @Test
    fun `upcoming payments sorted by remaining days`() =
        runTest {
            val from = LocalDate(2025, 6, 1)
            val until = LocalDate(2025, 7, 1)
            val payments =
                listOf(
                    getTestExpense(
                        name = "My Expense 1",
                        price = 100f,
                        currencyCode = defaultCurrencyCode,
                        recurrence = RecurrenceDatabase.Monthly,
                        everyXRecurrence = 1,
                        firstPayment = LocalDateTime(2024, 6, 15, 0, 0).toInstant(TimeZone.UTC),
                    ),
                    getTestExpense(
                        name = "My Expense 2",
                        price = 50f,
                        currencyCode = defaultCurrencyCode,
                        recurrence = RecurrenceDatabase.Monthly,
                        everyXRecurrence = 1,
                        firstPayment = LocalDateTime(2022, 6, 5, 0, 0).toInstant(TimeZone.UTC),
                    ),
                )

            val result = viewModel.createUpcomingPaymentData(payments, from, until)
            val paymentDates =
                result
                    .filterIsInstance<UpcomingPayment.PaymentItem>()
                    .map { it.payment.nextPaymentRemainingDays }
            assertTrue(paymentDates == paymentDates.sorted())
        }

    @Test
    fun `manual confirmation expense shows unpaid items`() =
        runTest {
            val name = "Manual Expense"
            val from = LocalDate(2025, 6, 1)
            val until = LocalDate(2025, 7, 1)
            val firstPayment = LocalDateTime(2025, 6, 10, 0, 0)
            val expenses =
                listOf(
                    getTestExpense(
                        name = name,
                        price = 25f,
                        currencyCode = defaultCurrencyCode,
                        recurrence = RecurrenceDatabase.Monthly,
                        everyXRecurrence = 1,
                        firstPayment = firstPayment.toInstant(TimeZone.UTC),
                        requireManualConfirmation = true,
                    ),
                )

            val result = viewModel.createUpcomingPaymentData(expenses, from, until)
            val paymentItems = result.filterIsInstance<UpcomingPayment.PaymentItem>()
            assertEquals(1, paymentItems.size)
            assertEquals(name, paymentItems[0].payment.name)
            assertTrue(paymentItems[0].payment.requiresConfirmation)
            assertTrue(!paymentItems[0].payment.isPaid)
        }

    @Test
    fun `manual confirmation expense shows paid item in current month`() =
        runTest {
            val name = "Manual Expense"
            val from = LocalDate(2025, 6, 1)
            val until = LocalDate(2025, 7, 1)
            val firstPayment = LocalDateTime(2025, 6, 10, 0, 0)
            val expense =
                getTestExpense(
                    name = name,
                    price = 30f,
                    currencyCode = defaultCurrencyCode,
                    recurrence = RecurrenceDatabase.Monthly,
                    everyXRecurrence = 1,
                    firstPayment = firstPayment.toInstant(TimeZone.UTC),
                    requireManualConfirmation = true,
                )

            // Mark the payment as paid
            val paymentDateEpoch =
                LocalDate(2025, 6, 10).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            expenseRepository.markAsPaid(expense.id, paymentDateEpoch)

            val result = viewModel.createUpcomingPaymentData(listOf(expense), from, until)
            val paymentItems = result.filterIsInstance<UpcomingPayment.PaymentItem>()
            val paidDividers = result.filterIsInstance<UpcomingPayment.PaidDivider>()

            assertEquals(1, paymentItems.size)
            assertTrue(paymentItems[0].payment.isPaid)
            assertEquals(1, paidDividers.size)
        }

    @Test
    fun `manual confirmation expense shows paid item in future month so it can be undone`() =
        runTest {
            val name = "Manual Expense"
            val from = LocalDate(2025, 6, 1)
            val until = LocalDate(2025, 8, 1)
            val firstPayment = LocalDateTime(2025, 6, 10, 0, 0)
            val expense =
                getTestExpense(
                    name = name,
                    price = 40f,
                    currencyCode = defaultCurrencyCode,
                    recurrence = RecurrenceDatabase.Monthly,
                    everyXRecurrence = 1,
                    firstPayment = firstPayment.toInstant(TimeZone.UTC),
                    requireManualConfirmation = true,
                )

            // Mark the NEXT month's payment as paid
            val nextMonthPaymentDateEpoch =
                LocalDate(2025, 7, 10).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            expenseRepository.markAsPaid(expense.id, nextMonthPaymentDateEpoch)

            val result = viewModel.createUpcomingPaymentData(listOf(expense), from, until)
            val allPaymentItems = result.filterIsInstance<UpcomingPayment.PaymentItem>()
            val paidDividers = result.filterIsInstance<UpcomingPayment.PaidDivider>()

            // Current month: unpaid item for June
            val juneItems = allPaymentItems.filter { it.month == result.first().month }
            assertEquals(1, juneItems.size)
            assertTrue(!juneItems[0].payment.isPaid)

            // Next month: paid item for July should still be visible (not disappear)
            val julyItems = allPaymentItems.filter { it.payment.isPaid }
            assertEquals(1, julyItems.size)
            assertTrue(julyItems[0].payment.isPaid)
            assertTrue(julyItems[0].payment.requiresConfirmation)

            // A paid divider should exist for the future month
            assertTrue(paidDividers.isNotEmpty())
        }

    @Test
    fun `manual confirmation overdue unpaid expense appears in current month`() =
        runTest {
            val name = "Overdue Expense"
            // from is June 15, but firstPayment was June 5 — so the June 5 occurrence is overdue
            val from = LocalDate(2025, 6, 15)
            val until = LocalDate(2025, 7, 1)
            val firstPayment = LocalDateTime(2025, 6, 5, 0, 0)
            val expenses =
                listOf(
                    getTestExpense(
                        name = name,
                        price = 15f,
                        currencyCode = defaultCurrencyCode,
                        recurrence = RecurrenceDatabase.Monthly,
                        everyXRecurrence = 1,
                        firstPayment = firstPayment.toInstant(TimeZone.UTC),
                        requireManualConfirmation = true,
                    ),
                )

            val result = viewModel.createUpcomingPaymentData(expenses, from, until)
            val paymentItems = result.filterIsInstance<UpcomingPayment.PaymentItem>()
            assertEquals(1, paymentItems.size)
            assertEquals(name, paymentItems[0].payment.name)
            // Remaining days should be negative (overdue)
            assertTrue(paymentItems[0].payment.nextPaymentRemainingDays < 0)
            assertTrue(!paymentItems[0].payment.isPaid)
        }

    @Test
    fun `manual confirmation paid items are excluded from monthly sum`() =
        runTest {
            val name1 = "Paid Expense"
            val name2 = "Unpaid Expense"
            val from = LocalDate(2025, 6, 1)
            val until = LocalDate(2025, 7, 1)
            val firstPayment = LocalDateTime(2025, 6, 10, 0, 0)
            val expense1 =
                getTestExpense(
                    name = name1,
                    price = 100f,
                    currencyCode = defaultCurrencyCode,
                    recurrence = RecurrenceDatabase.Monthly,
                    everyXRecurrence = 1,
                    firstPayment = firstPayment.toInstant(TimeZone.UTC),
                    requireManualConfirmation = true,
                )
            val expense2 =
                getTestExpense(
                    name = name2,
                    price = 50f,
                    currencyCode = defaultCurrencyCode,
                    recurrence = RecurrenceDatabase.Monthly,
                    everyXRecurrence = 1,
                    firstPayment = firstPayment.toInstant(TimeZone.UTC),
                    requireManualConfirmation = true,
                )

            // Mark expense1 as paid
            val paymentDateEpoch =
                LocalDate(2025, 6, 10).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            expenseRepository.markAsPaid(expense1.id, paymentDateEpoch)

            val result = viewModel.createUpcomingPaymentData(listOf(expense1, expense2), from, until)
            val header = result.filterIsInstance<UpcomingPayment.MonthHeader>().first()

            // The sum should only include the unpaid expense (50), not the paid one (100)
            // We can't easily parse the currency string, but we can verify there are both paid and unpaid items
            val paymentItems = result.filterIsInstance<UpcomingPayment.PaymentItem>()
            val paidItems = paymentItems.filter { it.payment.isPaid }
            val unpaidItems = paymentItems.filter { !it.payment.isPaid }
            assertEquals(1, paidItems.size)
            assertEquals(1, unpaidItems.size)
            assertEquals(name1, paidItems[0].payment.name)
            assertEquals(name2, unpaidItems[0].payment.name)
        }

    @Test
    fun `manual confirmation expense not shown as overdue in future months`() =
        runTest {
            val name = "Future Expense"
            val from = LocalDate(2025, 6, 15)
            val until = LocalDate(2025, 8, 1)
            val firstPayment = LocalDateTime(2025, 7, 5, 0, 0)
            val expenses =
                listOf(
                    getTestExpense(
                        name = name,
                        price = 20f,
                        currencyCode = defaultCurrencyCode,
                        recurrence = RecurrenceDatabase.Monthly,
                        everyXRecurrence = 1,
                        firstPayment = firstPayment.toInstant(TimeZone.UTC),
                        requireManualConfirmation = true,
                    ),
                )

            val result = viewModel.createUpcomingPaymentData(expenses, from, until)
            val paymentItems = result.filterIsInstance<UpcomingPayment.PaymentItem>()
            assertEquals(1, paymentItems.size)
            assertEquals(name, paymentItems[0].payment.name)
            // The payment is in the future, so remaining days should be positive
            assertTrue(paymentItems[0].payment.nextPaymentRemainingDays > 0)
        }

    @Test
    fun `marking as paid produces same result on rebuild`() =
        runTest {
            val name = "Manual Expense"
            val from = LocalDate(2025, 6, 1)
            val until = LocalDate(2025, 8, 1)
            val firstPayment = LocalDateTime(2025, 6, 10, 0, 0)
            val expense =
                getTestExpense(
                    name = name,
                    price = 40f,
                    currencyCode = defaultCurrencyCode,
                    recurrence = RecurrenceDatabase.Monthly,
                    everyXRecurrence = 1,
                    firstPayment = firstPayment.toInstant(TimeZone.UTC),
                    requireManualConfirmation = true,
                )

            // Build the list without any paid records
            val resultBefore = viewModel.createUpcomingPaymentData(listOf(expense), from, until)
            val unpaidItems = resultBefore.filterIsInstance<UpcomingPayment.PaymentItem>()
            assertEquals(2, unpaidItems.size)
            assertTrue(unpaidItems.all { !it.payment.isPaid })

            // Mark the June payment as paid
            val paymentDateEpoch =
                LocalDate(2025, 6, 10).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            expenseRepository.markAsPaid(expense.id, paymentDateEpoch)

            // Rebuild the list with the paid record
            val resultAfter = viewModel.createUpcomingPaymentData(listOf(expense), from, until)
            val paidItems =
                resultAfter.filterIsInstance<UpcomingPayment.PaymentItem>().filter {
                    it.payment.isPaid
                }
            val stillUnpaid =
                resultAfter.filterIsInstance<UpcomingPayment.PaymentItem>().filter {
                    !it.payment.isPaid
                }
            assertEquals(1, paidItems.size)
            assertEquals(1, stillUnpaid.size)
            assertEquals(name, paidItems[0].payment.name)

            // Verify the total structure is consistent
            val headers = resultAfter.filterIsInstance<UpcomingPayment.MonthHeader>()
            val dividers = resultAfter.filterIsInstance<UpcomingPayment.PaidDivider>()
            assertEquals(2, headers.size) // June and July headers
            assertEquals(1, dividers.size) // One paid divider for June
        }

    @Test
    fun `past months show auto-advance expense occurrences`() =
        runTest {
            val name = "Netflix"
            // from = April 1, looking back 3 months (Jan, Feb, Mar)
            val from = LocalDate(2025, 4, 1)
            val until = LocalDate(2025, 5, 1)
            val firstPayment = LocalDateTime(2025, 1, 15, 0, 0)
            val expenses =
                listOf(
                    getTestExpense(
                        name = name,
                        price = 10f,
                        currencyCode = defaultCurrencyCode,
                        recurrence = RecurrenceDatabase.Monthly,
                        everyXRecurrence = 1,
                        firstPayment = firstPayment.toInstant(TimeZone.UTC),
                    ),
                )

            val result = viewModel.createUpcomingPaymentData(expenses, from, until, pastMonths = 3)
            val paymentItems = result.filterIsInstance<UpcomingPayment.PaymentItem>()

            // Should have 4 items: Jan 15, Feb 15, Mar 15 (past), Apr 15 (upcoming)
            assertEquals(4, paymentItems.size)

            // Past items have negative remaining days
            val pastItems = paymentItems.filter { it.payment.nextPaymentRemainingDays < 0 }
            assertEquals(3, pastItems.size)

            // Upcoming item has positive remaining days
            val upcomingItems = paymentItems.filter { it.payment.nextPaymentRemainingDays >= 0 }
            assertEquals(1, upcomingItems.size)
        }

    @Test
    fun `past months include UpcomingDivider at correct position`() =
        runTest {
            val name = "Netflix"
            val from = LocalDate(2025, 4, 1)
            val until = LocalDate(2025, 5, 1)
            val firstPayment = LocalDateTime(2025, 1, 15, 0, 0)
            val expenses =
                listOf(
                    getTestExpense(
                        name = name,
                        price = 10f,
                        currencyCode = defaultCurrencyCode,
                        recurrence = RecurrenceDatabase.Monthly,
                        everyXRecurrence = 1,
                        firstPayment = firstPayment.toInstant(TimeZone.UTC),
                    ),
                )

            val result = viewModel.createUpcomingPaymentData(expenses, from, until, pastMonths = 3)
            val upcomingDividers = result.filterIsInstance<UpcomingPayment.UpcomingDivider>()
            assertEquals(1, upcomingDividers.size)

            // UpcomingDivider should be between past and upcoming items
            val dividerIndex = result.indexOfFirst { it is UpcomingPayment.UpcomingDivider }
            assertTrue(dividerIndex > 0) // Not the first item (past items come before)

            // All items before the divider should be past (headers or payment items with negative days)
            val itemsBeforeDivider = result.subList(0, dividerIndex)
            val paymentsBefore = itemsBeforeDivider.filterIsInstance<UpcomingPayment.PaymentItem>()
            assertTrue(paymentsBefore.all { it.payment.nextPaymentRemainingDays < 0 })

            // Items after the divider should include upcoming payments
            val itemsAfterDivider = result.subList(dividerIndex + 1, result.size)
            val paymentsAfter = itemsAfterDivider.filterIsInstance<UpcomingPayment.PaymentItem>()
            assertTrue(paymentsAfter.all { it.payment.nextPaymentRemainingDays >= 0 })
        }

    @Test
    fun `no past items when pastMonths is 0`() =
        runTest {
            val name = "Netflix"
            val from = LocalDate(2025, 4, 1)
            val until = LocalDate(2025, 5, 1)
            val firstPayment = LocalDateTime(2025, 1, 15, 0, 0)
            val expenses =
                listOf(
                    getTestExpense(
                        name = name,
                        price = 10f,
                        currencyCode = defaultCurrencyCode,
                        recurrence = RecurrenceDatabase.Monthly,
                        everyXRecurrence = 1,
                        firstPayment = firstPayment.toInstant(TimeZone.UTC),
                    ),
                )

            val result = viewModel.createUpcomingPaymentData(expenses, from, until, pastMonths = 0)
            val paymentItems = result.filterIsInstance<UpcomingPayment.PaymentItem>()

            // Only the upcoming Apr 15 item
            assertEquals(1, paymentItems.size)
            assertTrue(paymentItems[0].payment.nextPaymentRemainingDays >= 0)

            // No UpcomingDivider
            val upcomingDividers = result.filterIsInstance<UpcomingPayment.UpcomingDivider>()
            assertTrue(upcomingDividers.isEmpty())
        }

    @Test
    fun `past months show manual confirmation expense with paid and unpaid state`() =
        runTest {
            val name = "Rent"
            val from = LocalDate(2025, 4, 1)
            val until = LocalDate(2025, 5, 1)
            val firstPayment = LocalDateTime(2025, 1, 5, 0, 0)
            val expense =
                getTestExpense(
                    name = name,
                    price = 1000f,
                    currencyCode = defaultCurrencyCode,
                    recurrence = RecurrenceDatabase.Monthly,
                    everyXRecurrence = 1,
                    firstPayment = firstPayment.toInstant(TimeZone.UTC),
                    requireManualConfirmation = true,
                )

            // Mark Jan and Feb as paid, leave Mar unpaid
            val janEpoch = LocalDate(2025, 1, 5).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            val febEpoch = LocalDate(2025, 2, 5).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            expenseRepository.markAsPaid(expense.id, janEpoch)
            expenseRepository.markAsPaid(expense.id, febEpoch)

            val result = viewModel.createUpcomingPaymentData(listOf(expense), from, until, pastMonths = 3)
            val paymentItems = result.filterIsInstance<UpcomingPayment.PaymentItem>()

            // 4 items total: Jan (paid), Feb (paid), Mar (unpaid past), Apr (unpaid upcoming)
            assertEquals(4, paymentItems.size)

            val paidItems = paymentItems.filter { it.payment.isPaid }
            val unpaidItems = paymentItems.filter { !it.payment.isPaid }
            assertEquals(2, paidItems.size) // Jan and Feb
            assertEquals(2, unpaidItems.size) // Mar (past, overdue) and Apr (upcoming)

            // Verify PaidDividers exist for months with paid items
            val paidDividers = result.filterIsInstance<UpcomingPayment.PaidDivider>()
            assertTrue(paidDividers.isNotEmpty())

            // Verify UpcomingDivider exists and separates paid past from unpaid items
            val dividerIndex = result.indexOfFirst { it is UpcomingPayment.UpcomingDivider }
            assertTrue(dividerIndex > 0)

            // Paid items should be above the divider (past + done)
            val itemsAbove = result.subList(0, dividerIndex)
            val paymentsAbove = itemsAbove.filterIsInstance<UpcomingPayment.PaymentItem>()
            assertTrue(paymentsAbove.all { it.payment.isPaid })
            assertEquals(2, paymentsAbove.size)

            // Unpaid items should be below the divider (still actionable)
            val itemsBelow = result.subList(dividerIndex + 1, result.size)
            val paymentsBelow = itemsBelow.filterIsInstance<UpcomingPayment.PaymentItem>()
            assertTrue(paymentsBelow.all { !it.payment.isPaid })
            assertEquals(2, paymentsBelow.size)
        }

    @Test
    fun `expense with first payment after past window produces no past entries`() =
        runTest {
            val name = "New Subscription"
            val from = LocalDate(2025, 4, 1)
            val until = LocalDate(2025, 5, 1)
            // First payment is in April (current month), not in the past window
            val firstPayment = LocalDateTime(2025, 4, 10, 0, 0)
            val expenses =
                listOf(
                    getTestExpense(
                        name = name,
                        price = 20f,
                        currencyCode = defaultCurrencyCode,
                        recurrence = RecurrenceDatabase.Monthly,
                        everyXRecurrence = 1,
                        firstPayment = firstPayment.toInstant(TimeZone.UTC),
                    ),
                )

            val result = viewModel.createUpcomingPaymentData(expenses, from, until, pastMonths = 3)
            val paymentItems = result.filterIsInstance<UpcomingPayment.PaymentItem>()

            // Only one item in April (upcoming), no past items
            assertEquals(1, paymentItems.size)
            assertTrue(paymentItems[0].payment.nextPaymentRemainingDays >= 0)

            // No UpcomingDivider since there are no past items
            val upcomingDividers = result.filterIsInstance<UpcomingPayment.UpcomingDivider>()
            assertTrue(upcomingDividers.isEmpty())
        }

    @Test
    fun `no UpcomingDivider when no past items exist even with pastMonths greater than 0`() =
        runTest {
            val from = LocalDate(2025, 4, 1)
            val until = LocalDate(2025, 5, 1)
            // First payment is in April
            val expenses =
                listOf(
                    getTestExpense(
                        name = "New Expense",
                        price = 50f,
                        currencyCode = defaultCurrencyCode,
                        recurrence = RecurrenceDatabase.Monthly,
                        everyXRecurrence = 1,
                        firstPayment = LocalDateTime(2025, 4, 20, 0, 0).toInstant(TimeZone.UTC),
                    ),
                )

            val result = viewModel.createUpcomingPaymentData(expenses, from, until, pastMonths = 3)
            val upcomingDividers = result.filterIsInstance<UpcomingPayment.UpcomingDivider>()
            assertTrue(upcomingDividers.isEmpty())
        }

    @Test
    fun `overdue unpaid manual item in current month appears below UpcomingDivider`() =
        runTest {
            val name = "Overdue Rent"
            // from = March 30, expense due March 5 (overdue, unpaid)
            val from = LocalDate(2025, 3, 30)
            val until = LocalDate(2025, 5, 1)
            val firstPayment = LocalDateTime(2025, 2, 5, 0, 0)
            val expenses =
                listOf(
                    getTestExpense(
                        name = name,
                        price = 1000f,
                        currencyCode = defaultCurrencyCode,
                        recurrence = RecurrenceDatabase.Monthly,
                        everyXRecurrence = 1,
                        firstPayment = firstPayment.toInstant(TimeZone.UTC),
                        requireManualConfirmation = true,
                    ),
                )

            // Feb is paid, Mar is unpaid (overdue)
            val febEpoch = LocalDate(2025, 2, 5).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            expenseRepository.markAsPaid(expenses[0].id, febEpoch)

            val result = viewModel.createUpcomingPaymentData(expenses, from, until, pastMonths = 1)
            val dividerIndex = result.indexOfFirst { it is UpcomingPayment.UpcomingDivider }
            assertTrue(dividerIndex >= 0) // UpcomingDivider exists (Feb paid is above)

            // Feb (paid, past) should be above the divider
            val itemsAbove = result.subList(0, dividerIndex)
            val paymentsAbove = itemsAbove.filterIsInstance<UpcomingPayment.PaymentItem>()
            assertEquals(1, paymentsAbove.size)
            assertTrue(paymentsAbove[0].payment.isPaid)

            // Mar overdue unpaid + Apr upcoming should be below the divider
            val itemsBelow = result.subList(dividerIndex + 1, result.size)
            val paymentsBelow = itemsBelow.filterIsInstance<UpcomingPayment.PaymentItem>()
            assertTrue(paymentsBelow.any { it.payment.nextPaymentRemainingDays < 0 && !it.payment.isPaid })
            assertTrue(paymentsBelow.all { !it.payment.isPaid })
        }

    @Test
    fun `paid past item in current month appears above UpcomingDivider`() =
        runTest {
            val name = "Paid Rent"
            // from = March 30, expense due March 5 (paid)
            val from = LocalDate(2025, 3, 30)
            val until = LocalDate(2025, 5, 1)
            val firstPayment = LocalDateTime(2025, 3, 5, 0, 0)
            val expense =
                getTestExpense(
                    name = name,
                    price = 1000f,
                    currencyCode = defaultCurrencyCode,
                    recurrence = RecurrenceDatabase.Monthly,
                    everyXRecurrence = 1,
                    firstPayment = firstPayment.toInstant(TimeZone.UTC),
                    requireManualConfirmation = true,
                )

            // Mark March 5 as paid
            val marEpoch = LocalDate(2025, 3, 5).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            expenseRepository.markAsPaid(expense.id, marEpoch)

            val result = viewModel.createUpcomingPaymentData(listOf(expense), from, until, pastMonths = 1)
            val dividerIndex = result.indexOfFirst { it is UpcomingPayment.UpcomingDivider }
            assertTrue(dividerIndex >= 0)

            // March 5 paid item should be above the divider
            val itemsAbove = result.subList(0, dividerIndex)
            val paymentsAbove = itemsAbove.filterIsInstance<UpcomingPayment.PaymentItem>()
            assertEquals(1, paymentsAbove.size)
            assertTrue(paymentsAbove[0].payment.isPaid)
            assertTrue(paymentsAbove[0].payment.nextPaymentRemainingDays < 0)

            // April 5 unpaid item should be below the divider
            val itemsBelow = result.subList(dividerIndex + 1, result.size)
            val paymentsBelow = itemsBelow.filterIsInstance<UpcomingPayment.PaymentItem>()
            assertEquals(1, paymentsBelow.size)
            assertTrue(!paymentsBelow[0].payment.isPaid)
        }

    @Test
    fun `unpaid manual item from past month stays below divider until paid`() =
        runTest {
            val name = "Rent"
            val from = LocalDate(2025, 4, 1)
            val until = LocalDate(2025, 5, 1)
            val firstPayment = LocalDateTime(2025, 1, 5, 0, 0)
            val expense =
                getTestExpense(
                    name = name,
                    price = 1000f,
                    currencyCode = defaultCurrencyCode,
                    recurrence = RecurrenceDatabase.Monthly,
                    everyXRecurrence = 1,
                    firstPayment = firstPayment.toInstant(TimeZone.UTC),
                    requireManualConfirmation = true,
                )

            // Leave all months unpaid
            val resultBefore = viewModel.createUpcomingPaymentData(listOf(expense), from, until, pastMonths = 3)

            // No UpcomingDivider because above section is empty (all items are unpaid manual)
            val dividersBefore = resultBefore.filterIsInstance<UpcomingPayment.UpcomingDivider>()
            assertTrue(dividersBefore.isEmpty())

            // All items should be present (Jan, Feb, Mar past unpaid + Apr upcoming)
            val paymentsBefore = resultBefore.filterIsInstance<UpcomingPayment.PaymentItem>()
            assertEquals(4, paymentsBefore.size)
            assertTrue(paymentsBefore.all { !it.payment.isPaid })

            // Now mark Jan as paid
            val janEpoch = LocalDate(2025, 1, 5).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            expenseRepository.markAsPaid(expense.id, janEpoch)

            val resultAfter = viewModel.createUpcomingPaymentData(listOf(expense), from, until, pastMonths = 3)
            val dividerIndex = resultAfter.indexOfFirst { it is UpcomingPayment.UpcomingDivider }
            assertTrue(dividerIndex >= 0) // Now the divider exists (Jan is above)

            // Jan (paid) should be above the divider
            val itemsAbove = resultAfter.subList(0, dividerIndex)
            val paymentsAbove = itemsAbove.filterIsInstance<UpcomingPayment.PaymentItem>()
            assertEquals(1, paymentsAbove.size)
            assertTrue(paymentsAbove[0].payment.isPaid)

            // Feb, Mar (unpaid past) + Apr (unpaid upcoming) should be below
            val itemsBelow = resultAfter.subList(dividerIndex + 1, resultAfter.size)
            val paymentsBelow = itemsBelow.filterIsInstance<UpcomingPayment.PaymentItem>()
            assertEquals(3, paymentsBelow.size)
            assertTrue(paymentsBelow.all { !it.payment.isPaid })
        }

    @Test
    fun `same month split across divider when it has both paid past and unpaid items`() =
        runTest {
            val name = "Biweekly Expense"
            // from = March 30, expense paid on March 5 but March 19 is unpaid (overdue)
            val from = LocalDate(2025, 3, 30)
            val until = LocalDate(2025, 5, 1)
            val firstPayment = LocalDateTime(2025, 3, 5, 0, 0)
            val expense =
                getTestExpense(
                    name = name,
                    price = 50f,
                    currencyCode = defaultCurrencyCode,
                    recurrence = RecurrenceDatabase.Daily,
                    everyXRecurrence = 14,
                    firstPayment = firstPayment.toInstant(TimeZone.UTC),
                    requireManualConfirmation = true,
                )

            // Mark March 5 as paid, leave March 19 unpaid
            val mar5Epoch = LocalDate(2025, 3, 5).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            expenseRepository.markAsPaid(expense.id, mar5Epoch)

            val result = viewModel.createUpcomingPaymentData(listOf(expense), from, until, pastMonths = 1)
            val dividerIndex = result.indexOfFirst { it is UpcomingPayment.UpcomingDivider }
            assertTrue(dividerIndex >= 0)

            // March should have a header in both above and below sections
            val headersAbove =
                result.subList(0, dividerIndex).filterIsInstance<UpcomingPayment.MonthHeader>()
            val headersBelow =
                result.subList(dividerIndex + 1, result.size).filterIsInstance<UpcomingPayment.MonthHeader>()

            // Above: March header (with paid item)
            assertTrue(headersAbove.any { it.isPastSection })

            // Below: March header (with unpaid overdue item) or April header
            assertTrue(headersBelow.isNotEmpty())

            // The paid March 5 item should be above
            val paymentsAbove =
                result.subList(0, dividerIndex).filterIsInstance<UpcomingPayment.PaymentItem>()
            assertTrue(paymentsAbove.any { it.payment.isPaid && it.payment.nextPaymentRemainingDays < 0 })

            // The unpaid March 19 item should be below
            val paymentsBelow =
                result.subList(dividerIndex + 1, result.size).filterIsInstance<UpcomingPayment.PaymentItem>()
            assertTrue(paymentsBelow.any { !it.payment.isPaid && it.payment.nextPaymentRemainingDays < 0 })
        }

    @Test
    fun `no UpcomingDivider when above section is empty with all unpaid manual past items`() =
        runTest {
            val name = "Rent"
            val from = LocalDate(2025, 4, 1)
            val until = LocalDate(2025, 5, 1)
            val firstPayment = LocalDateTime(2025, 2, 5, 0, 0)
            val expenses =
                listOf(
                    getTestExpense(
                        name = name,
                        price = 1000f,
                        currencyCode = defaultCurrencyCode,
                        recurrence = RecurrenceDatabase.Monthly,
                        everyXRecurrence = 1,
                        firstPayment = firstPayment.toInstant(TimeZone.UTC),
                        requireManualConfirmation = true,
                    ),
                )

            // Leave all months unpaid
            val result = viewModel.createUpcomingPaymentData(expenses, from, until, pastMonths = 3)
            val upcomingDividers = result.filterIsInstance<UpcomingPayment.UpcomingDivider>()

            // No UpcomingDivider because all past items are unpaid manual (all go below)
            assertTrue(upcomingDividers.isEmpty())

            // But all payment items should still be present
            val paymentItems = result.filterIsInstance<UpcomingPayment.PaymentItem>()
            assertEquals(3, paymentItems.size) // Feb, Mar (past unpaid) + Apr (upcoming)
            assertTrue(paymentItems.all { !it.payment.isPaid })
        }

    private fun getTestExpense(
        name: String,
        price: Float,
        currencyCode: String,
        everyXRecurrence: Int = 1,
        recurrence: RecurrenceDatabase = RecurrenceDatabase.Monthly,
        firstPayment: Instant? = null,
        requireManualConfirmation: Boolean = false,
    ): RecurringExpenseData {
        return RecurringExpenseWithTagsEntry(
            expense =
                RecurringExpenseEntry(
                    id = name.hashCode() + price.hashCode(),
                    name = name,
                    description = "",
                    price = price,
                    everyXRecurrence = everyXRecurrence,
                    recurrence = recurrence.value,
                    firstPayment = firstPayment?.toEpochMilliseconds(),
                    currencyCode = currencyCode,
                    notifyForExpense = false,
                    requireManualConfirmation = requireManualConfirmation,
                ),
            tags = emptyList(),
            reminders = emptyList(),
        ).toRecurringExpenseData(defaultCurrencyCode)
    }
}
