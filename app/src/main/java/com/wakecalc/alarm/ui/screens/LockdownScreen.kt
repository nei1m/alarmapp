package com.wakecalc.alarm.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakecalc.alarm.MainViewModel
import com.wakecalc.alarm.admin.Lockdown
import com.wakecalc.alarm.challenge.ChallengeGenerator
import com.wakecalc.alarm.challenge.MathExpr
import kotlinx.coroutines.delay

@Composable
fun LockdownScreen(modifier: Modifier, vm: MainViewModel) {
    val context = LocalContext.current
    var adminActive by remember { mutableStateOf(Lockdown.isAdminActive(context)) }

    // refresh admin status when returning to the screen
    LaunchedEffect(Unit) { adminActive = Lockdown.isAdminActive(context) }

    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text("Lockdown", fontSize = 24.sp, fontWeight = FontWeight.Medium)
        Text(
            "Makes WakeCalc very hard to delete or skip. It cannot be made truly " +
                "impossible without rooting your phone — so there's a fail-safe below.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp
        )
        Spacer(Modifier.height(14.dp))

        LockRow("Device Admin", if (adminActive) "Active — uninstall blocked" else "Off — tap to activate")
        if (!adminActive) {
            OutlinedButton(
                onClick = {
                    context.startActivity(Lockdown.enableAdminIntent(context))
                },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) { Text("Activate uninstall protection") }
        }
        Spacer(Modifier.height(8.dp))

        LockRow("Full-screen lock screen", "Rings over the lock screen · on")
        Spacer(Modifier.height(8.dp))
        LockRow("No snooze / no dismiss", "Only a correct answer stops it · on")
        Spacer(Modifier.height(8.dp))
        LockRow("Reboot persistence", "Alarm restarts after power-off · on")
        Spacer(Modifier.height(8.dp))

        // helpful permission shortcuts
        OutlinedButton(
            onClick = {
                runCatching {
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        android.net.Uri.parse("package:${context.packageName}")))
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Open app permissions (exact alarm, notifications, overlay)") }

        Spacer(Modifier.height(18.dp))
        FailsafeCard(vm)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun LockRow(title: String, subtitle: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text(title, fontSize = 14.sp)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FailsafeCard(vm: MainViewModel) {
    val context = LocalContext.current
    val prefs = vm.prefs

    var active by remember { mutableStateOf(prefs.failsafeStartedAt != 0L) }
    var solved by remember { mutableIntStateOf(prefs.failsafeSolved) }
    var remaining by remember { mutableLongStateOf(Lockdown.cooldownRemainingMs(prefs)) }
    var problem by remember { mutableStateOf(ChallengeGenerator.generate(level = 1)) }
    var answer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<String?>(null) }

    // tick the cooldown while active
    LaunchedEffect(active) {
        while (active) {
            remaining = Lockdown.cooldownRemainingMs(prefs)
            delay(1000)
        }
    }

    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("⚠ Fail-safe escape hatch", color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Medium)
            Text(
                "Emergency removal, just in case. Solve ${Lockdown.FAILSAFE_REQUIRED_SOLVES} problems " +
                    "and wait a 60-second cooldown, then uninstall.",
                color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp
            )
            Spacer(Modifier.height(10.dp))

            if (!active) {
                Button(
                    onClick = {
                        Lockdown.startFailsafe(prefs)
                        active = true; solved = 0; remaining = Lockdown.FAILSAFE_COOLDOWN_MS
                        problem = ChallengeGenerator.generate(level = 1); answer = ""; feedback = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Start emergency removal") }
            } else {
                Text("Solved $solved / ${Lockdown.FAILSAFE_REQUIRED_SOLVES}",
                    color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
                Text("Cooldown: ${remaining / 1000}s",
                    color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))

                if (solved < Lockdown.FAILSAFE_REQUIRED_SOLVES) {
                    Text(problem.prompt, fontSize = 18.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                    OutlinedTextField(
                        value = answer, onValueChange = { answer = it; feedback = null },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        label = { Text("Answer") }
                    )
                    Spacer(Modifier.height(6.dp))
                    Button(onClick = {
                        if (MathExpr.equivalent(answer, problem.correctAnswer, problem.indefinite)) {
                            Lockdown.recordFailsafeSolve(prefs)
                            solved = prefs.failsafeSolved
                            problem = ChallengeGenerator.generate(level = 1); answer = ""
                            feedback = null
                        } else feedback = "Not equivalent — try again."
                    }) { Text("Submit") }
                    feedback?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                }

                Spacer(Modifier.height(10.dp))
                val ready = solved >= Lockdown.FAILSAFE_REQUIRED_SOLVES && remaining == 0L
                Button(
                    onClick = { Lockdown.completeUninstall(context, prefs) },
                    enabled = ready,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(if (ready) "Uninstall WakeCalc now" else "Uninstall (locked)") }

                OutlinedButton(onClick = {
                    Lockdown.cancelFailsafe(prefs); active = false; solved = 0
                }, modifier = Modifier.padding(top = 4.dp)) { Text("Cancel") }
            }
        }
    }
}
