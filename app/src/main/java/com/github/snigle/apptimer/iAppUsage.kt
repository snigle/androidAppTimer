package com.github.snigle.apptimer

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.util.Log

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

    override fun StartTimer(app: AppUsage, duration: Long) {
        app.timer = Timer(duration)
        app.timer!!.Start()
        apps[app.packageName] = app
    }

    override fun ResumeTimer(app: AppUsage) {
        println("Resume Timer : ${app.packageName}, with ${app.timer!!.GetDuration() / 1000}s")

        app.timer!!.Start()
        apps[app.packageName] = app
    }

    override fun PauseTimer(app: AppUsage) {
        println("Pause Timer : ${app.packageName}, with ${app.timer!!.GetDuration() / 1000}s")
        apps[app.packageName]?.timer?.Pause()
    }

    override fun ExtendTimer(app: AppUsage, duration: Long) {
        apps[app.packageName]?.timer?.Extends(duration)

    }

    override fun Close(app: AppUsage) {
        apps[app.packageName]?.timer = null
    }

    override fun AskDuration(appConfig: AppConfig, app: AppUsage, callback: (duration: Long) -> Unit)
    {
        Log.d(LogService, "ask duration for app ${app.packageName}")
        servicePopup.openPopup2(appConfig, app) { duration ->
            Log.d(LogService, "received duration ${duration / 1000}s for app ${app.packageName}")
            callback(duration)
            servicePopup.removeAll()
        }
    }

    override fun AskTerminate(appConfig: AppConfig, app: AppUsage, callback: (duration: Long) -> Unit) {
        Log.d(LogService, "ask terminate for app ${app.packageName}")
        servicePopup.openPopup2(appConfig, app) { duration ->
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
            servicePopup.removeAll()
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
            if (usageStats.lastTimeUsed > lastTimeStamp) {
                lastTimeStamp = usageStats.lastTimeUsed
                lastUsedApp = packageName
            }
        }

        return lastUsedApp
    }
}