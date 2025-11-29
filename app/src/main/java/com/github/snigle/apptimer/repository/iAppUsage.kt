package com.github.snigle.apptimer.repository

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.snigle.apptimer.LogService
import com.github.snigle.apptimer.MainActivity
import com.github.snigle.apptimer.ServicePopup
import com.github.snigle.apptimer.composable.PopupCompose
import com.github.snigle.apptimer.composable.TimerCompose
import com.github.snigle.apptimer.domain.AppConfig
import com.github.snigle.apptimer.domain.AppUsage
import com.github.snigle.apptimer.domain.IAppUsage
import com.github.snigle.apptimer.domain.Timer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Calendar

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

    private fun refreshDailyUsage(app: AppUsage) {
        val usageStatsManager = servicePopup.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        val endOfDay = System.currentTimeMillis()

        // Use queryAndAggregateUsageStats for a more direct aggregation
        val usageStatsMap = usageStatsManager.queryAndAggregateUsageStats(startOfDay, endOfDay)
        val usageStat = usageStatsMap[app.packageName]

        if (usageStat != null) {
            app.dailyUsage = usageStat.totalTimeInForeground
        } else {
            app.dailyUsage = 0L // Reset if no stats are found
        }
    }

    override fun Save(app: AppUsage) {
        apps[app.packageName] = app
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun DisplayTimer(timer: Timer, onclick: () -> Unit) {
        servicePopup.timerComponent = {
            TimerCompose(timer = timer, onClick = {
                Log.d(LogService, "click on timer")
                onclick()
            })
        }

        GlobalScope.launch(Dispatchers.Main) {
            servicePopup.minimize()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun HidePopup() {
        GlobalScope.launch(Dispatchers.Main) {
            servicePopup.removeAll()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun openPopup(appConfig: AppConfig, app: AppUsage, callback: (duration: Long?)->Unit) {

        refreshDailyUsage(app)

        servicePopup.timerSettingComponent = {
            PopupCompose(
                appUsage = app,
                appLabel = appConfig.name,
                setTimer = callback,
                settingsIntent = { ->
                    // Start settings intent
                    val intent = Intent(servicePopup.applicationContext, MainActivity::class.java).apply {
                        putExtra("highlight_package", app.packageName)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    servicePopup.startActivity(intent)
                })
        }

        GlobalScope.launch(Dispatchers.Main) {
            servicePopup.expand()
        }
    }

    override fun AskDuration(appConfig: AppConfig, app: AppUsage, callback: (duration: Long) -> Unit)
    {
        Log.d(LogService, "ask duration for app ${app.packageName}")
        openPopup(appConfig, app) { duration ->
            var duration = duration
            if (duration == null) {
                duration = appConfig.defaultDuration.inWholeMilliseconds
            }
            Log.d(LogService, "received duration ${duration / 1000}s for app ${app.packageName}")
            servicePopup.removeAll()
            callback(duration)
        }
    }

    override fun AskTerminate(appConfig: AppConfig, app: AppUsage, callback: (duration: Long?) -> Unit) {
        Log.d(LogService, "ask terminate for app ${app.packageName}")
        openPopup(appConfig, app) { duration ->
            servicePopup.removeAll()

            if (duration == null) {
                Log.d(
                    LogService,
                    "received back for app ${app.packageName}"
                )
            } else if (duration != 0L) {
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
        val fifteenSecondsAgo = currentTimestamp - (15 * 1000) // 15s ago
        val appList: Map<String, UsageStats> =
            usageStatsManager.queryAndAggregateUsageStats(fifteenSecondsAgo, currentTimestamp)

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