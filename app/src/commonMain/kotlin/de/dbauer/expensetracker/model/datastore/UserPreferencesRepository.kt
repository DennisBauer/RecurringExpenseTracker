package de.dbauer.expensetracker.model.datastore

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

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) : IUserPreferencesRepository {
    inner class Preference<T>(
        private val key: Preferences.Key<T>,
        private val defaultValue: T,
    ) : IUserPreferencesRepository.IPreference<T> {
        override suspend fun save(value: T): Unit =
            withContext(Dispatchers.IO) {
                dataStore.edit { preferences ->
                    preferences[this@Preference.key] = value
                }
            }

        override fun get(): Flow<T> {
            return dataStore.data
                .map {
                    it[this.key] ?: this.defaultValue
                }.distinctUntilChanged()
        }

        @Composable
        override fun collectAsState(): State<T> {
            return dataStore.data
                .map {
                    it[this.key] ?: this.defaultValue
                }.collectAsState(this.defaultValue)
        }
    }

    override val gridMode = Preference(booleanPreferencesKey("IS_GRID_MODE"), false)
    override val biometricSecurity = Preference(booleanPreferencesKey("BIOMETRIC_SECURITY"), false)
    override val defaultCurrency = Preference(stringPreferencesKey("DEFAULT_CURRENCY"), "")
    override val showConvertedCurrency = Preference(booleanPreferencesKey("SHOW_CONVERTED_CURRENCY"), true)
    override val upcomingPaymentNotification =
        Preference(booleanPreferencesKey("IS_UPCOMING_PAYMENT_NOTIFICATION"), false)
    override val themeMode = Preference(intPreferencesKey("THEME_MODE"), 0)
    override val defaultTab = Preference(intPreferencesKey("DEFAULT_TAB"), 0)

    // default: 8:00 in the morning
    override val upcomingPaymentNotificationTime =
        Preference(intPreferencesKey("UPCOMING_PAYMENT_NOTIFICATION_TIME"), 480)

    // default: 3 days in advance
    override val upcomingPaymentNotificationDaysAdvance =
        Preference(intPreferencesKey("UPCOMING_PAYMENT_NOTIFICATION_DAYS_ADVANCE"), 3)
    override val widgetBackgroundTransparent =
        Preference(booleanPreferencesKey("WIDGET_BACKGROUND_TRANSPARENT"), false)
    override val whatsNewVersionShown: IUserPreferencesRepository.IPreference<Int> =
        Preference(intPreferencesKey("WHATS_NEW_VERSION_SHOWN"), 0)
}
