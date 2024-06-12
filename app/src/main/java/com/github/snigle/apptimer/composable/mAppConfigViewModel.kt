package com.github.snigle.apptimer.composable

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.snigle.apptimer.domain.AppConfig
import com.github.snigle.apptimer.domain.IAppConfig
import kotlinx.coroutines.launch

class AppConfigViewModelFactory(private val appConfigRepo: IAppConfig) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppConfigViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppConfigViewModel(appConfigRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
class AppConfigViewModel(private val appConfigRepo: IAppConfig) : ViewModel() {
    private val _apps = MutableLiveData<List<AppConfig>>(listOf(
    ))

    val apps: LiveData<List<AppConfig>> = _apps

    init {
        // Initialize ViewModel with preferences
        viewModelScope.launch {
            _apps.value = appConfigRepo.List()
        }
    }
    fun updateAppStatus(index: Int, isMonitored: Boolean) {
        val updatedApps = _apps.value?.mapIndexed { i, app ->
            if (i == index) {
                // Create copy to refresh view
                val copy = app.copy(monitor = isMonitored)
                // Save update in repository
                appConfigRepo.Save(copy)
                // Return the copy in the view
                copy
            } else app
        }
        updatedApps.let {
            _apps.value = it
        }
    }
}