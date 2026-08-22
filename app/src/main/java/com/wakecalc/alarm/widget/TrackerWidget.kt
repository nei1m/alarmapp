package com.wakecalc.alarm.widget

import android.content.Context
import android.content.Intent
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
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.wakecalc.alarm.MainActivity

/**
 * The weekly "tracker bar" widget: a row of seven bars, one per weekday,
 * tall + accent when you woke up (solved), short + dim when you didn't.
 */
class TrackerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val stats = WidgetUpdater.loadStats(context)
        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF211F26))
                    .cornerRadius(20.dp)
                    .padding(14.dp)
                    .clickable(
                        actionStartActivity(
                            Intent(context, MainActivity::class.java)
                                .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                        )
                    )
            ) {
                Text(
                    "THIS WEEK · 🔥 ${stats.streak}",
                    style = TextStyle(color = ColorProvider(Color(0xFFD0BCFF)), fontSize = 12.sp)
                )
                Row(
                    modifier = GlanceModifier.fillMaxSize().padding(top = 8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    stats.week.forEach { bar ->
                        Column(
                            modifier = GlanceModifier.defaultWeight().fillMaxHeight().padding(horizontal = 3.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = GlanceModifier
                                    .width(16.dp)
                                    .height(if (bar.solved) 46.dp else 10.dp)
                                    .cornerRadius(6.dp)
                                    .background(
                                        if (bar.solved) Color(0xFFD0BCFF) else Color(0xFF49454F)
                                    )
                            ) {}
                            Text(
                                bar.dayLabel,
                                style = TextStyle(color = ColorProvider(Color(0xFFCAC4D0)), fontSize = 10.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}

class TrackerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TrackerWidget()
}
