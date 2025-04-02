package data

data class CurrencyOption(val currencyCode: String, val currencyName: String) {
    companion object {
        val INVALID = CurrencyOption("", "")
    }
}
