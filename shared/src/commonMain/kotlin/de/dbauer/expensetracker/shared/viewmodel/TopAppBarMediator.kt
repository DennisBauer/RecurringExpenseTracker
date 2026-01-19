package de.dbauer.expensetracker.shared.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavBackStack
import de.dbauer.expensetracker.shared.data.NavRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow

class TopAppBarMediator {
    private val _backStack = mutableStateListOf<NavRoute>(NavRoute.HomePane)
    val backStack: List<NavRoute> get() = _backStack

    private val _currentRoute = MutableStateFlow(_backStack.first())
    val currentRoute: StateFlow<NavRoute> get() = _currentRoute

    var showAddOrEditTagDialog by mutableStateOf(false)
    val showDeleteExpenseButton = MutableStateFlow(false)
    var showDeleteExpenseConfirmDialog by mutableStateOf(false)

    fun add(route: NavRoute) {
        _backStack.add(route)
        _currentRoute.value = _backStack.last()
    }

    fun navigateUp() {
        _backStack.removeLastOrNull()
        _currentRoute.value = _backStack.last()
    }

    fun setBackStack(backStack: List<NavRoute>) {
        _backStack.clear()
        _backStack.addAll(backStack)
        _currentRoute.value = _backStack.last()
    }
}
