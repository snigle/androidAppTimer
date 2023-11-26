package com.github.snigle.apptimer

import android.app.Application
import androidx.preference.PreferenceManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow

class MyViewModel(preferences: Preference, application: Application) : AndroidViewModel(application) {
    private val preferences = Preference(PreferenceManager.getDefaultSharedPreferences(application))

    private val _mapApplication : MutableStateFlow<Map<String,Boolean>> = MutableStateFlow(emptyMap())

    val mapApplication: StateFlow<Map<String, Boolean>> = _mapApplication

    private fun preferenceKey(appName: String): String {
        return "app_time_application_$appName"
    }
    fun load(appList: List<String>): Unit{

        _mapApplication.value = Preference.load(appList).toMutableMap()
    }
    init {
        // Load user data from SharedPreferences when ViewModel is created
        //_mapApplication.value = preferences.getString("user_data_key", "") ?: ""
    }
    // Function to update or add a key-value pair in the map
    fun updateMap(key: String, value: Boolean) {
        val updatedMap = _mapApplication.value.toMutableMap()
        updatedMap[key] = value
        _mapApplication.value = updatedMap.toMap()
        preferences.edit().putBoolean(preferenceKey(key), value).apply()
    }

    // Function to remove a key-value pair from the map
    fun removeKey(key: String) {
        val updatedMap = _mapApplication.value.toMutableMap()
        updatedMap.remove(key)
        _mapApplication.value = updatedMap.toMap()
        preferences.edit().remove(preferenceKey(key)).apply()

    }

    // Custom getter to get a specific value from the map using StateFlow
    fun getValueByKey(key: String): Flow<Boolean?> {
        return mapApplication.map { it[key] }
    }

//    fun saveUserData(userData: String) {
//        _mapApplication.value = userData
//
//        // Save user data to SharedPreferences
//        preferences.edit().putString("user_data_key", userData).apply()
//    }
}

