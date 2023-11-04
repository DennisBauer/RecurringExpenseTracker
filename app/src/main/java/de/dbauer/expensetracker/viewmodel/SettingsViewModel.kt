package de.dbauer.expensetracker.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.dbauer.expensetracker.model.DatabaseBackupRestore
import java.io.File

class SettingsViewModel(
    private val databasePath: String,
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

    companion object {
        fun create(databasePath: String): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    SettingsViewModel(
                        databasePath = databasePath,
                    )
                }
            }
        }
    }
}
