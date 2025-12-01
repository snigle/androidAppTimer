package com.github.snigle.apptimer.domain

import android.util.Log
import com.github.snigle.apptimer.LogService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


enum class TimerStatus {
    Created, Running, Paused
}

data class Timer(var duration: Long) {
    private var lastStart: Long = 0
    private var status = TimerStatus.Created
    private var aggregatedDuration: Long = 0
    private var logs: MutableList<String> = mutableListOf()

    fun ElapseTime(): Long {
        return duration - GetTimeLeft()
    }

    fun GetTimeLeft(): Long {
        val now = System.currentTimeMillis()
        var timeLeft = duration - aggregatedDuration
        if (status == TimerStatus.Paused) {
            return timeLeft
        }
        if (lastStart > 0) {
            timeLeft = timeLeft - now + lastStart
        }
        return timeLeft
    }

    fun GetAggregateDuration(): Long {
        return aggregatedDuration
    }

    fun Timeout(): Boolean {
        return GetTimeLeft() <= 0
    }

    fun Expired(): Boolean {
        val now = System.currentTimeMillis()
        return status == TimerStatus.Paused && now - lastStart > 15 * 60 * 1000
    }

    fun Extends(newDuration: Long) {
        if (status != TimerStatus.Paused) {
            return
        }
        duration += newDuration
        Start()
        log("Extends")
    }

    fun Pause() {
        // Already paused
        if (status != TimerStatus.Running) {
            return
        }
        val now = System.currentTimeMillis()
        aggregatedDuration += now - lastStart
        status = TimerStatus.Paused
        log("Pause")
    }

    fun Start() {
        // Already starter
        if (status == TimerStatus.Running) {
            return
        }
        val now = System.currentTimeMillis()

        lastStart = now
        status = TimerStatus.Running
        log("Start")
    }

    fun IsPaused(): Boolean {
        return status == TimerStatus.Paused
    }

    private fun log(text: String) {
        val now = System.currentTimeMillis()
        val message =
            "${formatTime(now)} ${text} (${formatTime(lastStart)}) (${formatDuration(duration)}) (${
                formatDuration(aggregatedDuration)
            })"
        logs.add(message)
        Log.d(LogService, message)
    }

    private fun formatTime(time: Long): String {
        // Create a Date object from the current time in milliseconds
        val currentDate = Date(time)

        // Define the date format you want to use
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        // Format the date to the desired format
        return dateFormat.format(currentDate)
    }

    private fun formatDuration(time: Long): String {
        val minute = time / 1000 / 60
        if (minute != 0L) {
            return "${minute}m"
        }
        return "${time / 1000}s"
    }

    fun IsRunning(): Boolean {
        return status == TimerStatus.Running
    }

}