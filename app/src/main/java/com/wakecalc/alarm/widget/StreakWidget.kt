package com.wakecalc.alarm.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.wakecalc.alarm.MainActivity

class StreakWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val stats = WidgetUpdater.loadStats(context)
        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF381E72))
                    .cornerRadius(20.dp)
                    .padding(16.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    "WAKE STREAK",
                    style = TextStyle(color = ColorProvider(Color(0xFFD0BCFF)), fontSize = 12.sp)
                )
                Text(
                    "🔥 ${stats.streak} days",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFEDE7F6)),
                        fontSize = 30.sp, fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    "${stats.totalSolved} problems solved",
                    style = TextStyle(color = ColorProvider(Color(0xFFCFC4E8)), fontSize = 12.sp)
                )
            }
        }
    }
}

class StreakWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StreakWidget()
}
