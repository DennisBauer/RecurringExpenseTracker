package de.dbauer.expensetracker.shared.model

import de.dbauer.expensetracker.shared.browserRegion

private var systemCurrencyCode = "USD"

actual fun getSystemCurrencyCode(): String = systemCurrencyCode

/**
 * Resolves the currency of the browser's region (e.g. EUR for "de-DE") from the bundled
 * currency list. Call once before the UI starts; keeps USD if the region is unknown.
 */
suspend fun initSystemCurrencyCode(currencyProvider: CurrencyProvider = CurrencyProvider()) {
    val region = browserRegion() ?: return
    val currencies = runCatching { currencyProvider.retrieveCurrencies() }.getOrNull() ?: return
    currencies.currencyCodeForRegion(region)?.let { systemCurrencyCode = it }
}
