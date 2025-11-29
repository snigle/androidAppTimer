package com.github.snigle.apptimer.usecase

import android.util.Log
import com.github.snigle.apptimer.LogService
import com.github.snigle.apptimer.domain.AppConfig
import com.github.snigle.apptimer.domain.AppUsage
import com.github.snigle.apptimer.domain.IAppConfig
import com.github.snigle.apptimer.domain.IAppUsage
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.concurrent.atomic.AtomicReference

class uAppMonitoring(
    private val appUsageRepo: IAppUsage,
    private val appConfigRepo: IAppConfig
) {

    var lastRunningAppRef = AtomicReference<AppUsage?>()
    var waiting = AtomicReference<Boolean>(false)
    suspend fun MonitorRunningApp() {

        coroutineScope {
            while (isActive) {
                val runningApp = appUsageRepo.FindRunning()
                if (runningApp.IsZero()) {
                    delay(5000L)
                    continue
                }

                val lastRunningApp = lastRunningAppRef.get()
                if (lastRunningApp != null && lastRunningApp.packageName != runningApp.packageName) {
                    if (lastRunningApp.HaveTimer()) {
                        lastRunningApp.timer!!.Pause()
                        appUsageRepo.Save(lastRunningApp)
                    }
                    appUsageRepo.HidePopup()
                    waiting.set(false)
                    Log.d(LogService, "switch app from ${lastRunningApp.packageName}: ${runningApp.packageName}")

                }
                lastRunningAppRef.set(runningApp)

                // Lock if popup is displayed
                if (waiting.get()) {
                    delay(1000L) // Check more frequently when waiting for user input
                    continue
                }

                val config = appConfigRepo.Find(runningApp.packageName)
                if (runningApp.HaveTimer()) {
                    if (runningApp.timer!!.IsPaused()) {
                        runningApp.timer!!.Start()
                        appUsageRepo.Save(runningApp)
                        appUsageRepo.DisplayTimer(runningApp.timer!!, { AskTerminate(config, runningApp) })
                    }
                    Log.d(LogService, "TimeLeft ${config.name}: ${runningApp.timer!!.GetTimeLeft()}")
                    if (runningApp.timer!!.Timeout()) {
                        AskTerminate(config, runningApp)
                    }
                } else if (config.monitor) {
                    InitTimer(config, runningApp)
                } else {
                    Log.d(LogService, "do nothing on app ${config.name} ${runningApp.packageName} ")
                }

                delay(5000L) // Main delay to reduce battery consumption
            }
            Log.d(LogService, "monitoring stopped")

        }
    }


    fun InitTimer(appConfig: AppConfig, app: AppUsage) {
        app.timer = com.github.snigle.apptimer.domain.Timer(appConfig.defaultDuration.inWholeMilliseconds)
        app.timer!!.Start()
        appUsageRepo.Save(app)
        appUsageRepo.DisplayTimer(app.timer!!, { AskTerminate(appConfig, app) })
    }

    fun AskTimer(appConfig: AppConfig, app: AppUsage) {
        waiting.set(true)
        appUsageRepo.AskDuration(appConfig, app) { duration ->
            if (duration == 0L) { // Customer leave
                waiting.set(false)
            }
            app.timer = com.github.snigle.apptimer.domain.Timer(duration)
            app.timer!!.Start()
            appUsageRepo.Save(app)
            appUsageRepo.DisplayTimer(app.timer!!, { AskTerminate(appConfig, app) })
            waiting.set(false)
        }
    }

    fun TimerSettings() {
        val runningApp = appUsageRepo.FindRunning()
        if (runningApp.IsZero()) {
            return
        }

        val appConfig = appConfigRepo.Find(runningApp.packageName)
        return AskTerminate(appConfig, runningApp);
    }

    fun AskTerminate(appConfig: AppConfig, app: AppUsage) {
        waiting.set(true)
        app.timer!!.Pause()
        Log.d(LogService, "ask terminate for app ${app.packageName} ${app.timer!!.ElapseTime() / 1000} ${app.timer!!.GetAggregateDuration() / 1000}")

        appUsageRepo.AskTerminate(appConfig, app) { duration ->


            if (duration == null) {
                // Canceled popup, do nothing. Let's main execution to restart the timer
            } else if (duration != 0L) {
                app.timer!!.Extends(duration)
                appUsageRepo.Save(app)
                appUsageRepo.DisplayTimer(app.timer!!, { AskTerminate(appConfig, app) })
            } else {
                app.timer = null
                appUsageRepo.Save(app)
            }
            waiting.set(false)
        }
    }
}