package com.github.snigle.apptimer.repository

import android.content.Context
import android.os.PowerManager
import com.github.snigle.apptimer.domain.IScreenManager

class ScreenManager(private val context: Context) : IScreenManager {

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    override fun isOff(): Boolean {
        return !powerManager.isInteractive
    }
}
