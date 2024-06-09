package com.github.snigle.apptimer

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo

class AppConfigRepo(val preferences: SharedPreferences, val packageManager: PackageManager): IAppConfig{
    override fun List(): List<AppConfig> {

        val list = getAppInfoList()
        return list.map {appInfo: ResolveInfo ->
            val packageName = appInfo.activityInfo.packageName
            var monitoringEnabled = false
            // Set default value
            monitoringEnabled = if (!preferences.contains(preferenceKey(packageName))) {
                getDefaultValue(packageName)
            } else {
                preferences.getBoolean(preferenceKey(packageName), false)
            }
            AppConfig(packageName, appInfo.loadLabel(packageManager).toString(), monitoringEnabled)
        }

    }
    private fun preferenceKey(appName: String): String {
        return "app_time_application_$appName"
    }

    private fun getDefaultValue(packageName: String): Boolean {
        return packageName.contains(Regex("(snapchat|facebook|instagram|tiktok|twitter|chrome.|firefox|game|netflix|youtube)")) ||
                packageName.contains(Regex("(music|message|messenger|launcher|contact)"))
    }

    override fun Find(packageName: String): AppConfig {
        TODO("Not yet implemented")
    }

    override fun Save(app: AppConfig) {
        preferences.edit().putBoolean(preferenceKey(app.packageName), app.monitor).apply()
    }


    fun getAppInfoList(): List<ResolveInfo> {
        // Create an Intent to filter only launchable apps
        val launcherIntent = Intent(Intent.ACTION_MAIN, null)
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        // Retrieve a list of apps that can be launched
        return packageManager.queryIntentActivities(launcherIntent, 0)
    }

    fun getAppList(appList: List<ResolveInfo>): List<String> {
        return appList.map { it.activityInfo.packageName }
    }
    fun getAppList(): List<String> {
        return getAppList(getAppInfoList())
    }

}