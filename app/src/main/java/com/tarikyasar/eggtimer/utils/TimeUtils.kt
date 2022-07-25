package com.tarikyasar.eggtimer.utils

import java.text.SimpleDateFormat

object TimeUtils {
    fun formatTime(remainingTime: Long): String {
        val simpleDateFormat = SimpleDateFormat("mm:ss")
        val timeString = simpleDateFormat.format(remainingTime)

        return timeString
    }
}