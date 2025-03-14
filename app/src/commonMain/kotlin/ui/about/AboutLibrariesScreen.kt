package ui.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.settings_libraries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutLibrariesScreen(
    onNavigateBack: () -> Unit,
<<<<<<< HEAD
<<<<<<< HEAD
    modifier: Modifier = Modifier,
=======
    modifier: Modifier = Modifier
>>>>>>> 4ac3d00 (feat: add about page)
=======
    modifier: Modifier = Modifier
>>>>>>> e7ee9921430f1ef42eab5b2143d0765976a01cf2
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(Res.string.settings_libraries)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
<<<<<<< HEAD
<<<<<<< HEAD
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { padding ->
        LibrariesContainer(
            modifier =
                Modifier.padding(padding)
                .fillMaxSize(),
            colors =
                LibraryDefaults.libraryColors(
                backgroundColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                badgeBackgroundColor = MaterialTheme.colorScheme.primary,
                badgeContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
=======
=======
>>>>>>> e7ee9921430f1ef42eab5b2143d0765976a01cf2
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->
        LibrariesContainer(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            colors = LibraryDefaults.libraryColors(
                backgroundColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                badgeBackgroundColor = MaterialTheme.colorScheme.primary,
                badgeContentColor = MaterialTheme.colorScheme.onPrimary
            )
<<<<<<< HEAD
>>>>>>> 4ac3d00 (feat: add about page)
=======
>>>>>>> e7ee9921430f1ef42eab5b2143d0765976a01cf2
        )
    }
}
