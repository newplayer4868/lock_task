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
import android.widget.TextView
import android.widget.Toast
import com.cap.locktask.R
import com.cap.locktask.manager.LockPresetManager
import com.cap.locktask.utils.PresetEvaluator
import com.cap.locktask.utils.SharedPreferencesUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import model.Preset

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

        val unlockBtn = lockScreenView?.findViewById<Button>(R.id.unlockButton)
        val runPreset = SharedPreferencesUtils.loadPreset(applicationContext, "runpreset")
        val infoTextView = lockScreenView?.findViewById<TextView>(R.id.presetInfoTextView)

        val infoText = runPreset?.let {
            """
    🔒 활성화된 프리셋: ${it.name}
    ⏰ 시작: ${it.startTime} ~ 종료: ${it.endTime}
    🔁 요일: ${it.week?.joinToString(", ") ?: "모든 요일"}
    🔓 남은 긴급 해제 횟수: ${it.unlocknum}
    """.trimIndent()
        } ?: "⚠️ 프리셋 정보 없음"

        infoTextView?.text = infoText

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
