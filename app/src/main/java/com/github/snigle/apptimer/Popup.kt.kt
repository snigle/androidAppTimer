package com.github.snigle.apptimer

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.torrydo.floatingbubbleview.FloatingBubbleListener
import com.torrydo.floatingbubbleview.service.expandable.BubbleBuilder
import com.torrydo.floatingbubbleview.service.expandable.ExpandableBubbleService
import com.torrydo.floatingbubbleview.service.expandable.ExpandedBubbleBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class Popup: ExpandableBubbleService() {

    override fun configBubble(): BubbleBuilder? {


        return BubbleBuilder(this)

            // set bubble view

            // or our sweetie, Jetpack Compose
            .bubbleCompose {
                Surface(
                    modifier = Modifier.width(30.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                    Button(onClick = { removeAll()}) {
                        Text(text = "Close")
                    }
                }
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
                override fun onFingerMove(x: Float, y: Float) {} // The location of the finger on the screen which triggers the movement of the bubble.
                override fun onFingerUp(x: Float, y: Float) {}   // ..., when finger release from bubble
                override fun onFingerDown(x: Float, y: Float) {} // ..., when finger tap the bubble
            })
    }

    override fun configExpandedBubble(): ExpandedBubbleBuilder? {


        return ExpandedBubbleBuilder(this)
            .expandedCompose {
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

        Timer().scheduleAtFixedRate(CustomTimerTask(this),0,1000)

    }

    private val timerTask: TimerTask = object : TimerTask() {
        override fun run() {
            Log.d("test", "timerrrrr : ")
            //GlobalScope.launch(Dispatchers.Main) {
            //            popup.minimize()
            //        }
            //expand()

        }
    }



    }

class StartedApp(val packageName: String, duration : Long, startedAt: Long) {
    // StartTimer

    // HaveTimer
}

class CustomTimerTask(private val popup: Popup) : TimerTask() {

    private val apps = mutableMapOf<String,StartedApp>()

    override fun run() {
        val lastApp = this.getLastStartedApp(popup)
        if (!apps.containsKey(lastApp.packageName)) {
            Log.d("YourService", "TimerTask is running: " + lastApp.packageName)
            apps[lastApp.packageName] = lastApp
        }
        GlobalScope.launch(Dispatchers.Main) {
            //popup.minimize()
        }
        // Here, you can use the context to perform any operations
        // that require the context within the TimerTask
    }

    private fun getForegroundApp(context: Context): String {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val currentTime = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            currentTime - 1000 * 10,  // Check for last 10 seconds
            currentTime
        )

        stats?.let {
            if (it.isNotEmpty()) {
                return it[0].packageName
            }
        }

        return "" // No foreground app found
    }

    fun getLastStartedApp(context: Context): StartedApp {
        val currentTimestamp = System.currentTimeMillis()
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        // Set the time window for querying usage statistics
        val oneHourAgo = currentTimestamp - (60 * 1000) // 1m ago
        val appList: Map<String, UsageStats> = usageStatsManager.queryAndAggregateUsageStats(oneHourAgo, currentTimestamp)

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
