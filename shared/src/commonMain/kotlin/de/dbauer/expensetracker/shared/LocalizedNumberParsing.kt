package de.dbauer.expensetracker.shared

/**
 * Parses a number typed in a locale that uses [decimalSeparator] and [groupSeparator].
 *
 * Grouping is only accepted in groups of three digits. A single group separator that is not
 * followed by exactly three digits is read as the decimal separator, so "12.99" still parses
 * in locales using ',' as decimal separator. Whitespace (incl. no-break spaces) is ignored.
 */
internal fun parseLocalizedFloat(
    input: String,
    decimalSeparator: Char,
    groupSeparator: Char?,
): Float? {
    val text = input.filterNot { it.isWhitespace() || it == ' ' || it == ' ' }
    if (text.isEmpty()) return null

    val normalized =
        when {
            decimalSeparator in text -> {
                if (groupSeparator != null && text.lastIndexOf(groupSeparator) > text.indexOf(decimalSeparator)) {
                    // Group separator after the decimal separator, e.g. "1,234.56" in a ',' locale.
                    return null
                }
                text.filterNot { it == groupSeparator }.replace(decimalSeparator, '.')
            }

            groupSeparator != null && groupSeparator in text -> {
                val parts = text.split(groupSeparator)
                val isGrouping = parts.first().isNotEmpty() && parts.drop(1).all { it.length == 3 }
                when {
                    isGrouping -> parts.joinToString("")
                    parts.size == 2 -> "${parts[0]}.${parts[1]}"
                    else -> return null
                }
            }

            else -> {
                text
            }
        }
    return normalized.toFloatOrNull()
}
