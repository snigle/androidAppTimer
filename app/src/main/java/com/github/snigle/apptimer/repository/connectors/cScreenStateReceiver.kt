package com.github.snigle.apptimer.repository.connectors

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import com.github.snigle.apptimer.LogService
import com.github.snigle.apptimer.ServicePopup

class ScreenStateReceiver(private val servicePopup: ServicePopup,onScreenOn : ()->Unit) {
    private var screenStateReceiver : ScreenStateReceiverBroadcaster? = null
    public var screenOn = true

    init {
        this.screenStateReceiver = ScreenStateReceiverBroadcaster(
            {
                this.screenOn = true
                onScreenOn()
            },
            {this.screenOn = false}
        )
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        servicePopup.registerReceiver(screenStateReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
        Log.d(LogService, "register screen state receiver")
    }

    fun Destroy() {
            servicePopup.unregisterReceiver(this.screenStateReceiver)
            Log.d(LogService, "register screen state receiver")
    }
}

class ScreenStateReceiverBroadcaster(private val onScreenOn: () -> Unit, private val onScreenOff: () -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_ON -> onScreenOn()
            Intent.ACTION_SCREEN_OFF -> onScreenOff()
        }
    }
}