package com.example.airquality.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.airquality.BuildConfig
import com.example.airquality.model.AirQualityData
import com.example.airquality.repository.retrofit.RetrofitInstance
import com.example.airquality.repository.room.AppDatabase
import com.example.airquality.repository.room.entity.RecentLocation
import retrofit2.Response

class Repository(application: Application) {

    private val recentLocationDAO = AppDatabase.getInstance(application)!!.getRecentLocationDAO()

    companion object {
        const val AIR_VISUAL_API_KEY = BuildConfig.AIRVISUAL_API_KEY
        private var instance: Repository? = null

        fun getInstance(application : Application): Repository? { // singleton pattern
            if (instance == null) instance = Repository(application)
            return instance
        }
    }

    //Use Retrofit
    suspend fun getAirQuality(latitude: Double, longitude: Double): Response<AirQualityData> {
        return RetrofitInstance.nearestCityAPI.getAirQualityData(
            latitude.toString(),
            longitude.toString(),
            AIR_VISUAL_API_KEY
        )
    }

    //Use Room

    fun getAllRecentLocations(): LiveData<List<RecentLocation>> = recentLocationDAO.findAll()

    suspend fun getRecentLocation(item: RecentLocation): RecentLocation? {
        var id: String = item.id.toString()
        return recentLocationDAO.find(id)
    }

    suspend fun createRecentLocation(item: RecentLocation) {
        recentLocationDAO.create(item)
    }

    suspend fun deleteRecentLocation(item: RecentLocation) {
        recentLocationDAO.delete(item)
    }


}