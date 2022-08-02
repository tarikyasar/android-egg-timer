package com.tarikyasar.eggtimer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.borutsky.neumorphism.NeumorphicFrameLayout
import com.tarikyasar.eggtimer.databinding.ActivityMainBinding
import com.tarikyasar.eggtimer.utils.Constants
import com.tarikyasar.eggtimer.utils.TimeUtils

class MainActivity : AppCompatActivity() {

    private val eggMap = mapOf(
        EggCookTypes.SOFT_BOILED to 4L,
        EggCookTypes.MEDIUM_BOILED to 6L,
        EggCookTypes.HARD_BOILED to 12L
    )
    private var cookTime = eggMap[EggCookTypes.MEDIUM_BOILED]!!
    private var timerState = TimerStates.STOPPED
    private lateinit var binding: ActivityMainBinding
    private lateinit var timer: CountDownTimer
    private lateinit var mediaPlayer: MediaPlayer

    // First one is previously selected radio button
    // Second one is currently selected radio button
    private var radioButtonStates =
        mutableListOf(R.id.radioButtonMediumCooked, R.id.radioButtonMediumCooked)

    // Animations
    private var scaleAnimationsStartButton = mutableListOf<Animation>()
    private var scaleAnimationsStopButton = mutableListOf<Animation>()
    private lateinit var scaleUpRadioButton: Animation
    private lateinit var scaleDownRadioButton: Animation

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize media player
        mediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.alarm_sound)
        mediaPlayer.isLooping = true

        // Initialize animations
        scaleAnimationsStartButton = mutableListOf(
            AnimationUtils.loadAnimation(this, R.anim.scale_up),
            AnimationUtils.loadAnimation(this, R.anim.scale_down)
        )
        scaleAnimationsStopButton = mutableListOf(
            AnimationUtils.loadAnimation(this, R.anim.scale_up),
            AnimationUtils.loadAnimation(this, R.anim.scale_down)
        )
        scaleUpRadioButton = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        scaleDownRadioButton = AnimationUtils.loadAnimation(this, R.anim.scale_down)

        binding.radioButtonMediumCooked.startAnimation(scaleUpRadioButton)

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

        binding.buttonStart.setOnTouchListener { _, event ->
            if (timerState != TimerStates.STARTED) {
                if (event?.action == MotionEvent.ACTION_UP) {
                    binding.buttonStart.startAnimation(scaleAnimationsStartButton[1])

                    timer = object : CountDownTimer(cookTime * Constants.DEFAULT_TIME, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            binding.etTimer.setText(TimeUtils.formatTime(millisUntilFinished))
                        }

                        override fun onFinish() {
                            builder.show()
                            mediaPlayer.start()
                        }
                    }

                    timer.start()
                    timerState = TimerStates.STARTED
                    binding.radioGroupEggCookType.isActivated = false
                    binding.buttonStart.state = NeumorphicFrameLayout.State.CONCAVE
                    binding.buttonStop.state = NeumorphicFrameLayout.State.CONVEX

                } else if (event?.action == MotionEvent.ACTION_DOWN) {
                    binding.buttonStart.startAnimation(scaleAnimationsStartButton[0])
                }

                true
            } else false
        }

        binding.buttonStop.setOnTouchListener { _, event ->
            if (this@MainActivity::timer.isInitialized && timerState != TimerStates.STOPPED) {
                if (event?.action == MotionEvent.ACTION_UP) {
                    binding.buttonStop.startAnimation(scaleAnimationsStopButton[1])

                    resetTimer()
                } else if (event?.action == MotionEvent.ACTION_DOWN) {
                    binding.buttonStop.startAnimation(scaleAnimationsStopButton[0])
                }

                true
            } else false

        }

        binding.radioGroupEggCookType.setOnCheckedChangeListener { _, checkedId ->
            radioButtonStates[0] = radioButtonStates[1]
            radioButtonStates[1] = checkedId

            updateRadioButtonAnimations(radioButtonStates)

            when (checkedId) {
                R.id.radioButtonSoftCooked -> {
                    cookTime = eggMap[EggCookTypes.SOFT_BOILED]!!
                    binding.etTimer.setText(TimeUtils.formatTime(cookTime * Constants.DEFAULT_TIME))

                }
                R.id.radioButtonMediumCooked -> {
                    cookTime = eggMap[EggCookTypes.MEDIUM_BOILED]!!
                    binding.etTimer.setText(TimeUtils.formatTime(cookTime * Constants.DEFAULT_TIME))


                }
                R.id.radioButtonHardCooked -> {
                    cookTime = eggMap[EggCookTypes.HARD_BOILED]!!
                    binding.etTimer.setText(TimeUtils.formatTime(cookTime * Constants.DEFAULT_TIME))

                }
            }

            resetTimer()
        }
    }

    private fun updateRadioButtonAnimations(
        radioButtonStates: List<Int>
    ) {
        when (radioButtonStates[0]) {
            R.id.radioButtonSoftCooked -> binding.radioButtonSoftCooked.startAnimation(
                scaleDownRadioButton
            )
            R.id.radioButtonMediumCooked -> binding.radioButtonMediumCooked.startAnimation(
                scaleDownRadioButton
            )
            R.id.radioButtonHardCooked -> binding.radioButtonHardCooked.startAnimation(
                scaleDownRadioButton
            )
        }

        when (radioButtonStates[1]) {
            R.id.radioButtonSoftCooked -> binding.radioButtonSoftCooked.startAnimation(
                scaleUpRadioButton
            )
            R.id.radioButtonMediumCooked -> binding.radioButtonMediumCooked.startAnimation(
                scaleUpRadioButton
            )
            R.id.radioButtonHardCooked -> binding.radioButtonHardCooked.startAnimation(
                scaleUpRadioButton
            )
        }
    }

    private fun resetTimer() {
        if (this::timer.isInitialized) {
            timer.cancel()
            timerState = TimerStates.STOPPED
            binding.buttonStop.state = NeumorphicFrameLayout.State.CONCAVE
            binding.buttonStart.state = NeumorphicFrameLayout.State.CONVEX
            binding.etTimer.setText(TimeUtils.formatTime(cookTime * Constants.DEFAULT_TIME))
        }
    }
}