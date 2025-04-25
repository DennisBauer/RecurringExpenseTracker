package ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import getAppVersion
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.github
import recurringexpensetracker.app.generated.resources.ic_launcher
import recurringexpensetracker.app.generated.resources.liberapay
import recurringexpensetracker.app.generated.resources.settings_about_libraries
import recurringexpensetracker.app.generated.resources.settings_about_made_by
import recurringexpensetracker.app.generated.resources.settings_about_support
import recurringexpensetracker.app.generated.resources.settings_about_version

@Composable
fun AboutScreen(
    onLibrariesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_launcher),
            contentDescription = null,
            modifier =
                Modifier
                    .padding(vertical = 32.dp)
                    .size(120.dp),
        )
        ListItem(
            modifier =
                Modifier.clickable {
                    uriHandler.openUri("https://github.com/dennisbauer/RecurringExpenseTracker")
                },
            headlineContent = {
                Text(text = stringResource(Res.string.settings_about_version))
            },
            supportingContent = {
                Text(getAppVersion())
            },
            trailingContent = {
                Image(
                    painter = painterResource(Res.drawable.github),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            },
        )
        ListItem(
            modifier =
                Modifier.clickable {
                    uriHandler.openUri("https://github.com/DennisBauer")
                },
            headlineContent = {
                Text(text = stringResource(Res.string.settings_about_made_by) + " DennisBauer")
            },
            trailingContent = {
                Row {
                    Image(
                        painter = painterResource(Res.drawable.github),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            },
        )
        ListItem(
            modifier =
                Modifier.clickable {
                    uriHandler.openUri("https://liberapay.com/DennisBauer")
                },
            headlineContent = {
                Text(text = stringResource(Res.string.settings_about_support))
            },
            trailingContent = {
                Row {
                    Image(
                        painter = painterResource(Res.drawable.liberapay),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            },
        )
        ListItem(
            modifier = Modifier.clickable { onLibrariesClick() },
            headlineContent = {
                Text(text = stringResource(Res.string.settings_about_libraries))
            },
        )
    }
}
