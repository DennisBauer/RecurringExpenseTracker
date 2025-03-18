package de.dbauer.expensetracker

import Constants
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import asString
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import data.HomePane
import data.SettingsPane
import data.UpcomingPane
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import model.DatabaseBackupRestore
import model.database.UserPreferencesRepository
import model.notification.ExpenseNotificationManager
import model.notification.NotificationLoopReceiver
import model.notification.startAlarmLooper
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.get
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
import viewmodel.MainActivityViewModel
import java.io.File

class MainActivity : AppCompatActivity() {
    private val databasePath by lazy { getDatabasePath(Constants.DATABASE_NAME).path }
    private val userPreferencesRepository = get<UserPreferencesRepository>()
    private val expenseNotificationManager = get<ExpenseNotificationManager>()
    private val mainActivityViewModel: MainActivityViewModel by viewModels()

    private val biometricPromptManager: BiometricPromptManager by lazy { BiometricPromptManager(this) }

    private val biometricSetup =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            lifecycleScope.launch {
                if (it.resultCode == FINISH_TASK_WITH_ACTIVITY) {
                    triggerAuthPrompt()
                } else if (it.resultCode == RESULT_CANCELED) {
                    userPreferencesRepository.biometricSecurity.save(false)
                }
            }
        }

    @OptIn(ExperimentalPermissionsApi::class)
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

        val startRoute =
            IntentCompat.getSerializableExtra(intent, EXTRA_START_ROUTE, StartRoute::class.java)?.destination
                ?: StartRoute.Home.destination

        val invalidExpenseId = -1
        val expenseId = intent.getIntExtra(EXTRA_EXPENSE_ID, invalidExpenseId)
        if (expenseId != invalidExpenseId) {
            lifecycleScope.launch {
                expenseNotificationManager.markNotificationAsShown(expenseId)
            }
        }

        // Register on change for upcoming payment notification and reschedule alarm looper
        lifecycleScope.launch {
            userPreferencesRepository.upcomingPaymentNotification.get().collect {
                startAlarmLooper(NotificationLoopReceiver::class.java)
            }
        }
        lifecycleScope.launch {
            userPreferencesRepository.upcomingPaymentNotificationTime.get().collect {
                startAlarmLooper(NotificationLoopReceiver::class.java)
            }
        }

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
                        val backupSuccessful =
                            DatabaseBackupRestore().exportDatabaseFile(
                                databasePath = databasePath,
                                targetUri = it,
                                applicationContext = applicationContext,
                            )
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
                        File(databasePath).parent?.let { targetPath ->
                            val backupRestored =
                                DatabaseBackupRestore().importDatabaseFile(
                                    srcZipUri = it,
                                    targetPath = targetPath,
                                    applicationContext = applicationContext,
                                )
                            val toastString =
                                if (backupRestored) {
                                    Res.string.settings_backup_restored_toast
                                } else {
                                    Res.string.settings_backup_not_restored_toast
                                }.asString()
                            Toast.makeText(this@MainActivity, toastString, Toast.LENGTH_LONG).show()

                            if (backupRestored) {
                                // Restart Activity after restoring backup to make sure the repository is updated
                                finish()
                                startActivity(intent)
                            }
                        }
                    }
                }

            var notificationPermissionGranted by rememberSaveable { mutableStateOf(true) }
            var notificationPermissionState: PermissionState? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionState =
                    rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
                notificationPermissionGranted = notificationPermissionState.status.isGranted
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
                            onClickBackup = {
                                backupPathLauncher.launch(Constants.DEFAULT_BACKUP_NAME)
                            },
                            onClickRestore = {
                                importPathLauncher.launch(arrayOf(Constants.BACKUP_MIME_TYPE))
                            },
                            isGridMode = isGridMode,
                            biometricSecurity = biometricSecurity,
                            onBiometricSecurityChange = {
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
                            canUseNotifications = true,
                            hasNotificationPermission = notificationPermissionGranted,
                            requestNotificationPermission = {
                                notificationPermissionState?.launchPermissionRequest()
                            },
                            navigateToPermissionsSettings = {
                                navigateToNotificationPermissionSettings()
                            },
                            startRoute = startRoute,
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

    private fun navigateToNotificationPermissionSettings() {
        startActivity(
            Intent().apply {
                setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            },
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        biometricPromptManager.onDestroy()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val FINISH_TASK_WITH_ACTIVITY = 2
        private const val EXTRA_EXPENSE_ID = "intent_expense_id"
        private const val EXTRA_START_ROUTE = "intent_start_route"

        fun newInstance(
            context: Context,
            expenseId: Int,
            startRoute: StartRoute,
        ): Intent {
            return Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(EXTRA_EXPENSE_ID, expenseId)
                putExtra(EXTRA_START_ROUTE, startRoute)
            }
        }
    }
}

enum class StartRoute(val destination: String) {
    Home(HomePane.ROUTE),
    Upcoming(UpcomingPane.ROUTE),
    Settings(SettingsPane.ROUTE),
}
