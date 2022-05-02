package com.example.airquality.repository.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.airquality.repository.room.entity.RecentLocation

@Dao
interface RecentLocationDAO {

    @Query("SELECT * FROM RecentLocation ORDER BY RecentLocation.id DESC")
    fun findAll(): LiveData<List<RecentLocation>>

    @Query("SELECT * FROM RecentLocation WHERE RecentLocation.id = :id")
    suspend fun find(id: String): RecentLocation?

    @Insert
    suspend fun create(item: RecentLocation)

    @Delete
    suspend fun delete(item: RecentLocation)
}