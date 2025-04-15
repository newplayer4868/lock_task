package com.cap.locktask.worker

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.cap.locktask.utils.PresetEvaluator
import com.cap.locktask.utils.SharedPreferencesUtils
import kotlinx.coroutines.runBlocking
import service.LockScreenService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PresetCheckWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("PresetCheckWorker", "500초 딜레이")
        kotlinx.coroutines.delay(500)
        Log.d("PresetCheckWorker", "🔁 doWork() 진입 - 조건 평가 시작")


        Log.d("PresetCheckWorker", "🌀 PresetCheckWorker 작동 시작됨")

        val presetNames = SharedPreferencesUtils.getAllPresetNames(applicationContext)
        Log.d("PresetCheckWorker", "📦 저장된 프리셋 수: ${presetNames.size}")

        if (presetNames.isEmpty()) {
            Log.d("PresetCheckWorker", "⚠️ 저장된 프리셋이 없습니다.")
            return Result.success()
        }

        Log.d("PresetCheckWorker", "🔎 PresetEvaluator.evaluateAllPresets 호출 시도")

        val matched = try {
            PresetEvaluator.evaluateAllPresets(applicationContext)
        } catch (e: Exception) {
            Log.e("PresetCheckWorker", "❌ evaluateAllPresets 실행 중 예외 발생: ${e.message}", e)
            null
        }

        if (matched == null) {
            Log.d("PresetCheckWorker", "❌ 조건을 만족하는 프리셋이 없음 → LockScreen 종료 요청")

            val stopIntent = Intent(applicationContext, LockScreenService::class.java).apply {
                action = "ACTION_STOP_LOCK"
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.d("PresetCheckWorker", "📡 stopForegroundService 호출")
                    applicationContext.startForegroundService(stopIntent)
                } else {
                    applicationContext.startService(stopIntent)
                }
            } catch (e: Exception) {
                Log.e("PresetCheckWorker", "❗ LockScreen 종료 요청 실패: ${e.message}", e)
            }

            return Result.success()
        }


        return if (matched != null) {
            Log.d("PresetCheckWorker", "✅ 조건 만족 프리셋 발견 → LockScreenService 실행 시도")

            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            Log.d("PresetCheckWorker", "⏰ 현재 시간: $currentTime")
            Log.d("PresetCheckWorker", "이름: ${matched.name}")
            Log.d("PresetCheckWorker", "위도: ${matched.latitude}")
            Log.d("PresetCheckWorker", "경도: ${matched.longitude}")
            //Log.d("PresetCheckWorker", "반경: ${matched.radius}")
            Log.d("PresetCheckWorker", "활성화 시간: ${matched.Time}")
            Log.d("PresetCheckWorker", "시작 시간: ${matched.startTime}")
            Log.d("PresetCheckWorker", "종료 시간: ${matched.endTime}")
            Log.d("PresetCheckWorker", "선택된 앱들: ${matched.selectedApps?.joinToString() ?: "없음"}")
            Log.d("PresetCheckWorker", "잠금 타입: ${matched.lockType}")
            Log.d("PresetCheckWorker", "해제 가능 횟수: ${matched.unlocknum}")
            Log.d("PresetCheckWorker", "요일: ${matched.week?.joinToString() ?: "없음"}")
            Log.d("PresetCheckWorker", "활성 상태: ${matched.isactivity}")

            try {
                val intent = Intent(applicationContext, LockScreenService::class.java).apply {
                    putExtra("preset", matched)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.d("PresetCheckWorker", "👉 startForegroundService 호출 시도")
                    applicationContext.startForegroundService(intent)
                } else {
                    Log.d("PresetCheckWorker", "👉 startService 호출 시도")
                    applicationContext.startService(intent)
                }

                Log.d("PresetCheckWorker", "✅ 서비스 실행 요청 완료")
            } catch (e: Exception) {
                Log.e("PresetCheckWorker", "❌ LockScreenService 실행 중 예외 발생: ${e.message}", e)
            }

            Result.success()
        } else {
            Log.d("PresetCheckWorker", "❌ 조건을 만족하는 프리셋이 없음")
            Result.success()
        }
    }
}
