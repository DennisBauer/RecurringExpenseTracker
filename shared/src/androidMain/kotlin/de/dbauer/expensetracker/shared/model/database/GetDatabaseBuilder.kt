package de.dbauer.expensetracker.shared.model.database

import Constants
import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<RecurringExpenseDatabase> {
    val dbFile = context.getDatabasePath(Constants.DATABASE_NAME)
    return Room
        .databaseBuilder<RecurringExpenseDatabase>(
            context = context.applicationContext,
            name = dbFile.absolutePath,
        ).setDriver(AndroidSQLiteDriver())
}
