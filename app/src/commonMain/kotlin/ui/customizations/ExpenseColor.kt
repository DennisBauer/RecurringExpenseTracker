package ui.customizations

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ui.theme.expense_predefined_blue
import ui.theme.expense_predefined_cyan
import ui.theme.expense_predefined_green
import ui.theme.expense_predefined_maroon
import ui.theme.expense_predefined_mint
import ui.theme.expense_predefined_orange
import ui.theme.expense_predefined_pink
import ui.theme.expense_predefined_purple
import ui.theme.expense_predefined_red
import ui.theme.expense_predefined_turquoise
import ui.theme.expense_predefined_yellow

enum class ExpenseColor(private val value: Int) {
    Dynamic(1),
    Red(2),
    Orange(3),
    Yellow(4),
    Green(5),
    Mint(6),
    Turquoise(7),
    Cyan(8),
    Blue(9),
    Purple(10),
    Pink(11),
    Maroon(12),
    ;

    @Composable
    fun getColor(): Color {
        return when (this) {
            Dynamic -> MaterialTheme.colorScheme.surfaceVariant
            Red -> expense_predefined_red
            Orange -> expense_predefined_orange
            Yellow -> expense_predefined_yellow
            Green -> expense_predefined_green
            Mint -> expense_predefined_mint
            Turquoise -> expense_predefined_turquoise
            Cyan -> expense_predefined_cyan
            Blue -> expense_predefined_blue
            Purple -> expense_predefined_purple
            Pink -> expense_predefined_pink
            Maroon -> expense_predefined_maroon
        }
    }

    fun toInt(): Int {
        return value
    }

    companion object {
        fun fromInt(value: Int?): ExpenseColor {
            return when (value) {
                Dynamic.value -> Dynamic
                Red.value -> Red
                Orange.value -> Orange
                Yellow.value -> Yellow
                Green.value -> Green
                Mint.value -> Mint
                Turquoise.value -> Turquoise
                Cyan.value -> Cyan
                Blue.value -> Blue
                Purple.value -> Purple
                Pink.value -> Pink
                Maroon.value -> Maroon
                else -> Dynamic
            }
        }
    }
}
