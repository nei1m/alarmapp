package com.wakecalc.alarm

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wakecalc.alarm.alarm.AlarmService
import com.wakecalc.alarm.ui.screens.AlarmScreen
import com.wakecalc.alarm.ui.screens.DashboardScreen
import com.wakecalc.alarm.ui.screens.LockdownScreen
import com.wakecalc.alarm.ui.screens.TrackerScreen
import com.wakecalc.alarm.ui.screens.WidgetsScreen
import com.wakecalc.alarm.ui.theme.WakeCalcTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        maybeRequestNotifications()
        setContent {
            WakeCalcTheme { AppRoot(::testAlarm) }
        }
    }

    private fun testAlarm() {
        val svc = Intent(this, AlarmService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(svc)
        else startService(svc)
    }

    private val notifPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private fun maybeRequestNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

private enum class Tab(val label: String, val icon: ImageVector) {
    DASH("Home", Icons.Filled.Dashboard),
    ALARM("Alarm", Icons.Filled.Alarm),
    TRACK("Tracker", Icons.Filled.BarChart),
    WIDGETS("Widgets", Icons.Filled.Widgets),
    LOCK("Lock", Icons.Filled.Lock)
}

@Composable
private fun AppRoot(onTestAlarm: () -> Unit) {
    val vm: MainViewModel = viewModel()
    val stats by vm.stats.collectAsState()
    var tab by remember { mutableStateOf(Tab.DASH) }

    Surface(color = MaterialTheme.colorScheme.background) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    Tab.values().forEach { t ->
                        NavigationBarItem(
                            selected = tab == t,
                            onClick = { tab = t },
                            icon = { Icon(t.icon, contentDescription = t.label) },
                            label = { Text(t.label) }
                        )
                    }
                }
            }
        ) { inner ->
            val m = Modifier.padding(inner)
            when (tab) {
                Tab.DASH -> DashboardScreen(m, stats, vm, onTestAlarm)
                Tab.ALARM -> AlarmScreen(m, vm, onTestAlarm)
                Tab.TRACK -> TrackerScreen(m, stats)
                Tab.WIDGETS -> WidgetsScreen(m, stats)
                Tab.LOCK -> LockdownScreen(m, vm)
            }
        }
    }
}
