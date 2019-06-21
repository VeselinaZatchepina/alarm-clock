package com.github.veselinazatchepina.alarmclock.repositories

import com.github.veselinazatchepina.alarmclock.data.AlarmClock
import io.reactivex.Completable
import io.reactivex.Observable

interface AlarmClockRepository {

    fun getAlarmClock(): Observable<AlarmClock>

    fun saveAlarmClock(alarmClock: AlarmClock): Completable
}