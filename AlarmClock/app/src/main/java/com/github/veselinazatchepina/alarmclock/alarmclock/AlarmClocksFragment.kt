package com.github.veselinazatchepina.alarmclock.alarmclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.github.veselinazatchepina.alarmclock.*
import com.github.veselinazatchepina.alarmclock.data.AlarmClock
import com.github.veselinazatchepina.alarmclock.receivers.AlarmClockTaskManager
import kotlinx.android.synthetic.main.alarm_clock_data.view.*
import kotlinx.android.synthetic.main.fragment_alarm_clocks.*
import org.json.JSONArray
import java.lang.StringBuilder
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 1. Пользователь нажимает на кнопку добавить будильник.
 * 2. Пользователь заполняет данные: время и дни для повтора будильника.
 * 3. Пользователь нажимает кнопку "OK":
 *  - введенные данные проверяются на валидность;
 *  - сохраняются данные будильника в локальной БД;
 *  - устанавливается время будильника на экране;
 *  - создается задача на новый будильник [AlarmClockTaskManager];
 */
class AlarmClocksFragment : Fragment() {

    private var rootView: View? = null
    private var timeRemainingReceiver: BroadcastReceiver? = null
    private val alarmClockViewModel by lazy {
        ViewModelProviders.of(this).get(AlarmClockViewModel::class.java)
    }

    companion object {

        fun createInstance(): AlarmClocksFragment {
            return AlarmClocksFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_alarm_clocks, container, false)
        alarmClockViewModel.fetchAlarmClock()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        floatingActionButton.setOnClickListener {
            getAlarmDataDialog().show()
        }

        alarmClockViewModel.liveAlarmClock.observe(this, androidx.lifecycle.Observer {
            setAlarmClockDataText(it.hours, it.minutes)
            setTimeLeftText(it)
        })
    }

    private fun setAlarmClockDataText(hours: Int, minutes: Int) {
        alarmClockDataToWakeup.text =
            String.format(
                getString(R.string.alarm_clock_data_time),
                clockDataToString(hours),
                clockDataToString(minutes)
            )
    }

    /**
     * Регистрируем BroadcastReceiver для прослушивания изменения системного времени по минутам.
     * В зависимости от полученного времени обновляем текст "Time left:...".
     * Оставшееся время до будильника считается в часах.
     */
    override fun onResume() {
        super.onResume()
        timeRemainingReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context?, intent: Intent?) {
                alarmClockViewModel.liveAlarmClock.value?.let {
                    setTimeLeftText(it)
                }
            }
        }
        requireActivity().registerReceiver(timeRemainingReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
    }

    private fun setTimeLeftText(alarmClock: AlarmClock) {
        val a = getTimeLeft(alarmClock.hours, alarmClock.minutes)
        alarmClockRemainingDataToWakeup?.text = String.format(
            getString(R.string.alarm_clock_data_remaining_time),
            TimeUnit.MILLISECONDS.toHours(getTimeLeft(alarmClock.hours, alarmClock.minutes)).toString()
        )
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(timeRemainingReceiver)
    }

    /**
     * Метод создает диалог для настройки будильника.
     */
    private fun getAlarmDataDialog(): AlertDialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.alarm_clock_data, null)
        builder.setView(dialogView)
        val dialog = builder.create()
        defineAlarmDataDialogOkBtnClickListener(dialogView, dialog)
        return dialog
    }

    private fun defineAlarmDataDialogOkBtnClickListener(dialogView: View, dialog: AlertDialog) {
        dialogView.alarmClockDataOkBtn.setOnClickListener {
            val hourString = dialogView.alarmClockDataHours.text.toString()
            val minuteString = dialogView.alarmClockDataMinuets.text.toString()

            if (hourString.isEmpty() || minuteString.isEmpty()) {
                return@setOnClickListener
            }

            val hours = clockDataToInt(hourString)
            val minutes = clockDataToInt(minuteString)

            if (hours < HOURS_MAX_VALUE && minutes < MINUTES_MAX_VALUE) {
                dialogView.alarmClockData.visibility = View.GONE
                setAlarmClockDataText(hours, minutes)
                alarmClockViewModel.saveAlarmClock(AlarmClock(hours = hours, minutes = minutes))

                AlarmClockTaskManager(requireContext())
                    .createAlarmTask(
                        getAlarmClockDaysString(dialogView),
                        clockDataToMillis(hours, minutes)
                    )

                dialog.dismiss()
            } else {
                dialogView.alarmClockData.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Метод рассчитывает время оставшееся до будильника.
     */
    private fun getTimeLeft(hours: Int, minutes: Int): Long {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        dueDate.set(Calendar.HOUR_OF_DAY, hours)
        dueDate.set(Calendar.MINUTE, minutes)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        return dueDate.timeInMillis - currentDate.timeInMillis
    }

    private fun clockDataToInt(timeValue: String) =
        if (timeValue.startsWith("0") && timeValue != "0") {
            timeValue.substring(1, 2).toInt()
        } else {
            timeValue.toInt()
        }

    private fun clockDataToString(timeValue: Int): String =
        if (timeValue.toString().length == 2) {
            timeValue.toString()
        } else {
            val stringBuilder = StringBuilder()
            stringBuilder.append("0")
            stringBuilder.append(timeValue)
            stringBuilder.toString()
        }

    private fun clockDataToMillis(hours: Int, minutes: Int) =
        TimeUnit.HOURS.toMillis(hours.toLong()) + TimeUnit.MINUTES.toMillis(minutes.toLong())

    /**
     * Метод формирует строку для хранения дней повторения будильника.
     * Дни недели для повторения хранятся в виде строки, полученной из jsonArray,
     * в котором хранится значения true or false,
     * в зависимости от того, установлен ли будильник на данный день или нет.
     */
    private fun getAlarmClockDaysString(dialogView: View): String {
        val days = listOf(
            dialogView.mondayCheckBox.isChecked,
            dialogView.tuesdayCheckBox.isChecked,
            dialogView.wednesdayCheckBox.isChecked,
            dialogView.thursdayCheckBox.isChecked,
            dialogView.fridayCheckBox.isChecked,
            dialogView.saturdayCheckBox.isChecked,
            dialogView.sundayCheckBox.isChecked
        )
        val jsonArray = JSONArray(days)
        return jsonArray.toString()
    }
}