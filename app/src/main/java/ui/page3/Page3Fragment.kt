package ui.page3

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cap.locktask.R
import android.media.MediaPlayer
import android.os.Vibrator
import android.os.VibrationEffect
import android.content.Context
import android.os.Build


class Page3Fragment : Fragment(R.layout.b_fragment_page3) {

    private lateinit var timerText: TextView
    private lateinit var startPauseButton: Button
    private lateinit var resetButton: Button
    private lateinit var modeButton: Button
    private lateinit var repeatButton: Button
    private lateinit var sessionStatusText: TextView

    private var isRunning = false
    private var isWorkMode = true
    private var remainingMillis = 0L
    private var timer: CountDownTimer? = null

    private var currentWorkDuration = 25 * 60 * 1000L
    private var currentBreakDuration = 10 * 60 * 1000L
    private var repeatCount = 1
    private var currentCycle = 0

    private val workBreakOptions = listOf(
        Pair(25 * 60 * 1000L, 10 * 60 * 1000L),
        Pair(30 * 60 * 1000L, 10 * 60 * 1000L),
        Pair(45 * 60 * 1000L, 15 * 60 * 1000L),

    )
    private var workBreakIndex = 0

    private val repeatOptions = listOf(1, 2, 3)
    private var repeatIndex = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timerText = view.findViewById(R.id.timerText)
        startPauseButton = view.findViewById(R.id.startPauseButton)
        resetButton = view.findViewById(R.id.resetButton)
        modeButton = view.findViewById(R.id.modeButton)
        repeatButton = view.findViewById(R.id.repeatButton)

        updateTimerText(currentWorkDuration)

        modeButton.setOnClickListener {
            workBreakIndex = (workBreakIndex + 1) % workBreakOptions.size
            val (work, rest) = workBreakOptions[workBreakIndex]
            currentWorkDuration = work
            currentBreakDuration = rest
            modeButton.text = "${work / 60000}/${rest / 60000}"
            resetTimer()
        }

        repeatButton.setOnClickListener {
            repeatIndex = (repeatIndex + 1) % repeatOptions.size
            repeatCount = repeatOptions[repeatIndex]
            repeatButton.text = "${repeatCount}회 반복"
            resetTimer()
        }

        startPauseButton.setOnClickListener {
            if (isRunning) pauseTimer() else startWorkSession()
        }

        resetButton.setOnClickListener {
            resetTimer()
        }
    }

    private fun startWorkSession() {
        isWorkMode = true
        currentCycle = 0
        remainingMillis = currentWorkDuration
        modeButton.isEnabled = false
        repeatButton.isEnabled = false
        startTimer()
    }


    private fun startTimer() {
        timer = object : CountDownTimer(remainingMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = millisUntilFinished
                updateTimerText(remainingMillis)
            }

            override fun onFinish() {
                notifyTimerFinished()

                if (isWorkMode) {
                    currentCycle++
                    if (currentCycle >= repeatCount) {
                        isRunning = false
                        updateTimerText(0)
                        startPauseButton.text = "시작"
                        sessionStatusText.text = "✅ 완료"
                        modeButton.isEnabled = true
                        repeatButton.isEnabled = true
                        return
                    }
                    isWorkMode = false
                    remainingMillis = currentBreakDuration
                    sessionStatusText.text = "🛌 휴식 중"
                    startTimer()
                } else {
                    isWorkMode = true
                    remainingMillis = currentWorkDuration
                    sessionStatusText.text = "💼 작업 중"
                    startTimer()
                }
            }

        }.start()

        isRunning = true
        startPauseButton.text = "일시정지"
    }

    private fun pauseTimer() {
        timer?.cancel()
        isRunning = false
        startPauseButton.text = "시작"
    }

    private fun resetTimer() {
        timer?.cancel()
        isRunning = false
        isWorkMode = true
        remainingMillis = currentWorkDuration
        currentCycle = 0
        updateTimerText(remainingMillis)
        startPauseButton.text = "시작"
        modeButton.isEnabled = true
        repeatButton.isEnabled = true
    }


    private fun updateTimerText(millis: Long) {
        val minutes = millis / 1000 / 60
        val seconds = (millis / 1000) % 60
        timerText.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun notifyTimerFinished() {


        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }
}

