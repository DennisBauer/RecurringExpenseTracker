package di

import androidx.room.RoomDatabase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import viewmodel.database.RecurringExpenseDatabase
import viewmodel.database.getDatabaseBuilder

actual val platformModule =
    module {
        singleOf(::getDatabaseBuilder).bind<RoomDatabase.Builder<RecurringExpenseDatabase>>()
    }
