package com.github.snigle.apptimer.composable

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.snigle.apptimer.domain.AppConfig
import kotlin.time.Duration.Companion.minutes


@Composable
fun ApplicationList(
    viewModel: AppConfigViewModel, packageManager: PackageManager
) {
    val appsWithIcons by viewModel.apps.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.loadApps(packageManager)
    }

    LazyColumn {
        itemsIndexed(appsWithIcons) { index, appWithIcon ->
            Application(
                applicationName = appWithIcon.appConfig.name,
                packageName = appWithIcon.appConfig.packageName,
                appIcon = appWithIcon.icon,
                checked = appWithIcon.appConfig.monitor,
                onToggle = { _, checked -> viewModel.updateAppStatus(index, checked) })
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
    Row(modifier.height(64.dp)) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            if (appIcon != null) {
                Image(
                    bitmap = appIcon,
                    contentDescription = "icon",
                    modifier = Modifier.size(48.dp)
                )
            } else {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            Modifier
                .weight(1F)
                .align(Alignment.CenterVertically)
        ) {
            Text(text = applicationName, style = MaterialTheme.typography.bodyLarge)
            Text(text = packageName, style = MaterialTheme.typography.bodySmall)
        }
        Column(Modifier.align(Alignment.CenterVertically)) {
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
        val context = LocalContext.current
        val dummyAppConfig = AppConfig("com.android.chrome", "Chrome", true, 5.minutes)
        val appWithIcon = AppConfigWithIcon(dummyAppConfig, null)

        Column {
            Application(
                applicationName = appWithIcon.appConfig.name,
                packageName = appWithIcon.appConfig.packageName,
                appIcon = appWithIcon.icon,
                checked = appWithIcon.appConfig.monitor,
                onToggle = { _, _ -> })
        }

    }
}