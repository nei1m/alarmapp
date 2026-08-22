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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakecalc.alarm.data.Stats
import com.wakecalc.alarm.data.WakeStats

@Composable
fun TrackerScreen(modifier: Modifier, stats: Stats) {
    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text("Tracker", fontSize = 24.sp, fontWeight = FontWeight.Medium)
        Text("Weekly habit view", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                Text("Wake times this week", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(10.dp))
                WeeklyBars(stats.week, maxHeight = 120)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("Best", WakeStats.formatMinutes(stats.bestWakeMinutes), Modifier.weight(1f))
            StatTile("Streak", "🔥 ${stats.streak}", Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        StatTile("Average wake time", WakeStats.formatMinutes(stats.avgWakeMinutes), Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))

        Text("Log", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        if (stats.totalSolved == 0) {
            Text(
                "No wake-ups logged yet. Solve your first morning problem to start the streak.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp
            )
        } else {
            stats.week.filter { it.solved }.forEach { bar ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.padding(12.dp)) {
                        Text("${bar.dayLabel} · woke ${WakeStats.formatMinutes(bar.minutesOfDay)}",
                            modifier = Modifier.weight(1f), fontSize = 13.sp)
                        Text("✓ solved", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
