package com.github.snigle.apptimer

import java.time.Duration
import java.time.Instant
import java.time.LocalDate

interface IAppUsage {
    fun Find(packageName: String): AppUsage?
    fun Save(usage: AppUsage)
}

// App describe the time usage of an application.
class AppUsage {

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

        return "${if (hours > 0) "$hours"+"h " else ""}" +
                "${if (minutes > 0) "$minutes"+"m" else ""}"
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
        val PreviewTimedoutAppUsage = AppUsage("Facebook")
        val PreviewDailyUsageAppUsage = AppUsage("Facebook")

        init {
            PreviewTimedoutAppUsage.startedAt = 10
            PreviewTimedoutAppUsage.duration = 1

            PreviewDailyUsageAppUsage.dailyDuration = Daily(60*60, LocalDate.now().dayOfYear)
        }
    }
}