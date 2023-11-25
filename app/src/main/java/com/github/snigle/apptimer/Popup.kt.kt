package com.github.snigle.apptimer

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import com.torrydo.floatingbubbleview.FloatingBubbleListener
import com.torrydo.floatingbubbleview.service.expandable.BubbleBuilder
import com.torrydo.floatingbubbleview.service.expandable.ExpandableBubbleService
import com.torrydo.floatingbubbleview.service.expandable.ExpandedBubbleBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class Popup : ExpandableBubbleService() {

    private var currentAppInPopup: StartedApp = StartedApp("empty", 0, 0)
    private val apps = mutableMapOf<String, StartedApp>()


    override fun configBubble(): BubbleBuilder {


        return BubbleBuilder(this)

            // set bubble view

            // or our sweetie, Jetpack Compose
            .bubbleCompose {
                PopupCompose(
                    app = this.currentAppInPopup,
                    setTimer = { app: StartedApp -> this.setTimer(app)},
                    settingsIntent = {app : StartedApp ->

                        GlobalScope.launch(Dispatchers.Main) {
                            removeAll()
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                            startActivity(intent)
                        }

                        apps.remove(app.packageName)
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
        Log.d("test", "restart service")
        enableBubbleDragging(false)

        Timer().scheduleAtFixedRate(
            CustomTimerTask(
                this,
                callback = { app: StartedApp -> this.detectApp(app) }), 0, 1000
        )

    }

    private fun detectApp(lastApp: StartedApp): Unit {
        // check if app is watached in user settings
        if (lastApp.packageName == "com.github.snigle.apptimer" || lastApp.packageName.contains("launcher")) {
            return
        }
        if (!apps.containsKey(lastApp.packageName)) {
            Log.d("YourService", "detected app ${lastApp.packageName}")
            apps[lastApp.packageName] = lastApp
            this.askTimer(lastApp)
        }
    }

    private fun askTimer(app: StartedApp) {
        this.currentAppInPopup = app
        //this.minimize()
        var popup : Popup = this
        GlobalScope.launch(Dispatchers.Main) {
        popup.minimize()
        }
    }

    private fun setTimer(app: StartedApp) {
        apps[app.packageName] = app
        this.removeAll()
    }

}

class StartedApp(val packageName: String, duration: Long, startedAt: Long) {
    // StartTimer

    // HaveTimer
}

class CustomTimerTask(private val popup: Popup, private val callback: (app: StartedApp) -> Unit) :
    TimerTask() {

    private val apps = mutableMapOf<String, StartedApp>()

    override fun run() {
        this.callback(this.getLastStartedApp(popup))

        //GlobalScope.launch(Dispatchers.Main) {
        //popup.minimize()
        //}
        // Here, you can use the context to perform any operations
        // that require the context within the TimerTask
    }


    fun getLastStartedApp(context: Context): StartedApp {
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

        return StartedApp(lastUsedApp, 0, lastTimeStamp)
    }
}
