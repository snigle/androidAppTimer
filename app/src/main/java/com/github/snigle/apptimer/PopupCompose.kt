package com.github.snigle.apptimer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.Instant

@Composable
fun PopupCompose(
    modifier: Modifier = Modifier,
    app: StartedApp,
    setTimer: (app: StartedApp)->Unit,
    settingsIntent: (app: StartedApp)->Unit,
    close: (app: StartedApp)->Unit,
    ) {
    return Surface(
        modifier = Modifier.width(200.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(  modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(text = "Add timer for ${app.packageName}")

                if (app.expired()) {
                    Button(onClick = { close(app) }) {
                        Text(text = "Close")
                    }
                }
                Button(onClick = { setTimer(StartedApp(app.packageName, 10, Instant.now().epochSecond))}) {
                    Text(text = "10 seconds")
                }
                Button(onClick = { setTimer(StartedApp(app.packageName, 5 * 60, Instant.now().epochSecond))}) {
                    Text(text = "5 Minutes")
                }
                Button(onClick = { settingsIntent(app) }) {
                    Text(text = "Settings")
                }
            }
        }
    }
}

@Composable
@Preview
fun PopupComposePreview() {
return PopupCompose(app = StartedApp("test", 0,0), setTimer = { _ -> }, settingsIntent = {_ ->}, close = {})
}

@Composable
@Preview
fun PopupComposeExpiredPreview() {
    return PopupCompose(app = StartedApp("test", 10,0), setTimer = { _ -> }, settingsIntent = {_ ->}, close = {})
}