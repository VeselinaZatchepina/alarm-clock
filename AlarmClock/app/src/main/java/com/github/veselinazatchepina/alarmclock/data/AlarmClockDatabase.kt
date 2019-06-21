package com.github.veselinazatchepina.alarmclock.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(AlarmClock::class), version = 1)
abstract class AlarmClockDatabase : RoomDatabase() {

    companion object {
        private var INSTANCE: AlarmClockDatabase? = null

        fun getInstance(context: Context): AlarmClockDatabase? {
            if (INSTANCE == null) {
                synchronized(AlarmClockDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        AlarmClockDatabase::class.java, "alarmclock")
                        .build()
                }
            }
            return INSTANCE
        }
    }

    abstract fun alarmClockDao(): AlarmClockDao

}