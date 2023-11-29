package com.github.snigle.apptimer

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.preference.PreferenceManager
import com.torrydo.floatingbubbleview.FloatingBubbleListener
import com.torrydo.floatingbubbleview.service.expandable.BubbleBuilder
import com.torrydo.floatingbubbleview.service.expandable.ExpandableBubbleService
import com.torrydo.floatingbubbleview.service.expandable.ExpandedBubbleBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Timer
import java.util.TimerTask

val LogService = "apptimer.popup"

class Popup : ExpandableBubbleService() {
    private var popupLabel: String = ""
    private lateinit var preferences: Preference
    private lateinit var packageManager: PackageManager

    private var popupApp: StartedApp = StartedApp()
    private var previoustRunningApp: StartedApp = StartedApp()

    private val apps = mutableMapOf<String, StartedApp>()


    override fun configBubble(): BubbleBuilder {


        return BubbleBuilder(this)

            // set bubble view

            // or our sweetie, Jetpack Compose
            .bubbleCompose {
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

            // set style for the bubble, fade animation by default
            .bubbleStyle(null)

            // set start location for the bubble, (x=0, y=0) is the top-left
            .startLocation(0, 0)    // in dp
            .startLocationPx(0, 0)  // in px


            // enable auto animate bubble to the left/right side when release, true by default
            .enableAnimateToEdge(false)

            // set close-bubble view
            // .closeBubbleView(ViewHelper.fromDrawable(this, com.torrydo.floatingbubbleview.R.drawable.ic_close_bubble, 60, 60))

            // set style for close-bubble, null by default
            // .closeBubbleStyle(null)

            // DYNAMIC_CLOSE_BUBBLE: close-bubble moving based on the bubble's location
            // FIXED_CLOSE_BUBBLE (default): bubble will automatically move to the close-bubble when it reaches the closable-area
            //.closeBehavior(CloseBubbleBehavior.DYNAMIC_CLOSE_BUBBLE)

            // the more value (dp), the larger closeable-area
            .distanceToClose(0)

            .bubbleDraggable(false)

            // enable bottom background, false by default
            //.bottomBackground(true)

            .addFloatingBubbleListener(object : FloatingBubbleListener {
                override fun onFingerMove(
                    x: Float, y: Float
                ) {
                } // The location of the finger on the screen which triggers the movement of the bubble.

                override fun onFingerUp(
                    x: Float, y: Float
                ) {
                }   // ..., when finger release from bubble

                override fun onFingerDown(x: Float, y: Float) {} // ..., when finger tap the bubble
            })
    }

    override fun configExpandedBubble(): ExpandedBubbleBuilder {


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

        Timer().scheduleAtFixedRate(
            CustomTimerTask(
                this,
                callback = { packageName: String -> this.detectRunningApp(packageName) }), 0, 1000
        )

    }

    // Start 10h00 , timer 5m
    // Pause 10h01 , ...
    // Start 10h15 , timer 4m
    private fun detectRunningApp(packageName: String): Unit {
        // Do nothing if popup is open
        if (popupApp.packageName != "") {
            return
        }

        var app = apps[packageName]
        if (app == null) {
            Log.d(LogService, "detected new app ${packageName}")
            app = StartedApp(packageName)
            apps[packageName] = app
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

    private fun openPopup(app: StartedApp) {
        this.popupApp = app

        var popup: Popup = this
        GlobalScope.launch(Dispatchers.Main) {
            val appInfo =
                packageManager.getApplicationInfo(app.packageName, PackageManager.GET_META_DATA)
            popup.popupLabel = packageManager.getApplicationLabel(appInfo).toString()

            popup.expand()
        }
    }

    private fun closePopup() {
        removeAll()
        this.popupApp = StartedApp("")
    }
}

class Dayli(var duration: Long, val day: Int)

class StartedApp(val packageName: String = "") {

    private val cleanExpiredDuration = 5 * 60 // 5 minutes
    private var pausedAt: Long = 0L
    private var pauseDuration: Long = 0L
    private var duration: Long = 0L
    private var startedAt: Long = 0L
    private var createdAt: Long = 0L
    private var dailyDuration: Dayli = Dayli(0, 0)

    fun startTimer(duration: Long): Unit {
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
    }

    fun resume(): Unit {
        pauseDuration = Instant.now().epochSecond - pausedAt
        pausedAt = 0
        this.startedAt = Instant.now().epochSecond
    }

    fun reset(): Unit {
        this.pausedAt = 0
        this.startedAt = 0L
        this.duration = 0L
    }

    fun isZero(): Boolean {
        return this.packageName == ""
    }

    companion object {
        val PreviewTimedoutApp = StartedApp("Facebook")

        init {
            PreviewTimedoutApp.startedAt = 10
            PreviewTimedoutApp.duration = 1
        }
    }
}

class CustomTimerTask(
    private val popup: Popup, private val callback: (packageName: String) -> Unit
) : TimerTask() {

    private val apps = mutableMapOf<String, StartedApp>()

    override fun run() {
        val packageName = this.getLastStartedApp(popup)
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
