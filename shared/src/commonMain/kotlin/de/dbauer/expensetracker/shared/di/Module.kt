package de.dbauer.expensetracker.shared.di

import de.dbauer.expensetracker.shared.model.CurrencyProvider
import de.dbauer.expensetracker.shared.model.ExchangeRateProvider
import de.dbauer.expensetracker.shared.model.FakeExchangeRateProvider
import de.dbauer.expensetracker.shared.model.IExchangeRateProvider
import de.dbauer.expensetracker.shared.model.database.ExpenseRepository
import de.dbauer.expensetracker.shared.model.database.FakePreviewExpenseRepository
import de.dbauer.expensetracker.shared.model.database.IExpenseRepository
import de.dbauer.expensetracker.shared.model.database.RecurringExpenseDao
import de.dbauer.expensetracker.shared.model.database.RecurringExpenseDatabase
import de.dbauer.expensetracker.shared.model.datastore.FakeUserPreferencesRepository
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.shared.model.notification.ExpenseNotificationManager
import de.dbauer.expensetracker.shared.ui.whatsnew.FakeWhatsNew
import de.dbauer.expensetracker.shared.ui.whatsnew.IWhatsNew
import de.dbauer.expensetracker.shared.ui.whatsnew.WhatsNew
import de.dbauer.expensetracker.shared.viewmodel.EditRecurringExpenseViewModel
import de.dbauer.expensetracker.shared.viewmodel.MainNavigationViewModel
import de.dbauer.expensetracker.shared.viewmodel.RecurringExpenseViewModel
import de.dbauer.expensetracker.shared.viewmodel.SettingsViewModel
import de.dbauer.expensetracker.shared.viewmodel.TagsScreenViewModel
import de.dbauer.expensetracker.shared.viewmodel.UpcomingPaymentsViewModel
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
        singleOf(::ExpenseNotificationManager)
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
