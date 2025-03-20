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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import recurringexpensetracker.app.generated.resources.settings_about_app
import recurringexpensetracker.app.generated.resources.settings_about_libraries
import recurringexpensetracker.app.generated.resources.settings_about_made_by
import recurringexpensetracker.app.generated.resources.settings_about_support
import recurringexpensetracker.app.generated.resources.settings_about_version

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onLibrariesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(Res.string.settings_about_app)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        val uriHandler = LocalUriHandler.current

        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
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
}
