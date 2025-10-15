package de.dbauer.expensetracker.di

import de.dbauer.expensetracker.model.CurrencyProvider
import de.dbauer.expensetracker.model.ExchangeRateProvider
import de.dbauer.expensetracker.model.FakeExchangeRateProvider
import de.dbauer.expensetracker.model.IExchangeRateProvider
import de.dbauer.expensetracker.model.database.ExpenseRepository
import de.dbauer.expensetracker.model.database.FakePreviewExpenseRepository
import de.dbauer.expensetracker.model.database.IExpenseRepository
import de.dbauer.expensetracker.model.database.RecurringExpenseDao
import de.dbauer.expensetracker.model.database.RecurringExpenseDatabase
import de.dbauer.expensetracker.model.datastore.FakeUserPreferencesRepository
import de.dbauer.expensetracker.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.model.notification.ExpenseNotificationManager
import de.dbauer.expensetracker.ui.whatsnew.FakeWhatsNew
import de.dbauer.expensetracker.ui.whatsnew.IWhatsNew
import de.dbauer.expensetracker.ui.whatsnew.WhatsNew
import de.dbauer.expensetracker.viewmodel.EditRecurringExpenseViewModel
import de.dbauer.expensetracker.viewmodel.MainNavigationViewModel
import de.dbauer.expensetracker.viewmodel.RecurringExpenseViewModel
import de.dbauer.expensetracker.viewmodel.SettingsViewModel
import de.dbauer.expensetracker.viewmodel.TagsScreenViewModel
import de.dbauer.expensetracker.viewmodel.UpcomingPaymentsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

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
        single<IExchangeRateProvider> { ExchangeRateProvider() }
        single { ExpenseNotificationManager(get(), get()) }
        viewModelOf(::MainNavigationViewModel)
        viewModelOf(::TagsScreenViewModel)
        single<IWhatsNew> { WhatsNew(get()) }
    }

val previewModule =
    module {
        viewModelOf(::RecurringExpenseViewModel)
        viewModelOf(::SettingsViewModel)
        viewModel { (expenseId: Int?) ->
            EditRecurringExpenseViewModel(expenseId, get(), get(), get())
        }
        single<IExchangeRateProvider> { FakeExchangeRateProvider() }
        singleOf(::CurrencyProvider)
        single<IExpenseRepository> { FakePreviewExpenseRepository() }
        single<IUserPreferencesRepository> { FakeUserPreferencesRepository() }
        viewModelOf(::TagsScreenViewModel)
        single<IWhatsNew> { FakeWhatsNew() }
    }
