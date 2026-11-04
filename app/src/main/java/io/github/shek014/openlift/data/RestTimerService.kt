package io.github.shek014.openlift.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import io.github.shek014.openlift.MainActivity
import io.github.shek014.openlift.OpenLiftApplication
import io.github.shek014.openlift.R

/** Keeps the rest timer running outside the app, and shows the time left in a notification. */
class RestTimerService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ActionSkip) {
            (application as OpenLiftApplication).restTimer.stop()
            return START_NOT_STICKY
        }
        val endsAt = intent?.getLongExtra(ExtraEndsAt, 0L) ?: 0L
        val millisLeft = endsAt - SystemClock.elapsedRealtime()

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, RestTimerNotificationId, countdownNotification(millisLeft), type)

        // Stops the phone sleeping through the end of the rest. It lets go on its own just after.
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "OpenLift:RestTimer")
            .apply { acquire(millisLeft.coerceAtLeast(0) + 5_000) }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        wakeLock?.takeIf { it.isHeld }?.release()
        super.onDestroy()
    }

    private fun countdownNotification(millisLeft: Long): Notification {
        RestNotifications.createChannels(this)
        val skip = PendingIntent.getService(
            this,
            0,
            Intent(this, RestTimerService::class.java).setAction(ActionSkip),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, RestNotifications.CountdownChannel)
            .setSmallIcon(R.drawable.ic_rest_timer)
            .setContentTitle(getString(R.string.rest_notification_title))
            .setContentIntent(RestNotifications.openApp(this))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis() + millisLeft)
            .addAction(0, getString(R.string.rest_notification_skip), skip)
            .build()
    }

    companion object {
        const val ExtraEndsAt = "ends_at"
        private const val ActionSkip = "io.github.shek014.openlift.SKIP_REST"
        private const val RestTimerNotificationId = 1
    }
}

object RestNotifications {
    const val CountdownChannel = "rest_countdown"
    private const val RestOverChannel = "rest_over"
    private const val RestOverNotificationId = 2
    private const val RestOverTimeoutMillis = 60_000L
    private const val RecordChannel = "personal_records"
    private var nextRecordNotificationId = 100

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CountdownChannel,
                context.getString(R.string.rest_countdown_channel),
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
        manager.createNotificationChannel(
            NotificationChannel(
                RestOverChannel,
                context.getString(R.string.rest_over_channel),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { enableVibration(true) },
        )
    }

    fun openApp(context: Context): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private fun allowed(context: Context) = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

    /** Does nothing if notifications aren't allowed. The workout screen still shows the record. */
    fun showRecord(context: Context, exerciseName: String, details: String) {
        if (!allowed(context)) return
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(
                RecordChannel,
                context.getString(R.string.record_channel),
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
        val notification = NotificationCompat.Builder(context, RecordChannel)
            .setSmallIcon(R.drawable.ic_rest_timer)
            .setContentTitle(context.getString(R.string.record_title, exerciseName))
            .setContentText(details)
            .setStyle(NotificationCompat.BigTextStyle().bigText(details))
            .setContentIntent(openApp(context))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(nextRecordNotificationId++, notification)
    }

    /** Does nothing if notifications aren't allowed. The timer still vibrates in that case. */
    fun showRestOver(context: Context) {
        if (!allowed(context)) return
        createChannels(context)
        val notification = NotificationCompat.Builder(context, RestOverChannel)
            .setSmallIcon(R.drawable.ic_rest_timer)
            .setContentTitle(context.getString(R.string.rest_over_title))
            .setContentText(context.getString(R.string.rest_over_text))
            .setContentIntent(openApp(context))
            .setAutoCancel(true)
            .setTimeoutAfter(RestOverTimeoutMillis)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(context).notify(RestOverNotificationId, notification)
    }
}
