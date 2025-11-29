package com.github.snigle.apptimer.domain

import android.util.Log
import com.github.snigle.apptimer.LogService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface IAppUsage {
    fun FindRunning(): AppUsage
    fun AskDuration(appConfig: AppConfig, app: AppUsage, callback: (duration: Long) -> Unit)
    fun AskTerminate(appConfig: AppConfig, app: AppUsage, callback: (duration: Long?) -> Unit)

    fun DisplayTimer(timer: Timer, onclick: () -> Unit)

    fun HidePopup()
    fun Save(app: AppUsage)
}

data class AppUsage(val packageName: String, var timer: Timer?, var dailyUsage: Long = 0L) {

    fun IsZero(): Boolean {
        return packageName == ""
    }

    fun HaveTimer(): Boolean {
        return timer != null
    }


}
