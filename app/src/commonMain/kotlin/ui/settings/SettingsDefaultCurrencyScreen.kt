package ui.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.settings_choose_currency
import recurringexpensetracker.app.generated.resources.settings_currency_exchange_info
import recurringexpensetracker.app.generated.resources.settings_currency_exchange_last_update
import viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDefaultCurrencyScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel<SettingsViewModel>(),
) {
    LazyColumn(modifier = modifier) {
        item {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.padding(top = 8.dp, start = 16.dp),
            )
            Text(
                text = stringResource(Res.string.settings_currency_exchange_info),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Text(
                text =
                    stringResource(
                        Res.string.settings_currency_exchange_last_update,
                        viewModel.exchangeRateLastUpdate,
                    ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            SettingsHeaderElement(
                header = Res.string.settings_choose_currency,
            )
        }
        items(viewModel.availableCurrencies) { currency ->
            val selected = viewModel.selectedCurrencyCode == currency.code
            Surface(
                onClick = {
                    viewModel.onCurrencySelected(currency)
                    onNavigateBack()
                },
                tonalElevation = if (selected) 4.dp else 0.dp,
            ) {
                Text(
                    text = "${currency.name} (${currency.symbol})",
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Bold.takeIf { selected },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                )
            }
        }
    }
}
