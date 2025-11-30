package com.github.snigle.apptimer.domain

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

interface IAppConfig {
    fun List(): List<AppConfig>
    fun Find(packageName: String): AppConfig

    fun Save(app: AppConfig)
}

data class AppConfig(val packageName: String, val name: String, var monitor : Boolean, var defaultDuration: Duration = 5.seconds ) {

}