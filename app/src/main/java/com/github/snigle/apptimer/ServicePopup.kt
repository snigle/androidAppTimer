package com.github.snigle.apptimer

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.material3.Text
import androidx.preference.PreferenceManager
import com.torrydo.floatingbubbleview.service.expandable.BubbleBuilder
import com.torrydo.floatingbubbleview.service.expandable.ExpandableBubbleService
import com.torrydo.floatingbubbleview.service.expandable.ExpandedBubbleBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.TimerTask

val LogService = "apptimer.popup"

class ServicePopup : ExpandableBubbleService() {
    private var popupLabel: String = ""
    private lateinit var preferences: Preference
    private lateinit var packageManager: PackageManager

    private var popupApp: StartedApp = StartedApp()
    private var previoustRunningApp: StartedApp = StartedApp()

    private val apps = mutableMapOf<String, StartedApp>()


    override fun configBubble(): BubbleBuilder {
        return BubbleBuilder(this).bubbleCompose { Text(text = "") }
    }

    var bubbleAppConfig: AppConfig? = null
    var bubbleAppUsage: AppUsage? = null
    var bubbleTimerCallBack: (duration:Long) -> Unit = {}
    var bubbleCloseCallBack: (duration:Long) -> Unit = {}

    override fun configExpandedBubble(): ExpandedBubbleBuilder {


        return ExpandedBubbleBuilder(this).expandedCompose {
            PopupCompose2(appUsage = this.bubbleAppUsage!!,
                appLabel = this.bubbleAppConfig!!.name,
                setTimer = bubbleTimerCallBack,
                close = { app: AppUsage ->
                    // Start android launcher
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.addCategory(Intent.CATEGORY_HOME)
                    startActivity(intent)

                    removeAll()
                },
                settingsIntent = { app: AppUsage ->

                    // Start settings intent
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)

                    removeAll()
                })

        }
            // handle key code

            .style(null)
            //
            .fillMaxWidth(true)
            // animate to the left/right side when release, trfalseue by default
            .enableAnimateToEdge(false)
            // set background dimmer
            .dimAmount(0.6f).draggable(false)


    }

    fun configExpandedBubbleOld(): ExpandedBubbleBuilder {


        return ExpandedBubbleBuilder(this).expandedCompose {
            PopupCompose(app = this.popupApp,
                appLabel = this.popupLabel,
                setTimer = { app: StartedApp, duration: Long ->
                    app.startTimer(duration)
                    closePopup()
                },
                close = { app: StartedApp ->
                    // Start android launcher
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.addCategory(Intent.CATEGORY_HOME)
                    startActivity(intent)
                    // close popup and reset timer
                    app.reset()
                    closePopup()

                },
                settingsIntent = { app: StartedApp ->

                    // Start settings intent
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)

                    app.reset()
                    closePopup()
                })

        }
            // handle key code

            .style(null)
            //
            .fillMaxWidth(true)
            // animate to the left/right side when release, trfalseue by default
            .enableAnimateToEdge(false)
            // set background dimmer
            .dimAmount(0.6f).draggable(false)


    }

    override fun onCreate() {
        super.onCreate()
        preferences = Preference(PreferenceManager.getDefaultSharedPreferences(applicationContext))
        packageManager = applicationContext.packageManager
        Log.d("test", "restart service")

//        Timer().schedule(
//            CustomTimerTask(this,
//                callback = { packageName: String -> this.detectRunningApp(packageName) }),
//            0,
//            1000
//        )
        uAppMonitoring(AppUsageRepo(servicePopup = this),AppConfigRepo(PreferenceManager.getDefaultSharedPreferences(applicationContext), packageManager)).MonitorRunningApp()

    }

    // Start 10h00 , timer 5m
    // Pause 10h01 , ...
    // Start 10h15 , timer 4m
    private fun detectRunningApp(packageName: String): Unit {

        var app = apps[packageName]
        if (app == null) {
            Log.d(LogService, "detected new app ${packageName}")
            app = StartedApp(packageName)
            apps[packageName] = app
        }

        // Do nothing if popup is open
        if (popupApp.packageName != "") {
            // Application have been close with back or home button
            if (popupApp.packageName != app.packageName) {
                app.reset()
                closePopup()
            }
            return
        }

        // Detect Pause
        if (previoustRunningApp.packageName != app.packageName) {

            if (previoustRunningApp.haveTimer()) {
                Log.d(LogService, "detect app pause ${previoustRunningApp.packageName}")
                previoustRunningApp.pause()
            }

            if (app.haveTimer()) {
                Log.d(LogService, "detect app resume ${app.packageName}")
                app.resume()
            }

            if (!app.haveTimer() && preferences.get(app.packageName)) {
                Log.d(LogService, "start new timer ${app.packageName}")
                this.openPopup(app)
            }

        }

        if (app.haveTimer()) {
// Todo: Detect lock screen ?
            if (app.expired()) {
                Log.d(LogService, "detected expired app ${app.packageName}")
                // Remove timer
                app.reset()
                this.openPopup(app)
                // Detect timed out
            } else if (app.timedOut()) {
                Log.d(LogService, "detected timed out app ${app.packageName}")
                this.openPopup(app)
            }

        }

        previoustRunningApp = app
    }

    fun openPopup2(appConfig: AppConfig, app: AppUsage, callback: (duration: Long)->Unit) {
        this.bubbleAppUsage = app
        this.bubbleAppConfig = appConfig
        this.bubbleTimerCallBack = callback

        var servicePopup: ServicePopup = this
        GlobalScope.launch(Dispatchers.Main) {
            servicePopup.expand()
        }
    }

    private fun openPopup(app: StartedApp) {
        this.popupApp = app

        var servicePopup: ServicePopup = this
        GlobalScope.launch(Dispatchers.Main) {
            val appInfo =
                packageManager.getApplicationInfo(app.packageName, PackageManager.GET_META_DATA)
            servicePopup.popupLabel = packageManager.getApplicationLabel(appInfo).toString()

            servicePopup.expand()
        }
    }

    private fun closePopup() {
        removeAll()
        this.popupApp = StartedApp("")
    }
}

