package com.github.snigle.apptimer.repository.connectors

import com.github.snigle.apptimer.domain.AppUsage
import java.util.concurrent.ConcurrentHashMap

class LocalStorage (){

    private val apps = ConcurrentHashMap<String, AppUsage>()

    fun GetList(): Collection<AppUsage> {
        return this.apps.values
    }

    fun GetApp(packageName: String): AppUsage? {
        return this.apps[packageName]
    }

    fun SaveApp(app: AppUsage) {
        this.apps[app.packageName] = app
    }


}