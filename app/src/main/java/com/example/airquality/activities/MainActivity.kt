package com.example.airquality.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.airquality.R
import com.example.airquality.databinding.ActivityMainBinding
import com.example.airquality.model.AirQualityData
import com.example.airquality.model.LatLonData
import com.example.airquality.repository.Repository
import com.example.airquality.repository.room.entity.RecentLocation
import com.example.airquality.utils.LocationProvider
import com.example.airquality.viewModels.MainViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.IllegalArgumentException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    private val PERMISSIONS_REQUEST_CODE = 100

    private var REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private lateinit var getGPSPermissionLauncher: ActivityResultLauncher<Intent>

    private lateinit var locationProvider: LocationProvider

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, MainViewModel.Factory(application))[MainViewModel::class.java]
    }

    private lateinit var startMapActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var startRecentLocationActivity: ActivityResultLauncher<Intent>

    private val repository: Repository by lazy {
        Repository.getInstance(application)!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startMapActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result?.resultCode ?: 0 == Activity.RESULT_OK) {
                    var latitude = result?.data?.getDoubleExtra("latitude", 0.0) ?: 0.0
                    var longitude = result?.data?.getDoubleExtra("longitude", 0.0) ?: 0.0
//
//                    Log.d("return to Main", longitude.toString())
                    var address = getCurrentAddress(latitude, longitude)
//
                    var title = address?.thoroughfare
                    var subTitle = address?.countryName.plus(" ").plus(address?.adminArea)
//
//
//                    var latLonData = LatLonData(latitude,longitude)
//
//                    updateLatLon(latLonData)
//                    updateUI(latLonData)

                    val recentLocation: RecentLocation =
                        RecentLocation(null, title, subTitle, latitude, longitude)
                    lifecycleScope.launch {
                        repository.createRecentLocation(recentLocation)
                    }
                    var latLonData = LatLonData(latitude, longitude)
                    updateProcess(latLonData)
                }
            }

        startRecentLocationActivity =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result?.resultCode ?: 0 == Activity.RESULT_OK) {
                    var latitude = result?.data?.getDoubleExtra("latitude", 0.0) ?: 0.0
                    var longitude = result?.data?.getDoubleExtra("longitude", 0.0) ?: 0.0
                    var latLonData = LatLonData(latitude, longitude)
                    updateProcess(latLonData)
                }

            }

        mainViewModel.latLon.observe(this) {
            Log.d("viewmodel-latlon", it.toString())
            updateUI(it)
        }
        mainViewModel.airQuality.observe(this) {
            Toast.makeText(this, "?????? ?????? ???????????? ??????!", Toast.LENGTH_LONG).show()
            Log.d("viewmodel-air", it.toString())
            updateAirUI(it)
        }

        mainViewModel.errorMessage.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }

        checkAllPermissions() //?????? ??????
        updateLatLon(null) //?????? ????????????
        setRefreshButton() //????????????
        setFab() //?????? ??? ?????? ??????
        setHistory() //?????? ?????? ?????? ?????? ??????
    }

    private fun checkAllPermissions() {
        if (!isLocationServicesAvailable()) {
            showDialogForLocationServiceSetting()
        } else {
            isRunTimePermissionsGranted()
        }
    }

    //?????? ???????????? ??????????????? ??????
    private fun isLocationServicesAvailable(): Boolean {
        val locaitonManager = getSystemService(LOCATION_SERVICE) as LocationManager

        //?????? ???????????? GPS??? ??????????????? ?????? ??????????????? ??? ??? ???????????? ?????? ?????? ?????? true
        return (locaitonManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locaitonManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        ))
    }

    //????????? ?????? ????????? ?????? ??????????????? ??????
    private fun isRunTimePermissionsGranted() {
        val hasFineLocationManager = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val hasCoarseLocationManager = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (hasFineLocationManager != PackageManager.PERMISSION_GRANTED || hasCoarseLocationManager != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                REQUIRED_PERMISSIONS,
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    //?????? ????????? ?????? ??? ??? ?????? ??? ????????????
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.size == REQUIRED_PERMISSIONS.size) {
            var checkResult = true

            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    checkResult = false
                    break
                }
            }
            if (checkResult) {
                //?????? ?????? ????????????
                updateLatLon(null)
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "????????? ?????????????????????. ?????? ?????? ???????????? ????????? ??????????????????.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    private fun showDialogForLocationServiceSetting() {
        getGPSPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            //???????????? GPS??? ????????? ???????????? ??????
            if (isLocationServicesAvailable()) {
                isRunTimePermissionsGranted()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "?????? ???????????? ????????? ??? ????????????.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }

        }
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("?????? ????????? ????????????")
        builder.setMessage("?????? ???????????? ?????? ????????????. ???????????? ?????? ????????? ??? ????????????.")

        builder.setCancelable(true)
        builder.setPositiveButton("??????", DialogInterface.OnClickListener { _, _ ->
            val callGPSSettingIntent =
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS) //???????????? ????????? ????????? ??????
            getGPSPermissionLauncher.launch(callGPSSettingIntent)
        })
        builder.setNegativeButton("??????", DialogInterface.OnClickListener { dialog, _ ->
            dialog.cancel()
            Toast.makeText(this@MainActivity, "???????????? ???????????????(GPS) ?????? ??? ??????????????????.", Toast.LENGTH_SHORT)
                .show()
            finish()
        })
        builder.create().show()
    }

    private fun updateProcess(latLonData: LatLonData) {
        var latitude = latLonData.latitude
        var longitude = latLonData.longitude

        var latLonData = LatLonData(latitude, longitude)

        updateLatLon(latLonData)
        updateUI(latLonData)
    }

    private fun updateLatLon(latLonData: LatLonData?) {
        locationProvider = LocationProvider(this@MainActivity)

        var latitude = latLonData?.latitude ?: 0.0
        var longitude = latLonData?.longitude ?: 0.0

        //????????? ?????? ????????? ????????????.
        if (latitude == 0.0 || longitude == 0.0) {
            latitude = locationProvider.getlocationLatitude()
            longitude = locationProvider.getLocationLongitude()
        }
        Log.d("MainActivity", "latitude: $latitude")
        Log.d("MainActivity", "longitude: $longitude")
        mainViewModel.updateLatLon(latitude, longitude)
    }

    fun updateUI(latLonData: LatLonData) {
        var latitude = latLonData.latitude
        var longitude = latLonData.longitude
        if (latitude != 0.0 || longitude != 0.0) {
            //1. ?????? ????????? ???????????? UI ????????????
            val address = getCurrentAddress(latitude, longitude)
            address?.let {
                binding.tvLocationTitle.text = "${it.thoroughfare}"
                binding.tvLocationSubtitle.text = "${it.countryName} ${it.adminArea}"
            }

            //2. ?????? ???????????? ?????? ????????????
            mainViewModel.updateAirQuality(latitude, longitude)

        } else {
            Toast.makeText(this@MainActivity, "??????, ?????? ????????? ????????? ??? ????????????.", Toast.LENGTH_LONG).show()
        }
    }

    //???????????? ???????????? ?????? ?????????
    private fun setRefreshButton() {
        binding.btnRefresh.setOnClickListener {
            var latitude = mainViewModel.latLon.value!!.latitude
            var longitude = mainViewModel.latLon.value!!.longitude
            mainViewModel.updateAirQuality(latitude, longitude)
        }
    }

    //??? ?????? ?????? ?????? ????????? ?????????
    private fun setFab() {
        binding.fab.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("currentLat", mainViewModel.latLon.value?.latitude)
            intent.putExtra("currentLng", mainViewModel.latLon.value?.longitude)
            startMapActivityResultLauncher.launch(intent)
        }
    }

    private fun setHistory() {
        binding.recentLocation.setOnClickListener {
            val intent = Intent(this, RecentLocationActivity::class.java)
            startRecentLocationActivity.launch(intent)
        }
    }

    private fun getCurrentAddress(latitude: Double, longitude: Double): Address? {
        val geocoder = Geocoder(this, Locale.getDefault())

        val addresses: List<Address>?

        addresses = try {
            geocoder.getFromLocation(latitude, longitude, 7)
        } catch (ioException: IOException) {
            Toast.makeText(this, "???????????? ????????? ?????????????????????.", Toast.LENGTH_LONG).show()
            return null
        } catch (illegalArgumentException: IllegalArgumentException) {
            Toast.makeText(this, "????????? ??????, ???????????????.", Toast.LENGTH_LONG).show()
            return null
        }

        //????????? ???????????? ????????? ???????????? ?????? ??????
        if (addresses == null || addresses.isEmpty()) {
            Toast.makeText(this, "????????? ???????????? ???????????????.", Toast.LENGTH_LONG).show()
            return null
        }

        val address: Address = addresses[0]
        return address
    }

    private fun updateAirUI(airQualityData: AirQualityData) {

        val pollutionData = airQualityData.data.current.pollution

        binding.tvCount.text = pollutionData.aqius.toString()

        val dateTime = ZonedDateTime.parse(pollutionData.ts).withZoneSameInstant(
            ZoneId.of("Asia/Seoul")
        ).toLocalDateTime()
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        binding.tvCheckTime.text = dateTime.format(dateFormatter).toString()

        when (pollutionData.aqius) {
            in 0..50 -> {
                binding.tvTitle.text = "??????"
                binding.imgBg.setImageResource(R.drawable.bg_good)
            }
            in 51..150 -> {
                binding.tvTitle.text = "??????"
                binding.imgBg.setImageResource(R.drawable.bg_soso)
            }
            in 151..200 -> {
                binding.tvTitle.text = "??????"
                binding.imgBg.setImageResource(R.drawable.bg_bad)
            }
            else -> {
                binding.tvTitle.text = "?????? ??????"
                binding.imgBg.setImageResource(R.drawable.bg_worst)
            }
        }
    }


}

