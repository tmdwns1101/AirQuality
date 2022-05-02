package com.example.airquality.repository.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.airquality.repository.room.dao.RecentLocationDAO
import com.example.airquality.repository.room.entity.RecentLocation

@Database(entities = [RecentLocation::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getRecentLocationDAO(): RecentLocationDAO

    companion object {
        val databaseName = "db_todo"
        var appDatabase: AppDatabase? = null

        fun getInstance(ctx: Context): AppDatabase? {
            if (appDatabase == null) {
                appDatabase =
                    Room.databaseBuilder(ctx, AppDatabase::class.java, databaseName).build()
            }
            return appDatabase
        }
    }
}