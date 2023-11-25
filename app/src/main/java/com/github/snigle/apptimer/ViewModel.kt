package com.github.snigle.apptimer

import android.app.Application
import androidx.preference.PreferenceManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MyViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = PreferenceManager.getDefaultSharedPreferences(application)

    private val _userData = MutableLiveData<String>()
    val userData: LiveData<String> = _userData

    init {
        // Load user data from SharedPreferences when ViewModel is created
        _userData.value = preferences.getString("user_data_key", "") ?: ""
    }

    fun saveUserData(userData: String) {
        _userData.value = userData

        // Save user data to SharedPreferences
        preferences.edit().putString("user_data_key", userData).apply()
    }
}