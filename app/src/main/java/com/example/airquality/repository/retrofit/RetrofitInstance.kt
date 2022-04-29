package com.example.airquality.repository.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "https://api.airvisual.com/v2/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val nearestCityAPI: NearestCityAPI by lazy {
        retrofit.create(NearestCityAPI::class.java)
    }
}