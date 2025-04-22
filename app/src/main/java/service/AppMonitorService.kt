package service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.cap.locktask.utils.AppUsageTracker
import com.cap.locktask.worker.PresetCheckWorker

class AppMonitorService : AccessibilityService() {

    private var isInTargetApp = false
    private var sessionStartTime: Long = 0L
    private var currentPackage: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val currentApp = event?.packageName?.toString() ?: return
        val prefs = getSharedPreferences("monitor_prefs", MODE_PRIVATE)
        val targetApps = prefs.getStringSet("target_packages", emptySet()) ?: return

        Log.d("AppMonitor", "👀 현재 앱: $currentApp, 감시 목록: $targetApps")

        if (targetApps.contains(currentApp)) {
            if (!isInTargetApp) {
                // 앱 사용 시작 감지
                isInTargetApp = true
                sessionStartTime = System.currentTimeMillis()
                currentPackage = currentApp
                Log.d("AppMonitor", "▶️ 감시 시작: $currentApp")
            }
        } else {
            if (isInTargetApp) {
                // 앱 사용 종료 감지
                val usedMillis = System.currentTimeMillis() - sessionStartTime
                currentPackage?.let {
                    AppUsageTracker.addUsageTime(applicationContext, it, usedMillis)
                    Log.d("AppMonitor", "⏱️ 사용 기록 저장: $it - ${usedMillis / 1000}초")
                }

                // 잠금 재개
                val request = OneTimeWorkRequestBuilder<PresetCheckWorker>().build()
                WorkManager.getInstance(this).enqueue(request)
                Log.d("AppMonitor", "🚪 앱 벗어남 → 조건 평가 요청")

                isInTargetApp = false
                sessionStartTime = 0L
                currentPackage = null
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AppMonitor", "✅ 접근성 서비스 연결됨")

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            packageNames = null
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        }
        this.serviceInfo = info
    }

    override fun onInterrupt() {}
}
