package com.example.madassignment.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [User::class, Job::class, CommunityPost::class, UserProfile::class, Employer::class, EmployerProfile::class,EmployerJobPost::class,EmployeeJobPost::class],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE jobs ADD COLUMN isSaved INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE jobs ADD COLUMN isApplied INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE jobs ADD COLUMN userId INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE jobs ADD COLUMN subtitle TEXT DEFAULT ''")
            }
        }

        // Migration from version 2 to 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE jobs ADD COLUMN salary TEXT DEFAULT 'RM 5000 - RM 6000'")
                database.execSQL("ALTER TABLE jobs ADD COLUMN category TEXT DEFAULT 'Management (Marketing & Communications)'")
                database.execSQL("ALTER TABLE jobs ADD COLUMN originalJobId INTEGER DEFAULT 0")
            }
        }

        // Migration from version 3 to 4
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE jobs ADD COLUMN requiredSkills TEXT DEFAULT ''")
            }
        }

        // Migration from version 4 to 5 (safe incremental update)
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // This can be empty if no new changes, but helps with version increment
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "madassignment_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration() // ⬅️ This is important for development
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}