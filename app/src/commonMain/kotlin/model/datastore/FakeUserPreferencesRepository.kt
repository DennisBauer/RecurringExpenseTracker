package model.datastore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

private class FakePreference<T>(private val default: T) : IUserPreferencesRepository.IPreference<T> {
    var value = MutableStateFlow(default)

    override suspend fun save(value: T) {
        this.value.value = value
    }

    override fun get(): Flow<T> {
        return value
    }

    @Composable
    override fun collectAsState(): State<T> {
        return value.collectAsState(default)
    }
}

class FakeUserPreferencesRepository() : IUserPreferencesRepository {
    override val gridMode: IUserPreferencesRepository.IPreference<Boolean> = FakePreference(false)
    override val biometricSecurity: IUserPreferencesRepository.IPreference<Boolean> = FakePreference(true)
    override val defaultCurrency: IUserPreferencesRepository.IPreference<String> = FakePreference("EUR")
    override val showConvertedCurrency: IUserPreferencesRepository.IPreference<Boolean> = FakePreference(true)
    override val upcomingPaymentNotification: IUserPreferencesRepository.IPreference<Boolean> =
        FakePreference(true)
    override val themeMode: IUserPreferencesRepository.IPreference<Int> = FakePreference(0)
    override val defaultTab: IUserPreferencesRepository.IPreference<Int> = FakePreference(0)
    override val upcomingPaymentNotificationTime: IUserPreferencesRepository.IPreference<Int> = FakePreference(900)
    override val upcomingPaymentNotificationDaysAdvance: IUserPreferencesRepository.IPreference<Int> =
        FakePreference(3)
    override val widgetBackgroundTransparent: IUserPreferencesRepository.IPreference<Boolean> =
        FakePreference(false)
}
