package com.wakecalc.alarm.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.wakecalc.alarm.R
import com.wakecalc.alarm.data.Prefs

/**
 * Foreground service that actually rings. Plays the chosen MP3 on a loop at
 * alarm volume, holds a wake lock, vibrates, and shows a full-screen-intent
 * notification that launches the challenge activity. Only a correct answer
 * (via AlarmActivity -> ACTION_STOP) stops it. There is no snooze.
 */
class AlarmService : Service() {

    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var rampRunnable: Runnable? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopEverything()
            return START_NOT_STICKY
        }
        startForeground(NOTIF_ID, buildNotification())
        acquireWakeLock()
        startSound()
        startVibration()
        launchChallenge()
        // START_STICKY: if the system kills us, try to come back ringing.
        return START_STICKY
    }

    private fun launchChallenge() {
        val i = Intent(this, AlarmActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
    }

    private fun buildNotification(): Notification {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID, "Alarm", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "The wake-up alarm"
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            nm.createNotificationChannel(ch)
        }
        val full = PendingIntent.getActivity(
            this, 71,
            Intent(this, AlarmActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val label = Prefs(this).label
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (label.isBlank()) "Solve to silence the alarm" else label)
            .setContentText("A Calc problem is waiting — no snooze.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(full, true)
            .setContentIntent(full)
            .build()
    }

    private fun startSound() {
        val prefs = Prefs(this)
        val uri: Uri = prefs.soundUri?.let { runCatching { Uri.parse(it) }.getOrNull() }
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        // Force alarm-stream volume up so a silent media volume doesn't mute it.
        val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        runCatching {
            audio.setStreamVolume(
                AudioManager.STREAM_ALARM,
                audio.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                0
            )
        }

        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            isLooping = true
            runCatching {
                setDataSource(this@AlarmService, uri)
                prepare()
                start()
            }.onFailure {
                // If the chosen MP3 can't be read, fall back to the default alarm.
                runCatching {
                    reset()
                    val fb = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    setDataSource(this@AlarmService, fb)
                    prepare(); start()
                }
            }
        }
        applyVolume(prefs)
    }

    /** Sets the player volume from prefs, optionally fading it up over ~30s. */
    private fun applyVolume(prefs: Prefs) {
        val target = (prefs.volume / 100f).coerceIn(0f, 1f)
        val p = player ?: return
        rampRunnable?.let { handler.removeCallbacks(it) }
        if (!prefs.gradualVolume) {
            p.setVolume(target, target)
            return
        }
        val stepMs = 700L
        val steps = 43 // ~30 seconds
        p.setVolume(0f, 0f)
        var i = 0
        val r = object : Runnable {
            override fun run() {
                i++
                val frac = (i.toFloat() / steps).coerceIn(0f, 1f)
                val v = target * frac
                player?.setVolume(v, v)
                if (frac < 1f) handler.postDelayed(this, stepMs)
            }
        }
        rampRunnable = r
        handler.postDelayed(r, stepMs)
    }

    private fun startVibration() {
        if (!Prefs(this).vibrate) return
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 600, 400)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        wakeLock = pm.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "wakecalc:alarm"
        ).also { it.acquire(10 * 60 * 1000L) }
    }

    private fun stopEverything() {
        rampRunnable?.let { handler.removeCallbacks(it) }
        rampRunnable = null
        runCatching { player?.stop(); player?.release() }
        player = null
        runCatching { vibrator?.cancel() }
        runCatching { if (wakeLock?.isHeld == true) wakeLock?.release() }
        wakeLock = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopEverything()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "wakecalc_alarm"
        const val NOTIF_ID = 42
        const val ACTION_STOP = "com.wakecalc.alarm.ACTION_STOP"

        fun stop(context: Context) {
            val i = Intent(context, AlarmService::class.java).apply { action = ACTION_STOP }
            context.startService(i)
        }
    }
}
