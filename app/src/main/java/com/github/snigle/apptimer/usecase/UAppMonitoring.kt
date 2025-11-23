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
                        appUsageRepo.DisplayTimer(runningApp.timer!!, {AskTerminate(config, runningApp)})
                    }
                    Log.d(LogService, "TimeLeft: ${runningApp.timer!!.GetTimeLeft()}")
                    if (runningApp.timer!!.Timeout()) {
                        AskTerminate(config, runningApp)
                    }
                    return
                }

                if (config.monitor) {
                    InitTimer(config, runningApp)
                    //AskTimer(config, runningApp)
                }

            }
        }, 0, 1000)
    }


    fun InitTimer(appConfig: AppConfig, app: AppUsage) {
        app.timer = com.github.snigle.apptimer.domain.Timer(appConfig.defaultDuration.inWholeMilliseconds)
        app.timer!!.Start()
        appUsageRepo.Save(app)
        appUsageRepo.DisplayTimer(app.timer!!, {AskTerminate(appConfig, app)})
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
            appUsageRepo.DisplayTimer(app.timer!!, {AskTerminate(appConfig, app)})
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
        Log.d(LogService, "ask terminate for app ${app.packageName} ${app.timer!!.ElapseTime()/1000} ${app.timer!!.GetAggregateDuration()/1000}")

        appUsageRepo.AskTerminate(appConfig, app) { duration ->
            if (duration == null) {
                app.timer?.Start()
                appUsageRepo.Save(app)
            } else if (duration != 0L) {
                app.timer!!.Extends(duration)
                appUsageRepo.Save(app)
                appUsageRepo.DisplayTimer(app.timer!!,  {AskTerminate(appConfig, app)})
            } else {
                app.timer = null
                appUsageRepo.Save(app)
            }
            waiting.set(false)
        }
    }
}