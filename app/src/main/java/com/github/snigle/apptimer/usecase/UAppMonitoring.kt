package com.github.snigle.apptimer.usecase

import android.util.Log
import com.github.snigle.apptimer.LogService
import com.github.snigle.apptimer.domain.AppUsage
import com.github.snigle.apptimer.domain.IAppUsage
import com.github.snigle.apptimer.domain.IScreenManager
import com.github.snigle.apptimer.domain.Timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AppMonitoring(
    private val appUsageRepo: IAppUsage,
    private val screenManagerRepo: IScreenManager
) {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var job: Job? = null;

    fun MonitorRunningApp() {

        Log.d(LogService, "start coroutine")
        if (job != null && job!!.isActive) {
            Log.d(LogService, "coroutine already running")
            return
        }

        this.job = serviceScope.launch {

            var previousApp: AppUsage? = null

            while (true) {
                val sleepTime = 5000L

                val app = appUsageRepo.FindRunning()
                Log.d(LogService, "app ${app.packageName} ${app.timer?.ElapseTime()?.div(1000)}")

                // Wait if we wait user input from popup
                if (app.popupDisplayed) {
                    delay(sleepTime)
                    continue
                }

                // Close previous on app change
                if (previousApp?.packageName != app.packageName && previousApp?.config?.monitor == true && previousApp.HaveTimer()) {
                    previousApp.timer!!.Pause()
                    appUsageRepo.HidePopup(previousApp)
                }
                previousApp = app


                if (app.config.monitor) {
                    handleAppTimer(app)
                }

                // Auto pause and stop coroutine if screen is off.
                // Coroutine will be restarted by the service on startup.
                if (screenManagerRepo.IsDisabled()) {
                    Log.d(
                        LogService,
                        "app ${app.packageName} pause timer because screen is disabled"
                    )
                    if (app.HaveTimer()) {
                        app.timer!!.Pause()
                        appUsageRepo.HidePopup(app)
                    }
                    break
                }

                delay(sleepTime) // Main delay to reduce battery consumption
            }

            Log.d(LogService, "coroutine stopped")
        }

    }

    private fun displayTimer(app: AppUsage) {
        appUsageRepo.DisplayTimer(app, {
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
            }
        })
    }

    private suspend fun handleAppTimer(app: AppUsage) {
        // Old timer still running, clean it.
        if (app.timer?.Expired() == true) {
            Log.d(LogService, "app ${app.packageName} expired")
            app.timer = null
            appUsageRepo.HidePopup(app)
        }

        // Have timer timeout
        if (app.HaveTimer() && app.timer?.Timeout() == true) {
            Log.d(LogService, "app ${app.packageName} expired")
            app.timer!!.Pause()

            val extendDuration = appUsageRepo.DisplayPopup(app)
            if (extendDuration != null && extendDuration > 0L) {
                Log.d(LogService, "app ${app.packageName} extends")

                app.timer!!.Extends(extendDuration)
                app.timer!!.Start()
                displayTimer(app)
            } else {
                Log.d(LogService, "app ${app.packageName} closed")
            }
            // App don't have timer yet
        } else if (!app.HaveTimer()) {
            Log.d(LogService, "app ${app.packageName} init timer")
            app.timer = Timer(app.config.defaultDuration.inWholeMilliseconds)
            app.timer!!.Start()
            displayTimer(app)
            // App has been resume
        } else if (app.HaveTimer() && app.timer!!.IsPaused()) {
            Log.d(LogService, "app ${app.packageName} resume timer")
            app.timer!!.Start()
            displayTimer(app)
        }
    }

}