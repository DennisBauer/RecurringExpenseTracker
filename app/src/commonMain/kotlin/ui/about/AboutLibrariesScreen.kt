package ui.about

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.ui.compose.LibraryColors
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.LibraryDimensions
import com.mikepenz.aboutlibraries.ui.compose.LibraryPadding
import com.mikepenz.aboutlibraries.ui.compose.LibraryTextStyles
import com.mikepenz.aboutlibraries.ui.compose.m3.libraryColors
import com.mikepenz.aboutlibraries.ui.compose.rememberLibraries
import com.mikepenz.aboutlibraries.ui.compose.util.author
import com.mikepenz.aboutlibraries.ui.compose.util.htmlReadyLicenseContent
import com.mikepenz.aboutlibraries.ui.compose.util.strippedLicenseContent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.settings_about_libraries

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun AboutLibrariesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(Res.string.settings_about_libraries)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { padding ->
        val libraries by rememberLibraries {
            Res.readBytes("files/aboutlibraries.json").decodeToString()
        }
        val hasLibraries = libraries?.libraries?.isNotEmpty() == true
        if (hasLibraries) {
            LibrariesContainer(
                libraries,
                modifier =
                    Modifier
                        .padding(padding)
                        .fillMaxSize(),
            )
        } else {
            Text(
                text = "Used libraries are only shown in release builds.",
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .wrapContentSize(),
            )
        }
    }
}

@Composable
fun LibrariesContainer(
    libraries: Libs?,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    showAuthor: Boolean = true,
    showDescription: Boolean = false,
    showVersion: Boolean = true,
    showLicenseBadges: Boolean = true,
    padding: LibraryPadding = LibraryDefaults.libraryPadding(),
    dimensions: LibraryDimensions = LibraryDefaults.libraryDimensions(),
    textStyles: LibraryTextStyles = LibraryDefaults.libraryTextStyles(),
    colors: LibraryColors = LibraryDefaults.libraryColors(),
    header: (LazyListScope.() -> Unit)? = null,
    divider: (@Composable LazyItemScope.() -> Unit)? = null,
    footer: (LazyListScope.() -> Unit)? = null,
    onLibraryClick: ((Library) -> Unit)? = null,
    licenseDialogConfirmText: String = "OK",
) {
    val uriHandler = LocalUriHandler.current

    val libs = libraries?.libraries ?: persistentListOf()
    val openDialog = remember { mutableStateOf<Library?>(null) }

    Libraries(
        libraries = libs,
        modifier = modifier,
        lazyListState = lazyListState,
        contentPadding = contentPadding,
        showAuthor = showAuthor,
        showDescription = showDescription,
        showVersion = showVersion,
        showLicenseBadges = showLicenseBadges,
        colors = colors,
        padding = padding,
        dimensions = dimensions,
        textStyles = textStyles,
        header = header,
        divider = divider,
        footer = footer,
        onLibraryClick = { library ->
            val license = library.licenses.firstOrNull()
            if (onLibraryClick != null) {
                onLibraryClick(library)
            } else if (!license?.htmlReadyLicenseContent.isNullOrBlank()) {
                openDialog.value = library
            } else if (!license?.url.isNullOrBlank()) {
                license.url?.also {
                    try {
                        uriHandler.openUri(it)
                    } catch (_: Throwable) {
                        println("Failed to open url: $it")
                    }
                }
            }
        },
    )

    val library = openDialog.value
    if (library != null) {
        LicenseDialog(library, colors, licenseDialogConfirmText) {
            openDialog.value = null
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LicenseDialog(
    library: Library,
    colors: LibraryColors = LibraryDefaults.libraryColors(),
    confirmText: String = "OK",
    onDismiss: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(),
        content = {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = colors.backgroundColor,
                contentColor = colors.contentColor,
            ) {
                Column {
                    val interactionSource = remember { MutableInteractionSource() }
                    Box(
                        modifier =
                            Modifier
                                .indication(interactionSource, LocalIndication.current)
                                .focusable(interactionSource = interactionSource)
                                .verticalScroll(scrollState)
                                .padding(8.dp)
                                .weight(1f),
                    ) {
                        LicenseDialogBody(library = library, colors = colors)
                    }
                    TextButton(
                        modifier =
                            Modifier
                                .align(Alignment.End)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        onClick = onDismiss,
                        colors =
                            ButtonDefaults.textButtonColors(
                                contentColor = colors.dialogConfirmButtonColor,
                            ),
                    ) {
                        Text(confirmText)
                    }
                }
            }
        },
    )
}

@Composable
internal fun LicenseDialogBody(
    library: Library,
    colors: LibraryColors,
    modifier: Modifier = Modifier,
) {
    val license = remember(library) { library.strippedLicenseContent.takeIf { it.isNotEmpty() } }
    if (license != null) {
        Text(
            text = license,
            modifier = modifier,
            color = colors.contentColor,
        )
    }
}

/**
 * Displays all provided libraries in a simple list.
 */
@Composable
fun Libraries(
    libraries: ImmutableList<Library>,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    showAuthor: Boolean = true,
    showDescription: Boolean = false,
    showVersion: Boolean = true,
    showLicenseBadges: Boolean = true,
    colors: LibraryColors = LibraryDefaults.libraryColors(),
    padding: LibraryPadding = LibraryDefaults.libraryPadding(),
    dimensions: LibraryDimensions = LibraryDefaults.libraryDimensions(),
    textStyles: LibraryTextStyles = LibraryDefaults.libraryTextStyles(),
    header: (LazyListScope.() -> Unit)? = null,
    divider: (@Composable LazyItemScope.() -> Unit)? = null,
    footer: (LazyListScope.() -> Unit)? = null,
    onLibraryClick: ((Library) -> Unit)? = null,
) {
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        modifier,
        verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing),
        state = lazyListState,
        contentPadding = contentPadding,
    ) {
        header?.invoke(this)
        libraryItems(
            libraries = libraries,
            showAuthor = showAuthor,
            showDescription = showDescription,
            showVersion = showVersion,
            showLicenseBadges = showLicenseBadges,
            colors = colors,
            padding = padding,
            textStyles = textStyles,
            divider = divider,
        ) { library ->
            val license = library.licenses.firstOrNull()
            if (onLibraryClick != null) {
                onLibraryClick.invoke(library)
            } else if (!license?.url.isNullOrBlank()) {
                license.url?.also {
                    try {
                        uriHandler.openUri(it)
                    } catch (_: Throwable) {
                        println("Failed to open url: $it")
                    }
                }
            }
        }
        footer?.invoke(this)
    }
}

