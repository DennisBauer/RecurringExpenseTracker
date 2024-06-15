package ui.editexpense

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import conditional
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.edit_expense_color
import ui.customizations.ExpenseColor
import ui.theme.ExpenseTrackerTheme

@Composable
fun ColorOption(
    expenseColor: ExpenseColor,
    onSelectExpenseColor: (ExpenseColor) -> Unit,
    modifier: Modifier = Modifier,
) {
    var colorPickerOpen by rememberSaveable { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .padding(vertical = 8.dp)
                .clickable { colorPickerOpen = true },
    ) {
        Text(
            text = stringResource(Res.string.edit_expense_color),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.weight(1f))
        expenseColor.getColor().let {
            Canvas(
                modifier =
                    Modifier
                        .size(48.dp),
                onDraw = {
                    drawCircle(color = it)
                },
            )
        }
    }

    if (colorPickerOpen) {
        ColorPickerDialog(
            predefinedColors = getAvailableColors(),
            onDismiss = { colorPickerOpen = false },
            currentlySelected = expenseColor,
            onSelectColor = onSelectExpenseColor,
        )
    }
}

@Composable
private fun ColorPickerDialog(
    predefinedColors: List<ExpenseColor>,
    onDismiss: (() -> Unit),
    currentlySelected: ExpenseColor,
    onSelectColor: ((ExpenseColor) -> Unit),
) {
    val gridState = rememberLazyGridState()

    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(48.dp),
                state = gridState,
            ) {
                items(predefinedColors) { color ->
                    val outlineColor = MaterialTheme.colorScheme.onSurface

                    Canvas(
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .clip(RoundedCornerShape(50))
                                .conditional(currentlySelected == color) {
                                    border(
                                        width = 2.dp,
                                        color = outlineColor,
                                        shape = RoundedCornerShape(50),
                                    )
                                }.background(color.getColor())
                                .requiredSize(48.dp)
                                .clickable {
                                    onSelectColor(color)
                                    onDismiss()
                                },
                    ) {
                    }
                }
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun getAvailableColors(): List<ExpenseColor> {
    return ExpenseColor.entries
}

@Preview
@Composable
private fun ColorOptionPreview() {
    ExpenseTrackerTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth(),
        ) {
            ColorOption(
                expenseColor = ExpenseColor.Red,
                onSelectExpenseColor = {},
            )
        }
    }
}

@Preview
@Composable
private fun ColorPickerDialogPreview() {
    ExpenseTrackerTheme {
        var selectedColor by remember { mutableStateOf(ExpenseColor.Green) }
        ColorPickerDialog(
            predefinedColors = getAvailableColors(),
            onDismiss = {},
            currentlySelected = selectedColor,
            onSelectColor = { selectedColor = it },
        )
    }
}