class Daily(private var duration: Long, val day: Int) {
    fun addDuration(duration: Long): Unit {
        if (duration > 0) {
            this.duration += duration
        }
    }

    fun getDuration(): Long {
        return this.duration
    }
}

class StartedApp {

    val packageName: String
    private val cleanExpiredDuration = 5 * 60 // 5 minutes
    private var pausedAt: Long
    private var pauseDuration: Long
    private var duration: Long
    private var startedAt: Long
    private var createdAt: Long
    private var dailyDuration: Daily

    constructor(packageName: String = "") {
        this.packageName = packageName

        // Copy paste from reset func
        this.pausedAt = 0L
        this.pauseDuration = 0L
        this.duration = 0L
        this.startedAt = 0L
        this.createdAt = 0L
        this.dailyDuration = Daily(0, 0)
    }

    fun reset(): Unit {
        if (this.timedOut()) {
            this.registerDailyUsage()
        }
        this.pausedAt = 0L
        this.pauseDuration = 0L
        this.duration = 0L
        this.startedAt = 0L
        this.createdAt = 0L
    }

    fun registerDailyUsage(): Unit {
        if (!this.hasDailyUsage()) {
            this.dailyDuration = Daily(0, LocalDate.now().dayOfYear)
        }
        this.dailyDuration.addDuration(this.duration)
    }

    fun hasDailyUsage(): Boolean {
        return this.dailyDuration.day == LocalDate.now().dayOfYear && this.dailyDuration.getDuration() > 0
    }

    fun formatDailyUsage(): String {
        val duration = Duration.ofSeconds(this.dailyDuration.getDuration())
        val hours = duration.toHours()
        val minutes = duration.minusHours(hours).toMinutes()

        return "${if (hours > 0) "$hours" + "h " else ""}" + "${if (minutes > 0) "$minutes" + "m" else ""}"
    }

    fun startTimer(duration: Long): Unit {
        this.reset()
        this.duration = duration
        this.startedAt = Instant.now().epochSecond
        this.createdAt = Instant.now().epochSecond
    }

    fun haveTimer(): Boolean {
        return this.duration > 0L && this.startedAt > 0L
    }

    fun timedOut(): Boolean {
        return duration > 0 && (Instant.now().epochSecond - duration > startedAt)
    }

    fun expired(): Boolean {
        return duration > 0 && ((Instant.now().epochSecond - duration - cleanExpiredDuration > createdAt) || (pauseDuration > cleanExpiredDuration))
    }

    fun pause(): Unit {
        pausedAt = Instant.now().epochSecond
        this.duration = Instant.now().epochSecond - this.startedAt
        this.registerDailyUsage()
    }

    fun resume(): Unit {
        pauseDuration = Instant.now().epochSecond - pausedAt
        pausedAt = 0
        this.startedAt = Instant.now().epochSecond
    }


    fun isZero(): Boolean {
        return this.packageName == ""
    }

    companion object {
        val PreviewTimedoutApp = StartedApp("Facebook")
        val PreviewDailyUsageApp = StartedApp("Facebook")

        init {
            PreviewTimedoutApp.startedAt = 10
            PreviewTimedoutApp.duration = 1

            PreviewDailyUsageApp.dailyDuration = Daily(60 * 60, LocalDate.now().dayOfYear)
        }
    }
}

class CustomTimerTask(
    private val servicePopup: ServicePopup, private val callback: (packageName: String) -> Unit
) : TimerTask() {

    private val apps = mutableMapOf<String, StartedApp>()

    override fun run() {
        val packageName = this.getLastStartedApp(servicePopup)
        if (packageName != "") {
            this.callback(packageName)
        }
    }


    fun getLastStartedApp(context: Context): String {
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
