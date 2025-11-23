@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.snigle.apptimer.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.snigle.apptimer.domain.AppUsage
import com.github.snigle.apptimer.domain.Timer
import com.github.snigle.apptimer.domain.formatDurationInSeconds
import com.github.snigle.apptimer.ui.theme.AppTimerTheme

@Composable
fun PopupCompose(
    modifier: Modifier = Modifier,
    appUsage: AppUsage,
    appLabel: String,
    setTimer: (duration: Long?) -> Unit,
    settingsIntent: () -> Unit,
) {
    // This effect ensures that if the composable is disposed of without a user choice (e.g., navigating away),
    // the timer callback is still triggered to unlock the corresponding use case.
    var choiceMade by remember { mutableStateOf(false) }
    val onSetTimer = { duration: Long? ->
        choiceMade = true
        setTimer(duration)
    }

    DisposableEffect(Unit) {
        onDispose {
            if (!choiceMade) {
                onSetTimer(null) // Assuming null is the "no-action" value
            }
        }
    }

    // Scrim to dim the background
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Only dismiss if the timer has not run out
                if (appUsage.timer != null && !appUsage.timer!!.Timeout()) {
                    onSetTimer(null)
                }
            },
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main content surface, mimicking a bottom sheet
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // Header
                Text(
                    text = appLabel,
                    style = MaterialTheme.typography.headlineSmall
                )

                // Time Information
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val elapsedTime = (appUsage.timer?.ElapseTime() ?: 0) / 1000
                    val totalTime = (appUsage.timer?.duration ?: 0) / 1000
                    InfoColumn(label = "Temps écoulé", value = formatDurationInSeconds(elapsedTime))
                    InfoColumn(label = "Temps total", value = formatDurationInSeconds(totalTime))
                }


                // Conditional content based on timer state
                if (appUsage.timer != null && appUsage.timer!!.Timeout()) {
                    // When time is up
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Temps écoulé !",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { onSetTimer(null) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Quitter $appLabel")
                        }
                    }
                } else {
                    // When there's still time, or no timer is set
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Prolonger de :",
                            style = MaterialTheme.typography.titleMedium
                        )
                        // Time extension buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                        ) {
                            ExtendButton(text = "1 min") { onSetTimer(1 * 60 * 1000) }
                            ExtendButton(text = "5 min") { onSetTimer(5 * 60 * 1000) }
                            ExtendButton(text = "15 min") { onSetTimer(15 * 60 * 1000) }
                            ExtendButton(text = "30 min") { onSetTimer(30 * 60 * 1000) }
                        }
                    }
                }

                // Settings button (only if no timer is set)
                if (!appUsage.HaveTimer()) {
                    TextButton(onClick = {
                        settingsIntent()
                        onSetTimer(null)
                    }) {
                        Text("Configurer les limites pour cette application")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ExtendButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(modifier = modifier, onClick = onClick) {
        Text(text)
    }
}


@Preview(showBackground = true)
@Composable
fun PopupComposePreview_NoTimer() {
    AppTimerTheme {
        PopupCompose(
            appUsage = AppUsage("com.example.app", null),
            appLabel = "Social App",
            setTimer = { },
            settingsIntent = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PopupComposePreview_WithTime() {
    val timer = Timer(30 * 60 * 1000).apply { Start() }
    // Simulate that 5 minutes have passed
    try {
        val field = timer::class.java.getDeclaredField("lastStart")
        field.isAccessible = true
        field.set(timer, System.currentTimeMillis() - 5 * 60 * 1000)
    } catch (e: Exception) {
        // Handle reflection exception in preview
    }

    AppTimerTheme {
        PopupCompose(
            appUsage = AppUsage("com.example.app", timer),
            appLabel = "Social App",
            setTimer = { },
            settingsIntent = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PopupComposePreview_TimeOut() {
    AppTimerTheme {
        PopupCompose(
            appUsage = AppUsage("com.example.app", Timer(0)),
            appLabel = "Social App",
            setTimer = { },
            settingsIntent = { },
        )
    }
}
