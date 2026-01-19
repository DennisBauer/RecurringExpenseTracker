package de.dbauer.expensetracker.shared.di

import Constants
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.RoomDatabase
import de.dbauer.expensetracker.shared.model.database.RecurringExpenseDatabase
import de.dbauer.expensetracker.shared.model.database.getDatabaseBuilder
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.shared.model.datastore.UserPreferencesRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule =
    module {
        singleOf(::getDatabaseBuilder).bind<RoomDatabase.Builder<RecurringExpenseDatabase>>()
        single<DataStore<Preferences>> {
            val context = get<Context>()
            PreferenceDataStoreFactory.create {
                context.preferencesDataStoreFile(name = Constants.USER_PREFERENCES_DATA_STORE)
            }
        }
        singleOf(::UserPreferencesRepository).bind<IUserPreferencesRepository>()
    }
