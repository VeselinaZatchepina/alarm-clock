package com.github.veselinazatchepina.alarmclock.domain

import com.github.veselinazatchepina.alarmclock.data.AlarmClock
import io.reactivex.Completable
import io.reactivex.Observable

interface AlarmClockUseCase {

    fun getAlarmClock(): Observable<AlarmClock>

    fun saveAlarmClock(alarmClock: AlarmClock): Completable
}