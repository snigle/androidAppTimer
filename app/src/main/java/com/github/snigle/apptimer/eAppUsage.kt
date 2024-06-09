package com.github.snigle.apptimer

import java.time.Duration
import java.time.Instant
import java.time.LocalDate

interface IAppUsage {
    fun FindRunning(): AppUsage
    fun StartTimer(app: AppUsage, duration: Long)
    fun PauseTimer(app: AppUsage)
    fun ExtendTimer(app: AppUsage, duration: Long)
    fun Close(app: AppUsage)
    fun AskDuration(appConfig: AppConfig, app: AppUsage, callback: (duration: Long) -> Unit)
    fun AskTerminate(appConfig: AppConfig, app: AppUsage, callback: (duration: Long) -> Unit)
    fun ResumeTimer(runningApp: AppUsage)
}

data class AppUsage(val packageName: String, var timer: Timer?) {

    fun IsZero(): Boolean {
        return packageName == ""
    }

    fun HaveTimer(): Boolean {
        return timer != null
    }


}

data class Timer(var duration: Long) {
    private var lastStart: Long = 0
    private var aggregatedDuration: Long = 0

    fun GetDuration(): Long {
        val now = System.currentTimeMillis()
        if (lastStart != 0L) return now - lastStart + aggregatedDuration
        return aggregatedDuration
    }

    fun Timeout(): Boolean {
        return GetDuration() > duration
    }

    fun Extends(newDuration: Long) {
        duration += duration
        Start()
    }

    fun Pause() {
        val now = System.currentTimeMillis()
        aggregatedDuration += now - lastStart
        lastStart = 0
    }

    fun Start() {
        val now = System.currentTimeMillis()
        if (lastStart != 0L) {
            aggregatedDuration += now - lastStart
        }
        lastStart = now
    }

    fun IsPaused(): Boolean {
        return lastStart == 0L
    }

}

// App describe the time usage of an application.
class AppUsageOld {

    val packageName: String
    private val cleanExpiredDuration = 5 * 60 // 5 minutes
    private var pausedAt: Long
    private var pauseDuration: Long
    private var duration: Long
    private var startedAt: Long
    private var createdAt: Long
    private var dailyDuration: Daily

    constructor(packageName: String = "") {
        this.packageName = packageName

        // Copy paste from reset func
        this.pausedAt = 0L
        this.pauseDuration = 0L
        this.duration = 0L
        this.startedAt = 0L
        this.createdAt = 0L
        this.dailyDuration = Daily(0, 0)
    }

    fun reset(): Unit {
        if (this.timedOut()) {
            this.registerDailyUsage()
        }
        this.pausedAt = 0L
        this.pauseDuration = 0L
        this.duration = 0L
        this.startedAt = 0L
        this.createdAt = 0L
    }

    fun registerDailyUsage(): Unit {
        if (!this.hasDailyUsage()) {
            this.dailyDuration = Daily(0, LocalDate.now().dayOfYear)
        }
        this.dailyDuration.addDuration(this.duration)
    }

    fun hasDailyUsage(): Boolean {
        return this.dailyDuration.day == LocalDate.now().dayOfYear && this.dailyDuration.getDuration() > 0
    }

    fun formatDailyUsage(): String {
        val duration = Duration.ofSeconds(this.dailyDuration.getDuration())
        val hours = duration.toHours()
        val minutes = duration.minusHours(hours).toMinutes()

        return "${if (hours > 0) "$hours" + "h " else ""}" + "${if (minutes > 0) "$minutes" + "m" else ""}"
    }

    fun startTimer(duration: Long): Unit {
        this.reset()
        this.duration = duration
        this.startedAt = Instant.now().epochSecond
        this.createdAt = Instant.now().epochSecond
    }

    fun haveTimer(): Boolean {
        return this.duration > 0L && this.startedAt > 0L
    }

    fun timedOut(): Boolean {
        return duration > 0 && (Instant.now().epochSecond - duration > startedAt)
    }

    fun expired(): Boolean {
        return duration > 0 && ((Instant.now().epochSecond - duration - cleanExpiredDuration > createdAt) || (pauseDuration > cleanExpiredDuration))
    }

    fun pause(): Unit {
        pausedAt = Instant.now().epochSecond
        this.duration = Instant.now().epochSecond - this.startedAt
        this.registerDailyUsage()
    }

    fun resume(): Unit {
        pauseDuration = Instant.now().epochSecond - pausedAt
        pausedAt = 0
        this.startedAt = Instant.now().epochSecond
    }


    fun isZero(): Boolean {
        return this.packageName == ""
    }

    companion object {
        val PreviewTimedoutAppUsageOld = AppUsageOld("Facebook")
        val PreviewDailyUsageAppUsageOld = AppUsageOld("Facebook")

        init {
            PreviewTimedoutAppUsageOld.startedAt = 10
            PreviewTimedoutAppUsageOld.duration = 1

            PreviewDailyUsageAppUsageOld.dailyDuration = Daily(60 * 60, LocalDate.now().dayOfYear)
        }
    }
}