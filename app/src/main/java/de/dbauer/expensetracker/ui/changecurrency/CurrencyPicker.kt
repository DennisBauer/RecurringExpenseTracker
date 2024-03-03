package de.dbauer.expensetracker.ui.changecurrency

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPicker(
    current: Locale,
    items: List<Locale>,
    onItemClicked: (Locale) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    val testNumber = 123_456.78F
    val sheetState: SheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 16.dp),
    ) {
        Text(
            text = "Selected currency is: $current",
            modifier = Modifier.fillMaxWidth(),
        )

        TextField(value = query, onValueChange = { query = it })
        LazyColumn {
            for (locale in items) {
                val currency =
                    NumberFormat.getCurrencyInstance(locale).currency!! // nulls already filtered out
                if (
                    currency.currencyCode.contains(query, ignoreCase = true) ||
                    locale.displayName.contains(query, ignoreCase = true)
                ) {
                    item {
                        CurrencyItem(locale, currency, testNumber, onItemClicked)
                    }
                }
            }
        }

    }
}

@Composable
private fun CurrencyItem(
    locale: Locale,
    currency: Currency,
    testNumber: Float,
    onItemClicked: (Locale) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currencyCode = NumberFormat.getCurrencyInstance(locale).currency?.currencyCode
    Surface(
        color = Color.Transparent,
        modifier = modifier.fillMaxWidth(),
        onClick = { onItemClicked(locale) },
    ) {
        Row {
            Text(text = currency.currencyCode)
            Spacer(modifier = Modifier.padding(10.dp))
            Text(
                text = NumberFormat
                    .getCurrencyInstance(locale)
                    .format(testNumber),
            )
        }
    }
}
