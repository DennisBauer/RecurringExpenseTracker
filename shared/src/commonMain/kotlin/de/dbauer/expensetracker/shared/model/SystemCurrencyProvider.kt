package de.dbauer.expensetracker.shared.model

expect fun getSystemCurrencyCode(): String

/**
 * Returns the code of the fiat currency used in [region] (ISO 3166-1 alpha-2), or `null` if
 * no currency lists that region.
 */
internal fun List<Currency>.currencyCodeForRegion(region: String): String? {
    val normalizedRegion = region.uppercase()
    return firstOrNull { it.type == "fiat" && normalizedRegion in it.countries }?.code
}
