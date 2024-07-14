import androidx.compose.ui.Modifier
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

expect fun Float.toCurrencyString(currencyCode: String): String

expect fun Float.toLocalString(): String

expect fun String.toFloatLocaleAware(): Float?

fun LocalDate.isSameDay(other: LocalDate): Boolean {
    return this.year == other.year &&
        this.month == other.month &&
        this.dayOfMonth == other.dayOfMonth
}

fun LocalDate.isInDaysAfter(other: LocalDate): Boolean {
    if (this.year > other.year) {
        return true
    } else if (this.year == other.year &&
        this.month > other.month
    ) {
        return true
    } else if (this.year == other.year &&
        this.month == other.month &&
        this.dayOfMonth > other.dayOfMonth
    ) {
        return true
    }
    return false
}

fun Modifier.conditional(
    condition: Boolean,
    modifier: Modifier.() -> Modifier,
): Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}

expect fun Instant.toLocaleString(): String

suspend fun StringResource.asString(): String {
    return getString(this)
}
