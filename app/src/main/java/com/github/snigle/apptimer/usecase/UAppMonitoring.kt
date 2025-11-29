package com.github.snigle.apptimer.usecase

import android.util.Log
import com.github.snigle.apptimer.LogService
import com.github.snigle.apptimer.domain.AppConfig
import com.github.snigle.apptimer.domain.AppUsage
import com.github.snigle.apptimer.domain.IAppConfig
import com.github.snigle.apptimer.domain.IAppUsage
import com.github.snigle.apptimer.domain.IScreenManager
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicReference

class uAppMonitoring(
    private val appUsageRepo: IAppUsage,
    private val appConfigRepo: IAppConfig,
    private val screenManager: IScreenManager
) {

    var lastRunningAppRef = AtomicReference<AppUsage?>()
    var waiting = AtomicReference<Boolean>(false)
    suspend fun MonitorRunningApp() {

        while (true) {
            // If screen is off, pause any running timer to save battery
            if (screenManager.isOff()) {
                val lastRunningApp = lastRunningAppRef.get()
                if (lastRunningApp != null && lastRunningApp.HaveTimer() && !lastRunningApp.timer!!.IsPaused()) {
                    lastRunningApp.timer!!.Pause()
                    appUsageRepo.Save(lastRunningApp)
                    Log.d(LogService, "Screen is off, timer paused for ${lastRunningApp.packageName}")
                }
                delay(15000L) // Check less frequently when screen is off
                continue
            }

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
                Log.d(LogService, "TimeLeft: ${runningApp.timer!!.GetTimeLeft()}")
                if (runningApp.timer!!.Timeout()) {
                    AskTerminate(config, runningApp)
                }
            } else {
                if (config.monitor) {
                    InitTimer(config, runningApp)
                }
            }

            delay(5000L) // Main delay to reduce battery consumption
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
                app.timer?.Start()
                appUsageRepo.Save(app)
                appUsageRepo.DisplayTimer(app.timer!!, { AskTerminate(appConfig, app) })
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