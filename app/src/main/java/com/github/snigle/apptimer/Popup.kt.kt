package com.github.snigle.apptimer

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
    private lateinit var preferences : Preference

    private var currentAppInPopup: StartedApp = StartedApp()
    private var previoustRunningApp: StartedApp = StartedApp()

    private val apps = mutableMapOf<String, StartedApp>()


    override fun configBubble(): BubbleBuilder {


        return BubbleBuilder(this)

            // set bubble view

            // or our sweetie, Jetpack Compose
            .bubbleCompose {
                PopupCompose(
                    app = this.currentAppInPopup,
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
            .startLocation(100, 100)    // in dp
            .startLocationPx(100, 100)  // in px


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
            .distanceToClose(100)

            // enable bottom background, false by default
            //.bottomBackground(true)

            .addFloatingBubbleListener(object : FloatingBubbleListener {
                override fun onFingerMove(
                    x: Float,
                    y: Float
                ) {
                } // The location of the finger on the screen which triggers the movement of the bubble.

                override fun onFingerUp(
                    x: Float,
                    y: Float
                ) {
                }   // ..., when finger release from bubble

                override fun onFingerDown(x: Float, y: Float) {} // ..., when finger tap the bubble
            })
    }

    override fun configExpandedBubble(): ExpandedBubbleBuilder {


        return ExpandedBubbleBuilder(this).expandedCompose {
            Greeting("tata")
        }
            // handle key code

            .style(null)
            //
            .fillMaxWidth(true)
            // animate to the left/right side when release, trfalseue by default
            .enableAnimateToEdge(false)
            // set background dimmer
            .dimAmount(0.6f)

    }

    override fun onCreate() {
        super.onCreate()
        preferences = Preference(PreferenceManager.getDefaultSharedPreferences(applicationContext))
        Log.d("test", "restart service")
        enableBubbleDragging(false)

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
        if (currentAppInPopup.packageName != "") {
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
        this.currentAppInPopup = app
        var popup: Popup = this
        GlobalScope.launch(Dispatchers.Main) {
            popup.minimize()
        }
    }

    private fun closePopup() {
        removeAll()
        this.currentAppInPopup = StartedApp()
    }
}

class StartedApp(val packageName: String = "") {

    private val cleanExpiredDuration = 5 * 60 // 4 minutes
    private var pause = false
    private var duration: Long = 0L
    private var startedAt: Long = 0L

    fun startTimer(duration: Long): Unit {
        this.duration = duration
        this.startedAt = Instant.now().epochSecond
    }

    fun haveTimer(): Boolean {
        return this.duration > 0L && this.startedAt > 0L
    }

    fun timedOut(): Boolean {
        return duration > 0 && (Instant.now().epochSecond - duration > startedAt)
    }

    fun expired(): Boolean {
        return duration > 0 && (Instant.now().epochSecond - duration - cleanExpiredDuration > startedAt)
    }

    fun pause(): Unit {
        pause = true
        this.duration = Instant.now().epochSecond - this.startedAt
    }

    fun resume(): Unit {
        pause = false
        this.startedAt = Instant.now().epochSecond
    }

    fun reset(): Unit {
        this.pause = false
        this.startedAt = 0L
        this.duration = 0L
    }
}

class CustomTimerTask(
    private val popup: Popup,
    private val callback: (packageName: String) -> Unit
) :
    TimerTask() {

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
