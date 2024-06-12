package com.github.snigle.apptimer.composable

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmapOrNull


@Composable
fun ApplicationList(
    viewModel: AppConfigViewModel, packageManager: PackageManager
) {
    val apps by viewModel.apps.observeAsState(emptyList())

    LazyColumn {

        itemsIndexed(apps) { index, application ->
            val appIcon = packageManager.getApplicationIcon(application.packageName)
            Application(applicationName = application.name,
                packageName = application.packageName,
                appIcon = appIcon.toBitmapOrNull()?.asImageBitmap(),
                checked = application.monitor,
                onToggle = { packageName, checked -> viewModel.updateAppStatus(index, checked) })


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
                Image(appIcon, contentDescription = "icon", modifier = Modifier.width(30.dp))
            }
        }
        Column(Modifier.weight(1F)) {
            Text(text = applicationName)
            Text(text = packageName)
        }
        Column {
            Switch(checked = checked,
                onCheckedChange = { enabled -> onToggle(packageName, enabled) })
        }
    }

}

@Composable
@Preview
fun ApplicationPreview() {
    return Surface(
        modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
    ) {
        Column {

            Application(applicationName = "Chrome",
                packageName = "google.chrome",
                null,
                checked = true,
                onToggle = { _, _ -> })
            Application(applicationName = "Chrome",
                packageName = "google.chrome",
                null,
                checked = true,
                onToggle = { _, _ -> })
        }

    }
}
