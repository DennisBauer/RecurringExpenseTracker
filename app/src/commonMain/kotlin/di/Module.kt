package di

import model.CurrencyProvider
import model.database.ExpenseRepository
import model.database.RecurringExpenseDao
import model.database.RecurringExpenseDatabase
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import viewmodel.EditRecurringExpenseViewModel
import viewmodel.RecurringExpenseViewModel
import viewmodel.SettingsViewModel
import viewmodel.UpcomingPaymentsViewModel

expect val platformModule: Module

val sharedModule =
    module {
        singleOf(::ExpenseRepository)
        single {
            RecurringExpenseDatabase.getRecurringExpenseDatabase(get()).recurringExpenseDao()
        }.bind<RecurringExpenseDao>()
        viewModelOf(::RecurringExpenseViewModel)
        viewModelOf(::UpcomingPaymentsViewModel)
        viewModel { (expenseId: Int?) ->
            EditRecurringExpenseViewModel(expenseId, get())
        }
        singleOf(::CurrencyProvider)
        viewModelOf(::SettingsViewModel)
    }
