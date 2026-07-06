package de.dbauer.expensetracker.shared.model.datastore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import eu.anifantakis.lib.ksafe.KSafe
import eu.anifantakis.lib.ksafe.KSafeWriteMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * [IUserPreferencesRepository] backed by KSafe on all platforms.
 *
 * [migrateLegacyPreferences] performs a one-time import of preferences stored by a previous
 * app version (DataStore). All reads and writes are gated on it so the first launch after an
 * update never observes pre-migration defaults.
 */
class KSafeUserPreferencesRepository(
    private val ksafe: KSafe,
    private val migrateLegacyPreferences: (suspend (KSafe) -> Unit)? = null,
) : IUserPreferencesRepository {
    private val migrationMutex = Mutex()
    private var migrated = migrateLegacyPreferences == null

    private suspend fun ensureMigrated() {
        if (migrated) return
        migrationMutex.withLock {
            if (!migrated) {
                migrateLegacyPreferences?.invoke(ksafe)
                migrated = true
            }
        }
    }

    inner class Preference<T>(
        private val sourceFlow: Flow<T>,
        private val defaultValue: T,
        private val persist: suspend (T) -> Unit,
    ) : IUserPreferencesRepository.IPreference<T> {
        override suspend fun save(value: T) {
            ensureMigrated()
            persist(value)
        }

        override fun get(): Flow<T> =
            flow {
                ensureMigrated()
                emitAll(sourceFlow)
            }

        @Composable
        override fun collectAsState(): State<T> = get().collectAsState(defaultValue)
    }

    private inline fun <reified T> preference(
        key: String,
        defaultValue: T,
    ): Preference<T> =
        Preference(
            sourceFlow = ksafe.getFlow(key, defaultValue),
            defaultValue = defaultValue,
            // Plain mode: these are UI settings, not secrets.
            persist = { ksafe.put(key, it, KSafeWriteMode.Plain) },
        )

    override val gridMode = preference("IS_GRID_MODE", false)
    override val biometricSecurity = preference("BIOMETRIC_SECURITY", false)
    override val defaultCurrency = preference("DEFAULT_CURRENCY", "")
    override val showConvertedCurrency = preference("SHOW_CONVERTED_CURRENCY", true)
    override val upcomingPaymentNotification = preference("IS_UPCOMING_PAYMENT_NOTIFICATION", false)
    override val themeMode = preference("THEME_MODE", 0)
    override val defaultTab = preference("DEFAULT_TAB", 0)

    // default: 8:00 in the morning
    override val upcomingPaymentNotificationTime = preference("UPCOMING_PAYMENT_NOTIFICATION_TIME", 480)

    // default: 3 days in advance
    override val upcomingPaymentNotificationDaysAdvance =
        preference("UPCOMING_PAYMENT_NOTIFICATION_DAYS_ADVANCE", 3)
    override val widgetBackgroundTransparent = preference("WIDGET_BACKGROUND_TRANSPARENT", false)
    override val whatsNewVersionShown = preference("WHATS_NEW_VERSION_SHOWN", 0)
    override val upcomingPaymentHorizonMonths = preference("UPCOMING_PAYMENT_HORIZON_MONTHS", 120)
}
