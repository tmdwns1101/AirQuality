package com.example.airquality.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.airquality.model.AirQualityData
import com.example.airquality.model.LatLonData
import com.example.airquality.repository.Repository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: Repository) : ViewModel() {

    private val _latLon: MutableLiveData<LatLonData> = MutableLiveData()
    private val _airQuality: MutableLiveData<AirQualityData> = MutableLiveData()
    private val _errorMessage: MutableLiveData<String> = MutableLiveData()



    val latLon: LiveData<LatLonData>
        get() = _latLon
    val airQuality: LiveData<AirQualityData>
        get() = _airQuality

    val errorMessage: LiveData<String>
        get() = _errorMessage
    init {
        _latLon.value = LatLonData(0.0, 0.0)

    }

    fun updateLatLon(lat: Double, lon: Double) {
        var latLon = _latLon.value
        latLon?.latitude = lat
        latLon?.longitude = lon
    }

    fun updateAirQuality(latitude: Double, longitude: Double) = viewModelScope.launch {
        val response = repository.getAirQuality(latitude, longitude)
        if(response.isSuccessful) _airQuality.value = response.body()
        else {
            Log.d("MainViewModel", response.errorBody()?.string()!!)

            _errorMessage.value = "데이터를 불러올 수 없습니다."

        }
    }

    class Factory(private val application : Application) : ViewModelProvider.Factory { // factory pattern
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(Repository.getInstance(application)!!) as T
        }
    }

}