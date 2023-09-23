package de.dbauer.expensetracker.viewmodel.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RecurringExpense::class], version = 1)
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
                    ).build()
                instance = tmpInstance
                tmpInstance
            }
        }
    }
}
