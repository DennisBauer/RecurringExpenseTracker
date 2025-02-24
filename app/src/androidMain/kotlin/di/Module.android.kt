package di

import Constants
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.RoomDatabase
import model.database.RecurringExpenseDatabase
import model.database.UserPreferencesRepository
import model.database.getDatabaseBuilder
import model.notification.SystemNotificationBuilder
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
        singleOf(::UserPreferencesRepository)
        factory<AlarmManager> {
            val context = get<Context>()
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        }
        factory<NotificationManager> {
            val context = get<Context>()
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        factory<SystemNotificationBuilder> {
            SystemNotificationBuilder(get<Context>(), get<NotificationManager>())
        }
    }
