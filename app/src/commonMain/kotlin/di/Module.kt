package di

import model.CurrencyProvider
import model.ExchangeRateProvider
import model.database.ExpenseRepository
import model.database.FakeExpenseRepository
import model.database.IExpenseRepository
import model.database.RecurringExpenseDao
import model.database.RecurringExpenseDatabase
import model.datastore.FakeUserPreferencesRepository
import model.datastore.IUserPreferencesRepository
import model.notification.ExpenseNotificationManager
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import viewmodel.EditRecurringExpenseViewModel
import viewmodel.MainNavigationViewModel
import viewmodel.RecurringExpenseViewModel
import viewmodel.SettingsViewModel
import viewmodel.UpcomingPaymentsViewModel

expect val platformModule: Module

val sharedModule =
    module {
        singleOf(::ExpenseRepository).bind<IExpenseRepository>()
        single {
            RecurringExpenseDatabase.getRecurringExpenseDatabase(get()).recurringExpenseDao()
        }.bind<RecurringExpenseDao>()
        viewModelOf(::RecurringExpenseViewModel)
        viewModelOf(::UpcomingPaymentsViewModel)
        viewModel { (expenseId: Int?) ->
            EditRecurringExpenseViewModel(expenseId, get(), get(), get())
        }
        singleOf(::CurrencyProvider)
        viewModelOf(::SettingsViewModel)
        singleOf(::ExchangeRateProvider)
        singleOf(::ExpenseNotificationManager)
        viewModelOf(::MainNavigationViewModel)
    }

val previewModule =
    module {
        single<IExpenseRepository> { FakeExpenseRepository() }
        single<IUserPreferencesRepository> { FakeUserPreferencesRepository() }
    }
