package de.dbauer.expensetracker.shared.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.dbauer.expensetracker.shared.data.HomePane
import de.dbauer.expensetracker.shared.data.NavRoute
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.shared.generated.resources.Res
import recurringexpensetracker.shared.generated.resources.delete
import recurringexpensetracker.shared.generated.resources.tags_add_new
import recurringexpensetracker.shared.generated.resources.top_app_bar_icon_button_grid_close_content_desc
import recurringexpensetracker.shared.generated.resources.top_app_bar_icon_button_grid_open_content_desc
import kotlin.collections.last
import kotlin.time.Duration.Companion.seconds

data class TopAppBarAction(val icon: ImageVector, val contentDescription: StringResource, val onClick: () -> Unit)

class TopAppBarViewModel(
    private val topAppBarMediator: TopAppBarMediator,
    private val userPreferencesRepository: IUserPreferencesRepository,
) : ViewModel() {
    val title =
        topAppBarMediator.currentRoute
            .map {
                it.title
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), NavRoute.HomePane.title)
    val showBackAction =
        topAppBarMediator.currentRoute
            .map {
                it.showBackAction
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), false)
    private val _actions = mutableStateListOf<TopAppBarAction>()
    val actions: List<TopAppBarAction> get() = _actions
    private val actionsMutex = Mutex()

    private val isGridMode = userPreferencesRepository.gridMode.get()

    init {
        viewModelScope.launch {
            topAppBarMediator.currentRoute.collect {
                updateActions(it, topAppBarMediator.showDeleteExpenseButton.value)
            }
        }
        viewModelScope.launch {
            isGridMode.collect {
                updateActions(
                    topAppBarMediator.currentRoute.value,
                    topAppBarMediator.showDeleteExpenseButton.value,
                )
            }
        }
        viewModelScope.launch {
            topAppBarMediator.showDeleteExpenseButton.collect {
                updateActions(topAppBarMediator.currentRoute.value, it)
            }
        }
    }

    private suspend fun updateActions(
        topOfBackstack: NavRoute,
        showDeleteExpenseButton: Boolean,
    ) {
        val newActions = mutableListOf<TopAppBarAction>()
        when (topOfBackstack) {
            NavRoute.HomePane,
            NavRoute.UpcomingPane,
            -> {
                val isGridModeValue = isGridMode.first()
                newActions.add(
                    TopAppBarAction(
                        icon = if (isGridModeValue) Icons.Filled.TableRows else Icons.Filled.GridView,
                        contentDescription =
                            if (isGridModeValue) {
                                Res.string.top_app_bar_icon_button_grid_close_content_desc
                            } else {
                                Res.string.top_app_bar_icon_button_grid_open_content_desc
                            },
                        onClick = {
                            viewModelScope.launch {
                                userPreferencesRepository.gridMode.save(!isGridModeValue)
                            }
                        },
                    ),
                )
            }

            is NavRoute.EditExpensePane -> {
                if (showDeleteExpenseButton) {
                    newActions.add(
                        TopAppBarAction(
                            icon = Icons.Filled.Delete,
                            contentDescription = Res.string.delete,
                            onClick = {
                                topAppBarMediator.showAddOrEditTagDialog = true
                            },
                        ),
                    )
                }
            }

            NavRoute.SettingsPane -> {}

            NavRoute.TagsPane -> {
                newActions.add(
                    TopAppBarAction(
                        icon = Icons.Filled.Add,
                        contentDescription = Res.string.tags_add_new,
                        onClick = {
                            topAppBarMediator.showAddOrEditTagDialog = true
                        },
                    ),
                )
            }

            NavRoute.WhatsNew -> {
                TODO()
            }
        }
        actionsMutex.withLock {
            _actions.clear()
            _actions.addAll(newActions)
        }
    }
}
