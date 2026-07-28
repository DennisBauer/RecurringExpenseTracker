package de.dbauer.expensetracker.shared.model.database

import Constants
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteDriver

fun getDatabaseBuilder(driver: SQLiteDriver): RoomDatabase.Builder<RecurringExpenseDatabase> {
    return Room
        .databaseBuilder<RecurringExpenseDatabase>(name = Constants.DATABASE_NAME)
        .setDriver(driver)
}
