package de.dbauer.expensetracker.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.dbauer.expensetracker.model.DatabaseBackupRestore
import de.dbauer.expensetracker.viewmodel.database.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.io.File
import java.util.Locale

class SettingsViewModel(
    private val databasePath: String,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val databaseBackupRestore = DatabaseBackupRestore()

    suspend fun backupDatabase(
        targetUri: Uri,
        applicationContext: Context,
    ): Boolean {
        return databaseBackupRestore.exportDatabaseFile(databasePath, targetUri, applicationContext)
    }

    suspend fun restoreDatabase(
        srcZipUri: Uri,
        applicationContext: Context,
    ): Boolean {
        val targetPath = File(databasePath).parent ?: return false
        return databaseBackupRestore.importDatabaseFile(srcZipUri, targetPath, applicationContext)
    }

    suspend fun changeGlobalCurrency(locale: Locale) {
        userPreferencesRepository.saveCurrency(locale)
    }

    fun getGlobalCurrency(): StateFlow<Locale> {
        return userPreferencesRepository.getCurrency().stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            Locale.getDefault(),
        )
    }

    companion object {

        fun create(
            databasePath: String,
            userPreferencesRepository: UserPreferencesRepository,
        ): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    SettingsViewModel(
                        databasePath = databasePath,
                        userPreferencesRepository = userPreferencesRepository,
                    )
                }
            }
        }
    }
}
