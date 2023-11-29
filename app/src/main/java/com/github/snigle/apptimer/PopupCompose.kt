@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.snigle.apptimer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopupCompose(
    modifier: Modifier = Modifier,
    app: StartedApp,
    appLabel: String,
    setTimer: (app: StartedApp, duration: Long) -> Unit,
    settingsIntent: (app: StartedApp) -> Unit,
    close: (app: StartedApp) -> Unit,
) {


    return    Column(
        modifier = Modifier
//            .fillMaxSize()
//            .fillMaxWidth()
            .width(LocalConfiguration.current.screenWidthDp.dp + 100.dp)
//            .background(Color(0,0,0,30)),
            .background(Color.Transparent)
    ) {

        Spacer(modifier = Modifier.weight(1f, true ))

        Box(
modifier=Modifier.background(Color(210,210,210), RoundedCornerShape(16.dp)),

        ) {

//
//        ModalBottomSheet(
//            dragHandle = {},
//            onDismissRequest = {  },
//            //windowInsets= WindowInsets.Companion.Bo
//            sheetState = SheetState(true, confirmValueChange = {_ -> false},skipHiddenState = true, initialValue = SheetValue.Expanded),
//        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    if (app.timedOut()) {

                        Button(onClick = { close(app) }) {
                            Text(text = "Leave $appLabel ?")
                        }
                    } else {
                        Text(text = "How long time do you want to stay on $appLabel ?")
                    }
                    Row {
                        Button(onClick = { setTimer(app, 10) }) {
                            Text(text = "1 Minute")
                        }
                        Button(onClick = { setTimer(app, 10 * 60) }) {
                            Text(text = "5 Minutes")
                        }
                    }
                    Row {
                        Button(onClick = { setTimer(app, 15 * 60) }) {
                            Text(text = "15 Minutes")
                        }
                        Button(onClick = { setTimer(app, 30 * 60) }) {
                            Text(text = "30 Minutes")
                        }
                    }

                    if (!app.timedOut()) {
                        Row {

                            Button(onClick = { settingsIntent(app) }) {
                                Text(text = "Settings")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun PopupComposePreview() {
    return PopupCompose(app = StartedApp("test"),
        appLabel = "Facebook",
        setTimer = { _, _ -> },
        settingsIntent = { _ -> },
        close = {})
}

@Composable
@Preview
fun PopupComposeExpiredPreview() {
    return PopupCompose(app = StartedApp.PreviewTimedoutApp,
        appLabel = "Facebook",
        setTimer = { _, _ -> },
        settingsIntent = { _ -> },
        close = {})
}