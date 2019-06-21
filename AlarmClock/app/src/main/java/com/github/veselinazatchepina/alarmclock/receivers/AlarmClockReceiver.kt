package com.github.veselinazatchepina.alarmclock.receivers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.*
import android.media.AudioAttributes
import com.github.veselinazatchepina.alarmclock.*
import com.github.veselinazatchepina.alarmclock.enums.AlarmClockAction


/**
 * В данном ресивере формируется уведомление для будильника.
 * Действия по нажатию на кнопки на панели уведомления будут обрабатываться [AlarmClockActionReceiver].
 */
class AlarmClockReceiver : BroadcastReceiver() {

    private var workRequestId: UUID? = null
    private var alarmClockDays: String? = null
    private var alarmClockHours: Int? = null
    private var alarmClockMinutes: Int? = null

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
        workRequestId = intent?.getSerializableExtra(ALARM_CLOCK_WORK_REQUEST_ID) as UUID?
        alarmClockDays = intent?.getStringExtra(ALARM_CLOCK_ALARM_DAYS)
        alarmClockHours = intent?.getIntExtra(ALARM_CLOCK_ALARM_HOURS, 0)
        alarmClockMinutes = intent?.getIntExtra(ALARM_CLOCK_ALARM_MINUTES, 0)
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
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            channel.setSound(
                Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + R.raw.alarm_clock_sound),
                audioAttributes
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm_on_black_24dp)
            .setContentIntent(launchIntent)
            .setDefaults(Notification.DEFAULT_LIGHTS)
            .setVibrate(LongArray(3).plus(0).plus(500).plus(1000))
            .setContentTitle(context.getString(R.string.alarm_clock_notification_title))
            .addAction(R.drawable.ic_alarm_off_black_24dp, context.getString(R.string.alarm_clock_notification_dismiss_text), defineActionPendingIntent(context, NOTIFICATION_REQUEST_CODE_DISMISS, AlarmClockAction.DISMISS))
            .addAction(R.drawable.ic_alarm_on_black_24dp, context.getString(R.string.alarm_clock_notification_snooze_text), defineActionPendingIntent(context, NOTIFICATION_REQUEST_CODE_SNOOZE, AlarmClockAction.SNOOZE))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(
                Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + R.raw.alarm_clock_sound)
            )
        val notification = builder.build()
        notificationManager.notify(NOTIFICATION_ALARM_CLOCK_ID, notification)
    }

    private fun defineActionIntent(context: Context?, actionType: AlarmClockAction): Intent {
        val intent = Intent(context, AlarmClockActionReceiver::class.java)
        intent.putExtra(NOTIFICATION_ACTION_TYPE, actionType.value)
        intent.putExtra(NOTIFICATION_WORK_REQUEST_ID, workRequestId)
        intent.putExtra(NOTIFICATION_ALARM_DAYS, alarmClockDays)
        intent.putExtra(NOTIFICATION_ALARM_HOURS, alarmClockHours)
        intent.putExtra(NOTIFICATION_ALARM_MINUTES, alarmClockMinutes)
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