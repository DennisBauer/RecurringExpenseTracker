package ui

enum class DefaultTab(val value: Int) {
    Home(0),
    Upcoming(1),
    ;

    companion object {
        fun fromInt(value: Int) = entries.first { it.value == value }
    }
}
