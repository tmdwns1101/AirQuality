package com.example.airquality.viewModels

import android.app.Application
import androidx.lifecycle.*
import com.example.airquality.repository.Repository
import com.example.airquality.repository.room.entity.RecentLocation
import kotlinx.coroutines.launch

class RecentLocationViewModel(private val repository: Repository): ViewModel() {

    private val _recentLocationList: LiveData<List<RecentLocation>> = repository.getAllRecentLocations()

    val recentLocationList: LiveData<List<RecentLocation>>
        get() = _recentLocationList


    fun deleteRecentLocation(item: RecentLocation) = viewModelScope.launch {
        repository.deleteRecentLocation(item)
    }


    class Factory(private val application : Application) : ViewModelProvider.Factory { // factory pattern
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RecentLocationViewModel(Repository.getInstance(application)!!) as T
        }
    }
}