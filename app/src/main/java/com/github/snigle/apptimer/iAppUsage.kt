package com.github.snigle.apptimer

class AppUsageRepo : IAppUsage {

    // Local storage
    private val apps = mutableMapOf<String, AppUsage>()

    override fun Find(packageName: String): AppUsage? {
        return apps[packageName]
    }

    override fun Save(usage: AppUsage) {
        apps[usage.packageName] = usage
    }

}