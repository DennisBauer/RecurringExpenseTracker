package ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.top_app_bar_icon_button_grid_close_content_desc
import recurringexpensetracker.app.generated.resources.top_app_bar_icon_button_grid_open_content_desc

@Composable
fun ToggleGridModeButton(
    onToggleGridMode: () -> Unit,
    isGridMode: Boolean,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onToggleGridMode,
        modifier = modifier,
    ) {
        Icon(
            imageVector =
                if (isGridMode) Icons.Filled.TableRows else Icons.Filled.GridView,
            contentDescription =
                if (isGridMode) {
                    stringResource(
                        Res.string.top_app_bar_icon_button_grid_close_content_desc,
                    )
                } else {
                    stringResource(
                        Res.string.top_app_bar_icon_button_grid_open_content_desc,
                    )
                },
        )
    }
}
