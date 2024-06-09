package com.github.snigle.apptimer

import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicReference

class uAppMonitoring(val appUsageRepo: IAppUsage, val appConfigRepo: IAppConfig) {

    var lastRunningAppRef = AtomicReference<AppUsage?>()
    var waiting = AtomicReference<Boolean>(false)
    fun MonitorRunningApp() {


        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (waiting.get()) {
                    return
                }

                val runningApp = appUsageRepo.FindRunning()
                if (runningApp.IsZero()) {
                    return
                }

                val lastRunningApp = lastRunningAppRef.get()
                if (lastRunningApp != null && lastRunningApp.HaveTimer() && lastRunningApp.packageName != runningApp.packageName) {
                    appUsageRepo.PauseTimer(lastRunningApp)
                }
                lastRunningAppRef.set(runningApp)


                val config = appConfigRepo.Find(runningApp.packageName)
                if (runningApp.HaveTimer()) {
                    if (runningApp.timer!!.IsPaused()) {
                        appUsageRepo.ResumeTimer(runningApp)
                    }
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
            appUsageRepo.StartTimer(app, duration)
            waiting.set(false)
        }
    }

    fun AskTerminate(appConfig: AppConfig, app: AppUsage) {
        waiting.set(true)
        appUsageRepo.AskTerminate(appConfig, app) { duration ->
            if (duration != 0L) {
                appUsageRepo.ExtendTimer(app, duration)
            } else {
                appUsageRepo.Close(app)
            }
            waiting.set(false)
        }
    }
}