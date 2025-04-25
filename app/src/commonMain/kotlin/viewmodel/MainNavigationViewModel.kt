package viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainNavigationViewModel : ViewModel() {
    var topAppBar by mutableStateOf<@Composable () -> Unit>({})
}
