package com.tarikyasar.eggtimer.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.borutsky.neumorphism.NeumorphicFrameLayout
import com.tarikyasar.eggtimer.R
import com.tarikyasar.eggtimer.callback.TimerCallback
import com.tarikyasar.eggtimer.databinding.FragmentTimerBinding
import com.tarikyasar.eggtimer.utils.Constants
import com.tarikyasar.eggtimer.utils.EggCookTypes
import com.tarikyasar.eggtimer.utils.EggTimerCallbackManager
import com.tarikyasar.eggtimer.utils.TimeUtils

class TimerFragment : Fragment(), TimerCallback {

    private val eggMap = mapOf(
        EggCookTypes.SOFT_BOILED to 4F * Constants.DEFAULT_TIME,
        EggCookTypes.MEDIUM_BOILED to 6F * Constants.DEFAULT_TIME,
        EggCookTypes.HARD_BOILED to 12F * Constants.DEFAULT_TIME
    )
    private var cookTime = eggMap[EggCookTypes.MEDIUM_BOILED]!!
    private lateinit var viewModel: TimerViewModel
    private lateinit var binding: FragmentTimerBinding
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
    private lateinit var builder: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onAttach(context: Context) {
        super.onAttach(context)

        binding = FragmentTimerBinding.inflate(layoutInflater)

        EggTimerCallbackManager.timerCallback = this

        // Initialize media player
        mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound)
        mediaPlayer.isLooping = true

        // Create custom alert dialog
        builder = AlertDialog.Builder(
            context,
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

        // Initialize animations
        scaleAnimationsStartButton = mutableListOf(
            AnimationUtils.loadAnimation(context, R.anim.scale_up),
            AnimationUtils.loadAnimation(context, R.anim.scale_down)
        )
        scaleAnimationsStopButton = mutableListOf(
            AnimationUtils.loadAnimation(context, R.anim.scale_up),
            AnimationUtils.loadAnimation(context, R.anim.scale_down)
        )

        scaleUpRadioButton = AnimationUtils.loadAnimation(context, R.anim.scale_up)
        scaleDownRadioButton = AnimationUtils.loadAnimation(context, R.anim.scale_down)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onStart() {
        super.onStart()

        viewModel = ViewModelProvider(this).get(TimerViewModel::class.java)

        binding.radioButtonMediumCooked.startAnimation(scaleUpRadioButton)

        binding.buttonStart.setOnTouchListener { _, event ->
            if (viewModel.isActive.not()) {
                println("Aa")
                viewModel.start(cookTime.toLong())

                if (event?.action == MotionEvent.ACTION_UP) {
                    binding.buttonStart.startAnimation(scaleAnimationsStartButton[1])

                } else if (event?.action == MotionEvent.ACTION_DOWN) {
                    binding.buttonStart.startAnimation(scaleAnimationsStartButton[0])

                    binding.buttonStart.state = NeumorphicFrameLayout.State.CONCAVE
                    binding.buttonStop.state = NeumorphicFrameLayout.State.CONVEX
                }

                true
            } else false
        }

        binding.buttonStop.setOnTouchListener { _, event ->
            if (viewModel.isActive) {
                if (event?.action == MotionEvent.ACTION_UP) {
                    binding.buttonStop.startAnimation(scaleAnimationsStopButton[1])
                } else if (event?.action == MotionEvent.ACTION_DOWN) {
                    binding.buttonStop.startAnimation(scaleAnimationsStopButton[0])

                    viewModel.reset()
                }

                true
            } else false
        }

        binding.radioGroupEggCookType.setOnCheckedChangeListener { _, checkedId ->
            radioButtonStates[0] = radioButtonStates[1]
            radioButtonStates[1] = checkedId

            updateRadioButtonAnimations(radioButtonStates)

            when (checkedId) {
                R.id.radioButtonSoftCooked -> cookTime = eggMap[EggCookTypes.SOFT_BOILED]!!
                R.id.radioButtonMediumCooked -> cookTime = eggMap[EggCookTypes.MEDIUM_BOILED]!!
                R.id.radioButtonHardCooked -> cookTime = eggMap[EggCookTypes.HARD_BOILED]!!
            }

            binding.tvTimer.text = TimeUtils.formatTime(cookTime.toLong())
            binding.timerSlider.value = cookTime / Constants.DEFAULT_TIME
            viewModel.reset()
        }

        binding.timerSlider.addOnChangeListener { _, value, _ ->
            cookTime = value * Constants.DEFAULT_TIME
            binding.tvTimer.text = TimeUtils.formatTime(cookTime.toLong())
        }
    }

    override fun onTimerStarted() {
        binding.timerSlider.isEnabled = viewModel.isActive.not()
    }

    override fun onTimeChanges(remainingTime: String) {
        binding.tvTimer.text = remainingTime
    }

    override fun onTimerFinished() {
        builder.show()
        mediaPlayer.start()
        binding.timerSlider.isEnabled = viewModel.isActive.not()
    }

    override fun onTimerReset() {
        binding.buttonStop.state = NeumorphicFrameLayout.State.CONCAVE
        binding.buttonStart.state = NeumorphicFrameLayout.State.CONVEX
        binding.tvTimer.text = TimeUtils.formatTime(cookTime.toLong())
        binding.timerSlider.isEnabled = viewModel.isActive.not()
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
}