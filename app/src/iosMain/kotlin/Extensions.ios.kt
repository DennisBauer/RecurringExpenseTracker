import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.NSTimeZone
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeZoneWithAbbreviation

actual fun Float.toCurrencyString(currencyCode: String): String {
    val numberFormatter = NSNumberFormatter()
    // TODO: Use currencyCode to adjust formatting according to selected currency
    numberFormatter.numberStyle = NSNumberFormatterCurrencyStyle
    return numberFormatter.stringFromNumber(NSNumber(this)) ?: ""
}

actual fun Float.toLocalString(): String {
    val numberFormatter = NSNumberFormatter()
    numberFormatter.numberStyle = NSNumberFormatterDecimalStyle
    return numberFormatter.stringFromNumber(NSNumber(this)) ?: ""
}

actual fun String.toFloatLocaleAware(): Float? {
    val numberFormatter = NSNumberFormatter()
    numberFormatter.numberStyle = NSNumberFormatterDecimalStyle
    val number = numberFormatter.numberFromString(this)
    return number?.floatValue
}

actual fun Instant.toLocaleString(): String {
    val dateFormatter = NSDateFormatter()
    dateFormatter.dateStyle = NSDateFormatterMediumStyle
    val date = NSDate.dateWithTimeIntervalSince1970(this.epochSeconds.toDouble())
    return dateFormatter.stringFromDate(date)
}

actual fun LocalDate.toMonthYearStringUTC(): String {
    val comps =
        NSDateComponents().apply {
            setYear(year)
            setMonth(monthNumber.toLong())
            setDay(1)
            timeZone = NSTimeZone.timeZoneWithAbbreviation("UTC")
        }
    val calendar = NSCalendar.currentCalendar
    val date = calendar.dateFromComponents(comps)!!

    val dateFormatter = NSDateFormatter()
    dateFormatter.dateFormat = "MMMM"
    return "${dateFormatter.stringFromDate(date)} $year"
}
