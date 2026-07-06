package de.dbauer.expensetracker.shared.di

import Constants
import androidx.room3.RoomDatabase
import de.dbauer.expensetracker.shared.model.database.RecurringExpenseDatabase
import de.dbauer.expensetracker.shared.model.database.getDatabaseBuilder
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.shared.model.datastore.KSafeUserPreferencesRepository
import de.dbauer.expensetracker.shared.model.datastore.importLegacyPreferences
import eu.anifantakis.lib.ksafe.KSafe
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual val platformModule =
    module {
        singleOf(::getDatabaseBuilder).bind<RoomDatabase.Builder<RecurringExpenseDatabase>>()
        single { KSafe() }
        single<IUserPreferencesRepository> {
            val documentDirectory: NSURL? =
                NSFileManager.defaultManager.URLForDirectory(
                    directory = NSDocumentDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = false,
                    error = null,
                )
            val legacyFilePath =
                "${requireNotNull(documentDirectory).path}/${Constants.USER_PREFERENCES_DATA_STORE}.preferences_pb"
            KSafeUserPreferencesRepository(get()) { ksafe ->
                if (NSFileManager.defaultManager.fileExistsAtPath(legacyFilePath)) {
                    ksafe.importLegacyPreferences(legacyFilePath.toPath())
                    NSFileManager.defaultManager.removeItemAtPath(legacyFilePath, null)
                }
            }
        }
    }
