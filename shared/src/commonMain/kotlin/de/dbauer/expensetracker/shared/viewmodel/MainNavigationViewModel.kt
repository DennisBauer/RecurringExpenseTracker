package de.dbauer.expensetracker.shared.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.dbauer.expensetracker.shared.data.NavRoute
import de.dbauer.expensetracker.shared.ui.whatsnew.IWhatsNew
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class MainNavigationViewModel(
    private val topAppBarMediator: TopAppBarMediator,
    private val whatsNew: IWhatsNew,
) : ViewModel() {
    var shouldShowWhatsNew by mutableStateOf(false)
        private set
    var topAppBar by mutableStateOf<@Composable () -> Unit>({})

    val backStack get() = topAppBarMediator.backStack
    val currentRoute get() = topAppBarMediator.currentRoute

    init {
        viewModelScope.launch {
            shouldShowWhatsNew = whatsNew.shouldShowWhatsNew()
        }
    }

    fun onBottomNavClick(route: NavRoute) {
        viewModelScope.launch {
            val backStackTmp = backStack.toMutableList()
            if (backStackTmp.size >= 2) {
                backStackTmp.subList(1, backStackTmp.size).clear()
            }
            if (!backStackTmp.contains(route)) {
                backStackTmp.add(route)
            }
            topAppBarMediator.setBackStack(backStackTmp)
        }
    }

    fun navigate(route: NavRoute) {
        topAppBarMediator.add(route)
    }

    fun navigateUp() {
        topAppBarMediator.navigateUp()
    }

    fun onWhatsNewShown() {
        viewModelScope.launch {
            whatsNew.markAsShown()
            shouldShowWhatsNew = false

            navigateUp()
        }
    }
}
