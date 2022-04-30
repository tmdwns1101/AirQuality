package com.example.airquality.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import java.lang.Exception

class LocationProvider(val context: Context) {

    private var location: Location? = null

    private var locationManager: LocationManager? = null

    init {
        getLocation()
    }


    private fun getLocation(): Location?  {
        try{
            //위치 시스템 서비스 가져오기
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            var gpsLocation: Location? = null
            var networkLocation: Location? = null

            val isGPSEnabled: Boolean = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled: Boolean = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            Log.i("LocaitonProvider","isGPSEnabled : $isGPSEnabled")
            Log.i("LocaitonProvider","isNetworkEnabled : $isNetworkEnabled")
            if(!isGPSEnabled && !isNetworkEnabled) {
                //GPS, Network provider 둘 다 못 사용하는 상황이므로 null 반환
                return null
            } else {
                val hasFineLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)

                val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

                Log.i("LocaitonProvider","hasFineLoc : $hasFineLocationPermission")
                Log.i("LocaitonProvider","hasCoarseLoc : $hasCoarseLocationPermission")
                //둘 중 한 개도 권한이 없으면 null 반환
                if(hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED || hasFineLocationPermission != PackageManager.PERMISSION_GRANTED) {
                    return null
                }
                Log.i("LocaitonProvider","locationManager : ${locationManager.toString()}")

                if(isNetworkEnabled) {
                    networkLocation = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }

                if(isGPSEnabled) {
                    gpsLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }
                Log.i("LocaitonProvider","networkLocation : $networkLocation")
                Log.i("LocaitonProvider","gpsLocation : $gpsLocation")
                if(gpsLocation != null && networkLocation != null) {
                    location = if(gpsLocation.accuracy > networkLocation.accuracy) {
                        gpsLocation
                    } else {
                        networkLocation
                    }
                } else {
                    if(gpsLocation != null) {
                        location = gpsLocation
                    }
                    if(networkLocation != null) {
                        location = networkLocation
                    }
                }
            }

        }catch (e: Exception) {
            e.printStackTrace()
        }
        Log.i("LocationProvider", "${location.toString()}")
        return location
    }

    //위치 정보를 가져오는 메서드
    fun getlocationLatitude(): Double {
        return location?.latitude ?: 0.0
    }

    //경도 정보를 가져오는 메서드
    fun getLocationLongitude(): Double {
        return location?.longitude ?: 0.0
    }


}