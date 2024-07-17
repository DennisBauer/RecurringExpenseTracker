package model.database

import Constants
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<RecurringExpenseDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), Constants.DATABASE_NAME)
    return Room.databaseBuilder<RecurringExpenseDatabase>(
        name = dbFile.absolutePath,
    )
}
