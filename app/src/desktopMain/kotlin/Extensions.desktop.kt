
import kotlinx.datetime.Instant
import java.text.DateFormat
import java.text.NumberFormat
import java.text.ParseException
import java.util.Date
import java.util.TimeZone

actual fun Float.toCurrencyString(): String {
    return NumberFormat.getCurrencyInstance().format(this)
}

actual fun Float.toLocalString(): String {
    return NumberFormat.getInstance().let {
        it.minimumFractionDigits = 2
        it.format(this)
    }
}

actual fun String.toFloatLocaleAware(): Float? {
    return try {
        NumberFormat.getInstance().parse(this)?.toFloat()
    } catch (e: ParseException) {
        null
    }
}

actual fun Instant.toLocaleString(): String {
    return DateFormat.getDateInstance().apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(this.toEpochMilliseconds()))
}
