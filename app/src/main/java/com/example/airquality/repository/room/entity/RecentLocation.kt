package com.example.airquality.repository.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RecentLocation (
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "title") var title: String?,
    @ColumnInfo(name = "sub_title") var subTitle: String?,
    @ColumnInfo(name = "latitude") var latitude: Double?,
    @ColumnInfo(name = "longitude") var longitude: Double?
)