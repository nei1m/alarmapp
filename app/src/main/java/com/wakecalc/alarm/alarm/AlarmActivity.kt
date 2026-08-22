package com.wakecalc.alarm.alarm

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.wakecalc.alarm.challenge.ChallengeGenerator
import com.wakecalc.alarm.challenge.MathExpr
import com.wakecalc.alarm.challenge.Problem
import com.wakecalc.alarm.data.AppDatabase
import com.wakecalc.alarm.data.Prefs
import com.wakecalc.alarm.data.WakeLog
import com.wakecalc.alarm.data.WakeStats
import com.wakecalc.alarm.ui.theme.WakeCalcTheme
import com.wakecalc.alarm.widget.WidgetUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * The full-screen, over-the-lock-screen alarm. You cannot leave until you
 * type an answer that is mathematically equivalent to the correct one.
 * Back is disabled and there is no snooze/dismiss button.
 */
class AlarmActivity : ComponentActivity() {

    private val startedAt = System.currentTimeMillis()
    private var wrongCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showWhenLockedAndTurnScreenOn()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Disable back — you can't back out of the alarm.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { /* swallow */ }
        })

        val prefs = Prefs(this)
        var level = prefs.difficulty

        setContent {
            WakeCalcTheme {
                var problem by remember { mutableStateOf(nextProblem(prefs, level)) }
                var answer by remember { mutableStateOf("") }
                var feedback by remember { mutableStateOf<String?>(null) }
                var solved by remember { mutableStateOf(false) }

                fun submit() {
                    if (answer.isBlank()) { feedback = "Type your answer to silence it."; return }
                    val ok = MathExpr.equivalent(answer, problem.correctAnswer, problem.indefinite)
                    if (ok) {
                        solved = true
                        onSolved(prefs, problem)
                    } else {
                        wrongCount++
                        feedback = "Not equivalent — it keeps ringing. Try again."
                        // escalate a bit if you keep missing
                        if (wrongCount % 3 == 0 && level < 3) {
                            level++
                            problem = nextProblem(prefs, level)
                            answer = ""
                            feedback = "New (harder) problem loaded."
                        }
                    }
                }

                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(16.dp))
                        Text("SOLVE TO SILENCE", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                        Text(
                            "No snooze · locked until correct",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp
                        )
                        Spacer(Modifier.height(24.dp))
                        AssistChip(onClick = {}, label = { Text(problem.category.label) })
                        Spacer(Modifier.height(16.dp))
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    problem.prompt,
                                    fontSize = 26.sp,
                                    fontFamily = FontFamily.Serif,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(problem.hint, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        OutlinedTextField(
                            value = answer,
                            onValueChange = { answer = it; feedback = null },
                            enabled = !solved,
                            label = { Text("Your answer (e.g. 6x^3/3 + 2x^2)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(),
                            keyboardActions = KeyboardActions(onDone = { submit() })
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { submit() },
                            enabled = !solved,
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) { Text(if (solved) "Silenced ✓" else "Submit answer") }

                        feedback?.let {
                            Spacer(Modifier.height(12.dp))
                            Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        }
                        if (solved) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Correct — good morning. Wake logged.",
                                color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            "Power menu is discouraged while ringing",
                            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    private fun nextProblem(prefs: Prefs, level: Int): Problem =
        ChallengeGenerator.generate(prefs.categories, level)

    private fun onSolved(prefs: Prefs, problem: Problem) {
        AlarmService.stop(this)
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val minutesOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val log = WakeLog(
            dayEpoch = WakeStats.todayEpochDay(now),
            wakeTimeMillis = now,
            minutesOfDay = minutesOfDay,
            solveMillis = now - startedAt,
            category = problem.category.label,
            prompt = problem.prompt
        )
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                AppDatabase.get(applicationContext).wakeDao().insert(log)
            }
            WidgetUpdater.updateAll(applicationContext)
            AlarmScheduler.reschedule(applicationContext)
            finish()
        }
    }

    private fun showWhenLockedAndTurnScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            km.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
    }
}
