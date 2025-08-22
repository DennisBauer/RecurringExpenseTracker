package de.dbauer.expensetracker.di

import Constants
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.RoomDatabase
import de.dbauer.expensetracker.model.database.RecurringExpenseDatabase
import de.dbauer.expensetracker.model.database.getDatabaseBuilder
import de.dbauer.expensetracker.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.model.datastore.UserPreferencesRepository
import okio.Path.Companion.toOkioPath
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import java.io.File

actual val platformModule =
    module {
        singleOf(::getDatabaseBuilder).bind<RoomDatabase.Builder<RecurringExpenseDatabase>>()
        single<DataStore<Preferences>> {
            PreferenceDataStoreFactory.createWithPath {
                File(
                    System.getProperty("java.io.tmpdir"),
                    "${Constants.USER_PREFERENCES_DATA_STORE}.preferences_pb",
                ).toOkioPath()
            }
        }
        singleOf(::UserPreferencesRepository).bind<IUserPreferencesRepository>()
    }
