package com.wakecalc.alarm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakecalc.alarm.data.Stats
import com.wakecalc.alarm.data.WakeStats

@Composable
fun WidgetsScreen(modifier: Modifier, stats: Stats) {
    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text("Widgets", fontSize = 24.sp, fontWeight = FontWeight.Medium)
        Text(
            "Long-press your home screen → Widgets → WakeCalc to add these.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp
        )
        Spacer(Modifier.height(14.dp))

        // Streak widget preview
        Column(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF381E72))
                .padding(16.dp)
        ) {
            Text("WAKE STREAK", color = Color(0xFFD0BCFF), fontSize = 12.sp)
            Text("🔥 ${stats.streak} days", color = Color(0xFFEDE7F6), fontSize = 30.sp, fontWeight = FontWeight.Medium)
            Text("${stats.totalSolved} solved", color = Color(0xFFCFC4E8), fontSize = 12.sp)
        }
        Spacer(Modifier.height(12.dp))

        // Tracker bar widget preview
        Column(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(16.dp)
        ) {
            Text("THIS WEEK", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            WeeklyBars(stats.week, maxHeight = 90)
        }
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("Solved", "${stats.totalSolved}", Modifier.weight(1f))
            StatTile("Avg wake", WakeStats.formatMinutes(stats.avgWakeMinutes), Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
    }
}
