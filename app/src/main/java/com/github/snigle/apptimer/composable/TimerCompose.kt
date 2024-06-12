@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.snigle.apptimer.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.snigle.apptimer.domain.Timer
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerCompose(
    modifier: Modifier = Modifier, timer: Timer
) {

    var timeLeft by remember { mutableStateOf((timer.GetTimeLeft() + 1000) / 1000) }
    var elapsedTime by remember { mutableStateOf((timer.ElapseTime()) / 1000) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft = (timer.GetTimeLeft() + 1000) / 1000
            elapsedTime = (timer.ElapseTime()) / 1000
        }
    }

    val progress = timeLeft / (timer.duration.toFloat() / 1000)


    return Column(
        modifier = Modifier.background(Color.Transparent)
    ) {


        Box {
            CircularCountdown(progress = progress, timeLeft = elapsedTime)


        }
    }
}

@Composable
fun CircularCountdown(progress: Float, timeLeft: Long) {
    var formatted = ""
    if (timeLeft / 60 > 0) {
        formatted += "${timeLeft / 60}m "
    }
    formatted += "${timeLeft - ((timeLeft / 60) * 60)}s"
    val strokeWidth = 24.dp
    Box(
//        modifier = Modifier.border(width = 2.dp, color = Color.Black, shape = CircleShape),
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(color = Color.White, shape = CircleShape)
            .clip(CircleShape)
            .size(70.dp)
    ) {
        Canvas(modifier = Modifier.size(70.dp)) {//.width(200.dp)) {
            drawCircle(
                color = Color.Gray,
                radius = size.minDimension / 2,
                style = Stroke(width = strokeWidth.toPx())
            )
            drawArc(
                color = Color.Green,
                startAngle = -90f,
                sweepAngle = 360 * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx())
            )
        }
        Text(
            text = "$formatted",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )


//            style = MaterialTheme.typography.copy(),
//
    }
}

//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    CountdownTimerApp(duration = 10)
//}

@Composable
@Preview
fun TimerComposePreview() {
    return TimerCompose(timer = Timer(70 * 1000))
}

@Composable
@Preview
fun TimerCircle() {
    return CircularCountdown(progress = 1.0F, timeLeft= 135L)
}

//@Composable
//@Preview
//fun PopupComposeUsagePreview() {
//    return PopupCompose(app = StartedApp.PreviewDailyUsageApp,
//        appLabel = "Facebook",
//        setTimer = { _, _ -> },
//        settingsIntent = { _ -> },
//        close = {})
//}
//
//@Composable
//@Preview
//fun PopupComposeExpiredPreview() {
//    return PopupCompose(app = StartedApp.PreviewTimedoutApp,
//        appLabel = "Facebook",
//        setTimer = { _, _ -> },
//        settingsIntent = { _ -> },
//        close = {})
//}