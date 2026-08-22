package com.wakecalc.alarm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakecalc.alarm.data.Stats
import com.wakecalc.alarm.data.WeeklyBar

/** A weekly bar chart. Solved days rise to full accent bars; earlier wake
 *  times are taller. Missed days are short and dim. */
@Composable
fun WeeklyBars(week: List<WeeklyBar>, maxHeight: Int = 96) {
    Row(
        Modifier.fillMaxWidth().height(maxHeight.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        week.forEach { bar ->
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                val h = if (bar.solved) barHeight(bar.minutesOfDay, maxHeight - 22) else 12
                Box(
                    Modifier
                        .width(18.dp)
                        .height(h.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (bar.solved) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
                Spacer(Modifier.height(4.dp))
                Text(bar.dayLabel, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Map wake minute-of-day to a bar height: 5:00 -> tallest, 10:00 -> shortest. */
private fun barHeight(minutesOfDay: Int, span: Int): Int {
    if (minutesOfDay < 0) return 14
    val earliest = 300 // 5:00
    val latest = 660   // 11:00
    val clamped = minutesOfDay.coerceIn(earliest, latest)
    val frac = 1f - (clamped - earliest).toFloat() / (latest - earliest)
    return (span * (0.4f + 0.6f * frac)).toInt().coerceAtLeast(16)
}

@Composable
fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(14.dp)
    ) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
