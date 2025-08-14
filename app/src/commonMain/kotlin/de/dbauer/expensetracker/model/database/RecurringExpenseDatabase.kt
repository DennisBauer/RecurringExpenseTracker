package de.dbauer.expensetracker.model.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object RecurringExpenseDatabaseConstructor : RoomDatabaseConstructor<RecurringExpenseDatabase> {
    override fun initialize(): RecurringExpenseDatabase
}

@Database(
    entities = [
        RecurringExpenseEntry::class,
        TagEntry::class,
        ExpenseTagCrossRefEntry::class,
    ],
    version = 8,
)
@ConstructedBy(RecurringExpenseDatabaseConstructor::class)
abstract class RecurringExpenseDatabase : RoomDatabase() {
    abstract fun recurringExpenseDao(): RecurringExpenseDao

    companion object {
        fun getRecurringExpenseDatabase(builder: Builder<RecurringExpenseDatabase>): RecurringExpenseDatabase {
            return builder
                .addMigrations(migration_1_2)
                .addMigrations(migration_2_3)
                .addMigrations(migration_3_4)
                .addMigrations(migration_4_5)
                .addMigrations(migration_5_6)
                .addMigrations(migration_6_7)
                .addMigrations(migration_7_8)
                .fallbackToDestructiveMigrationOnDowngrade(true)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
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
        val migration_7_8 =
            object : Migration(7, 8) {
                override fun migrate(connection: SQLiteConnection) {
                    // 1) Create new tables required by v8
                    connection.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `tags` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `title` TEXT NOT NULL,
                            `color` INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )

                    connection.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `ExpenseTagCrossRef` (
                            `expenseId` INTEGER NOT NULL,
                            `tagId` INTEGER NOT NULL,
                            PRIMARY KEY(`expenseId`, `tagId`)
                        )
                        """.trimIndent(),
                    )

                    connection.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_ExpenseTagCrossRef_tagId` ON `ExpenseTagCrossRef` (`tagId`)",
                    )
                    connection.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_ExpenseTagCrossRef_expenseId` ON `ExpenseTagCrossRef` (`expenseId`)",
                    )

                    // ----------------------------------------------------------
                    // 2) Map enum IDs (recurring_expenses.color) -> ARGB integer
                    //    and create one tag per distinct enum id present in DB.
                    // ----------------------------------------------------------
                    //
                    // Mapping used (light-theme hex -> decimal):
                    //   1 (Dynamic)  -> 0
                    //   2 (Red)      -> 0x80990000  = 2157510656
                    //   3 (Orange)   -> 0x80994d00  = 2157530368
                    //   4 (Yellow)   -> 0x80999900  = 2157549824
                    //   5 (Green)    -> 0x80009900  = 2147522816
                    //   6 (Mint)     -> 0x8000994d  = 2147522893
                    //   7 (Turquoise)-> 0x80009999  = 2147522969
                    //   8 (Cyan)     -> 0x80004c99  = 2147503257
                    //   9 (Blue)     -> 0x80000099  = 2147483801
                    //  10 (Purple)   -> 0x804c0099  = 2152464537
                    //  11 (Pink)     -> 0x80990099  = 2157510809
                    //  12 (Maroon)   -> 0x8099004d  = 2157510733

                    connection.execSQL(
                        """
                        INSERT INTO tags (title, color)
                        SELECT 'Untitled',
                          CASE color
                            WHEN 1 THEN 0
                            WHEN 2 THEN 2157510656
                            WHEN 3 THEN 2157530368
                            WHEN 4 THEN 2157549824
                            WHEN 5 THEN 2147522816
                            WHEN 6 THEN 2147522893
                            WHEN 7 THEN 2147522969
                            WHEN 8 THEN 2147503257
                            WHEN 9 THEN 2147483801
                            WHEN 10 THEN 2152464537
                            WHEN 11 THEN 2157510809
                            WHEN 12 THEN 2157510733
                            ELSE 0
                          END
                        FROM (
                          SELECT DISTINCT color
                          FROM recurring_expenses
                          WHERE color IS NOT NULL
                        )
                        """.trimIndent(),
                    )

                    // ----------------------------------------------------------
                    // 3) Create crossrefs: for each expense link to the tag that
                    //    contains the mapped color value for that expense's enum id.
                    // ----------------------------------------------------------
                    connection.execSQL(
                        """
                        INSERT OR IGNORE INTO ExpenseTagCrossRef (expenseId, tagId)
                        SELECT r.id AS expenseId, t.id AS tagId
                        FROM recurring_expenses r
                        JOIN tags t ON t.color =
                          (CASE r.color
                            WHEN 1 THEN 0
                            WHEN 2 THEN 2157510656
                            WHEN 3 THEN 2157530368
                            WHEN 4 THEN 2157549824
                            WHEN 5 THEN 2147522816
                            WHEN 6 THEN 2147522893
                            WHEN 7 THEN 2147522969
                            WHEN 8 THEN 2147503257
                            WHEN 9 THEN 2147483801
                            WHEN 10 THEN 2152464537
                            WHEN 11 THEN 2157510809
                            WHEN 12 THEN 2157510733
                            ELSE 0
                          END)
                        WHERE r.color IS NOT NULL
                        """.trimIndent(),
                    )

                    // ----------------------------------------------------------
                    // 4) Remove old 'color' column from recurring_expenses
                    //    (recreate table without the column and copy data).
                    // ----------------------------------------------------------
                    connection.execSQL("PRAGMA foreign_keys=OFF")

                    connection.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `recurring_expenses_new` (
                          `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                          `name` TEXT,
                          `description` TEXT,
                          `price` REAL,
                          `everyXRecurrence` INTEGER,
                          `recurrence` INTEGER,
                          `firstPayment` INTEGER,
                          `currencyCode` TEXT NOT NULL,
                          `notifyForExpense` INTEGER NOT NULL,
                          `notifyXDaysBefore` INTEGER,
                          `lastNotificationDate` INTEGER
                        )
                        """.trimIndent(),
                    )

                    connection.execSQL(
                        """
                        INSERT INTO recurring_expenses_new
                        (id, name, description, price, everyXRecurrence, recurrence, firstPayment, currencyCode, notifyForExpense, notifyXDaysBefore, lastNotificationDate)
                        SELECT id, name, description, price, everyXRecurrence, recurrence, firstPayment, currencyCode, notifyForExpense, notifyXDaysBefore, lastNotificationDate
                        FROM recurring_expenses
                        """.trimIndent(),
                    )

                    connection.execSQL("DROP TABLE IF EXISTS recurring_expenses")
                    connection.execSQL("ALTER TABLE recurring_expenses_new RENAME TO recurring_expenses")

                    connection.execSQL("PRAGMA foreign_keys=ON")
                }
            }
    }
}
