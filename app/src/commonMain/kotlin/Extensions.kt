import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import model.DateTimeCalculator
import model.database.UserPreferencesRepository
import model.getSystemCurrencyCode
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject
import ui.ThemeMode

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

expect fun LocalDate.toMonthYearStringUTC(): String

suspend fun StringResource.asString(): String {
    return getString(this)
}

fun LocalDate.getNextPaymentDays(): Int {
    return DateTimeCalculator.getDaysFromNowUntil(this)
}

suspend fun Flow<String>.getDefaultCurrencyCode(): String {
    return first().ifBlank { getSystemCurrencyCode() }
}

@Composable
fun useDarkTheme(): Boolean {
    val userPreferencesRepository = koinInject<UserPreferencesRepository>()
    val selectedTheme by userPreferencesRepository.themeMode.collectAsState()
    return when (selectedTheme) {
        ThemeMode.Dark.value -> true
        ThemeMode.Light.value -> false
        else -> isSystemInDarkTheme()
    }
}
