package com.github.snigle.apptimer

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

        Timer().scheduleAtFixedRate(CustomTimerTask(this),0,5000)

    }

    /*private val timerTask: TimerTask = object : TimerTask(private service ::Popup) {
        override fun run() {
            Log.d("test", "timerrrrr")
            minimize()
            //expand()
        }
    }*/

    }

class CustomTimerTask(private val popup: Popup) : TimerTask() {
    override fun run() {
        Log.d("YourService", "TimerTask is running")
        GlobalScope.launch(Dispatchers.Main) {
            popup.minimize()
        }
        // Here, you can use the context to perform any operations
        // that require the context within the TimerTask
    }
}
