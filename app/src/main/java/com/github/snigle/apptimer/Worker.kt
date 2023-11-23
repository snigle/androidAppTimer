package com.github.snigle.apptimer

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask

class CoroutineDownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private lateinit var popupWindow: PopupWindow


    private val timerTask: TimerTask = object : TimerTask() {
        override fun run() {
            Log.d("test", "timerrrrr")

            val intent = Intent(context, Popup::class.java)
            ContextCompat.startForegroundService(context, intent)
        }
    }

    override suspend fun doWork(): Result {
        val context = applicationContext

        withContext(Dispatchers.Main) {

        }
        Timer().scheduleAtFixedRate(timerTask,0,5000)
        return Result.success()
    }


}

val uploadWorkRequest: WorkRequest =
    OneTimeWorkRequestBuilder<CoroutineDownloadWorker>()
        .build()


