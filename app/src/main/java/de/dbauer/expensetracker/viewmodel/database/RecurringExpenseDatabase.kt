package de.dbauer.expensetracker.viewmodel.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [RecurringExpense::class], version = 2)
abstract class RecurringExpenseDatabase : RoomDatabase() {
    abstract fun recurringExpenseDao(): RecurringExpenseDao

    companion object {
        @Volatile
        private var instance: RecurringExpenseDatabase? = null

        fun getDatabase(context: Context): RecurringExpenseDatabase {
            return instance ?: synchronized(this) {
                val tmpInstance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        RecurringExpenseDatabase::class.java,
                        "recurring-expenses",
                    )
                        .addMigrations(migration_1_2)
                        .build()
                instance = tmpInstance
                tmpInstance
            }
        }

        private val migration_1_2 =
            object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        "ALTER TABLE recurring_expenses ADD COLUMN everyXRecurrence INTEGER DEFAULT 1",
                    )
                    database.execSQL("ALTER TABLE recurring_expenses ADD COLUMN recurrence INTEGER DEFAULT 3")
                }
            }
    }
}
