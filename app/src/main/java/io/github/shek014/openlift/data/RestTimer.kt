package io.github.shek014.openlift.data

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Counts down the rest between sets. While it runs, [RestTimerService] keeps the app alive with a
 * notification, so that it still alerts someone who has left the app or turned off the screen.
 */
class RestTimer(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onFinished: () -> Unit = {},
) {
    private val vibrator = context.getSystemService(Vibrator::class.java)
    private val remaining = MutableStateFlow<Int?>(null)
    private var job: Job? = null
    private var endsAt = 0L

    /** The whole seconds left, or null when no timer is running. */
    val secondsRemaining: StateFlow<Int?> = remaining.asStateFlow()

    fun start(seconds: Int) {
        endsAt = SystemClock.elapsedRealtime() + seconds * 1000L
        job?.cancel()
        job = scope.launch {
            while (true) {
                val left = secondsLeft()
                if (left <= 0) {
                    finish()
                    break
                }
                remaining.value = left
                delay(200)
            }
        }
        startService()
    }

    fun adjust(seconds: Int) {
        if (job?.isActive != true) return
        endsAt += seconds * 1000L
        // Taking off more time than is left just leaves a second, so it doesn't vibrate by surprise.
        endsAt = maxOf(endsAt, SystemClock.elapsedRealtime() + 1000L)
        remaining.value = secondsLeft()
        startService()
    }

    fun stop() {
        job?.cancel()
        remaining.value = null
        stopService()
    }

    private fun finish() {
        remaining.value = null
        stopService()
        vibrator?.vibrate(VibrationEffect.createOneShot(600, VibrationEffect.DEFAULT_AMPLITUDE))
        onFinished()
        val inForeground = ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        if (!inForeground) RestNotifications.showRestOver(context)
    }

    private fun secondsLeft(): Int = ((endsAt - SystemClock.elapsedRealtime() + 999) / 1000).toInt()

    private fun startService() {
        val intent = Intent(context, RestTimerService::class.java).putExtra(RestTimerService.ExtraEndsAt, endsAt)
        try {
            context.startForegroundService(intent)
        } catch (_: IllegalStateException) {
            // Android refuses this in some background states. The countdown carries on regardless.
        }
    }

    private fun stopService() {
        context.stopService(Intent(context, RestTimerService::class.java))
    }
}
