package de.dbauer.expensetracker.shared.di

import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteDriver
import de.dbauer.expensetracker.shared.model.database.RecurringExpenseDatabase
import de.dbauer.expensetracker.shared.model.database.getDatabaseBuilder
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.shared.model.datastore.KSafeUserPreferencesRepository
import eu.anifantakis.lib.ksafe.KSafe
import org.koin.dsl.module

// The SQLiteDriver is provided by the web app's module (a web worker backed driver).
actual val platformModule =
    module {
        single<RoomDatabase.Builder<RecurringExpenseDatabase>> { getDatabaseBuilder(get<SQLiteDriver>()) }
        single { KSafe() }
        single<IUserPreferencesRepository> { KSafeUserPreferencesRepository(get()) }
    }