internal inline fun LazyListScope.libraryItems(
    libraries: ImmutableList<Library>,
    showAuthor: Boolean = true,
    showDescription: Boolean = false,
    showVersion: Boolean = true,
    showLicenseBadges: Boolean = true,
    colors: LibraryColors,
    padding: LibraryPadding,
    textStyles: LibraryTextStyles,
    noinline divider: (@Composable LazyItemScope.() -> Unit)?,
    crossinline onLibraryClick: ((Library) -> Unit),
) {
    itemsIndexed(libraries) { index, library ->
        Library(
            library = library,
            onClick = { onLibraryClick.invoke(library) },
            showAuthor = showAuthor,
            showDescription = showDescription,
            showVersion = showVersion,
            showLicenseBadges = showLicenseBadges,
            colors = colors,
            padding = padding,
            textStyles = textStyles,
        )

        if (divider != null && index < libraries.lastIndex) {
            divider.invoke(this)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun Library(
    library: Library,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showAuthor: Boolean = true,
    showDescription: Boolean = false,
    showVersion: Boolean = true,
    showLicenseBadges: Boolean = true,
    colors: LibraryColors = LibraryDefaults.libraryColors(),
    padding: LibraryPadding = LibraryDefaults.libraryPadding(),
    textStyles: LibraryTextStyles = LibraryDefaults.libraryTextStyles(),
    typography: Typography = MaterialTheme.typography,
) {
    LibraryScaffoldLayout(
        modifier =
            modifier
                .fillMaxWidth()
                .background(colors.backgroundColor)
                .clickable { onClick.invoke() },
        name = {
            Text(
                text = library.name,
                style = typography.titleLarge,
                color = colors.contentColor,
                maxLines = textStyles.nameMaxLines,
                overflow = textStyles.nameOverflow,
            )
        },
        version = {
            val version = library.artifactVersion
            if (version != null && showVersion) {
                Text(
                    text = version,
                    style = typography.bodyMedium,
                    color = colors.contentColor,
                    maxLines = textStyles.versionMaxLines,
                    textAlign = TextAlign.Center,
                    overflow = textStyles.defaultOverflow,
                )
            }
        },
        author = {
            val author = library.author
            if (showAuthor && author.isNotBlank()) {
                Text(
                    text = author,
                    style = typography.bodyMedium,
                    color = colors.contentColor,
                    maxLines = textStyles.authorMaxLines,
                    overflow = textStyles.defaultOverflow,
                )
            }
        },
        description = {
            val description = library.description
            if (showDescription && !description.isNullOrBlank()) {
                Text(
                    text = description,
                    style = typography.bodySmall,
                    color = colors.contentColor,
                    maxLines = textStyles.descriptionMaxLines,
                    overflow = textStyles.defaultOverflow,
                )
            }
        },
        licenses = {
            if (showLicenseBadges && library.licenses.isNotEmpty()) {
                library.licenses.forEach {
                    Badge(
                        modifier = Modifier.padding(padding.badgePadding),
                        contentColor = colors.badgeContentColor,
                        containerColor = colors.badgeBackgroundColor,
                    ) {
                        Text(
                            modifier = Modifier.padding(padding.badgeContentPadding),
                            text = it.name,
                            style = textStyles.licensesTextStyle ?: LocalTextStyle.current,
                        )
                    }
                }
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryScaffoldLayout(
    name: @Composable BoxScope.() -> Unit,
    version: @Composable BoxScope.() -> Unit,
    author: @Composable BoxScope.() -> Unit,
    description: @Composable BoxScope.() -> Unit,
    licenses: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    libraryPadding: LibraryPadding = LibraryDefaults.libraryPadding(),
) {
    Layout(
        modifier = modifier.padding(libraryPadding.contentPadding),
        content = {
            Box(
                Modifier.layoutId(LibraryLayoutContent.Name).padding(libraryPadding.namePadding).fillMaxWidth(),
                content = name,
            )
            Box(
                Modifier.layoutId(LibraryLayoutContent.Version).padding(libraryPadding.versionPadding),
                content = version,
            )
            Box(Modifier.layoutId(LibraryLayoutContent.Author), content = author)
            Box(Modifier.layoutId(LibraryLayoutContent.Description), content = description)
            Row(Modifier.layoutId(LibraryLayoutContent.Licenses), content = licenses)
        },
    ) { measurables, constraints ->
        // don't allow version to take more than 30%
        val versionMaxWidth =
            if (constraints.maxWidth ==
                Constraints.Infinity
            ) {
                constraints.maxWidth
            } else {
                (constraints.maxWidth * 0.3f).toInt()
            }
        val versionPlaceable =
            measurables
                .fastFirst {
                    it.layoutId == LibraryLayoutContent.Version
                }.measure(constraints.copy(minWidth = 0, maxWidth = versionMaxWidth))

        val maxNameWidth =
            if (constraints.maxWidth ==
                Constraints.Infinity
            ) {
                constraints.maxWidth
            } else {
                (constraints.maxWidth - versionPlaceable.width).coerceAtLeast(0)
            }
        val namePlaceable =
            measurables
                .fastFirst {
                    it.layoutId == LibraryLayoutContent.Name
                }.measure(constraints.copy(minWidth = 0, maxWidth = maxNameWidth))

        val nameYOffset =
            if (versionPlaceable.height >
                namePlaceable.height
            ) {
                (versionPlaceable.height - namePlaceable.height) / 2
            } else {
                0
            }
        val versionYOffset =
            if (versionPlaceable.height <
                namePlaceable.height
            ) {
                (namePlaceable.height - versionPlaceable.height) / 2
            } else {
                0
            }

        val topLineHeight =
            versionPlaceable.height.coerceAtLeast(namePlaceable.height) +
                libraryPadding.verticalPadding.toPx().toInt()
        val authorPlaceable =
            measurables
                .fastFirst {
                    it.layoutId == LibraryLayoutContent.Author
                }.measure(constraints.copy(minWidth = 0))

        val authorGuideline =
            topLineHeight + authorPlaceable.height + libraryPadding.verticalPadding.toPx().toInt()
        val descriptionPlaceable =
            measurables
                .fastFirst {
                    it.layoutId == LibraryLayoutContent.Description
                }.measure(constraints.copy(minWidth = 0))

        val descriptionGuideline =
            authorGuideline + descriptionPlaceable.height + libraryPadding.verticalPadding.toPx().toInt()
        val licensesPlaceable =
            measurables
                .fastFirst {
                    it.layoutId == LibraryLayoutContent.Licenses
                }.measure(constraints.copy(minWidth = 0))

        val layoutHeight = descriptionGuideline + licensesPlaceable.height

        layout(constraints.maxWidth, layoutHeight) {
            namePlaceable.placeRelative(x = 0, y = nameYOffset)
            versionPlaceable.placeRelative(x = namePlaceable.width, y = versionYOffset)
            authorPlaceable.placeRelative(x = 0, y = topLineHeight)
            descriptionPlaceable.placeRelative(x = 0, y = authorGuideline)
            licensesPlaceable.placeRelative(x = 0, y = descriptionGuideline)
        }
    }
}

private enum class LibraryLayoutContent {
    Name,
    Version,
    Author,
    Description,
    Licenses,
}
