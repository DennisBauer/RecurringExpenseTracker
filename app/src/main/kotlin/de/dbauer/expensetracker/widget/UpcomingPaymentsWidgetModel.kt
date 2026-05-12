package de.dbauer.expensetracker.widget

import androidx.compose.runtime.mutableStateListOf
import de.dbauer.expensetracker.shared.data.UpcomingPaymentData
import de.dbauer.expensetracker.shared.model.UpcomingPaymentsExpander
import de.dbauer.expensetracker.shared.model.database.IExpenseRepository
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class UpcomingPaymentsWidgetModel(
    private val expenseRepository: IExpenseRepository,
    private val userPreferencesRepository: IUserPreferencesRepository,
) {
    private val _upcomingPaymentsData = mutableStateListOf<UpcomingPaymentData>()
    val upcomingPaymentsData: List<UpcomingPaymentData>
        get() = _upcomingPaymentsData

    suspend fun init() {
        combine(
            expenseRepository.allRecurringExpensesByPrice,
            userPreferencesRepository.upcomingPaymentHorizonMonths.get(),
        ) { expenses, horizonMonths -> expenses to horizonMonths }
            .collect { (recurringExpenses, horizonMonths) ->
                val from =
                    Clock.System
                        .now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                val until = from.plus(DatePeriod(months = horizonMonths))
                val payments =
                    UpcomingPaymentsExpander.collectFuturePayments(
                        expenseRepository = expenseRepository,
                        recurringExpenses = recurringExpenses,
                        from = from,
                        until = until,
                    )
                _upcomingPaymentsData.clear()
                _upcomingPaymentsData.addAll(payments)
            }
    }
}
