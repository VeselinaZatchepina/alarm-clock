package com.github.veselinazatchepina.alarmclock.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_clock")
class AlarmClock(@PrimaryKey(autoGenerate = true) var id: Long? = 0,
                 var hours: Int,
                 var minutes: Int,
                 var daysForRepeatAlarm: String = "")