package com.github.snigle.apptimer

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.github.snigle.apptimer.ui.theme.AppTimerTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState

class MainActivity : ComponentActivity() {
    private val viewModel: MyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (!this.checkPermissions()) {
            return
        }

        // Get a list of all installed packages
        val packageManager: PackageManager = packageManager

        // Create an Intent to filter only launchable apps
        val launcherIntent = Intent(Intent.ACTION_MAIN, null)
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        // Retrieve a list of apps that can be launched
        val appsList = packageManager.queryIntentActivities(launcherIntent, 0)

        // Filter and process the list of launchable apps
        for (appInfo in appsList) {
            val appName = appInfo.activityInfo.packageName
            // Process each appName as needed
            android.util.Log.d("mainActivity", "Installed Package: $appName")
        }



        val intent = Intent(this, Popup::class.java)
        ContextCompat.startForegroundService(this, intent)


        /*WorkManager
            .getInstance(this)
            .enqueue(uploadWorkRequest)*/
        setContent {
            AppTimerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .width(12.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyUI(viewModel = viewModel)
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        if (!this.checkPermissions()) {
            return
        }

    }

    fun checkPermissions(): Boolean {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
            return false
        }

        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        if (mode != AppOpsManager.MODE_ALLOWED) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
            return false
        }

        return true
    }



}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyUI(viewModel: MyViewModel) {
    val userData : String by viewModel.userData.observeAsState("")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Composable components using userData state
        Text(text = "User Data: $userData")

        // Input field to update user data
        TextField(
            value = userData,
            onValueChange = { newValue: String ->
                viewModel.saveUserData(newValue)
            }
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppTimerTheme {
        Greeting("Android")
    }
}