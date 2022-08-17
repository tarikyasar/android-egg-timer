package com.tarikyasar.eggtimer.ui

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import com.tarikyasar.eggtimer.utils.EggTimerCallbackManager
import com.tarikyasar.eggtimer.utils.TimeUtils

class TimerViewModel : ViewModel() {
    var isActive = false
    private var time: String = "6:00"
    private var minutes: Long = 6
    private lateinit var timer: CountDownTimer

    fun start(minutes: Long) {
        this.minutes = minutes
        isActive = true
        EggTimerCallbackManager.timerCallback?.onTimerStarted()

        timer = object : CountDownTimer(this.minutes, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                time = TimeUtils.formatTime(millisUntilFinished)
                EggTimerCallbackManager.timerCallback?.onTimeChanges(time)
            }

            override fun onFinish() {
                EggTimerCallbackManager.timerCallback?.onTimerFinished()
            }
        }

        timer.start()
    }

    fun reset() {
        isActive = false
        EggTimerCallbackManager.timerCallback?.onTimerReset()

        if (this::timer.isInitialized) {
            timer.cancel()
        }
    }
}