package de.dbauer.expensetracker.shared.di

import Constants
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
import java.io.File

actual val platformModule =
    module {
        singleOf(::getDatabaseBuilder).bind<RoomDatabase.Builder<RecurringExpenseDatabase>>()
        single { KSafe() }
        single<IUserPreferencesRepository> {
            val legacyFile =
                File(
                    System.getProperty("java.io.tmpdir"),
                    "${Constants.USER_PREFERENCES_DATA_STORE}.preferences_pb",
                )
            KSafeUserPreferencesRepository(get()) { ksafe ->
                if (legacyFile.exists()) {
                    ksafe.importLegacyPreferences(legacyFile.toOkioPath())
                    legacyFile.delete()
                }
            }
        }
    }
