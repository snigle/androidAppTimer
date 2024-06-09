@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.snigle.apptimer

import android.content.res.Resources.Theme
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
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

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
                    Row (modifier = Modifier.padding(2.dp,5.dp,2.dp,5.dp)){
                        Button(
                            onClick = { close(app) },
                            modifier = Modifier.weight(1F).height(50.dp),
                            elevation = ButtonDefaults.buttonElevation(5.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text(text = "Leave $appLabel ?", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    } else {
                        Text(text = "AppTimer: $appLabel" + "${if(app.hasDailyUsage()) "( temps passé : "+app.formatDailyUsage()+" )" else ""}")
                    }
                    val buttonModifier = Modifier
                        .weight(1F)
                        .padding(2.dp, 0.dp, 2.dp, 0.dp)
                    val rowModifier = Modifier.padding(5.dp,0.dp,5.dp,0.dp)
                    Row (modifier = rowModifier){
                        Button(modifier= buttonModifier, onClick = { setTimer(app, 1 * 60) }) {
                            Text(text = "1 Minute")
                        }
                        Button(modifier= buttonModifier, onClick = { setTimer(app, 5 * 60) }) {
                            Text(text = "5 Minutes")
                        }
                    }
                    Row (modifier = rowModifier){
                        Button(modifier= buttonModifier,onClick = { setTimer(app, 15 * 60) }) {
                            Text(text = "15 Minutes")
                        }
                        Button(modifier= buttonModifier,onClick = { setTimer(app, 30 * 60) }) {
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
fun PopupComposeUsagePreview() {
    return PopupCompose(app = StartedApp.PreviewDailyUsageApp,
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