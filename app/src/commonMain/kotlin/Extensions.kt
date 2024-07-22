import androidx.compose.ui.Modifier
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

expect fun Float.toCurrencyString(currencyCode: String): String

expect fun Float.toLocalString(): String

expect fun String.toFloatLocaleAware(): Float?

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
