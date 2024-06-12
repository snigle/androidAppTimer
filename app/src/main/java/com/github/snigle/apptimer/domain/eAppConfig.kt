package com.github.snigle.apptimer.domain

interface IAppConfig {
    fun List(): List<AppConfig>
    fun Find(packageName: String): AppConfig

    fun Save(app: AppConfig)
}
data class AppConfig(val packageName: String, val name: String, var monitor : Boolean) {

}