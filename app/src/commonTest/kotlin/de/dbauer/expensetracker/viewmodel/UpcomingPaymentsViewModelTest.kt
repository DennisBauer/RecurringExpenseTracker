package de.dbauer.expensetracker.viewmodel

import de.dbauer.expensetracker.model.FakeExchangeRateProvider
import de.dbauer.expensetracker.model.database.IExpenseRepository
import de.dbauer.expensetracker.model.database.RecurrenceDatabase
import de.dbauer.expensetracker.model.database.RecurringExpense
import de.dbauer.expensetracker.model.datastore.FakeUserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class UpcomingPaymentsViewModelTest {
    private val defaultCurrencyCode = "EUR"
    val expenseRepository =
        object : IExpenseRepository {
            var expenses: Flow<List<RecurringExpense>> = emptyFlow()

            override val allRecurringExpenses: Flow<List<RecurringExpense>>
                get() = expenses
            override val allRecurringExpensesByPrice: Flow<List<RecurringExpense>>
                get() = expenses

            override suspend fun getRecurringExpenseById(id: Int): RecurringExpense? {
                return expenses.first().find { it.id == id }
            }

            override suspend fun insert(recurringExpense: RecurringExpense) {}

            override suspend fun update(recurringExpense: RecurringExpense) {}

            override suspend fun delete(recurringExpense: RecurringExpense) {}
        }

    private lateinit var viewModel: UpcomingPaymentsViewModel

    @BeforeTest
    fun setup() {
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
            assertEquals(21, result.find { it.payment?.name == name }?.payment?.nextPaymentRemainingDays)
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
            assertEquals(4, result.size) // 1 header + 3 payments
            assertEquals(2, result[1].payment?.nextPaymentRemainingDays)
            assertEquals(14, result[2].payment?.nextPaymentRemainingDays)
            assertEquals(26, result[3].payment?.nextPaymentRemainingDays)
        }

    @Test
    fun `expenses with foreign currency prefix sum with ~`() =
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
    fun `expenses with no foreign currency do not prefix sum with ~`() =
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
                    .filterNot {
                        it.payment == null
                    }.map { it.payment!!.nextPaymentRemainingDays }
            assertTrue(paymentDates == paymentDates.sorted())
        }

    private fun getTestExpense(
        name: String,
        price: Float,
        currencyCode: String,
        everyXRecurrence: Int? = null,
        recurrence: RecurrenceDatabase? = null,
        firstPayment: Instant? = null,
    ): RecurringExpense {
        return RecurringExpense(
            id = name.hashCode() + price.hashCode(),
            name = name,
            description = null,
            price = price,
            everyXRecurrence = everyXRecurrence,
            recurrence = recurrence?.value,
            firstPayment = firstPayment?.toEpochMilliseconds(),
            color = null,
            currencyCode = currencyCode,
            notifyForExpense = false,
            notifyXDaysBefore = null,
            lastNotificationDate = null,
        )
    }
}
