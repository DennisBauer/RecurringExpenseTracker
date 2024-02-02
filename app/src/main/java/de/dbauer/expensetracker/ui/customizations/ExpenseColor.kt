package de.dbauer.expensetracker.ui.customizations

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import de.dbauer.expensetracker.R

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
            Red -> colorResource(id = R.color.expense_predefined_red)
            Orange -> colorResource(id = R.color.expense_predefined_orange)
            Yellow -> colorResource(id = R.color.expense_predefined_yellow)
            Green -> colorResource(id = R.color.expense_predefined_green)
            Mint -> colorResource(id = R.color.expense_predefined_mint)
            Turquoise -> colorResource(id = R.color.expense_predefined_turquoise)
            Cyan -> colorResource(id = R.color.expense_predefined_cyan)
            Blue -> colorResource(id = R.color.expense_predefined_blue)
            Purple -> colorResource(id = R.color.expense_predefined_purple)
            Pink -> colorResource(id = R.color.expense_predefined_pink)
            Maroon -> colorResource(id = R.color.expense_predefined_maroon)
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
