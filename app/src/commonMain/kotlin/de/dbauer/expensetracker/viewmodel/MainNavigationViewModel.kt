package de.dbauer.expensetracker.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.dbauer.expensetracker.ui.whatsnew.IWhatsNew
import kotlinx.coroutines.launch

class MainNavigationViewModel(
    private val whatsNew: IWhatsNew,
) : ViewModel() {
    var shouldShowWhatsNew by mutableStateOf(false)
        private set
    var topAppBar by mutableStateOf<@Composable () -> Unit>({})

    init {
        viewModelScope.launch {
            shouldShowWhatsNew = whatsNew.shouldShowWhatsNew()
        }
    }

    fun onWhatsNewShown() {
        viewModelScope.launch {
            whatsNew.markAsShown()
            shouldShowWhatsNew = false
        }
    }
}
