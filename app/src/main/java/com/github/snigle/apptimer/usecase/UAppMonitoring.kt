package com.github.snigle.apptimer.usecase

import android.util.Log
import com.github.snigle.apptimer.LogService
import com.github.snigle.apptimer.domain.AppConfig
import com.github.snigle.apptimer.domain.AppUsage
import com.github.snigle.apptimer.domain.IAppConfig
import com.github.snigle.apptimer.domain.IAppUsage
import com.github.snigle.apptimer.domain.IScreenManager
import com.github.snigle.apptimer.domain.Timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

class uAppMonitoring(
    private val appUsageRepo: IAppUsage,
    private val appConfigRepo: IAppConfig,
    private val screenManagerRepo: IScreenManager
) {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var job: Job? = null;

    var monitoring = AtomicReference<Boolean>(false)
    var waiting = AtomicReference<Boolean>(false)

    fun MonitorRunningApp() {

        Log.d(LogService, "start coroutine")
        if (job != null && job!!.isActive) {
            Log.d(LogService, "coroutine already running")
            return
        }

        this.job = serviceScope.launch {
            var popupDisplayed = false
            var timerDisplayed = ""

            while (true) {
            var sleepTime = 5000L

                val app = appUsageRepo.FindRunning()
                Log.d(LogService, "app ${app.packageName} ${app.timer?.ElapseTime()?.div(1000)}")

                if (!popupDisplayed && app.config.monitor) {
                    if (app.timer?.Expired() == true) {
                        Log.d(LogService, "app ${app.packageName} expired")
                        app.timer = null
                    }

                    if (app.HaveTimer() && app.timer?.Timeout() == true) {
                        Log.d(LogService, "app ${app.packageName} expired")

                        app.timer!!.Pause()
                        timerDisplayed = ""

                        val extendDuration = appUsageRepo.DisplayPopup(app)
                        if (extendDuration != null && extendDuration > 0L) {
                            Log.d(LogService, "app ${app.packageName} extends")

                            app.timer!!.Extends(extendDuration)
                            app.timer!!.Start()
                        } else {
                            Log.d(LogService, "app ${app.packageName} closed")
                            //app.timer = null
                        }
                        appUsageRepo.Save(app)
                        appUsageRepo.HidePopup()
                        continue
                    }

                    if (!app.HaveTimer()) {
                        Log.d(LogService, "app ${app.packageName} init timer")
                        app.timer = Timer(app.config.defaultDuration.inWholeMilliseconds)
                        app.timer!!.Start()
                        appUsageRepo.Save(app)
                    }

                    if (app.HaveTimer() && app.timer!!.IsPaused()) {
                        Log.d(LogService, "app ${app.packageName} resume timer")
                        app.timer!!.Start()
                        appUsageRepo.Save(app)
                    }

                    if (timerDisplayed != app.packageName) {
                        appUsageRepo.HidePopup()
                        appUsageRepo.DisplayTimer(app.timer!!, {
                            popupDisplayed = true
                            timerDisplayed = ""
                            serviceScope.launch {
                                Log.d(LogService, "app ${app.packageName} pause timer")

                                app.timer!!.Pause()
                                val extendDuration = appUsageRepo.DisplayPopup(app)
                                if (extendDuration != null && extendDuration > 0L) {
                                    Log.d(LogService, "app ${app.packageName} extends timer")
                                    app.timer!!.Extends(extendDuration)
                                }
                                Log.d(LogService, "app ${app.packageName} resume timer")

                                app.timer!!.Start()
                                appUsageRepo.Save(app)
                                popupDisplayed = false
                                // Quick display timer in next loop
                                sleepTime = 0L
                            }
                        })
                        timerDisplayed = app.packageName
                    }
                }


                // Check all other app if they have any timer and pause
                for (otherApps in appUsageRepo.ListWithTimer()) {
                    if (otherApps.packageName != app.packageName) {
                        otherApps.timer!!.Pause()
                        appUsageRepo.Save(otherApps)
                    }
                }
                if (!app.config.monitor) {
                    appUsageRepo.HidePopup()
                    timerDisplayed = ""
                }

                if (screenManagerRepo.IsDisabled() && app.HaveTimer()) {
                    Log.d(LogService, "app ${app.packageName} pause timer because screen is disabled")

                    app.timer!!.Pause()
                    appUsageRepo.Save(app)
                    appUsageRepo.HidePopup()
                    break
                }

                delay(sleepTime) // Main delay to reduce battery consumption
            }

            Log.d(LogService, "coroutine stopped")
        }

    }

}