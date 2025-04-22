package com.cap.locktask

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

import com.cap.locktask.utils.AlarmScheduler
import com.cap.locktask.utils.ScheduleChecker
import service.LockScreenService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "🚀 부팅 완료 - 조건 평가 루프 시작")

            // 핸드폰 키자마자 조건검사루프 시작
            Log.d("BootReceiver", "📤 ScheduleChecker.schedule(context) 호출")
            AlarmScheduler.scheduleRepeatingAlarm(context)
            Log.d("Alarmreceiver", "📤 Alarm receiver 호출")


        }
    }
}


