package com.github.veselinazatchepina.alarmclock.domain

import com.github.veselinazatchepina.alarmclock.data.AlarmClock
import com.github.veselinazatchepina.alarmclock.repositories.AlarmClockRepositoryImpl
import io.reactivex.Completable
import io.reactivex.Observable

object AlarmClockUseCaseImpl : AlarmClockUseCase {

    /**
     * Метод отдает последний заданный будильник
     */
    override fun getAlarmClock(): Observable<AlarmClock> {
        return AlarmClockRepositoryImpl.getAlarmClock()
    }

    /**
     * Метод сохраняет данные будильника.
     * Дни недели для повторения хранятся в виде строки, полученной из jsonArray, в котором хранится значения true or false,
     * в зависимости от того, установлен ли будильник на данный день или нет.
     */
    override fun saveAlarmClock(alarmClock: AlarmClock): Completable {
        return AlarmClockRepositoryImpl.saveAlarmClock(alarmClock)
    }
}