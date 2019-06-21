package com.github.veselinazatchepina.alarmclock.repositories

import com.github.veselinazatchepina.alarmclock.AlarmClockApp
import com.github.veselinazatchepina.alarmclock.data.AlarmClock
import com.github.veselinazatchepina.alarmclock.data.AlarmClockDatabase
import io.reactivex.Completable
import io.reactivex.Observable

object AlarmClockRepositoryImpl : AlarmClockRepository {

    private val database = AlarmClockDatabase.getInstance(AlarmClockApp.instance.applicationContext)!!

    /**
     * Метод отдает последний заданный будильник
     */
    override fun getAlarmClock(): Observable<AlarmClock> {
        return database.alarmClockDao().getAlarmClock()
    }

    /**
     * Метод сохраняет данные будильника.
     * Дни недели для повторения хранятся в виде строки, полученной из jsonArray, в котором хранится значения true or false,
     * в зависимости от того, установлен ли будильник на данный день или нет.
     */
    override fun saveAlarmClock(alarmClock: AlarmClock): Completable {
        return database.alarmClockDao().saveAlarmClock(alarmClock)
    }
}