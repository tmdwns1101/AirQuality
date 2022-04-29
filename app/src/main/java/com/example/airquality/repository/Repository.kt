package com.example.airquality.repository

import android.app.Application
import com.example.airquality.BuildConfig
import com.example.airquality.model.AirQualityData
import com.example.airquality.repository.retrofit.RetrofitInstance
import retrofit2.Response

class Repository(application: Application) {

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

}