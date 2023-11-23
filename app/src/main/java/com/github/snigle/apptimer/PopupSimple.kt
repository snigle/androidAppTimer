package com.github.snigle.apptimer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.Settings
import android.view.LayoutInflater
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class PopupSimple: Service() {


    private fun startNotificationForeground() {
        //val noti = NotificationHelper(this)
        //noti.createNotificationChannel()
        // startForeground(noti.notificationId, noti.defaultNotification())

        // TODO: ask notification permission
            val channel = NotificationChannel(
                "apptimer_service",
                "apptimer",
                NotificationManager.IMPORTANCE_DEFAULT   // IMPORTANCE_NONE recreate the notification if update
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
            NotificationManagerCompat.from(this).createNotificationChannel(channel)

        startForeground(102, NotificationCompat.Builder(this, "apptimer_service")
            .setOngoing(true)
            //.setSmallIcon(R.drawable.ic_rounded_blue_diamond)
            .setContentTitle("bubble is running")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setSilent(true)
            .build())

    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        if (!Settings.canDrawOverlays(this)) {
            throw SecurityException("Permission Denied: \"display over other app\" permission IS NOT granted!")
        }


        startNotificationForeground()
        val root = LayoutInflater.from(this).inflate(R.layout.popup, null)


    }


}