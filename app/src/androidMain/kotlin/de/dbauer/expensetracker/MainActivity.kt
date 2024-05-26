package de.dbauer.expensetracker

import Constants
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import asString
import de.dbauer.expensetracker.viewmodel.MainActivityViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.biometric_prompt_manager_title
import recurringexpensetracker.app.generated.resources.biometric_prompt_manager_unlock
import recurringexpensetracker.app.generated.resources.cancel
import recurringexpensetracker.app.generated.resources.settings_backup_created_toast
import recurringexpensetracker.app.generated.resources.settings_backup_not_created_toast
import recurringexpensetracker.app.generated.resources.settings_backup_not_restored_toast
import recurringexpensetracker.app.generated.resources.settings_backup_restored_toast
import security.BiometricPromptManager
import security.BiometricPromptManager.BiometricResult
import ui.MainContent
import ui.theme.ExpenseTrackerTheme
import viewmodel.RecurringExpenseViewModel
import viewmodel.SettingsViewModel
import viewmodel.UpcomingPaymentsViewModel
import viewmodel.database.UserPreferencesRepository

class MainActivity : AppCompatActivity() {
    private val recurringExpenseViewModel: RecurringExpenseViewModel by viewModels {
        RecurringExpenseViewModel.create((application as ExpenseTrackerApplication).repository)
    }
    private val upcomingPaymentsViewModel: UpcomingPaymentsViewModel by viewModels {
        UpcomingPaymentsViewModel.create((application as ExpenseTrackerApplication).repository)
    }
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModel.create(getDatabasePath(Constants.DATABASE_NAME).path)
    }
    private val userPreferencesRepository: UserPreferencesRepository by lazy {
        (application as ExpenseTrackerApplication).userPreferencesRepository
    }
    private val mainActivityViewModel: MainActivityViewModel by viewModels()

    private val biometricPromptManager: BiometricPromptManager by lazy { BiometricPromptManager(this) }

    private val biometricSetup =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            lifecycleScope.launch {
                if (it.resultCode == FINISH_TASK_WITH_ACTIVITY) {
                    triggerAuthPrompt()
                } else if (it.resultCode == Activity.RESULT_CANCELED) {
                    userPreferencesRepository.biometricSecurity.save(false)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            biometricPromptManager.promptResult.collectLatest {
                when (it) {
                    is BiometricResult.AuthenticationError -> {
                        Log.e(TAG, it.error)
                    }
                    BiometricResult.AuthenticationFailed -> {
                        Log.e(TAG, "Authentication failed")
                    }
                    BiometricResult.AuthenticationNotSet -> {
                        // open directly the setup settings for biometrics
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            biometricSetup.launch(
                                Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                    putExtra(
                                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                        biometricPromptManager.authenticators,
                                    )
                                },
                            )
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            // open old setup settings dialog
                            @Suppress("DEPRECATION")
                            biometricSetup.launch(Intent(Settings.ACTION_FINGERPRINT_ENROLL))
                        } else {
                            // open security settings
                            try {
                                startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                            } catch (_: ActivityNotFoundException) {
                            } finally {
                                launch {
                                    userPreferencesRepository.biometricSecurity.save(false)
                                }
                            }
                        }
                    }
                    BiometricResult.AuthenticationSuccess -> {
                        Log.i(TAG, "Authentication Success")
                        mainActivityViewModel.isUnlocked = true
                    }
                    BiometricResult.FeatureUnavailable -> {
                        Log.i(TAG, "Authentication unavailable")
                    }
                    BiometricResult.HardwareUnavailable -> {
                        Log.i(TAG, "Hardware not available")
                    }
                }
            }
        }

        val canUseBiometric = biometricPromptManager.canUseAuthenticator()

        setContent {
            val isGridMode by userPreferencesRepository.gridMode.collectAsState()
            val biometricSecurity by userPreferencesRepository.biometricSecurity.collectAsState()
            val backupPathLauncher =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument(Constants.BACKUP_MIME_TYPE),
                ) {
                    if (it == null) return@rememberLauncherForActivityResult
                    val takeFlags: Int =
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    applicationContext.contentResolver.takePersistableUriPermission(it, takeFlags)

                    lifecycleScope.launch {
                        val backupSuccessful = settingsViewModel.backupDatabase(it, applicationContext)
                        val toastString =
                            if (backupSuccessful) {
                                Res.string.settings_backup_created_toast
                            } else {
                                Res.string.settings_backup_not_created_toast
                            }.asString()
                        Toast.makeText(this@MainActivity, toastString, Toast.LENGTH_LONG).show()
                    }
                }
            val importPathLauncher =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) {
                    if (it == null) return@rememberLauncherForActivityResult
                    lifecycleScope.launch {
                        val backupRestored = settingsViewModel.restoreDatabase(it, applicationContext)
                        val toastString =
                            if (backupRestored) {
                                recurringExpenseViewModel.onDatabaseRestored()
                                upcomingPaymentsViewModel.onDatabaseRestored()
                                Res.string.settings_backup_restored_toast
                            } else {
                                Res.string.settings_backup_not_restored_toast
                            }.asString()
                        Toast.makeText(this@MainActivity, toastString, Toast.LENGTH_LONG).show()
                    }
                }

            ExpenseTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    if (biometricSecurity && !mainActivityViewModel.isUnlocked) {
                        LaunchedEffect(Unit) {
                            triggerAuthPrompt()
                        }
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Button(
                                onClick = {
                                    lifecycleScope.launch {
                                        triggerAuthPrompt()
                                    }
                                },
                            ) {
                                Text(text = stringResource(Res.string.biometric_prompt_manager_unlock))
                            }
                        }
                    } else {
                        MainContent(
                            weeklyExpense = recurringExpenseViewModel.weeklyExpense,
                            monthlyExpense = recurringExpenseViewModel.monthlyExpense,
                            yearlyExpense = recurringExpenseViewModel.yearlyExpense,
                            recurringExpenseData = recurringExpenseViewModel.recurringExpenseData,
                            onRecurringExpenseAdded = {
                                recurringExpenseViewModel.addRecurringExpense(it)
                            },
                            onRecurringExpenseEdited = {
                                recurringExpenseViewModel.editRecurringExpense(it)
                            },
                            onRecurringExpenseDeleted = {
                                recurringExpenseViewModel.deleteRecurringExpense(it)
                            },
                            onBackupClicked = {
                                backupPathLauncher.launch(Constants.DEFAULT_BACKUP_NAME)
                            },
                            onRestoreClicked = {
                                importPathLauncher.launch(arrayOf(Constants.BACKUP_MIME_TYPE))
                            },
                            upcomingPaymentsViewModel = upcomingPaymentsViewModel,
                            isGridMode = isGridMode,
                            biometricSecurity = biometricSecurity,
                            onBiometricSecurityChanged = {
                                lifecycleScope.launch {
                                    userPreferencesRepository.biometricSecurity.save(it)
                                }
                            },
                            toggleGridMode = {
                                lifecycleScope.launch {
                                    userPreferencesRepository.gridMode.save(!isGridMode)
                                }
                            },
                            canUseBiometric = canUseBiometric,
                        )
                    }
                }
            }
        }
    }

    private suspend fun triggerAuthPrompt() {
        biometricPromptManager.showBiometricPrompt(
            title = Res.string.biometric_prompt_manager_title.asString(),
            cancel = Res.string.cancel.asString(),
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        biometricPromptManager.onDestroy()
    }

    private companion object {
        private const val TAG = "MainActivity"
        private const val FINISH_TASK_WITH_ACTIVITY = 2
    }
}
