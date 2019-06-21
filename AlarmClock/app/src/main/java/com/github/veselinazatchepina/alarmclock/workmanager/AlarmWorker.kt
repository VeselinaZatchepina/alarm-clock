package com.github.veselinazatchepina.alarmclock.workmanager

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.veselinazatchepina.alarmclock.*
import com.github.veselinazatchepina.alarmclock.receivers.AlarmClockReceiver
import java.util.concurrent.TimeUnit

/**
 * 1. Менеджер запускается при наступлении заданного времени;
 * 2. Формируется и отправляется Intent для AlarmManager;
 * 3. Далее данные буду обрабатываться в [AlarmClockReceiver];
 * 4. Если пользователь не реагирует на будильник (не отменяет его),
 *      то автоматически формируется новая задача на отложенный запуск;
 */
class AlarmWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    companion object {
        const val WORK_MANAGER_INPUT_DATA_DAYS = "WORK_MANAGER_INPUT_DATA_DAYS"
        const val WORK_MANAGER_HOURS = "WORK_MANAGER_HOURS"
        const val WORK_MANAGER_MINUTES = "WORK_MANAGER_MINUTES"
    }

    override fun doWork(): Result {
        val alarmClockDays = inputData.getString(WORK_MANAGER_INPUT_DATA_DAYS) ?: ""
        val alarmClockHours = inputData.getInt(WORK_MANAGER_HOURS, 0)
        val alarmClockMinutes = inputData.getInt(WORK_MANAGER_MINUTES, 0)

        prepareAlarmIntent(alarmClockDays, alarmClockHours, alarmClockMinutes)

        Thread.sleep(TimeUnit.MINUTES.toMillis(1))

        if (!this.isStopped) {
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ALARM_CLOCK_ID)
            createAlarmTask(5, TimeUnit.SECONDS)
        }

        return Result.success()
    }

    private fun prepareAlarmIntent(alarmClockDays: String, alarmClockHours: Int, alarmClockMinutes: Int) {
        val pushMessageIntent = Intent(applicationContext, AlarmClockReceiver::class.java)
        pushMessageIntent.putExtra(ALARM_CLOCK_WORK_REQUEST_ID, this.id)
        pushMessageIntent.putExtra(ALARM_CLOCK_ALARM_DAYS, alarmClockDays)
        pushMessageIntent.putExtra(ALARM_CLOCK_ALARM_HOURS, alarmClockHours)
        pushMessageIntent.putExtra(ALARM_CLOCK_ALARM_MINUTES, alarmClockMinutes)
        sendPushIntent(pushMessageIntent)
    }

    private fun sendPushIntent(pushMessageIntent: Intent) {
        val startPushMessageIntent = PendingIntent.getBroadcast(
            applicationContext,
            AlarmClockReceiver.MESSAGE_CODE,
            pushMessageIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE)
        if (alarmManager is AlarmManager) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), startPushMessageIntent)
        }
    }

    /**
     * Метод создаёт новую задачу на запуск будильника
     *
     * @param delay время на которое откладывается запуск будильника
     * @param timeUnit единица измерения (минуты, секунды и т.д.)
     */
    private fun createAlarmTask(delay : Long, timeUnit: TimeUnit) {
        val alarmWorkRequest = OneTimeWorkRequestBuilder<AlarmWorker>()
            .setInitialDelay(delay, timeUnit)
            .build()
        WorkManager.getInstance().enqueue(alarmWorkRequest)
    }
}