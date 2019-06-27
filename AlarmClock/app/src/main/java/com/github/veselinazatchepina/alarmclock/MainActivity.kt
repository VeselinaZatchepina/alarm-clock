package com.github.veselinazatchepina.alarmclock

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.veselinazatchepina.alarmclock.alarmclock.AlarmClocksFragment

/**
 * Общее описание логики приложения:
 *
 * 1. Пользователь задает время и частоту повторения будильника;
 * 2. В заданное время формируется уведомление с данными будильника;
 * 3. Открывается уведомление;
 * 4. Одновременно с появлением уведомления запускается сервис для воспроизведения мелодии будильника AlarmClockReceiver;
 * 5. Обработка действий по нажатию на уведомление осуществляется в AlarmClockActionReceiver;
 */
class MainActivity : AppCompatActivity() {

    companion object {

        fun newIntent(context: Context?): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        replaceFragment(AlarmClocksFragment.createInstance())
    }

    private fun replaceFragment(fragment: Fragment?) {
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainContainer, fragment)
                .commit()
        }
    }
}

