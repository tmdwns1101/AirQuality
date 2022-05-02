package com.example.airquality.repository.retrofit

import com.example.airquality.model.AirQualityData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NearestCityAPI {
    @GET("nearest_city")
    suspend fun getAirQualityData(@Query("lat") lat: String, @Query("lon") lon: String, @Query("key") key: String): Response<AirQualityData>

}