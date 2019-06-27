package com.github.veselinazatchepina.alarmclock.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import com.github.veselinazatchepina.alarmclock.ALARM_CLOCK_ALARM_DAYS
import com.github.veselinazatchepina.alarmclock.ALARM_CLOCK_TIME_MILLIS
import java.util.*
import java.util.concurrent.TimeUnit

class AlarmClockTaskManager(private val context: Context?) {

    /**
     * Метод создает новую задачу на отложенный старт будильника.
     */
    fun createAlarmTask(
        alarmClockDays: String,
        alarmClockTimeInMillis: Long,
        hoursForDelay: Int = 24
    ) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.RTC_WAKEUP,
            getAlarmClockTime(alarmClockTimeInMillis, hoursForDelay),
            getAlarmPendingIntent(getAlarmIntent(alarmClockDays, alarmClockTimeInMillis))
        )
    }

    /**
     * Метод рассчитывает время вызова будильника.
     */
    private fun getAlarmClockTime(alarmClockTimeInMillis: Long, hoursForDelay: Int): Long {
        val currentDate = Calendar.getInstance()
        val alarmClockDate = Calendar.getInstance()
        val hours = TimeUnit.MILLISECONDS.toHours(alarmClockTimeInMillis).toInt()
        alarmClockDate.set(Calendar.HOUR_OF_DAY, hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(alarmClockTimeInMillis - TimeUnit.HOURS.toMillis(hours.toLong())).toInt()
        alarmClockDate.set(Calendar.MINUTE, minutes)
        alarmClockDate.set(Calendar.SECOND, 0)
        if (alarmClockDate.before(currentDate)) {
            alarmClockDate.add(Calendar.HOUR_OF_DAY, hoursForDelay)
        }
        return alarmClockDate.timeInMillis
    }

    private fun getAlarmIntent(alarmClockDays: String, alarmClockTimeInMillis: Long): Intent {
        val pushMessageIntent = Intent(context, AlarmClockReceiver::class.java)
        pushMessageIntent.putExtra(ALARM_CLOCK_ALARM_DAYS, alarmClockDays)
        pushMessageIntent.putExtra(ALARM_CLOCK_TIME_MILLIS, alarmClockTimeInMillis)
        return pushMessageIntent
    }

    private fun getAlarmPendingIntent(pushMessageIntent: Intent) = PendingIntent.getBroadcast(
        context,
        AlarmClockReceiver.MESSAGE_CODE,
        pushMessageIntent,
        PendingIntent.FLAG_CANCEL_CURRENT
    )

}