package com.github.veselinazatchepina.alarmclock

import android.app.Application

class AlarmClockApp : Application() {

    companion object {
        lateinit var instance: AlarmClockApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

}