package de.dbauer.expensetracker.viewmodel.database

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import java.util.Locale

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    private object Keys {

        val GLOBAL_CURRENCY = stringPreferencesKey("global_currency")
    }

    suspend fun saveCurrency(locale: Locale) {
        dataStore.edit { settings ->
            settings[Keys.GLOBAL_CURRENCY] = "${locale.isO3Country}_${locale.isO3Language}"
        }
    }
}
