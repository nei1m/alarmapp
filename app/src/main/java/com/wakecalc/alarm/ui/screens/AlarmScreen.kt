package com.wakecalc.alarm.ui.screens

import android.content.Intent
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakecalc.alarm.MainViewModel
import com.wakecalc.alarm.challenge.Problem
import com.wakecalc.alarm.data.WakeStats
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlarmScreen(modifier: Modifier, vm: MainViewModel, onTest: () -> Unit) {
    val context = LocalContext.current

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val name = queryName(context, uri) ?: "Selected track"
            vm.setSound(uri.toString(), name)
        }
    }

    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Alarm", fontSize = 24.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Switch(checked = vm.alarmEnabled, onCheckedChange = { vm.setEnabled(it) })
        }
        Spacer(Modifier.height(8.dp))

        // time stepper
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Stepper("Hour", vm.hour, 0, 23) { vm.setTime(it, vm.minute) }
            Text(":", fontSize = 40.sp, modifier = Modifier.padding(horizontal = 8.dp))
            Stepper("Min", vm.minute, 0, 59) { vm.setTime(vm.hour, it) }
        }
        Text(
            WakeStats.formatMinutes(vm.hour * 60 + vm.minute),
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        // days
        val labels = listOf("S", "M", "T", "W", "T", "F", "S") // index+1 = Calendar day
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            for (i in 0..6) {
                val day = i + 1
                val on = vm.days.contains(day)
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer)
                        .clickable { vm.toggleDay(day) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        labels[i],
                        color = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Alarm sound", fontSize = 14.sp)
                    Text(vm.soundName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(onClick = { picker.launch(arrayOf("audio/*")) }) { Text("Pick MP3") }
            }
        }
        Spacer(Modifier.height(10.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                Text("Challenge · Calc 1–2", fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Problem.Category.values().forEach { c ->
                        FilterChip(
                            selected = vm.categories.contains(c),
                            onClick = { vm.toggleCategory(c) },
                            label = { Text(c.label + "s") }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("Difficulty: ${difficultyLabel(vm.difficulty)}", fontSize = 13.sp)
                Slider(
                    value = vm.difficulty.toFloat(),
                    onValueChange = { vm.setDifficulty(it.toInt()) },
                    valueRange = 0f..3f, steps = 2
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Snooze", fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text("Disabled", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onTest, modifier = Modifier.fillMaxWidth().height(50.dp)) {
            Text("Test alarm")
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Stepper(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { onChange(if (value >= max) min else value + 1) }) {
            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Increase $label")
        }
        Text("%02d".format(value), fontSize = 40.sp, fontWeight = FontWeight.Medium)
        IconButton(onClick = { onChange(if (value <= min) max else value - 1) }) {
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Decrease $label")
        }
    }
}

private fun difficultyLabel(level: Int) = when (level) {
    0 -> "Easy"; 1 -> "Normal"; 2 -> "Hard"; else -> "Brutal"
}

private fun queryName(context: android.content.Context, uri: android.net.Uri): String? =
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { if (it.moveToFirst()) it.getString(0) else null }
