package de.dbauer.expensetracker.viewmodel.database

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    private object Keys {
        val IS_GRID_MODE = booleanPreferencesKey("IS_GRID_MODE")
        val BIOMETRIC_SECURITY = booleanPreferencesKey("BIOMETRIC_SECURITY")
    }

    suspend fun saveIsGridMode(isGridMode: Boolean) =
        withContext(Dispatchers.IO) {
            dataStore.edit {
                it[Keys.IS_GRID_MODE] = isGridMode
            }
        }

    fun getIsGridMode(): Flow<Boolean> = dataStore.data.map { it[Keys.IS_GRID_MODE] ?: false }

    suspend fun saveBiometricSecurity(biometricSecurity: Boolean) =
        withContext(Dispatchers.IO) {
            dataStore.edit {
                it[Keys.BIOMETRIC_SECURITY] = biometricSecurity
            }
        }

    fun getBiometricSecurity(): Flow<Boolean> = dataStore.data.map { it[Keys.BIOMETRIC_SECURITY] ?: false }
}
