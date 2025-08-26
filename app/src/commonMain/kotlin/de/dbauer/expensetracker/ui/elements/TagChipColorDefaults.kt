package de.dbauer.expensetracker.ui.elements

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.isSpecified
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

@Composable fun tagChipColorDefaults(tagColor: Color) =
    InputChipDefaults.inputChipColors().copy(
        containerColor = tagColor.copy(alpha = 0.2f),
        selectedContainerColor = tagColor,
        labelColor = contentColorForCustomBackground(tagColor.copy(alpha = 0.2f)),
        selectedLabelColor = contentColorForCustomBackground(tagColor),
        leadingIconColor = contentColorForCustomBackground(tagColor.copy(alpha = 0.2f)),
        selectedLeadingIconColor = contentColorForCustomBackground(tagColor),
    )

/**
 * WCAG 2.1 relative luminance for an sRGB color (0..1 channels).
 */
private fun relativeLuminance(color: Color): Double {
    fun channel(c: Double): Double = if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)

    val r = channel(color.red.toDouble())
    val g = channel(color.green.toDouble())
    val b = channel(color.blue.toDouble())

    // Rec. 709 coefficients
    return 0.2126 * r + 0.7152 * g + 0.0722 * b
}

/**
 * WCAG contrast ratio between two colors (1.0..21.0).
 */
private fun contrastRatio(
    a: Color,
    b: Color,
): Double {
    val l1 = relativeLuminance(a)
    val l2 = relativeLuminance(b)
    val lighter = max(l1, l2)
    val darker = min(l1, l2)
    return (lighter + 0.05) / (darker + 0.05)
}

/**
 * Choose between theme's onSurface color based on contrast.
 */
@Composable
private fun bestThemedOnColor(
    background: Color,
    baseSurface: Color = MaterialTheme.colorScheme.background,
    minContrast: Double = 4.5,
    lightCandidate: Color = MaterialTheme.colorScheme.inverseOnSurface,
    darkCandidate: Color = MaterialTheme.colorScheme.onSurface,
): Color {
    val effectiveBg = background.compositeOver(baseSurface)
    val cLight = contrastRatio(lightCandidate, effectiveBg)
    val cDark = contrastRatio(darkCandidate, effectiveBg)

    return when {
        cLight >= minContrast && cDark >= minContrast -> if (cLight >= cDark) lightCandidate else darkCandidate
        cLight >= minContrast -> lightCandidate
        cDark >= minContrast -> darkCandidate
        else -> if (cLight >= cDark) lightCandidate else darkCandidate
    }
}

/**
 * A drop-in helper that behaves like Material's contentColorFor(), but
 * falls back to a contrast-aware pick for other colors.
 *
 * If the background is a known scheme color (primary, surface, etc.),
 * it will return the corresponding on* token (via contentColorFor).
 * If not, it computes a contrast-aware on-color from the theme.
 */
@Composable
private fun contentColorForCustomBackground(
    background: Color,
    baseSurface: Color = MaterialTheme.colorScheme.background,
    minContrast: Double = 4.5,
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
): Color {
    // Try the standard Material mapping first
    val mapped = colorScheme.contentColorFor(background)
    if (mapped.isSpecified) return mapped

    // For other colors, compute a readable themed on-color
    return bestThemedOnColor(
        background = background,
        baseSurface = baseSurface,
        minContrast = minContrast,
        lightCandidate = colorScheme.inverseOnSurface,
        darkCandidate = colorScheme.onSurface,
    )
}
