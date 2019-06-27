package com.github.veselinazatchepina.alarmclock.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.veselinazatchepina.alarmclock.*
import com.github.veselinazatchepina.alarmclock.enums.AlarmClockAction
import com.github.veselinazatchepina.alarmclock.services.AlarmClockSoundService
import org.json.JSONArray
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * В данном ресивере осуществляется обработка нажатий кнопок "Dismiss" и "Snooze" на панели уведомлений.
 * При нажатии на "Dismiss" проверяется наличие повторов по дням.
 * В случае наличия повторов создается задача на запуск будильника через [AlarmClockTaskManager].
 */
class AlarmClockActionReceiver : BroadcastReceiver() {

    private var alarmClockDays: String? = null
    private var alarmClockTimeInMillis: Long? = null
    private var alarmClockAction: String? = null
    private var currentContext: Context? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        defineInputData(intent, context)
        stopCurrentAlarmClock()
        defineAlarmClockAction()
    }

    private fun defineInputData(intent: Intent?, context: Context?) {
        currentContext = context
        alarmClockAction = intent?.getStringExtra(NOTIFICATION_ACTION_TYPE)
        alarmClockDays = intent?.getStringExtra(NOTIFICATION_ALARM_DAYS)
        alarmClockTimeInMillis = intent?.getLongExtra(NOTIFICATION_ALARM_TIME_MILLIS, 0)
    }

    private fun stopCurrentAlarmClock() {
        currentContext?.stopService(Intent(currentContext, AlarmClockSoundService::class.java))
        val notificationManager =
            currentContext?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ALARM_CLOCK_ID)
    }

    private fun defineAlarmClockAction() {
        when (alarmClockAction) {
            AlarmClockAction.DISMISS.value -> {
                alarmClockDays?.let { days ->
                    if (alarmClockDays != null && days.isNotEmpty()) {
                        val jsonArray = JSONArray(days)
                        val daysForDelay = Array(jsonArray.length()) {
                            jsonArray.getBoolean(it)
                        }
                        val currentCalendar = Calendar.getInstance()
                        val dayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK)

                        defineWorkManagerForRepeatAlarmClock(daysForDelay, dayOfWeek)
                    }
                }
            }

            AlarmClockAction.SNOOZE.value -> {
                AlarmClockTaskManager(currentContext).createAlarmTask(
                    alarmClockDays ?: "",
                    (alarmClockTimeInMillis ?: 0) + TimeUnit.MINUTES.toMillis(SNOOZE_MINUTES_COUNT.toLong())
                )
            }
        }
    }

    /**
     * В методе осуществляется расчет времени на которое откладывается вызов будильника.
     */
    private fun defineWorkManagerForRepeatAlarmClock(daysList: Array<Boolean>, dayOfWeekPosition: Int) {
        if (!daysList.all { !it }) {
            var hoursToDelay = 24
            var isDayForDelayFound = false
            var index = 0
            while (!isDayForDelayFound) {

                for (dayForDelay in daysList) {
                    if (index <= dayOfWeekPosition - 1) {
                        index++
                        continue
                    }

                    if (dayForDelay) {
                        isDayForDelayFound = true
                        AlarmClockTaskManager(currentContext)
                            .createAlarmTask(
                                alarmClockDays ?: "",
                                alarmClockTimeInMillis ?: 0,
                                hoursToDelay)
                        break
                    } else {
                        hoursToDelay += 24
                    }
                    index++
                }
            }
        }
    }

}