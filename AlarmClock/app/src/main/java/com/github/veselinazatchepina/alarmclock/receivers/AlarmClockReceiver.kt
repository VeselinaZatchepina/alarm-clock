package com.github.veselinazatchepina.alarmclock.receivers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.os.Build
import androidx.core.app.NotificationCompat
import com.github.veselinazatchepina.alarmclock.*
import com.github.veselinazatchepina.alarmclock.enums.AlarmClockAction
import com.github.veselinazatchepina.alarmclock.services.AlarmClockSoundService


/**
 * В данном ресивере формируется уведомление для будильника.
 * Действия по нажатию на кнопки на панели уведомления будут обрабатываться [AlarmClockActionReceiver].
 */
class AlarmClockReceiver : BroadcastReceiver() {

    private var alarmClockDays: String? = null
    private var alarmClockTimeInMillis: Long? = null

    companion object {
        private const val CHANNEL_ID = "id_com.github.veselinazatchepina.alarmclock"
        private const val CHANNEL_NAME = "name_alarmclock"
        private const val CHANNEL_DESC = "desc_alarmclock"
        const val MESSAGE_CODE = 2019
        const val NOTIFICATION_REQUEST_CODE = 111
        const val NOTIFICATION_REQUEST_CODE_DISMISS = 222
        const val NOTIFICATION_REQUEST_CODE_SNOOZE = 333
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        defineInputData(intent)
        configureNotification(context)
    }

    private fun defineInputData(intent: Intent?) {
        alarmClockDays = intent?.getStringExtra(ALARM_CLOCK_ALARM_DAYS)
        alarmClockTimeInMillis = intent?.getLongExtra(ALARM_CLOCK_TIME_MILLIS, 0)
    }

    private fun configureNotification(context: Context?) {
        val launchIntent = PendingIntent
            .getActivity(
                context,
                NOTIFICATION_REQUEST_CODE,
                MainActivity.newIntent(context!!),
                PendingIntent.FLAG_CANCEL_CURRENT)

        showNotification(context, launchIntent)
    }

    private fun showNotification(context: Context?, launchIntent: PendingIntent) {
        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = CHANNEL_DESC
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm_on_black_24dp)
            .setContentIntent(launchIntent)
            .setDefaults(Notification.DEFAULT_LIGHTS)
            .setAutoCancel(false)
            .setContentTitle(context.getString(R.string.alarm_clock_notification_title))
            .addAction(R.drawable.ic_alarm_off_black_24dp, context.getString(R.string.alarm_clock_notification_dismiss_text), defineActionPendingIntent(context, NOTIFICATION_REQUEST_CODE_DISMISS, AlarmClockAction.DISMISS))
            .addAction(R.drawable.ic_alarm_on_black_24dp, context.getString(R.string.alarm_clock_notification_snooze_text), defineActionPendingIntent(context, NOTIFICATION_REQUEST_CODE_SNOOZE, AlarmClockAction.SNOOZE))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        val notification = builder.build()
        notificationManager.notify(NOTIFICATION_ALARM_CLOCK_ID, notification)
        startAlarmClockSoundService(context)
    }

    private fun startAlarmClockSoundService(context: Context) {
        val intent = Intent(context, AlarmClockSoundService::class.java)
        intent.putExtra(ALARM_CLOCK_ALARM_DAYS, alarmClockDays)
        intent.putExtra(ALARM_CLOCK_TIME_MILLIS, alarmClockTimeInMillis)
        context.startService(intent)
    }

    private fun defineActionIntent(context: Context?, actionType: AlarmClockAction): Intent {
        val intent = Intent(context, AlarmClockActionReceiver::class.java)
        intent.putExtra(NOTIFICATION_ACTION_TYPE, actionType.value)
        intent.putExtra(NOTIFICATION_ALARM_DAYS, alarmClockDays)
        intent.putExtra(NOTIFICATION_ALARM_TIME_MILLIS, alarmClockTimeInMillis)
        return intent
    }

    private fun defineActionPendingIntent(context: Context?, requestCode: Int, actionType: AlarmClockAction): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            requestCode,
            defineActionIntent(context, actionType),
            PendingIntent.FLAG_CANCEL_CURRENT
        )
}