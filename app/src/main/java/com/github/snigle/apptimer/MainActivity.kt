package com.github.snigle.apptimer

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmapOrNull
import com.github.snigle.apptimer.ui.theme.AppTimerTheme


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
        viewModel.load(appsList.map { it.activityInfo.name })

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
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ApplicationList(
                        applications = appsList.map { appInfo ->
                            var name = "NoName"
                            try {
                                name = appInfo.loadLabel(packageManager).toString()
                            } catch (e: Exception) {}
                            Application(
                                iconPackage = appInfo.activityInfo.packageName,
                                label = name,
                                packageName = appInfo.activityInfo.name
                            )
                        },
                        viewModel = viewModel,
                        packageManager
                    )
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

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MyUI(viewModel: MyViewModel) {
//    val userData: String by viewModel.mapApplication.observeAsState("")
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        // Composable components using userData state
//        Text(text = "User Data: $userData")
//
//        // Input field to update user data
//        TextField(
//            value = userData,
//            onValueChange = { newValue: String ->
//                viewModel.saveUserData(newValue)
//            }
//        )
//    }
//}

class Application(val label: String,val packageName: String, val iconPackage: String)

@Composable
fun ApplicationList(
    applications: List<Application>,
    viewModel: MyViewModel,
    packageManager: PackageManager
) {
    LazyColumn {

        items(applications) { application ->
            val appIcon = packageManager.getApplicationIcon(application.iconPackage)
            val checked : Boolean? by viewModel.getValueByKey(application.packageName).collectAsState(null)
            Application(
                applicationName = application.label,
                packageName = application.packageName,
                appIcon = appIcon.toBitmapOrNull()?.asImageBitmap(),
                checked = checked == true,
                onToggle = { packageName, checked -> viewModel.updateMap(packageName, checked) })


        }
    }
}

@Composable
fun Application(
    applicationName: String,
    packageName: String,
    appIcon: ImageBitmap?,
    checked: Boolean,
    onToggle: (packageName: String, enabled: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier) {
        Column {
            if (appIcon != null) {
                Image(appIcon, contentDescription = "icon")
            }
        }
        Column(Modifier.weight(1F)) {
            Text(text = applicationName)
            Text(text = packageName)
        }
        Column {
            Switch(
                checked = checked,
                onCheckedChange = { enabled -> onToggle(packageName, enabled) })
        }
    }

}

@Composable
@Preview
fun ApplicationPreview() {
    return Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {

            Application(
                applicationName = "Chrome",
                packageName = "google.chrome",
                null,
                checked = true,
                onToggle = { _, _ -> })
            Application(
                applicationName = "Chrome",
                packageName = "google.chrome",
                null,
                checked = true,
                onToggle = { _, _ -> })
        }

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