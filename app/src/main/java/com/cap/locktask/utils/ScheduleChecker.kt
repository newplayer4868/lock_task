package com.cap.locktask.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import com.cap.locktask.worker.PresetCheckWorker
import java.util.concurrent.TimeUnit

object ScheduleChecker {

    fun schedule(context: Context) {
        // 1. 주기적인 감시 등록
        val periodicRequest = PeriodicWorkRequestBuilder<PresetCheckWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "PresetCheckWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )

        Log.d("ScheduleChecker", "⏱️ PeriodicWork 등록 완료")

        // 2. 즉시 조건 검사도 1회 실행
        val oneTimeRequest = OneTimeWorkRequestBuilder<PresetCheckWorker>().build()
        WorkManager.getInstance(context).enqueue(oneTimeRequest)

        Log.d("ScheduleChecker", "🚀 OneTimeWorker 즉시 실행됨")
    }
}
