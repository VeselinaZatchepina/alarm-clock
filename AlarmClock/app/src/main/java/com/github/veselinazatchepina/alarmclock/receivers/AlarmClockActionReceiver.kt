package com.github.veselinazatchepina.alarmclock.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.github.veselinazatchepina.alarmclock.*
import com.github.veselinazatchepina.alarmclock.enums.AlarmClockAction
import com.github.veselinazatchepina.alarmclock.workmanager.AlarmWorker
import org.json.JSONArray
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * В данном ресивере осуществляется обработка нажатий кнопок "Dismiss" и "Snooze" на панели уведомлений.
 * При нажатии на "Dismiss" проверяется наличие повторов по дням.
 * В случае наличия повторов создается задача на запуск будильника.
 */
class AlarmClockActionReceiver : BroadcastReceiver() {

    private var workRequestId: UUID? = null
    private var alarmClockDays: String? = null
    private var alarmClockHours: Int? = null
    private var alarmClockMinutes: Int? = null
    private var action: String? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        defineInputData(intent)
        stopCurrentWork(workRequestId, context)
        defineAlarmClockAction()
    }

    private fun defineInputData(intent: Intent?) {
        workRequestId = intent?.getSerializableExtra(NOTIFICATION_WORK_REQUEST_ID) as UUID?
        action = intent?.getStringExtra(NOTIFICATION_ACTION_TYPE)
        alarmClockDays = intent?.getStringExtra(NOTIFICATION_ALARM_DAYS)
        alarmClockHours = intent?.getIntExtra(NOTIFICATION_ALARM_HOURS, 0) ?: 0
        alarmClockMinutes = intent?.getIntExtra(NOTIFICATION_ALARM_MINUTES, 0) ?: 0
    }

    private fun defineAlarmClockAction() {
        when (action) {
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
                createAlarmTask(5, TimeUnit.MINUTES)
            }
        }
    }

    private fun stopCurrentWork(workRequestId: UUID?, context: Context?) {
        workRequestId?.let {
            WorkManager.getInstance().cancelWorkById(it)
            val notificationManager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ALARM_CLOCK_ID)
        }
    }

    /**
     * Метод создает новую задачу на отложенный старт будильника.
     */
    private fun createAlarmTask(duration: Long, timeUnit: TimeUnit) {
        val alarmWorkRequest = OneTimeWorkRequestBuilder<AlarmWorker>()
            .setInitialDelay(duration, timeUnit)
            .build()
        WorkManager.getInstance().enqueue(alarmWorkRequest)
    }

    private fun getDelayTime(hours: Int, minutes: Int): Long {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        dueDate.set(Calendar.HOUR_OF_DAY, hours)
        dueDate.set(Calendar.MINUTE, minutes)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        return dueDate.timeInMillis - currentDate.timeInMillis
    }

    /**
     * В методе осуществляется расчет времени на котрое откладывается вызов будильника.
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
                        createAlarmTask(getDelayTime(alarmClockHours ?: 0, alarmClockMinutes ?: 0) + TimeUnit.HOURS.toMillis(hoursToDelay.toLong()), TimeUnit.MILLISECONDS)
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