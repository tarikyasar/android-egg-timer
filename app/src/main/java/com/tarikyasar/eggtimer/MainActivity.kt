package com.tarikyasar.eggtimer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tarikyasar.eggtimer.ui.TimerFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val timerFragment = TimerFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentFrame, timerFragment).commit()
    }
}