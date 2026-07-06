package de.dbauer.expensetracker.shared.model.database

import Constants
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<RecurringExpenseDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), Constants.DATABASE_NAME)
    return Room
        .databaseBuilder<RecurringExpenseDatabase>(
            name = dbFile.absolutePath,
        ).setDriver(BundledSQLiteDriver())
}
