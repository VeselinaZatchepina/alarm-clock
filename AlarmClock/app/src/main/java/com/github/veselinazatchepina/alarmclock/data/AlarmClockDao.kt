package com.github.veselinazatchepina.alarmclock.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Observable

@Dao
interface AlarmClockDao {

    @Query("SELECT * from alarm_clock ORDER BY id DESC LIMIT 1")
    fun getAlarmClock(): Observable<AlarmClock>

    @Insert(onConflict = REPLACE)
    fun saveAlarmClock(alarmClock: AlarmClock): Completable

}