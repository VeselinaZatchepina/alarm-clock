package com.github.veselinazatchepina.alarmclock.alarmclock

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.veselinazatchepina.alarmclock.data.AlarmClock
import com.github.veselinazatchepina.alarmclock.domain.AlarmClockUseCaseImpl
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class AlarmClockViewModel : ViewModel() {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    val liveAlarmClock = MutableLiveData<AlarmClock>()

    fun fetchAlarmClock() {
        compositeDisposable.add(AlarmClockUseCaseImpl.getAlarmClock()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { alarmclock ->
                    liveAlarmClock.value = alarmclock
                },
                { error ->
                    error.printStackTrace() }
            ))
    }

    fun saveAlarmClock(alarmClock: AlarmClock) {
        compositeDisposable.add(AlarmClockUseCaseImpl.saveAlarmClock(alarmClock)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {

                },
                { error ->
                    error.printStackTrace() }
            ))
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

}