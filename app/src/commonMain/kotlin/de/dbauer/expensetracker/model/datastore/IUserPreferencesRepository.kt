package de.dbauer.expensetracker.model.datastore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.Flow

interface IUserPreferencesRepository {
    interface IPreference<T> {
        suspend fun save(value: T)

        fun get(): Flow<T>

        @Composable
        fun collectAsState(): State<T>
    }

    val gridMode: IPreference<Boolean>
    val biometricSecurity: IPreference<Boolean>
    val defaultCurrency: IPreference<String>
    val showConvertedCurrency: IPreference<Boolean>
    val upcomingPaymentNotification: IPreference<Boolean>
    val themeMode: IPreference<Int>
    val defaultTab: IPreference<Int>
    val upcomingPaymentNotificationTime: IPreference<Int>
    val upcomingPaymentNotificationDaysAdvance: IPreference<Int>
    val widgetBackgroundTransparent: IPreference<Boolean>
}
