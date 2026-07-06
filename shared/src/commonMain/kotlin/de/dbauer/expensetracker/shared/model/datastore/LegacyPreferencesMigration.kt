package de.dbauer.expensetracker.shared.model.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import eu.anifantakis.lib.ksafe.KSafe
import eu.anifantakis.lib.ksafe.KSafeWriteMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import okio.Path

/**
 * One-time import of the preferences written by previous app versions with DataStore.
 * Callers check for the old file's existence and delete it after a successful import,
 * which makes the migration run at most once.
 */
suspend fun KSafe.importLegacyPreferences(dataStoreFile: Path) {
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    try {
        val dataStore = PreferenceDataStoreFactory.createWithPath(scope = scope) { dataStoreFile }
        val preferences = dataStore.data.first()

        preferences[booleanPreferencesKey("IS_GRID_MODE")]?.let { put("IS_GRID_MODE", it, KSafeWriteMode.Plain) }
        preferences[booleanPreferencesKey("BIOMETRIC_SECURITY")]?.let {
            put("BIOMETRIC_SECURITY", it, KSafeWriteMode.Plain)
        }
        preferences[
            stringPreferencesKey(
                "DEFAULT_CURRENCY",
            ),
        ]?.let { put("DEFAULT_CURRENCY", it, KSafeWriteMode.Plain) }
        preferences[booleanPreferencesKey("SHOW_CONVERTED_CURRENCY")]?.let {
            put("SHOW_CONVERTED_CURRENCY", it, KSafeWriteMode.Plain)
        }
        preferences[booleanPreferencesKey("IS_UPCOMING_PAYMENT_NOTIFICATION")]?.let {
            put("IS_UPCOMING_PAYMENT_NOTIFICATION", it, KSafeWriteMode.Plain)
        }
        preferences[intPreferencesKey("THEME_MODE")]?.let { put("THEME_MODE", it, KSafeWriteMode.Plain) }
        preferences[intPreferencesKey("DEFAULT_TAB")]?.let { put("DEFAULT_TAB", it, KSafeWriteMode.Plain) }
        preferences[intPreferencesKey("UPCOMING_PAYMENT_NOTIFICATION_TIME")]?.let {
            put("UPCOMING_PAYMENT_NOTIFICATION_TIME", it, KSafeWriteMode.Plain)
        }
        preferences[intPreferencesKey("UPCOMING_PAYMENT_NOTIFICATION_DAYS_ADVANCE")]?.let {
            put("UPCOMING_PAYMENT_NOTIFICATION_DAYS_ADVANCE", it, KSafeWriteMode.Plain)
        }
        preferences[booleanPreferencesKey("WIDGET_BACKGROUND_TRANSPARENT")]?.let {
            put("WIDGET_BACKGROUND_TRANSPARENT", it, KSafeWriteMode.Plain)
        }
        preferences[intPreferencesKey("WHATS_NEW_VERSION_SHOWN")]?.let {
            put("WHATS_NEW_VERSION_SHOWN", it, KSafeWriteMode.Plain)
        }
        preferences[intPreferencesKey("UPCOMING_PAYMENT_HORIZON_MONTHS")]?.let {
            put("UPCOMING_PAYMENT_HORIZON_MONTHS", it, KSafeWriteMode.Plain)
        }
    } finally {
        scope.cancel()
    }
}
