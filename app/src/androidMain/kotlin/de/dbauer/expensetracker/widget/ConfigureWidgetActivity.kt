package de.dbauer.expensetracker.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import de.dbauer.expensetracker.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.security.BiometricPromptManager
import de.dbauer.expensetracker.security.BiometricPromptManager.BiometricResult
import de.dbauer.expensetracker.ui.theme.ExpenseTrackerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.get
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.biometric_prompt_manager_title
import recurringexpensetracker.app.generated.resources.cancel
import recurringexpensetracker.app.generated.resources.dialog_ok
import recurringexpensetracker.app.generated.resources.widget_configuration_biometric
import recurringexpensetracker.app.generated.resources.widget_configuration_biometric_deactivate
import recurringexpensetracker.app.generated.resources.widget_configuration_title
import recurringexpensetracker.app.generated.resources.widget_grid_mode
import recurringexpensetracker.app.generated.resources.widget_transparent_background

class ConfigureWidgetActivity : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val userPreferencesRepository = get<IUserPreferencesRepository>()
    private val biometricPromptManager: BiometricPromptManager by lazy { BiometricPromptManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        appWidgetId = intent.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_CANCELED, resultValue)

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        lifecycleScope.launch {
            biometricPromptManager.promptResult.collectLatest {
                when (it) {
                    is BiometricResult.AuthenticationSuccess -> {
                        launch {
                            userPreferencesRepository.biometricSecurity.save(false)
                        }
                    }

                    else -> {
                        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        setResult(RESULT_CANCELED, resultValue)
                        finish()
                        return@collectLatest
                    }
                }
            }
        }

        setContent {
            val biometricPromptTitle = stringResource(Res.string.biometric_prompt_manager_title)
            val biometricCancel = stringResource(Res.string.cancel)
            ExpenseTrackerTheme {
                ConfigurationContent(
                    isBiometricEnabled = userPreferencesRepository.biometricSecurity.collectAsState().value,
                    isGridModeEnabled = userPreferencesRepository.gridMode.collectAsState().value,
                    isWidgetBackgroundTransparent =
                        userPreferencesRepository.widgetBackgroundTransparent
                            .collectAsState()
                            .value,
                    onShowBiometricPrompt = {
                        biometricPromptManager.showBiometricPrompt(
                            biometricPromptTitle,
                            biometricCancel,
                        )
                    },
                    onGridModeChange = {
                        lifecycleScope.launch(Dispatchers.IO) {
                            userPreferencesRepository.gridMode.save(it)
                        }
                    },
                    onWidgetBackgroundTransparentChange = {
                        lifecycleScope.launch(Dispatchers.IO) {
                            userPreferencesRepository.widgetBackgroundTransparent.save(it)
                        }
                    },
                    onConfirmClick = {
                        val resultValue =
                            Intent().putExtra(
                                AppWidgetManager.EXTRA_APPWIDGET_ID,
                                appWidgetId,
                            )
                        setResult(RESULT_OK, resultValue)
                        finish()
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigurationContent(
    isBiometricEnabled: Boolean,
    isGridModeEnabled: Boolean,
    isWidgetBackgroundTransparent: Boolean,
    onShowBiometricPrompt: () -> Unit,
    onGridModeChange: (Boolean) -> Unit,
    onWidgetBackgroundTransparentChange: (Boolean) -> Unit,
    onConfirmClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.widget_configuration_title),
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (isBiometricEnabled) {
                BiometricSection(onShowBiometricPrompt)
            } else {
                RowWithSwitch(
                    title = stringResource(Res.string.widget_grid_mode),
                    enabled = isGridModeEnabled,
                    onChange = onGridModeChange,
                )
                RowWithSwitch(
                    title = stringResource(Res.string.widget_transparent_background),
                    enabled = isWidgetBackgroundTransparent,
                    onChange = onWidgetBackgroundTransparentChange,
                )
                Button(onClick = onConfirmClick) {
                    Text(text = stringResource(Res.string.dialog_ok))
                }
            }
        }
    }
}

@Composable
private fun BiometricSection(onShowBiometricPrompt: () -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = stringResource(Res.string.widget_configuration_biometric))
        Button(
            onClick = onShowBiometricPrompt,
        ) {
            Text(text = stringResource(Res.string.widget_configuration_biometric_deactivate))
        }
    }
}

@Composable
private fun RowWithSwitch(
    title: String,
    enabled: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier.clickable(onClick = { onChange(!enabled) }),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = enabled,
            onCheckedChange = onChange,
        )
    }
}

private class IsGridModePreviewParameterProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(true, false)
}

@Preview
@Composable
private fun ConfigurationContentPreview(
    @PreviewParameter(IsGridModePreviewParameterProvider::class) isGridModeEnabled: Boolean,
) {
    ExpenseTrackerTheme {
        ConfigurationContent(
            isBiometricEnabled = false,
            isGridModeEnabled = isGridModeEnabled,
            isWidgetBackgroundTransparent = isGridModeEnabled,
            onShowBiometricPrompt = {},
            onGridModeChange = {},
            onWidgetBackgroundTransparentChange = {},
            onConfirmClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ConfigurationContentPreview3() {
    ExpenseTrackerTheme {
        ConfigurationContent(
            isBiometricEnabled = true,
            isGridModeEnabled = true,
            isWidgetBackgroundTransparent = true,
            onShowBiometricPrompt = {},
            onGridModeChange = {},
            onWidgetBackgroundTransparentChange = {},
            onConfirmClick = {},
        )
    }
}
