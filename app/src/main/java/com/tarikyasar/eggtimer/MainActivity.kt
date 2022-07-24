package com.tarikyasar.eggtimer

import android.app.AlertDialog
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputFilter
import android.widget.Button
import com.borutsky.neumorphism.NeumorphicFrameLayout
import com.tarikyasar.eggtimer.databinding.ActivityMainBinding
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {

    private val eggMap = mapOf(
        EggCookTypes.SOFT_BOILED to 4L,
        EggCookTypes.MEDIUM_BOILED to 6L,
        EggCookTypes.HARD_BOILED to 12L
    )
    private var cookTime = eggMap[EggCookTypes.MEDIUM_BOILED]!!
    private lateinit var binding: ActivityMainBinding
    private lateinit var timer: CountDownTimer
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.alarm_sound)
        mediaPlayer.isLooping = true

        // Create custom alert dialog
        val builder = AlertDialog.Builder(
            this@MainActivity,
            R.style.CustomAlertDialog
        ).create()
        val view = layoutInflater.inflate(R.layout.custom_alert_dialog, null)
        val button = view.findViewById<NeumorphicFrameLayout>(R.id.buttonClose)
        builder.setView(view)
        button.setOnClickListener {
            builder.dismiss()
            mediaPlayer.stop()
        }
        builder.setCanceledOnTouchOutside(false)

        binding.buttonStart.setOnClickListener {
            if (binding.buttonStart.state != NeumorphicFrameLayout.State.PRESSED) {
                timer = object : CountDownTimer(cookTime * 60000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        binding.tvTimer.text = formatTime(millisUntilFinished)
                    }

                    override fun onFinish() {
                        builder.show()
                        mediaPlayer.start()
                    }
                }

                timer.start()
                binding.radioGroupEggCookType.isActivated = false
                binding.buttonStart.state = NeumorphicFrameLayout.State.PRESSED
                binding.buttonStop.state = NeumorphicFrameLayout.State.CONCAVE
            }
        }

        binding.buttonStop.setOnClickListener {
            if (this::timer.isInitialized && binding.buttonStop.state != NeumorphicFrameLayout.State.PRESSED) {
                resetTimer()
            }
        }

        binding.radioGroupEggCookType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonSoftCooked -> {
                    cookTime = eggMap[EggCookTypes.SOFT_BOILED]!!
                    binding.tvTimer.text = formatTime(cookTime * 60000)
                }
                R.id.radioButtonMediumCooked -> {
                    cookTime = eggMap[EggCookTypes.MEDIUM_BOILED]!!
                    binding.tvTimer.text = formatTime(cookTime * 60000)
                }
                R.id.radioButtonHardCooked -> {
                    cookTime = eggMap[EggCookTypes.HARD_BOILED]!!
                    binding.tvTimer.text = formatTime(cookTime * 60000)
                }
            }

            resetTimer()
        }
    }

    private fun formatTime(remainingTime: Long): String {
        val simpleDateFormat = SimpleDateFormat("mm:ss")
        val timeString = simpleDateFormat.format(remainingTime)

        return timeString
    }

    private fun resetTimer() {
        if (this::timer.isInitialized) {
            timer.cancel()
            binding.buttonStop.state = NeumorphicFrameLayout.State.PRESSED
            binding.buttonStart.state = NeumorphicFrameLayout.State.CONVEX
            binding.tvTimer.text = formatTime(cookTime * 60000)
        }
    }

    enum class EggCookTypes {
        SOFT_BOILED,
        MEDIUM_BOILED,
        HARD_BOILED
    }
}