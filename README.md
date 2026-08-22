# WakeCalc — the alarm you have to *solve*

A Material 3 Android alarm that will not shut up until you solve a Calc 1–2
problem, has **no snooze**, is **very hard to delete**, tracks your wake-ups
like a habit game, and ships home-screen widgets.

This repo is set up to **build itself into an installable APK on GitHub** —
you don't need Android Studio or a build environment. See *Get the APK* below.

---

## What it does

- **Rings until you solve a problem.** A full-screen challenge appears over the
  lock screen. Only a *mathematically correct* answer silences it — no snooze,
  no dismiss, no back button.
- **Reads your math, not your typing.** Answers are checked for real equivalence,
  so for `∫(6x²+4x)dx` it accepts `2x^3+2x^2`, `6x^3/3+2x^2`, `2*x**3+2x^2`, and
  any form differing only by `+ C`. (Engine: `challenge/MathExpr.kt`, verified
  against 17 test cases.)
- **All of Calc 1–2.** Limits, derivatives (power/product/chain/trig), integrals
  (indefinite, definite, `1/x`), and geometric series. Pick which types you want
  and a difficulty level; difficulty escalates if you keep missing.
- **Pick any MP3.** Choose any audio file on your phone as the alarm tone. Plays
  on the alarm audio stream at full volume, looping.
- **Gamified habit tracker.** Logs the exact time you woke (i.e. solved), your
  streak, total solved, average and best wake time, and a weekly bar chart.
- **Home-screen widgets.** A streak widget and a weekly **tracker-bar** widget.
- **Lockdown.** Device Admin blocks normal uninstall; the alarm survives reboots;
  full-screen over the lock screen. Plus a deliberate **fail-safe** to remove it.

## How the "can't delete / can't turn off" part really works

Being honest about the limits, because Android enforces them:

- **Uninstall protection** uses the **Device Admin** API. Android refuses to
  uninstall an app that has an *active* admin, so the normal uninstall is blocked.
  You activate it once from the **Lock** tab.
- **Truly undeletable** is only possible on a **rooted** phone (or an ADB
  "device-owner" setup from a computer). Without that, a determined user can
  still remove the app via Settings. This build makes it *annoying and
  deliberate*, which is the point at 6:31 AM.
- **Blocking power-off** likewise can't be fully forced without root/device-owner.
  What this app does instead: full-screen alarm over the lock screen, a wake lock,
  and **reboot persistence** (the alarm reschedules itself after a restart), so
  powering the phone off doesn't get you out of the next alarm.

### The fail-safe (your "just in case" button)
On the **Lock** tab there's an emergency removal path. It requires solving
**3 problems** *and* waiting a **60-second cooldown**, then it deactivates Device
Admin and launches the uninstaller. This exists so you're never truly locked out,
but you can't rage-quit the moment the alarm goes off.

---

## Get the APK (no computer setup needed)

1. Create a free GitHub account if you don't have one.
2. Make a **new repository** (e.g. `wakecalc`). Keep it private if you like.
3. Upload **everything in this folder** to that repo (drag-and-drop works on
   github.com → "uploading an existing file"), or push it with git.
4. GitHub Actions runs automatically (see the **Actions** tab). In ~3–5 minutes
   it produces the APK.
5. Download it two ways:
   - **Actions tab** → latest run → **Artifacts** → `WakeCalc-debug-apk`, or
   - the **Releases** section → **Latest WakeCalc build** → `app-debug.apk`.
6. Copy `app-debug.apk` to your phone and open it. Allow "install unknown apps"
   when prompted.

> The workflow that does this is `.github/workflows/build.yml`.

## First-run setup on your phone

1. Open WakeCalc, go to the **Alarm** tab, set a time/days, pick your MP3, and
   toggle the alarm on.
2. Grant **notifications** and, if asked, **alarms & reminders (exact alarm)** —
   there's a shortcut button on the **Lock** tab.
3. On the **Lock** tab, tap **Activate uninstall protection** and confirm the
   Device Admin screen.
4. Add widgets: long-press the home screen → **Widgets** → **WakeCalc**.

## Removing it later
Use the **fail-safe** on the Lock tab (3 problems + 60s), which turns off Device
Admin and uninstalls. Or manually: Settings → Security → Device admin apps →
turn off WakeCalc, then uninstall normally.

---

## Building it yourself (optional)
Open the folder in **Android Studio** (Koala or newer) and press Run, or from a
machine with the Android SDK + JDK 17:

```
gradle assembleDebug        # or ./gradlew assembleDebug if you generate the wrapper
```

The Gradle wrapper JAR isn't committed; Android Studio will offer to create it,
or run `gradle wrapper` once. The CI uses a pinned Gradle 8.9 directly, so it
doesn't need the wrapper.

## Project layout
```
app/src/main/java/com/wakecalc/alarm/
  MainActivity.kt, MainViewModel.kt, WakeCalcApp.kt
  challenge/   MathExpr.kt · ChallengeGenerator.kt · Problem.kt
  alarm/       AlarmScheduler · AlarmReceiver · AlarmService · AlarmActivity · BootReceiver
  admin/       AppDeviceAdminReceiver · Lockdown
  data/        WakeLog · WakeDao · AppDatabase · Prefs · WakeStats
  widget/      StreakWidget · TrackerWidget · WidgetUpdater
  ui/          theme/Theme.kt · screens/*
```

## Tech
Kotlin · Jetpack Compose (Material 3) · AlarmManager exact alarms · foreground
service · Room · Glance app widgets · Device Admin. minSdk 26, targetSdk 34.
