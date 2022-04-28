package com.example.airquality.retrofit

import com.example.airquality.BuildConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AirQualityProvider {

    companion object {

        fun getAirQualityData(
            latitude: Double,
            longitude: Double,
            successHandler: (response: Response<AirQualityResponse>) -> Unit,
            failureHandler: () -> Unit
        ) {
            val retrofitAPI = RetrofitConnection.getInstance().create(
                AirQualityService::class.java
            )

            retrofitAPI.getAirQualityData(
                latitude.toString(),
                longitude.toString(),
                BuildConfig.AIRVISUAL_API_KEY
            ).enqueue(object : Callback<AirQualityResponse> {
                override fun onResponse(
                    call: Call<AirQualityResponse>,
                    response: Response<AirQualityResponse>
                ) {
                    if (response.isSuccessful) {
                        successHandler(response)
                    } else {
                        failureHandler()
                    }
                }

                override fun onFailure(call: Call<AirQualityResponse>, t: Throwable) {
                    t.printStackTrace()
                    failureHandler()
                }
            })
        }
    }
}