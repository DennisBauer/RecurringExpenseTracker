package de.dbauer.expensetracker.shared.di

import Constants
import android.content.Context
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room3.RoomDatabase
import de.dbauer.expensetracker.shared.model.database.RecurringExpenseDatabase
import de.dbauer.expensetracker.shared.model.database.getDatabaseBuilder
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.shared.model.datastore.KSafeUserPreferencesRepository
import de.dbauer.expensetracker.shared.model.datastore.importLegacyPreferences
import eu.anifantakis.lib.ksafe.KSafe
import okio.Path.Companion.toOkioPath
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule =
    module {
        singleOf(::getDatabaseBuilder).bind<RoomDatabase.Builder<RecurringExpenseDatabase>>()
        single { KSafe(get<Context>()) }
        single<IUserPreferencesRepository> {
            val legacyFile = get<Context>().preferencesDataStoreFile(name = Constants.USER_PREFERENCES_DATA_STORE)
            KSafeUserPreferencesRepository(get()) { ksafe ->
                if (legacyFile.exists()) {
                    ksafe.importLegacyPreferences(legacyFile.toOkioPath())
                    legacyFile.delete()
                }
            }
        }
    }
