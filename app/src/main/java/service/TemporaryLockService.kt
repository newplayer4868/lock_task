package service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.cap.locktask.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import model.TemporaryLockState

class TemporaryLockService : Service() {

    private var lockMinutes = TemporaryLockState.lockStartTime?.toInt()
    private var cntulMinutes = 0
    private var lockScreenView: View? = null
    private lateinit var windowManager: WindowManager

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lockMinutes = intent?.getIntExtra("LOCK_MINUTES", 0) ?: 0
        cntulMinutes = intent?.getIntExtra("CNTUL_MINUTES", 0) ?: 0

        Log.d("TemporaryLockService", "잠금 시간: $lockMinutes 분, 해제: $cntulMinutes ")

        startForeground(1, createNotification())
        showSimpleLockScreen()

        return START_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "temporary_lock_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Temporary Lock",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        return Notification.Builder(this, channelId)
            .setContentTitle("임시 잠금 실행 중")
            .setContentText("설정된 시간 동안 잠금이 유지됩니다.")
            .setSmallIcon(R.drawable.ic_lock) // 반드시 존재하는 아이콘
            .setOngoing(true)
            .build()
    }

    private fun showSimpleLockScreen() {
        val inflater = LayoutInflater.from(this)
        lockScreenView = inflater.inflate(R.layout.simple_lockscreen_layout, null)

        val timeTextView = lockScreenView?.findViewById<TextView>(R.id.timeTextView)
        val unlockButton = lockScreenView?.findViewById<Button>(R.id.unlockButton)
        val infoTextView = lockScreenView?.findViewById<TextView>(R.id.presetInfoTextView)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )

        unlockButton?.setOnClickListener {

            if (TemporaryLockState.cntul > 0) {
                TemporaryLockState.cntul--
                stopSelf()
            } else {
                unlockButton?.isEnabled = false
                unlockButton?.alpha = 0.5f
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.Main).launch {
                delay(60_000L) // 1분 후

                val restartIntent = Intent(applicationContext, TemporaryLockService::class.java).apply {
                    putExtra("LOCK_MINUTES", lockMinutes ?: 0)
                    putExtra("CNTUL_MINUTES", cntulMinutes)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    applicationContext.startForegroundService(restartIntent)
                } else {
                    applicationContext.startService(restartIntent)
                }

                Log.d("TemporaryLockService", "🔁 긴급 해제 후 1분 뒤 서비스 재시작됨")
            }
        }



        windowManager.addView(lockScreenView, params)

        val durationMillis = (lockMinutes ?: 0) * 60 * 1000L
        CoroutineScope(Dispatchers.Main).launch {
            delay(durationMillis)
            stopSelf()
        }

        CoroutineScope(Dispatchers.IO).launch {//IO 스레드로 바꿈
            val startTimeMillis = System.currentTimeMillis()
            val durationMillis = (lockMinutes ?: 0) * 60 * 1000L

            while (lockScreenView != null) {
                val elapsedMillis = System.currentTimeMillis() - startTimeMillis
                val remainingSeconds = (durationMillis - elapsedMillis) / 1000

                val now = java.util.Calendar.getInstance()
                val hour = now.get(java.util.Calendar.HOUR_OF_DAY)
                val minute = now.get(java.util.Calendar.MINUTE)
                val second = now.get(java.util.Calendar.SECOND)

                // 미리 텍스트 준비
                val formattedCurrentTime = String.format("%02d:%02d:%02d", hour, minute, second)
                val remainingMinutesPart = remainingSeconds / 60
                val remainingSecondsPart = remainingSeconds % 60
                val formattedRemainingTime = String.format("%02d:%02d", remainingMinutesPart, remainingSecondsPart)

                val infoText = buildString {
                    appendLine("🔓 긴급해제 남은 횟수: ${TemporaryLockState.cntul}")
                    appendLine("🔒 총 잠금 시간: ${durationMillis / 60000}분")
                    appendLine("⏳ 남은 시간: $formattedRemainingTime")
                }

                withContext(Dispatchers.Main) {
                    timeTextView?.text = formattedCurrentTime
                    infoTextView?.text = infoText
                }

                delay(1000)
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()

        TemporaryLockState.ison = false

        lockScreenView?.let {
            windowManager.removeView(it)
            lockScreenView = null
        }
        Log.d("TemporaryLockService", "🛑 TemporaryLockService 종료됨")
    }


    override fun onBind(intent: Intent?): IBinder? = null
}
