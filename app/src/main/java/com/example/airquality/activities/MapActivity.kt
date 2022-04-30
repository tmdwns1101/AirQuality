package com.example.airquality.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.airquality.R
import com.example.airquality.databinding.ActivityMapBinding
import com.example.airquality.utils.LocationProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding


    private var mMap: GoogleMap? = null

    private var currentLat: Double = 0.0 //Main 액티비티로 부터 전달받은 위도
    private var currentLng: Double = 0.0 //Main 액티비티로 부터 전달받은 경도


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentLat = intent.getDoubleExtra("currentLat",0.0)
        currentLng = intent.getDoubleExtra("currentLng",0.0)


        Log.d("MapActivity","lat : $currentLat")
        Log.d("MapActivity","lon : $currentLng")

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)


        binding.btnCheckHere.setOnClickListener {
            mMap?.let {

                val intent = Intent()
                intent.putExtra("latitude",it.cameraPosition.target.latitude)
                intent.putExtra("longitude",it.cameraPosition.target.longitude)
                setResult(Activity.RESULT_OK,intent)
                finish()
            }
        }

    }

    override fun onMapReady(gooleMap: GoogleMap) {
        mMap = gooleMap

        mMap?.let {
            val currentLocation = LatLng(currentLat,currentLng)
            it.setMaxZoomPreference(20.0f)
            it.setMinZoomPreference(12.0f)
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,16f))
        }
        setMarker()

        binding.fabCurrentLocation.setOnClickListener {
            val locationProvider = LocationProvider(this@MapActivity)

            val latitude = locationProvider.getlocationLatitude()
            val longitude = locationProvider.getLocationLongitude()

            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude),16f))
            setMarker()
        }
    }

    private fun setMarker() {
        mMap?.let {
            it.clear()
            val markOptions = MarkerOptions()
            markOptions.position(it.cameraPosition.target)
            markOptions.title("마커 위치")
            val marker = it.addMarker(markOptions)


            it.setOnCameraMoveListener {
                marker?.let { marker ->
                    marker.position = it.cameraPosition.target
                }
            }

        }
    }
}