package ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val md_theme_light_primary = Color(0xFF00658F)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFC7E7FF)
val md_theme_light_onPrimaryContainer = Color(0xFF001E2E)
val md_theme_light_secondary = Color(0xFF4F616E)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFD2E5F5)
val md_theme_light_onSecondaryContainer = Color(0xFF0B1D29)
val md_theme_light_tertiary = Color(0xFF63597C)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFE9DDFF)
val md_theme_light_onTertiaryContainer = Color(0xFF1F1635)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_background = Color(0xFFFCFCFF)
val md_theme_light_onBackground = Color(0xFF191C1E)
val md_theme_light_surface = Color(0xFFFCFCFF)
val md_theme_light_onSurface = Color(0xFF191C1E)
val md_theme_light_surfaceVariant = Color(0xFFDDE3EA)
val md_theme_light_onSurfaceVariant = Color(0xFF41484D)
val md_theme_light_outline = Color(0xFF71787E)
val md_theme_light_inverseOnSurface = Color(0xFFF0F0F3)
val md_theme_light_inverseSurface = Color(0xFF2E3133)
val md_theme_light_inversePrimary = Color(0xFF86CFFF)
val md_theme_light_surfaceTint = Color(0xFF00658F)
val md_theme_light_outlineVariant = Color(0xFFC1C7CE)
val md_theme_light_scrim = Color(0xFF000000)

val md_theme_dark_primary = Color(0xFF86CFFF)
val md_theme_dark_onPrimary = Color(0xFF00344C)
val md_theme_dark_primaryContainer = Color(0xFF004C6D)
val md_theme_dark_onPrimaryContainer = Color(0xFFC7E7FF)
val md_theme_dark_secondary = Color(0xFFB6C9D8)
val md_theme_dark_onSecondary = Color(0xFF21323E)
val md_theme_dark_secondaryContainer = Color(0xFF384956)
val md_theme_dark_onSecondaryContainer = Color(0xFFD2E5F5)
val md_theme_dark_tertiary = Color(0xFFCDC0E9)
val md_theme_dark_onTertiary = Color(0xFF342B4B)
val md_theme_dark_tertiaryContainer = Color(0xFF4B4263)
val md_theme_dark_onTertiaryContainer = Color(0xFFE9DDFF)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = Color(0xFF191C1E)
val md_theme_dark_onBackground = Color(0xFFE2E2E5)
val md_theme_dark_surface = Color(0xFF191C1E)
val md_theme_dark_onSurface = Color(0xFFE2E2E5)
val md_theme_dark_surfaceVariant = Color(0xFF41484D)
val md_theme_dark_onSurfaceVariant = Color(0xFFC1C7CE)
val md_theme_dark_outline = Color(0xFF8B9198)
val md_theme_dark_inverseOnSurface = Color(0xFF191C1E)
val md_theme_dark_inverseSurface = Color(0xFFE2E2E5)
val md_theme_dark_inversePrimary = Color(0xFF00658F)
val md_theme_dark_surfaceTint = Color(0xFF86CFFF)
val md_theme_dark_outlineVariant = Color(0xFF41484D)
val md_theme_dark_scrim = Color(0xFF000000)

val expense_predefined_red
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0x80ff6666) else Color(0x80990000)

val expense_predefined_orange
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0x80ffb366) else Color(0x80994d00)

val expense_predefined_yellow
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0x80ffff66) else Color(0x80999900)

val expense_predefined_green
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0x8066ff66) else Color(0x80009900)

val expense_predefined_mint
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0x8066ffb3) else Color(0x8000994d)

val expense_predefined_turquoise
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0x8066ffff) else Color(0x80009999)

val expense_predefined_cyan
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0x8066b2ff) else Color(0x80004c99)

val expense_predefined_blue
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0x807f66ff) else Color(0x80000099)

val expense_predefined_purple
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0x80cc66ff) else Color(0x804c0099)

val expense_predefined_pink
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0x80ff66ff) else Color(0x80990099)

val expense_predefined_maroon
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0x80ff66b3) else Color(0x8099004d)
