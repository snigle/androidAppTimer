package com.github.snigle.apptimer.repository

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.github.snigle.apptimer.domain.AppConfig
import com.github.snigle.apptimer.domain.IAppConfig
import kotlinx.coroutines.CoroutineScope

class AppConfigRepo(val preferences: SharedPreferences, val packageManager: PackageManager):
    IAppConfig {
    override fun List(): List<AppConfig> {

        val list = getAppInfoList()
        return list.map {appInfo: ResolveInfo ->
            getConfig(appInfo.activityInfo.packageName, appInfo.loadLabel(packageManager).toString())
        }

    }
    private fun preferenceKey(appName: String): String {
        return "app_time_application_$appName"
    }

    private fun getDefaultValue(packageName: String): Boolean {
        return packageName.contains(Regex("(snapchat|facebook|instagram|tiktok|twitter|chrome.|firefox|game|netflix|youtube|googlequicksearch)")) &&
                !packageName.contains(Regex("(music|message|messenger|launcher|contact|appTimer)"))
    }

    override fun Find(packageName: String): AppConfig {
        val appInfo =
            packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)

        return getConfig(appInfo.packageName, appInfo.loadLabel(packageManager).toString())
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

    fun getConfig(packageName: String, name: String): AppConfig {

        var monitoringEnabled = if (!preferences.contains(preferenceKey(packageName))) {
            getDefaultValue(packageName)
        } else {
            preferences.getBoolean(preferenceKey(packageName), false)
        }

        // Secure app timer app
        if (packageName.contains("com.github.snigle.apptimer")) {
            monitoringEnabled = false
        }

        return AppConfig(packageName, name, monitoringEnabled)
    }

    fun getAppList(appList: List<ResolveInfo>): List<String> {
        return appList.map { it.activityInfo.packageName }
    }
    fun getAppList(): List<String> {
        return getAppList(getAppInfoList())
    }

}