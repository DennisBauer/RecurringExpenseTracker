package de.dbauer.expensetracker.shared.ui

enum class UpcomingHorizon(val months: Int) {
    OneMonth(1),
    ThreeMonths(3),
    SixMonths(6),
    OneYear(12),
    TwoYears(24),
    FiveYears(60),
    TenYears(120),
    ;

    companion object {
        fun fromMonths(value: Int) = entries.firstOrNull { it.months == value } ?: TenYears
    }
}
