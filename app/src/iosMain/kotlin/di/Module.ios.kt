package di

import Constants
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import model.database.RecurringExpenseDatabase
import model.database.UserPreferencesRepository
import model.database.getDatabaseBuilder
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
        single<DataStore<Preferences>> {
            PreferenceDataStoreFactory.createWithPath {
                val documentDirectory: NSURL? =
                    NSFileManager.defaultManager.URLForDirectory(
                        directory = NSDocumentDirectory,
                        inDomain = NSUserDomainMask,
                        appropriateForURL = null,
                        create = false,
                        error = null,
                    )
                "${requireNotNull(documentDirectory).path}/${Constants.USER_PREFERENCES_DATA_STORE}.preferences_pb"
                    .toPath()
            }
        }
        singleOf(::UserPreferencesRepository)
    }
