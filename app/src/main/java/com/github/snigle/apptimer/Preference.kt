package com.github.snigle.apptimer

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo


class Preference(val preferences: SharedPreferences) {

    private fun preferenceKey(appName: String): String {
        return "app_time_application_$appName"
    }

    fun load(appList: List<String>): Map<String, Boolean> {
        val result: MutableMap<String, Boolean> = mutableMapOf()
        appList.forEach { name: String ->
            result[name] = get(name)
        }
        return result
    }

    fun save(app: String, value: Boolean): Unit {
        preferences.edit().putBoolean(preferenceKey(app), value).apply()
    }

    fun get(app: String): Boolean {
        // TODO: handle default values if no preference set
        return preferences.getBoolean(preferenceKey(app), false)
    }

    companion object {
        fun getAppInfoList(packageManager: PackageManager): List<ResolveInfo> {
            // Create an Intent to filter only launchable apps
            val launcherIntent = Intent(Intent.ACTION_MAIN, null)
            launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER)

            // Retrieve a list of apps that can be launched
            return packageManager.queryIntentActivities(launcherIntent, 0)
        }

        fun getAppList(appList: List<ResolveInfo>): List<String> {
            return appList.map { it.activityInfo.packageName }
        }
        fun getAppList(packageManager: PackageManager): List<String> {
            return getAppList(getAppInfoList(packageManager))
        }

    }

}