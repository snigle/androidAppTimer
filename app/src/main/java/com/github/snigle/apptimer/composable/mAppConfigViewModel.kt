package com.github.snigle.apptimer.composable

import android.content.pm.PackageManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.snigle.apptimer.domain.AppConfig
import com.github.snigle.apptimer.domain.IAppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppConfigViewModelFactory(private val appConfigRepo: IAppConfig) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppConfigViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppConfigViewModel(appConfigRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class AppConfigWithIcon(
    val appConfig: AppConfig,
    val icon: ImageBitmap?
)

class AppConfigViewModel(private val appConfigRepo: IAppConfig) : ViewModel() {
    private val _apps = MutableLiveData<List<AppConfigWithIcon>>(emptyList())
    val apps: LiveData<List<AppConfigWithIcon>> = _apps

    fun loadApps(packageManager: PackageManager) {
        viewModelScope.launch {
            val appConfigs = appConfigRepo.List()
            // Post value with no icons first to show the list quickly
            _apps.value = appConfigs.map { AppConfigWithIcon(it, null) }

            // Then load icons in background
            viewModelScope.launch(Dispatchers.IO) {
                val appsWithIcons = appConfigs.map { config ->
                    val icon = try {
                        packageManager.getApplicationIcon(config.packageName).toBitmapOrNull()?.asImageBitmap()
                    } catch (e: PackageManager.NameNotFoundException) {
                        null
                    }
                    AppConfigWithIcon(config, icon)
                }
                withContext(Dispatchers.Main) {
                    _apps.value = appsWithIcons
                }
            }
        }
    }

    fun updateAppStatus(index: Int, isMonitored: Boolean) {
        val currentApps = _apps.value
        if (currentApps != null && index >= 0 && index < currentApps.size) {
            val appToUpdate = currentApps[index]
            val updatedConfig = appToUpdate.appConfig.copy(monitor = isMonitored)

            // Save update in repository
            appConfigRepo.Save(updatedConfig)

            // Update the list in LiveData
            val updatedList = currentApps.toMutableList()
            updatedList[index] = appToUpdate.copy(appConfig = updatedConfig)
            _apps.value = updatedList
        }
    }
}