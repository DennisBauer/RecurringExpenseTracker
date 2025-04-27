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
import model.Currency
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.settings_choose_currency
import recurringexpensetracker.app.generated.resources.settings_currency_exchange_info
import recurringexpensetracker.app.generated.resources.settings_currency_exchange_last_update
import recurringexpensetracker.app.generated.resources.settings_system_default
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
        item {
            val selected = viewModel.selectedCurrencyCode.isBlank()
            CurrencyItem(
                currency = null,
                selected = selected,
                onSelectCurrency = {
                    viewModel.onCurrencySelected(null)
                    onNavigateBack()
                },
            )
        }
        items(viewModel.availableCurrencies) { currency ->
            val selected = viewModel.selectedCurrencyCode == currency.code
            CurrencyItem(
                currency = currency,
                selected = selected,
                onSelectCurrency = {
                    viewModel.onCurrencySelected(currency)
                    onNavigateBack()
                },
            )
        }
    }
}

@Composable
private fun CurrencyItem(
    currency: Currency?,
    selected: Boolean,
    onSelectCurrency: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onSelectCurrency,
        tonalElevation = if (selected) 4.dp else 0.dp,
        modifier = modifier,
    ) {
        val label =
            if (currency != null) {
                "${currency.name} (${currency.symbol})"
            } else {
                stringResource(Res.string.settings_system_default)
            }
        Text(
            text = label,
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Bold.takeIf { selected },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
    }
}
