package io.github.shek014.openlift.data

import android.content.Context
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Counts down the rest between sets and vibrates at the end. It only runs while the app is alive,
 * so it can't alert someone after Android has closed the app.
 */
class RestTimer(context: Context, private val scope: CoroutineScope) {
    private val vibrator = context.getSystemService(Vibrator::class.java)
    private val remaining = MutableStateFlow<Int?>(null)
    private var job: Job? = null
    private var endsAt = 0L

    /** The whole seconds left, or null when no timer is running. */
    val secondsRemaining: StateFlow<Int?> = remaining.asStateFlow()

    fun start(seconds: Int = DefaultSeconds) {
        endsAt = SystemClock.elapsedRealtime() + seconds * 1000L
        job?.cancel()
        job = scope.launch {
            while (true) {
                val left = secondsLeft()
                if (left <= 0) {
                    remaining.value = null
                    vibrator?.vibrate(VibrationEffect.createOneShot(600, VibrationEffect.DEFAULT_AMPLITUDE))
                    break
                }
                remaining.value = left
                delay(200)
            }
        }
    }

    fun adjust(seconds: Int) {
        if (job?.isActive != true) return
        endsAt += seconds * 1000L
        // Taking off more time than is left just leaves a second, so it doesn't vibrate by surprise.
        endsAt = maxOf(endsAt, SystemClock.elapsedRealtime() + 1000L)
        remaining.value = secondsLeft()
    }

    fun stop() {
        job?.cancel()
        remaining.value = null
    }

    private fun secondsLeft(): Int = ((endsAt - SystemClock.elapsedRealtime() + 999) / 1000).toInt()

    companion object {
        const val DefaultSeconds = 90
    }
}
