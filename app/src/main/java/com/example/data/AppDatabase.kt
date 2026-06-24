package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Expense::class, DebtDue::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun debtDueDao(): DebtDueDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `debts_dues` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`personName` TEXT NOT NULL, " +
                            "`amount` REAL NOT NULL, " +
                            "`description` TEXT NOT NULL, " +
                            "`date` INTEGER NOT NULL, " +
                            "`dueDate` INTEGER, " +
                            "`type` TEXT NOT NULL, " +
                            "`isCleared` INTEGER NOT NULL DEFAULT 0, " +
                            "`isSynced` INTEGER NOT NULL DEFAULT 0, " +
                            "`sheetRow` INTEGER" +
                            ")"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
