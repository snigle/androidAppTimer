package com.github.snigle.apptimer.repository

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.github.snigle.apptimer.LogService
import com.github.snigle.apptimer.MainActivity
import com.github.snigle.apptimer.ServicePopup
import com.github.snigle.apptimer.composable.PopupCompose
import com.github.snigle.apptimer.composable.TimerCompose
import com.github.snigle.apptimer.domain.AppConfig
import com.github.snigle.apptimer.domain.AppUsage
import com.github.snigle.apptimer.domain.IAppUsage
import com.github.snigle.apptimer.domain.Timer
import com.github.snigle.apptimer.repository.connectors.LocalStorage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Calendar
import kotlin.coroutines.resume

class AppUsageRepo(private val servicePopup: ServicePopup, private val localStorage: LocalStorage, private val appConfigRepo: AppConfigRepo) :
    IAppUsage {

    init {

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun FindRunning(): AppUsage {
        val packageName = getLastStartedApp(servicePopup)
        if (packageName == "") {
            return AppUsage("", AppConfig("", "", false), null)
        }
        var app = localStorage.GetApp(packageName)
        if (app == null || (app.timer != null && app.timer!!.Expired())) {
            app = AppUsage(packageName, appConfigRepo.Find(packageName), null)
            localStorage.SaveApp(app)
        }
        if (app.configDate.time < System.currentTimeMillis() - 60 * 60) {
            app.UpdateConfig(appConfigRepo.Find(packageName))
            localStorage.SaveApp(app)
        }
        return app
    }

    override fun ListWithTimer(): ArrayList<AppUsage> {
        val apps = localStorage.GetList()
        val result = ArrayList<AppUsage>()
        for (app in apps) {
            if (app.HaveTimer() && app.timer!!.IsRunning()) {
                result.add(app)
            }
        }
        return result
    }

    private fun refreshDailyUsage(app: AppUsage) {
        val usageStatsManager =
            servicePopup.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
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

    @OptIn(DelicateCoroutinesApi::class)
    override fun DisplayTimer(app: AppUsage, onclick: () -> Unit) {
        if (app.timer == null) {
            return
        }
        servicePopup.timerComponent = {
            TimerCompose(timer = app.timer!!, onClick = {
                Log.d(LogService, "click on timer")
                onclick()
            })
        }

        app.timerDisplayed = true
        app.popupDisplayed = false
        GlobalScope.launch(Dispatchers.Main) {
            servicePopup.minimize()
        }
    }

    override suspend fun DisplayPopup(
        app: AppUsage
    ): Long? {
        Log.d(LogService, "open popup for app ${app.packageName}")

        return suspendCancellableCoroutine { continuation ->
            openPopup(app.config, app) { duration ->
                servicePopup.removeAll()

                when (duration) {
                    null -> {
                        Log.d(LogService, "received back for app ${app.packageName}")
                    }
                    0L -> {
                        Log.d(LogService, "received close for app ${app.packageName}")
                        val intent = Intent(Intent.ACTION_MAIN)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.addCategory(Intent.CATEGORY_HOME)
                        servicePopup.startActivity(intent)
                    }
                    else -> {
                        Log.d(
                            LogService,
                            "received duration ${duration / 1000}s for app ${app.packageName}"
                        )
                    }
                }

                // Resume the coroutine with the duration (which can be null)
                app.popupDisplayed = false
                HidePopup(app)
                continuation.resume(duration)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun HidePopup(app: AppUsage) {
        app.popupDisplayed = false
        app.timerDisplayed = false

        GlobalScope.launch(Dispatchers.Main) {
            servicePopup.removeAll()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @OptIn(DelicateCoroutinesApi::class)
    fun openPopup(appConfig: AppConfig, app: AppUsage, callback: (duration: Long?) -> Unit) {

        refreshDailyUsage(app)

        servicePopup.timerSettingComponent = {
            PopupCompose(
                appUsage = app,
                appLabel = appConfig.name,
                setTimer = callback,
                settingsIntent = { ->
                    // Start settings intent
                    val intent =
                        Intent(servicePopup.applicationContext, MainActivity::class.java).apply {
                            putExtra("highlight_package", app.packageName)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    servicePopup.startActivity(intent)
                })
        }

        app.popupDisplayed = true
        app.timerDisplayed = false
        GlobalScope.launch(Dispatchers.Main) {
            servicePopup.expand()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun AskDuration(
        appConfig: AppConfig,
        app: AppUsage,
        callback: (duration: Long) -> Unit
    ) {
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

    @RequiresApi(Build.VERSION_CODES.Q)
    fun AskTerminate(
        appConfig: AppConfig,
        app: AppUsage,
        callback: (duration: Long?) -> Unit
    ) {
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


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getLastStartedApp(context: Context): String {
        val currentTimestamp = System.currentTimeMillis()
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        // Set the time window for querying usage statistics
        val fifteenSecondsAgo = currentTimestamp - (60 * 1000) // 1mn ago
        val appList: Map<String, UsageStats> =
            usageStatsManager.queryAndAggregateUsageStats(fifteenSecondsAgo, currentTimestamp)

        var lastUsedApp = ""
        var lastTimeStamp = 0L

        for ((packageName, usageStats) in appList) {
            // Use lastTimeVisible for more accuracy on Android Q+
            if (usageStats.lastTimeUsed > lastTimeStamp) {
                lastTimeStamp = usageStats.lastTimeUsed
                lastUsedApp = packageName
            }
        }

        return lastUsedApp
    }
}