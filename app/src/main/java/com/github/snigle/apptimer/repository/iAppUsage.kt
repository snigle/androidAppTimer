package com.github.snigle.apptimer.repository

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.snigle.apptimer.LogService
import com.github.snigle.apptimer.ServicePopup
import com.github.snigle.apptimer.domain.AppConfig
import com.github.snigle.apptimer.domain.AppUsage
import com.github.snigle.apptimer.domain.IAppUsage
import com.github.snigle.apptimer.domain.Timer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AppUsageRepo(private val servicePopup: ServicePopup) : IAppUsage {

    // Local storage
    private val apps = mutableMapOf<String, AppUsage>()


    init {

    }

    override fun FindRunning(): AppUsage {
        val packageName = getLastStartedApp(servicePopup)
        var app = apps[packageName]
        if ( app == null || (app.timer != null && app.timer!!.Expired())) {
            app = AppUsage(packageName ,null)
            apps[packageName] = app
        }
        return app
    }

    override fun Save(app: AppUsage) {
        apps[app.packageName] = app
    }

    override fun DisplayTimer(timer: Timer) {
        servicePopup.bubbleTimer = timer
        GlobalScope.launch(Dispatchers.Main) {
            servicePopup.minimize()
        }
    }

    override fun HidePopup() {
        GlobalScope.launch(Dispatchers.Main) {
            servicePopup.removeAll()
        }
    }

    fun openPopup(appConfig: AppConfig, app: AppUsage, callback: (duration: Long)->Unit) {
        servicePopup.bubbleAppUsage = app
        servicePopup.bubbleAppConfig = appConfig
        servicePopup.bubbleTimerCallBack = callback

        GlobalScope.launch(Dispatchers.Main) {
            servicePopup.expand()
        }
    }

    override fun AskDuration(appConfig: AppConfig, app: AppUsage, callback: (duration: Long) -> Unit)
    {
        Log.d(LogService, "ask duration for app ${app.packageName}")
        openPopup(appConfig, app) { duration ->
            Log.d(LogService, "received duration ${duration / 1000}s for app ${app.packageName}")
            servicePopup.removeAll()
            callback(duration)
        }
    }

    override fun AskTerminate(appConfig: AppConfig, app: AppUsage, callback: (duration: Long) -> Unit) {
        Log.d(LogService, "ask terminate for app ${app.packageName}")
        openPopup(appConfig, app) { duration ->
            servicePopup.removeAll()

            if (duration != 0L) {
                Log.d(
                    LogService,
                    "received duration ${duration / 1000}s for app ${app.packageName}"
                )
            } else {
                Log.d(
                    LogService,
                    "received close for app ${app.packageName}"
                )
                val intent = Intent(Intent.ACTION_MAIN)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.addCategory(Intent.CATEGORY_HOME)
                servicePopup.startActivity(intent)
            }
            callback(duration)
        }
    }



    private fun getLastStartedApp(context: Context): String {
        val currentTimestamp = System.currentTimeMillis()
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        // Set the time window for querying usage statistics
        val oneHourAgo = currentTimestamp - (60 * 1000) // 1m ago
        val appList: Map<String, UsageStats> =
            usageStatsManager.queryAndAggregateUsageStats(oneHourAgo, currentTimestamp)

        var lastUsedApp = ""
        var lastTimeStamp = 0L

        for ((packageName, usageStats) in appList) {
            //Log.d(LogService)
            if (usageStats.lastTimeUsed > lastTimeStamp) {
                lastTimeStamp = usageStats.lastTimeUsed
                lastUsedApp = packageName
            }
        }

        return lastUsedApp
    }
}