package com.github.snigle.apptimer.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.snigle.apptimer.R

@Composable
fun formatDurationInSeconds(seconds: Long): String {
    if (seconds < 0) {
        return "0" + stringResource(R.string.app_time_seconds_unit)
    }
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60

    val timeParts = ArrayList<String>()

    if (hours > 0) {
        timeParts.add("$hours" + stringResource(R.string.app_time_hours_unit))
    }
    if (minutes > 0) {
        timeParts.add("$minutes" + stringResource(R.string.app_time_minutes_unit))
    }
    timeParts.add("$remainingSeconds" + stringResource(R.string.app_time_seconds_unit))

    return timeParts.joinToString(" ")
}
