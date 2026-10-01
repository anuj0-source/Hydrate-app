package com.example.hydrate.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.hydrate.data.model.CustomMessage
import com.example.hydrate.data.model.WaterLog

@Database(entities = [WaterLog::class, CustomMessage::class], version = 1, exportSchema = false)
abstract class HydrateDatabase : RoomDatabase() {
    abstract fun hydrateDao(): HydrateDao

    companion object {
        @Volatile
        private var INSTANCE: HydrateDatabase? = null

        fun getDatabase(context: Context): HydrateDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HydrateDatabase::class.java,
                    "hydrate_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
