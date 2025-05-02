package ui.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.rememberLibraries
import org.jetbrains.compose.resources.ExperimentalResourceApi
import recurringexpensetracker.app.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
@Composable
fun AboutLibrariesScreen(modifier: Modifier = Modifier) {
    val libraries by rememberLibraries {
        Res.readBytes("files/aboutlibraries.json").decodeToString()
    }
    val hasLibraries = libraries?.libraries?.isNotEmpty() == true
    if (hasLibraries) {
        LibrariesContainer(
            libraries,
            modifier =
                modifier
                    .fillMaxSize(),
        )
    } else {
        Text(
            text = "Used libraries are only shown in release builds.",
            textAlign = TextAlign.Center,
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .wrapContentSize(),
        )
    }
}
