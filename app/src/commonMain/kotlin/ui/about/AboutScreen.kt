package ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.settings_about_app
import de.dbauer.expensetracker.BuildConfig
import org.jetbrains.compose.resources.painterResource
import recurringexpensetracker.app.generated.resources.github
import recurringexpensetracker.app.generated.resources.ic_launcher
import recurringexpensetracker.app.generated.resources.liberapay
import androidx.compose.foundation.clickable
import recurringexpensetracker.app.generated.resources.settings_about_made_by
import recurringexpensetracker.app.generated.resources.settings_about_version
import recurringexpensetracker.app.generated.resources.settings_about_libraries
import recurringexpensetracker.app.generated.resources.settings_about_support

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onLibrariesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(Res.string.settings_about_app)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Image(
                    painter = painterResource(Res.drawable.ic_launcher),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(vertical = 32.dp)
                        .size(120.dp)
                )
            }
            item {
                ListItem(
                    modifier = Modifier
                        .clickable {
                            openLinkInBrowser(context,"https://github.com/dennisbauer/RecurringExpenseTracker")
                        },
                    headlineContent = {
                        Text(text = stringResource(Res.string.settings_about_version))
                    },
                    supportingContent = {
                        Text(" ${BuildConfig.VERSION_NAME}")
                    },
                    trailingContent = {
                        Image(
                            painter = painterResource(Res.drawable.github),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                ListItem(
                    modifier = Modifier
                        .clickable {
                            openLinkInBrowser(context,"https://github.com/DennisBauer")
                        },
                    headlineContent = {
                        Text(text = stringResource(Res.string.settings_about_made_by)+" DennisBauer")
                    },
                    trailingContent = {
                        Row {
                            Image(
                                painter = painterResource(Res.drawable.github),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                ListItem(
                    modifier = Modifier
                        .clickable {
                            openLinkInBrowser(context,"https://liberapay.com/DennisBauer")
                        },
                    headlineContent = {
                        Text(text = stringResource(Res.string.settings_about_support))
                    },
                    trailingContent = {
                        Row {
                            Image(
                                painter = painterResource(Res.drawable.liberapay),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                ListItem(
                    modifier = Modifier
                        .clickable { onLibrariesClick() },
                    headlineContent = {
                        Text(text = stringResource(Res.string.settings_about_libraries))
                    },
                )
            }
        }
    }
}
