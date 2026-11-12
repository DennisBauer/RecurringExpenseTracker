package de.dbauer.expensetracker.shared.ui

import de.dbauer.expensetracker.shared.data.HomePane
import de.dbauer.expensetracker.shared.data.MainNavRoute
import de.dbauer.expensetracker.shared.data.UpcomingPane
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import kotlinx.coroutines.flow.first

enum class DefaultTab(val value: Int) {
    Home(0),
    Upcoming(1),
    ;

    companion object {
        fun fromInt(value: Int) = entries.first { it.value == value }
    }
}

/** The route of the tab the user chose to open on app start. */
suspend fun IUserPreferencesRepository.getDefaultTabRoute(): MainNavRoute =
    when (defaultTab.get().first()) {
        DefaultTab.Upcoming.value -> UpcomingPane
        else -> HomePane()
    }
