package de.dbauer.expensetracker.shared.model.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import eu.anifantakis.lib.ksafe.KSafe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toOkioPath
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class LegacyPreferencesMigrationTest {
    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun importsLegacyDataStoreValuesIntoKSafe() =
        runTest {
            val tempDir = File(System.getProperty("java.io.tmpdir"), "ksafe-migration-test-${Uuid.random()}")
            tempDir.mkdirs()
            val dataStoreFile = File(tempDir, "legacy.preferences_pb")

            // Write legacy preferences the way the old DataStore repository did.
            val writeScope = CoroutineScope(SupervisorJob())
            val legacyStore =
                PreferenceDataStoreFactory.createWithPath(
                    scope = writeScope,
                ) { dataStoreFile.toOkioPath() }
            legacyStore.edit {
                it[booleanPreferencesKey("IS_GRID_MODE")] = true
                it[stringPreferencesKey("DEFAULT_CURRENCY")] = "EUR"
                it[intPreferencesKey("THEME_MODE")] = 2
            }
            writeScope.cancel()

            val ksafe = KSafe(fileName = "migration_test_${Uuid.random().toHexString()}", baseDir = tempDir)
            try {
                ksafe.importLegacyPreferences(dataStoreFile.toOkioPath())

                assertEquals(true, ksafe.get("IS_GRID_MODE", false))
                assertEquals("EUR", ksafe.get("DEFAULT_CURRENCY", ""))
                assertEquals(2, ksafe.get("THEME_MODE", 0))
                // Key absent in the legacy store keeps its default.
                assertEquals(480, ksafe.get("UPCOMING_PAYMENT_NOTIFICATION_TIME", 480))
            } finally {
                tempDir.deleteRecursively()
            }
        }
}
