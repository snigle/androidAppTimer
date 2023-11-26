package com.github.snigle.apptimer

import android.app.Application
import androidx.preference.PreferenceManager
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class MyViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = Preference(PreferenceManager.getDefaultSharedPreferences(application))

    private val _mapApplication : MutableStateFlow<Map<String,Boolean>> = MutableStateFlow(emptyMap())

    private val mapApplication: StateFlow<Map<String, Boolean>> = _mapApplication

    fun init(appList: List<String>): Unit{
        _mapApplication.value = preferences.load(appList).toMutableMap()
    }

    // Function to update or add a key-value pair in the map
    fun updateMap(packageName: String, value: Boolean) {
        val updatedMap = _mapApplication.value.toMutableMap()
        updatedMap[packageName] = value
        _mapApplication.value = updatedMap.toMap()
        preferences.save(packageName, value)
    }

    // Custom getter to get a specific value from the map using StateFlow
    fun getValueByKey(key: String): Flow<Boolean?> {
        return mapApplication.map { it[key] }
    }

}

