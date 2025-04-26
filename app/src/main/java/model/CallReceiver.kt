package model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.telephony.TelephonyManager
import android.telephony.SmsManager
import android.util.Log
import model.TemporaryLockState

class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            Log.d("CallReceiver", "📞 전화 상태 변경 감지: state=$stateStr, 번호=$incomingNumber")

            if (stateStr == TelephonyManager.EXTRA_STATE_RINGING) {
                mutePhone(context)
            }
            if (stateStr == TelephonyManager.EXTRA_STATE_RINGING && incomingNumber != null) {
                Log.d("CallReceiver", "☎️ 전화 벨소리 감지됨: $incomingNumber")

                if (TemporaryLockState.ison && !TemporaryLockState.nopecall) {
                    Log.d("CallReceiver", "🚨임시잠금 + 수신거부 활성화 상태. 문자 자동 응답 시도")
                    sendAutoReply(context, incomingNumber)
                } else {
                    Log.d("CallReceiver", "⚠️{$TemporaryLockState.ison} {$TemporaryLockState.nopecall}조건 불충족. 문자 자동 응답 안 함")
                }
            }
        }
    }
    private fun mutePhone(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audioManager.ringerMode != AudioManager.RINGER_MODE_SILENT) {
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            Log.d("CallReceiver", "🔇 전화 수신 - 무음 모드 전환")
        }
    }

    private fun sendAutoReply(context: Context, incomingNumber: String) {
        val smsManager = SmsManager.getDefault()
        try {
            val message = TemporaryLockState.replyMessage
            smsManager.sendTextMessage(incomingNumber, null, message, null, null)
            Log.d("CallReceiver", "✅ 문자 자동 응답 완료: $message")
        } catch (e: Exception) {
            Log.e("CallReceiver", "❌ 문자 자동 응답 실패", e)
        }
    }
}

