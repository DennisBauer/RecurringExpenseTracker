package de.dbauer.expensetracker.ui

enum class ThemeMode(val value: Int) {
    FollowSystem(0),
    Dark(1),
    Light(2),
    Amoled(3),
    ;

    companion object {
        fun fromInt(value: Int) = entries.first { it.value == value }
    }
}
