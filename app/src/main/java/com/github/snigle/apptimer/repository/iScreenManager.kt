package com.github.snigle.apptimer.repository

import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.github.snigle.apptimer.repository.connectors.ScreenStateReceiver
import com.github.snigle.apptimer.ServicePopup
import com.github.snigle.apptimer.domain.IScreenManager

class ScreenManagerRepo(private val screenStateReceiver: ScreenStateReceiver) : IScreenManager {

    override fun IsDisabled(): Boolean {
        return !this.screenStateReceiver.screenOn
    }

}
