package com.tarikyasar.eggtimer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.widget.addTextChangedListener
import com.borutsky.neumorphism.NeumorphicFrameLayout
import com.tarikyasar.eggtimer.databinding.ActivityMainBinding
import com.tarikyasar.eggtimer.utils.Constants
import com.tarikyasar.eggtimer.utils.TimeUtils


class MainActivity : AppCompatActivity() {

    private val eggMap = mapOf(
        EggCookTypes.SOFT_BOILED to 4 * Constants.DEFAULT_TIME,
        EggCookTypes.MEDIUM_BOILED to 6 * Constants.DEFAULT_TIME,
        EggCookTypes.HARD_BOILED to 12 * Constants.DEFAULT_TIME
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
    private lateinit var inputMethodManager: InputMethodManager

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize input method manager
        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

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
        val button = view.findViewById<AppCompatButton>(R.id.buttonClose)
        builder.setView(view)
        button.setOnClickListener {
            builder.dismiss()
            mediaPlayer.stop()
        }
        builder.setCanceledOnTouchOutside(false)

        binding.buttonStart.setOnTouchListener { _, event ->
            if (timerState != TimerStates.STARTED && this::inputMethodManager.isInitialized) {
                inputMethodManager.hideSoftInputFromWindow(binding.etTimer.windowToken, 0)
                binding.etTimer.clearFocus()

                if (event?.action == MotionEvent.ACTION_UP) {
                    binding.buttonStart.startAnimation(scaleAnimationsStartButton[1])

                    timer = object : CountDownTimer(cookTime.toLong(), 1000) {
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
                    binding.buttonCustomTimer.text = getString(R.string.custom)

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
                    binding.etTimer.setText(TimeUtils.formatTime(cookTime.toLong()))

                }
                R.id.radioButtonMediumCooked -> {
                    cookTime = eggMap[EggCookTypes.MEDIUM_BOILED]!!
                    binding.etTimer.setText(TimeUtils.formatTime(cookTime.toLong()))


                }
                R.id.radioButtonHardCooked -> {
                    cookTime = eggMap[EggCookTypes.HARD_BOILED]!!
                    binding.etTimer.setText(TimeUtils.formatTime(cookTime.toLong()))

                }
            }

            resetTimer()
        }

        binding.buttonCustomTimer.setOnClickListener {
            if (this::inputMethodManager.isInitialized) {
                binding.etTimer.isEnabled = when (binding.etTimer.isEnabled) {
                    true -> {
                        performTouchEvent(binding.buttonStart)
                        binding.etTimer.isEnabled = false

                        false
                    }
                    false -> {
                        performTouchEvent(binding.buttonStop)
                        binding.etTimer.isEnabled = true
                        binding.etTimer.requestFocus()
                        binding.etTimer.setText("")
                        binding.buttonCustomTimer.text = getString(R.string.set_custom_timer)
                        inputMethodManager.showSoftInput(binding.etTimer, 0)

                        true
                    }
                }
            }
        }

        binding.etTimer.addTextChangedListener {
            if (it?.length == 4) {
                val minutes = it.toString().substring(0, 2).toDouble()
                val seconds = it.toString().substring(2, 4).toDouble()

                cookTime = ((minutes + seconds / 60) * Constants.DEFAULT_TIME).toInt()
                val str = it.toString().substring(0, 2) + ":" + it.toString().substring(2, 4)
                binding.etTimer.setText(str)
            }
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
            binding.etTimer.setText(TimeUtils.formatTime(cookTime.toLong()))
        }
    }

    private fun performTouchEvent(view: View) {
        view.dispatchTouchEvent(
            MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                0F,
                0F,
                0
            )
        )
        view.dispatchTouchEvent(
            MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP,
                0F,
                0F,
                0
            )
        )
    }
}