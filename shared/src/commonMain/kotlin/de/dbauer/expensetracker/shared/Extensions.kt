package de.dbauer.expensetracker.shared
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.shared.model.getSystemCurrencyCode
import de.dbauer.expensetracker.shared.ui.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject
import kotlin.time.Instant

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

suspend fun Flow<String>.getDefaultCurrencyCode(): String {
    return first().ifBlank { getSystemCurrencyCode() }
}

@Composable
fun useDarkTheme(): Boolean {
    val userPreferencesRepository = koinInject<IUserPreferencesRepository>()
    val selectedTheme by userPreferencesRepository.themeMode.collectAsState()
    return when (selectedTheme) {
        ThemeMode.Dark.value -> true
        ThemeMode.Light.value -> false
        else -> isSystemInDarkTheme()
    }
}
