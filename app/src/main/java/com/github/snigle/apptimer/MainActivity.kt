package com.github.snigle.apptimer

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.github.snigle.apptimer.composable.AppConfigViewModel
import com.github.snigle.apptimer.composable.AppConfigViewModelFactory
import com.github.snigle.apptimer.composable.ApplicationList
import com.github.snigle.apptimer.repository.AppConfigRepo
import com.github.snigle.apptimer.ui.theme.AppTimerTheme


class MainActivity : ComponentActivity() {
    private lateinit var viewModel: AppConfigViewModel
    private var havePermission by mutableStateOf(false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = AppConfigViewModelFactory(
            AppConfigRepo(
                PreferenceManager.getDefaultSharedPreferences(application), packageManager
            )
        )
        viewModel = ViewModelProvider(this, factory)[AppConfigViewModel::class.java]

        this.havePermission = this.checkPermissions()
        if (!this.havePermission) {
            this.askPermissions()
        }


        // Start background service
        startService()

        setContent {
            AppTimerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    if (!this.havePermission) {
                        Column {
                            Text(text = "Waiting for permission")
                            Button(onClick = { askPermissions() }) {
                                Text(text = "Ask for permission")
                            }
                        }
                    } else {
                        ApplicationList(
                            viewModel = viewModel, packageManager
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        this.havePermission = this.checkPermissions()
        startService()
    }

    fun checkPermissions(): Boolean {
        if (!Settings.canDrawOverlays(this)) {
            return false
        }

        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun askPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }

        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName
        )
        if (mode != AppOpsManager.MODE_ALLOWED) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        }

    }

    fun startService() {
        if (this.havePermission) {
            val intent = Intent(this, ServicePopup::class.java)
            ContextCompat.startForegroundService(this, intent)
        }
    }


}



