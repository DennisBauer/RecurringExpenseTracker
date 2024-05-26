package viewmodel.database

import Constants
import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

fun getDatabaseBuilder(): RoomDatabase.Builder<RecurringExpenseDatabase> {
    val dbFile = "${NSHomeDirectory()}/${Constants.DATABASE_NAME}"
    return Room.databaseBuilder<RecurringExpenseDatabase>(
        name = dbFile,
        factory = { RecurringExpenseDatabase::class.instantiateImpl() },
    )
}
