package com.github.snigle.apptimer.domain

import android.util.Log
import com.github.snigle.apptimer.LogService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface IAppUsage {
    fun FindRunning(): AppUsage

    fun Find(packageName: String): AppUsage

    fun ListWithTimer(): ArrayList<AppUsage>

    fun DisplayTimer(app: AppUsage, onclick: () -> Unit)

    suspend fun DisplayPopup(app: AppUsage): Long?


    fun HidePopup(app: AppUsage)
}

data class AppUsage(val packageName: String, var config: AppConfig, var timer: Timer?, var dailyUsage: Long = 0L) {
    var configDate: Date = Date()
    var popupDisplayed = false
    var timerDisplayed = false

    fun UpdateConfig(config: AppConfig) {
        this.config = config
        this.configDate = Date()
    }



    fun IsZero(): Boolean {
        return packageName == ""
    }

    fun HaveTimer(): Boolean {
        return timer != null
    }



}
