package com.cap.locktask

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.cap.locktask.worker.PresetCheckWorker

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("AlarmReceiver", "⏰ 알람 수신됨 - PresetCheckWorker 실행 시도")

        val request = OneTimeWorkRequestBuilder<PresetCheckWorker>().build()
        WorkManager.getInstance(context).enqueue(request)

        Log.d("AlarmReceiver", "✅ PresetCheckWorker 실행 요청 완료")
    }
}
