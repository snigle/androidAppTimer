@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.snigle.apptimer.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.snigle.apptimer.domain.Timer
import com.github.snigle.apptimer.ui.theme.AppTimerTheme
import kotlinx.coroutines.delay

@Composable
fun TimerCompose(
    modifier: Modifier = Modifier, timer: Timer,
    onClick: () -> Unit,
) {

    var timeLeft by remember { mutableStateOf((timer.GetTimeLeft() + 999) / 1000) }
    var elapsedTime by remember { mutableStateOf(timer.ElapseTime() / 1000) }
    val totalDuration = remember { timer.duration / 1000 }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft = (timer.GetTimeLeft() + 999) / 1000
            elapsedTime = timer.ElapseTime() / 1000
        }
    }

    val progress = if (totalDuration > 0) {
        (timeLeft / totalDuration.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }


    Box(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        CircularCountdown(
            progress = progress,
            elapsedTime = elapsedTime,
            totalDuration = totalDuration
        )
    }
}

@Composable
fun CircularCountdown(
    progress: Float,
    elapsedTime: Long,
    totalDuration: Long,
    modifier: Modifier = Modifier
) {
    val formattedElapsedTime = formatDurationInSeconds(elapsedTime)
    val formattedTotalDuration = formatDurationInSeconds(totalDuration)
    val strokeWidth = 6.dp

    // Hoist colors outside of the Canvas draw scope
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Track
            drawCircle(
                color = trackColor,
                style = Stroke(width = strokeWidth.toPx())
            )
            // Progress
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360 * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formattedElapsedTime,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Divider(
                modifier = Modifier.width(24.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = formattedTotalDuration,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Preview(name = "Timer Active")
@Composable
fun TimerComposePreview() {
    val timer = Timer(70 * 1000)
    timer.Start()
    // Simulate some time has passed for preview
    try {
        val field = timer::class.java.getDeclaredField("lastStart")
        field.isAccessible = true
        field.set(timer, System.currentTimeMillis() - 25 * 1000)
    } catch (_: Exception) {
    }

    AppTimerTheme {
        TimerCompose(timer = timer, onClick = {})
    }
}

@Preview(name = "Timer Circle Component")
@Composable
fun TimerCirclePreview() {
    AppTimerTheme {
        CircularCountdown(progress = 0.65f, elapsedTime = 25, totalDuration = 70)
    }
}
