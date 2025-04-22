package service

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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.cap.locktask.R
import com.cap.locktask.manager.LockPresetManager
import com.cap.locktask.utils.PresetEvaluator
import com.cap.locktask.utils.SharedPreferencesUtils
import com.cap.locktask.worker.PresetCheckWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import model.Preset
import java.util.Date
import java.util.Locale

class LockScreenService : Service() {

    private var lockScreenView: View? = null
    private lateinit var windowManager: WindowManager
    private val TAG = "LockScreenService"
    private var currentPreset: Preset? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        Log.d(TAG, "▶️락서비스 구동 시작")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1001, notification)

        val action = intent?.action
        if (action == "ACTION_STOP_LOCK") {
            Log.d(TAG, "🛑 ACTION_STOP_LOCK 수신 → stopSelf() 호출")
            LockPresetManager.clear(applicationContext)
            stopSelf()
            return START_NOT_STICKY
        }

        val source = intent?.getStringExtra("source") ?: "unknown"
        Log.d(TAG, "💥 onStartCommand - 호출된 출처: $source")




        val preset = intent?.getParcelableExtra<Preset>("preset")

        if (preset != null) {
            LockPresetManager.saveIfNotExists(applicationContext, preset)
        }
        currentPreset = LockPresetManager.getRunPreset(applicationContext)

        Log.d(TAG, "받은 프리셋: ${preset?.name}")
        Log.d(TAG, "잠금 타입: ${preset?.lockType}")
        Log.d(TAG, "시작 시간: ${preset?.startTime}")
        Log.d(TAG, "종료 시간: ${preset?.endTime}")
        Log.d(TAG, "요일: ${preset?.week}")
        Log.d(TAG, "잠금해제 횟수: ${preset?.unlocknum}")


        val runPreset = SharedPreferencesUtils.loadPreset(applicationContext, "runpreset")
        val allowedApps = runPreset?.selectedApps ?: emptyList()
        val appContainer = lockScreenView?.findViewById<LinearLayout>(R.id.appShortcutContainer)
        val selectedApps = runPreset?.selectedApps
        if (!selectedApps.isNullOrEmpty()) {
            val targetPackage = selectedApps[0]
            getSharedPreferences("monitor_prefs", MODE_PRIVATE)
                .edit()
                .putString("target_package", targetPackage)
                .apply()
        }

        if (runPreset != null) {
            Log.d(TAG, "🟢 복사본 프리셋 정보")
            Log.d(TAG, "이름: ${runPreset.name}")
            Log.d(TAG, "잠금 타입: ${runPreset.lockType}")
            Log.d(TAG, "시작 시간: ${runPreset.startTime}")
            Log.d(TAG, "종료 시간: ${runPreset.endTime}")
            //Log.d(TAG, "요일: ${runPreset.week}")
            Log.d(TAG, "잠금해제 횟수: ${runPreset.unlocknum}")
        } else {
            //Log.d(TAG, "⚠️ 복사본(runpreset) 불러오기 실패")
        }

        if (lockScreenView == null) {
            if (preset == null) {
                Log.d(TAG, "🚫 preset이 전달되지 않아 잠금화면 실행 안 함")
            } else {
                showLockScreen()
            }
        }


        return START_STICKY
    }

    private fun createNotification(): android.app.Notification {
        val channelId = "lock_service_channel"
        val channelName = "Lock Screen Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                channelName,
                android.app.NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.createNotificationChannel(channel)
        }

        return androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setContentTitle("LockScreen 실행 중")
            .setContentText("잠금 화면이 활성화되었습니다.")
            .setSmallIcon(R.drawable.ic_lock)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun showLockScreen() {
        val inflater = LayoutInflater.from(this)
        lockScreenView = inflater.inflate(R.layout.l_lockscreen_layout, null)
        val timeTextView = lockScreenView?.findViewById<TextView>(R.id.timeTextView)
        val unlockBtn = lockScreenView?.findViewById<Button>(R.id.unlockButton)
        val runPreset = SharedPreferencesUtils.loadPreset(applicationContext, "runpreset")
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
        CoroutineScope(Dispatchers.Main).launch {
            while (lockScreenView != null) {
                val cal = java.util.Calendar.getInstance()
                val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                val minute = cal.get(java.util.Calendar.MINUTE)
                val second = cal.get(java.util.Calendar.SECOND)

                val nowFormatted = String.format("%02d:%02d:%02d", hour, minute, second)
                timeTextView?.text = nowFormatted

                // 프리셋 종료 시간 파싱
                val endTimeStr = runPreset?.endTime ?: "00:00"
                val endHour = endTimeStr.split(":").getOrNull(0)?.toIntOrNull() ?: -1
                val endMinute = endTimeStr.split(":").getOrNull(1)?.toIntOrNull() ?: -1
                // 현재 시각 - 1분
                val oneMinuteAgo = cal.clone() as java.util.Calendar
                oneMinuteAgo.add(java.util.Calendar.MINUTE, -1)


                if (hour == endHour && minute == endMinute + 1) {
                    val request = OneTimeWorkRequestBuilder<PresetCheckWorker>().build()
                    WorkManager.getInstance(applicationContext).enqueue(request)
                }
                delay(1000)
            }
        }




        val infoText = runPreset?.let {
            """
    🔒 활성화된 프리셋: ${it.name}
    ⏰ 시작: ${it.startTime} ~ 종료: ${it.endTime}
    🔁 요일: ${it.week?.joinToString(", ") ?: "모든 요일"}
    🔓 남은 긴급 해제 횟수: ${it.unlocknum}
    """.trimIndent()
        } ?: "⚠️ 프리셋 정보 없음"

        infoTextView?.text = infoText
// 앱 바로가기 버튼 생성
        val shortcutContainer = lockScreenView?.findViewById<LinearLayout>(R.id.appShortcutContainer)
        val pm = packageManager
        val selectedApps = runPreset?.selectedApps ?: emptyList()

        getSharedPreferences("monitor_prefs", MODE_PRIVATE)
            .edit()
            .putStringSet("target_packages", selectedApps.toSet())
            .apply()

        selectedApps.forEach { packageName ->
            try {
                val icon = pm.getApplicationIcon(packageName)
                val launchIntent = pm.getLaunchIntentForPackage(packageName)

                if (launchIntent != null) {
                    val shortcutBtn = android.widget.ImageButton(this).apply {
                        Toast.makeText(this@LockScreenService, "${packageName} 실행 중입니다", Toast.LENGTH_SHORT).show()

                        setImageDrawable(icon)
                        layoutParams = LinearLayout.LayoutParams(150, 150).apply {
                            marginEnd = 16
                        }
                        background = null
                        contentDescription = pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0))
                        setOnClickListener {
                            startActivity(launchIntent.apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })

                            // 🔽 잠금 화면만 제거
                            lockScreenView?.let {
                                windowManager.removeView(it)
                                lockScreenView = null
                            }

                        }
                    }
                    shortcutContainer?.addView(shortcutBtn)
                } else {
                    Log.w(TAG, "⚠️ $packageName 실행 Intent 없음")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ $packageName 아이콘 불러오기 실패", e)
            }
        }

        // 긴급 해제 가능 여부 판단
        if ((runPreset?.unlocknum ?: 0) <= 0) {
            unlockBtn?.isEnabled = false
            unlockBtn?.alpha = 0.5f
            Log.d(TAG, "🚫 긴급 해제 버튼 비활성화됨 - unlocknum = 0")
        }

        unlockBtn?.setOnClickListener {
            if (!LockPresetManager.consumeUnlock(applicationContext)) {
                Toast.makeText(applicationContext, "긴급 해제 횟수를 모두 소진했습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            stopSelf()

        }

        lockScreenView?.findViewById<Button>(R.id.imageButton)?.setOnClickListener {
            // 추가 기능 버튼
        }

        lockScreenView?.findViewById<Button>(R.id.musicButton)?.setOnClickListener {
            // 추가 기능 버튼
        }

        windowManager.addView(lockScreenView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        lockScreenView?.let {
            windowManager.removeView(it)
            lockScreenView = null
        }
        Log.d(TAG, "🛑 LockScreenService 종료됨")
    }

    override fun onBind(intent: Intent?): IBinder? = null


}
