package viewmodel.database

import Constants
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import model.database.RecurringExpenseDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<RecurringExpenseDatabase> {
    val dbFile = context.getDatabasePath(Constants.DATABASE_NAME)
    return Room.databaseBuilder<RecurringExpenseDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath,
    )
}
