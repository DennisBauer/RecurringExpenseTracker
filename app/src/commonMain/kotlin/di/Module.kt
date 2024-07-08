package di

import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import viewmodel.RecurringExpenseViewModel
import viewmodel.UpcomingPaymentsViewModel
import viewmodel.database.ExpenseRepository
import viewmodel.database.RecurringExpenseDao
import viewmodel.database.RecurringExpenseDatabase

expect val platformModule: Module

val sharedModule =
    module {
        singleOf(::ExpenseRepository)
        single {
            RecurringExpenseDatabase.getRecurringExpenseDatabase(get()).recurringExpenseDao()
        }.bind<RecurringExpenseDao>()
        viewModelOf(::RecurringExpenseViewModel)
        viewModelOf(::UpcomingPaymentsViewModel)
    }
