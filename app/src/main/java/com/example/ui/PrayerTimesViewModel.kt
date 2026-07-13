package com.example.ui

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.RetrofitInstance
import com.example.api.Timings
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PrayerTimesViewModel : ViewModel() {
    private val _timings = MutableStateFlow<Timings?>(null)
    val timings: StateFlow<Timings?> = _timings
    
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    @SuppressLint("MissingPermission")
    fun fetchTimings(context: Context) {
        _loading.value = true
        _error.value = null
        
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
            if (location != null) {
                viewModelScope.launch {
                    try {
                        val response = RetrofitInstance.api.getTimings(location.latitude, location.longitude)
                        _timings.value = response.data.timings
                    } catch (e: Exception) {
                        _error.value = "Namaz vakitleri alınamadı: ${e.message}"
                    } finally {
                        _loading.value = false
                    }
                }
            } else {
                // Default to Istanbul if no location
                fetchTimingsForCoordinates(41.0082, 28.9784)
            }
        }.addOnFailureListener {
            // Default to Istanbul if error
            fetchTimingsForCoordinates(41.0082, 28.9784)
        }
    }
    
    private fun fetchTimingsForCoordinates(lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getTimings(lat, lng)
                _timings.value = response.data.timings
            } catch (e: Exception) {
                _error.value = "Namaz vakitleri alınamadı: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}
