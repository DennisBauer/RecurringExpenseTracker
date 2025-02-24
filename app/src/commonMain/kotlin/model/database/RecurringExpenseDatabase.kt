package model.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL

interface IClearAllTablesFix {
    fun clearAllTables()
}

expect object RecurringExpenseDatabaseCtor : RoomDatabaseConstructor<RecurringExpenseDatabase>

@Database(entities = [RecurringExpense::class], version = 7)
@ConstructedBy(RecurringExpenseDatabaseCtor::class)
abstract class RecurringExpenseDatabase : RoomDatabase(), IClearAllTablesFix {
    abstract fun recurringExpenseDao(): RecurringExpenseDao

    override fun clearAllTables() {}

    companion object {
        fun getRecurringExpenseDatabase(builder: Builder<RecurringExpenseDatabase>): RecurringExpenseDatabase {
            return builder
                .addMigrations(migration_1_2)
                .addMigrations(migration_2_3)
                .addMigrations(migration_3_4)
                .addMigrations(migration_4_5)
                .addMigrations(migration_5_6)
                .addMigrations(migration_6_7)
                .setDriver(BundledSQLiteDriver())
                .build()
        }

        private val migration_1_2 =
            object : Migration(1, 2) {
                override fun migrate(connection: SQLiteConnection) {
                    connection.execSQL(
                        "ALTER TABLE recurring_expenses ADD COLUMN everyXRecurrence INTEGER DEFAULT 1",
                    )
                    connection.execSQL("ALTER TABLE recurring_expenses ADD COLUMN recurrence INTEGER DEFAULT 3")
                }
            }

        private val migration_2_3 =
            object : Migration(2, 3) {
                override fun migrate(connection: SQLiteConnection) {
                    connection.execSQL("ALTER TABLE recurring_expenses ADD COLUMN firstPayment INTEGER DEFAULT 0")
                }
            }

        private val migration_3_4 =
            object : Migration(3, 4) {
                override fun migrate(connection: SQLiteConnection) {
                    connection.execSQL("ALTER TABLE recurring_expenses ADD COLUMN color INTEGER DEFAULT 0")
                }
            }

        private val migration_4_5 =
            object : Migration(4, 5) {
                override fun migrate(connection: SQLiteConnection) {
                    connection.execSQL("UPDATE recurring_expenses SET firstPayment = NULL WHERE firstPayment = 0")
                }
            }

        private val migration_5_6 =
            object : Migration(5, 6) {
                override fun migrate(connection: SQLiteConnection) {
                    connection.execSQL(
                        "ALTER TABLE recurring_expenses ADD COLUMN currencyCode TEXT DEFAULT '' NOT NULL",
                    )
                }
            }

        private val migration_6_7 =
            object : Migration(6, 7) {
                override fun migrate(connection: SQLiteConnection) {
                    connection.execSQL(
                        "ALTER TABLE recurring_expenses ADD COLUMN notifyForExpense INTEGER NOT NULL DEFAULT 1",
                    )
                    connection.execSQL(
                        "ALTER TABLE recurring_expenses ADD COLUMN notifyXDaysBefore INTEGER DEFAULT null",
                    )
                    connection.execSQL(
                        "ALTER TABLE recurring_expenses ADD COLUMN lastNotificationDate INTEGER DEFAULT 0",
                    )
                }
            }
    }
}
