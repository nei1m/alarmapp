package com.wakecalc.alarm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakecalc.alarm.MainViewModel
import com.wakecalc.alarm.data.Stats
import com.wakecalc.alarm.data.WakeStats

@Composable
fun DashboardScreen(modifier: Modifier, stats: Stats, vm: MainViewModel, onTest: () -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text("Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Medium)
        val today = if (stats.todaySolved)
            "Woke up ${WakeStats.formatMinutes(stats.todayWakeMinutes)} ✓"
        else "No wake-up logged yet today"
        Text(today, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("Streak", "🔥 ${stats.streak}", Modifier.weight(1f))
            StatTile("Solved", "${stats.totalSolved}", Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                Text("Next alarm", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val next = if (vm.alarmEnabled)
                    WakeStats.formatMinutes(vm.hour * 60 + vm.minute)
                else "Off"
                Text(next, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(16.dp))

        Text("This week", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                WeeklyBars(stats.week)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Avg wake ${WakeStats.formatMinutes(stats.avgWakeMinutes)}",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onTest, modifier = Modifier.fillMaxWidth()) {
            Text("Test the alarm now")
        }
        Spacer(Modifier.height(24.dp))
    }
}
