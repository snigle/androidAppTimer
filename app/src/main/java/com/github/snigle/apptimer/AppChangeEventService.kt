package com.github.snigle.apptimer

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo

@SuppressLint("AccessibilityPolicy")
class AppChangeEventService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val eventPackageName = event.packageName?.toString()
            if (!event.isFullScreen) {
                Log.d("AppChangeEventService", "ignore not fullscreen app $eventPackageName")
                return
            }

            if (event.className?.startsWith("android.inputmethodservice") == true) {
                Log.d("AppChangeEventService", "ignore keyboard package $eventPackageName ")
                return
            }

            if (eventPackageName ==packageName){
                return
            }
            Log.d("AppChangeEventService", "App changed to: $eventPackageName")

            val intent = Intent(this, ServicePopup::class.java).apply {
                action = ServicePopup.ACTION_APP_CHANGED
                putExtra(ServicePopup.EXTRA_PACKAGE_NAME, eventPackageName)
            }
            startService(intent)
        }
    }

    override fun onInterrupt() {
        Log.w("AppChangeEventService", "Service has been interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AppChangeEventService", "Accessibility service connected")
    }
}
