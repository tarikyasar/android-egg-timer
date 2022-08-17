package com.tarikyasar.eggtimer.callback

interface TimerCallback {
    fun onTimerStarted()
    fun onTimeChanges(remainingTime: String)
    fun onTimerFinished()
    fun onTimerReset()
}