package com.cap.locktask

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cap.locktask.utils.ScheduleChecker

class App : Application() {

    private val handler = Handler(Looper.getMainLooper())
    private val logRunnable = object : Runnable {
        override fun run() {
            Log.d("AppConditionChecker", "📢 나는 조건 비교를 위한 코드입니다.")
            handler.postDelayed(this, 10000)
        }
    }



    override fun onCreate() {
        super.onCreate()
        Log.d("AppConditionChecker", "🟢 앱 전체 실행됨 - 조건 비교 루프 시작")
        //조건 비교 WorkManager 등록
        ScheduleChecker.schedule(this)
        handler.post(logRunnable)

    }

}
