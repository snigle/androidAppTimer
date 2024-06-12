package com.github.snigle.apptimer.usecase

import android.util.Log
import com.github.snigle.apptimer.LogService
import com.github.snigle.apptimer.domain.AppConfig
import com.github.snigle.apptimer.domain.AppUsage
import com.github.snigle.apptimer.domain.IAppConfig
import com.github.snigle.apptimer.domain.IAppUsage
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicReference

class uAppMonitoring(val appUsageRepo: IAppUsage, val appConfigRepo: IAppConfig) {

    var lastRunningAppRef = AtomicReference<AppUsage?>()
    var waiting = AtomicReference<Boolean>(false)
    fun MonitorRunningApp() {


        Timer().schedule(object : TimerTask() {
            override fun run() {


                val runningApp = appUsageRepo.FindRunning()
                if (runningApp.IsZero()) {
                    return
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
                    return
                }

                val config = appConfigRepo.Find(runningApp.packageName)
                if (runningApp.HaveTimer()) {
                    if (runningApp.timer!!.IsPaused()) {
                        runningApp.timer!!.Start()
                        appUsageRepo.Save(runningApp)
                        appUsageRepo.DisplayTimer(runningApp.timer!!)
                    }
                    Log.d(LogService, "TimeLeft: ${runningApp.timer!!.GetTimeLeft()}")
                    if (runningApp.timer!!.Timeout()) {
                        AskTerminate(config, runningApp)
                    }
                    return
                }

                if (config.monitor) {
                    AskTimer(config, runningApp)
                }

            }
        }, 0, 1000)
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
            appUsageRepo.DisplayTimer(app.timer!!)
            waiting.set(false)
        }
    }

    fun AskTerminate(appConfig: AppConfig, app: AppUsage) {
        waiting.set(true)
        app.timer!!.Pause()
        appUsageRepo.AskTerminate(appConfig, app) { duration ->
            if (duration != 0L) {
                app.timer!!.Extends(duration)
                appUsageRepo.Save(app)
                appUsageRepo.DisplayTimer(app.timer!!)
            } else {
                app.timer = null
                appUsageRepo.Save(app)
            }
            waiting.set(false)
        }
    }
}