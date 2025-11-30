package com.github.snigle.apptimer.repository.connectors

import com.github.snigle.apptimer.domain.AppUsage

class LocalStorage (){

    private val apps = mutableMapOf<String, AppUsage>()


    fun GetApp(packageName: String): AppUsage? {
        return this.apps[packageName]
    }

    fun SaveApp(app: AppUsage) {
        this.apps[app.packageName] = app
    }


}