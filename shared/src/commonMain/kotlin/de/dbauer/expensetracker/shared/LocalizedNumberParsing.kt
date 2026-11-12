package de.dbauer.expensetracker.shared

/**
 * Parses a number typed in a locale that uses [decimalSeparator] and [groupSeparator].
 *
 * Grouping is only accepted in valid groups: the first group has 1 to 3 digits, the last group
 * has 3 digits and the groups in between have either all 3 digits or, like in the Indian
 * numbering system ("1,23,456"), all 2 digits. A single group separator that does not form a
 * valid group is read as the decimal separator, so "12.99" still parses in locales using ',' as
 * decimal separator. Whitespace (incl. no-break spaces) and bidi marks are ignored, and
 * Arabic-Indic digits are read like ASCII digits.
 */
internal fun parseLocalizedFloat(
    input: String,
    decimalSeparator: Char,
    groupSeparator: Char?,
): Float? {
    val signedText =
        input
            .filterNot { it.isWhitespace() || it == ' ' || it == ' ' || it in BIDI_MARKS }
            .map(::toAsciiDigit)
            .joinToString("")
    // Keep a leading sign out of the digit groups, e.g. "-123,456"
    val sign = signedText.takeWhile { it == '-' || it == '+' }.take(1)
    val text = signedText.substring(sign.length)
    if (text.isEmpty()) return null

    val normalized =
        when {
            decimalSeparator in text -> {
                val decimalIndex = text.indexOf(decimalSeparator)
                if (groupSeparator != null && text.lastIndexOf(groupSeparator) > decimalIndex) {
                    // Group separator after the decimal separator, e.g. "1,234.56" in a ',' locale.
                    return null
                }
                val integerPart = text.substring(0, decimalIndex)
                if (groupSeparator != null && groupSeparator in integerPart) {
                    if (!isValidGrouping(integerPart.split(groupSeparator))) return null
                }
                text.filterNot { it == groupSeparator }.replace(decimalSeparator, '.')
            }

            groupSeparator != null && groupSeparator in text -> {
                val parts = text.split(groupSeparator)
                when {
                    isValidGrouping(parts) -> parts.joinToString("")
                    parts.size == 2 -> "${parts[0]}.${parts[1]}"
                    else -> return null
                }
            }

            else -> {
                text
            }
        }
    return (sign + normalized).toFloatOrNull()
}

/** Arabic letter mark, left-to-right mark and right-to-left mark, as emitted by Intl for RTL locales. */
private val BIDI_MARKS = setOf('؜', '‎', '‏')

/** Maps Arabic-Indic and extended Arabic-Indic (Persian) digits to ASCII digits. */
private fun toAsciiDigit(char: Char): Char =
    when (char) {
        in '٠'..'٩' -> '0' + (char - '٠')
        in '۰'..'۹' -> '0' + (char - '۰')
        else -> char
    }

private fun isValidGrouping(groups: List<String>): Boolean {
    if (groups.size < 2) return false
    if (groups.first().length !in 1..3 || groups.last().length != 3) return false
    val middle = groups.subList(1, groups.size - 1)
    return middle.all { it.length == 3 } || middle.all { it.length == 2 }
}
