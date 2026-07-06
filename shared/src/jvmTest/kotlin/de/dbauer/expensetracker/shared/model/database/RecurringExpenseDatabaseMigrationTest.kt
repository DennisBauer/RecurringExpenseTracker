package de.dbauer.expensetracker.shared.model.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class RecurringExpenseDatabaseMigrationTest {
    @Test
    fun openingVersion2DatabaseRunsAllMigrations() =
        runTest {
            val dbFile = File.createTempFile("migration-test", ".db").apply { delete() }

            // Create a database as Room 2 would have left it at schema version 2.
            val connection = BundledSQLiteDriver().open(dbFile.absolutePath)
            try {
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recurring_expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT " +
                        "NOT NULL, `name` TEXT, `description` TEXT, `price` REAL, `everyXRecurrence` INTEGER, " +
                        "`recurrence` INTEGER)",
                )
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)",
                )
                connection.execSQL(
                    "INSERT OR REPLACE INTO room_master_table (id,identity_hash) " +
                        "VALUES(42, '4f934843c2afcb230319f21e407a1b5f')",
                )
                connection.execSQL(
                    "INSERT INTO `recurring_expenses` (`name`, `description`, `price`, `everyXRecurrence`, " +
                        "`recurrence`) VALUES ('Netflix', 'Family plan', 9.99, 1, 3)",
                )
                connection.execSQL("PRAGMA user_version = 2")
            } finally {
                connection.close()
            }

            val database =
                RecurringExpenseDatabase.getRecurringExpenseDatabase(
                    Room
                        .databaseBuilder<RecurringExpenseDatabase>(name = dbFile.absolutePath)
                        .setDriver(BundledSQLiteDriver()),
                )
            try {
                val expenses = database.recurringExpenseDao().getAllExpenses().first()
                assertEquals(1, expenses.size)
                assertEquals("Netflix", expenses.single().expense.name)
                // Migration 3->4 backfills color = 0, which migration 5->6 converts into a tag.
                assertEquals(
                    "Untitled",
                    expenses
                        .single()
                        .tags
                        .single()
                        .title,
                )
            } finally {
                database.close()
                dbFile.delete()
            }
        }
}
