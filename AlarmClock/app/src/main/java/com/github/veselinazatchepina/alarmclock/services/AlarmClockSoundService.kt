package com.github.veselinazatchepina.alarmclock.services

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.IBinder
import com.github.veselinazatchepina.alarmclock.*
import com.github.veselinazatchepina.alarmclock.receivers.AlarmClockTaskManager
import java.util.concurrent.TimeUnit

/**
 * Сервис предназначен для воспроизведения мелодии будильника,
 * а также для отсчета максимальной длительности работы будильника.
 */
class AlarmClockSoundService : Service() {

    private var handler: Handler? = null
    private var mediaPlayerHandler: Handler? = null
    private var progressRunnable: Runnable? = null
    private var mediaPlayerRunnable: Runnable? = null
    private var mediaPlayer: MediaPlayer? = null
    private var alarmClockDays: String? = null
    private var alarmClockTimeInMillis: Long? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        alarmClockDays = intent?.getStringExtra(ALARM_CLOCK_ALARM_DAYS)
        alarmClockTimeInMillis = intent?.getLongExtra(ALARM_CLOCK_TIME_MILLIS, 0)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        initMediaPlayer()
        snoozeAlarmClockIfTimeIsOver()
    }

    /**
     * После истечения заданного времени [ALARM_CLOCK_RINGING_TIME_MINUTES]:
     * 1. Скрывается текущее уведомление;
     * 2. Создается новая задача на будильник через [AlarmClockTaskManager];
     * 3. Сервис останавливается;
     */
    private fun snoozeAlarmClockIfTimeIsOver() {
        handler = Handler()
        progressRunnable = Runnable {
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ALARM_CLOCK_ID)
            AlarmClockTaskManager(this)
                .createAlarmTask(
                    alarmClockDays ?: "",
                    (alarmClockTimeInMillis ?: 0) + TimeUnit.MINUTES.toMillis(SNOOZE_MINUTES_COUNT.toLong())
                )
            stopSelf()
        }
        handler?.postDelayed(progressRunnable, TimeUnit.MINUTES.toMillis(ALARM_CLOCK_RINGING_TIME_MINUTES.toLong()))
    }

    private fun initMediaPlayer() {
        mediaPlayerHandler = Handler()
        mediaPlayerRunnable = Runnable {
            mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        }
        mediaPlayerHandler?.post(mediaPlayerRunnable)
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        handler?.removeCallbacks(progressRunnable)
        mediaPlayerHandler?.removeCallbacks(mediaPlayerRunnable)
        super.onDestroy()
    }
}