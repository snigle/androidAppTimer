package com.github.snigle.apptimer

import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.preference.PreferenceManager
import com.github.snigle.apptimer.repository.AppConfigRepo
import com.github.snigle.apptimer.repository.AppUsageRepo
import com.github.snigle.apptimer.repository.ScreenManagerRepo
import com.github.snigle.apptimer.repository.connectors.LocalStorage
import com.github.snigle.apptimer.repository.connectors.ScreenStateReceiver
import com.github.snigle.apptimer.usecase.AppMonitoring
import com.torrydo.floatingbubbleview.CloseBubbleBehavior
import com.torrydo.floatingbubbleview.FloatingBubbleListener
import com.torrydo.floatingbubbleview.service.expandable.BubbleBuilder
import com.torrydo.floatingbubbleview.service.expandable.ExpandableBubbleService
import com.torrydo.floatingbubbleview.service.expandable.ExpandedBubbleBuilder

val LogService = "apptimer.popup"

class ServicePopup : ExpandableBubbleService() {

    var timerComponent: @Composable () -> Unit = {}
    var timerSettingComponent: @Composable () -> Unit = {}

    private val screenStateReceiver = ScreenStateReceiver(this)
    private val localStorage = LocalStorage()
    private lateinit var configRepo : AppConfigRepo
    private lateinit var usecase : AppMonitoring

    override fun configBubble(): BubbleBuilder {
        // return BubbleBuilder(this).bubbleCompose { Text(text = "coucou") }
        return BubbleBuilder(this)

            // set bubble view

            // or our sweetie, Jetpack Compose
            .bubbleCompose {
                timerComponent()
            }


            // set style for the bubble, fade animation by default
            .bubbleStyle(null)

            // set start location for the bubble, (x=0, y=0) is the top-left
            .startLocation(0, 0)    // in dp
            .startLocationPx(0, 0)  // in px

            // enable auto animate bubble to the left/right side when release, true by default
            .enableAnimateToEdge(true)

            // set close-bubble view
//            .closeBubbleView(ViewHelper.fromDrawable(this, R.drawable.ic_close_bubble, 60, 60))

            // set style for close-bubble, null by default
            .closeBubbleStyle(null)

            // DYNAMIC_CLOSE_BUBBLE: close-bubble moving based on the bubble's location
            // FIXED_CLOSE_BUBBLE (default): bubble will automatically move to the close-bubble when it reaches the closable-area
            .closeBehavior(CloseBubbleBehavior.DYNAMIC_CLOSE_BUBBLE)

            // the more value (dp), the larger closeable-area
            .distanceToClose(100)

            // enable bottom background, false by default
            .bottomBackground(true)

            .addFloatingBubbleListener(object : FloatingBubbleListener {
                override fun onFingerMove(x: Float, y: Float) {} // The location of the finger on the screen which triggers the movement of the bubble.
                override fun onFingerUp(x: Float, y: Float) {}   // ..., when finger release from bubble
                override fun onFingerDown(x: Float, y: Float) {} // ..., when finger tap the bubble
            })

            // set the clickable perimeter of the bubble in pixels (default = 5f)
//            .triggerClickablePerimeterPx(5f)
    }


    override fun configExpandedBubble(): ExpandedBubbleBuilder {


        return ExpandedBubbleBuilder(this).expandedCompose {
            timerSettingComponent()
        }

            // handle key code

            .onDispatchKeyEvent {
                if (it.keyCode == KeyEvent.KEYCODE_BACK) {
                    minimize()
                }
                null
            }
            .draggable(true)
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

        configRepo = AppConfigRepo(
            PreferenceManager.getDefaultSharedPreferences(applicationContext),
            applicationContext.packageManager
        )
        usecase = AppMonitoring(
                AppUsageRepo(servicePopup = this@ServicePopup, this@ServicePopup.localStorage, configRepo),
        ScreenManagerRepo(
            this@ServicePopup.screenStateReceiver),
        )

        usecase.MonitorRunningApp()
        screenStateReceiver.RegisterReceiver { usecase.MonitorRunningApp() }

    }


    override fun onDestroy() {
        super.onDestroy()
        screenStateReceiver.Destroy()
    }


}
