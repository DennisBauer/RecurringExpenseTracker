package model.database

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    inner class Preference<T>(
        private val key: Preferences.Key<T>,
        private val defaultValue: T,
    ) {
        suspend fun save(value: T) =
            withContext(Dispatchers.IO) {
                dataStore.edit { preferences ->
                    preferences[this@Preference.key] = value
                }
            }

        fun get(): Flow<T> {
            return dataStore.data
                .map {
                    it[this.key] ?: this.defaultValue
                }.distinctUntilChanged()
        }

        @Composable
        fun collectAsState(): State<T> {
            return dataStore.data
                .map {
                    it[this.key] ?: this.defaultValue
                }.collectAsState(this.defaultValue)
        }
    }

    val gridMode = Preference(booleanPreferencesKey("IS_GRID_MODE"), false)
    val biometricSecurity = Preference(booleanPreferencesKey("BIOMETRIC_SECURITY"), false)
    val defaultCurrency = Preference(stringPreferencesKey("DEFAULT_CURRENCY"), "")
    val upcomingPaymentNotification = Preference(booleanPreferencesKey("IS_UPCOMING_PAYMENT_NOTIFICATION"), false)

    // default: 8:00 in the morning
    val upcomingPaymentNotificationTime = Preference(intPreferencesKey("UPCOMING_PAYMENT_NOTIFICATION_TIME"), 480)

    // default: 3 days in advance
    val upcomingPaymentNotificationDaysAdvance =
        Preference(intPreferencesKey("UPCOMING_PAYMENT_NOTIFICATION_DAYS_ADVANCE"), 3)
}
