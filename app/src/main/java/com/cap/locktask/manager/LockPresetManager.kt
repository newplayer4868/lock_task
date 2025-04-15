package com.cap.locktask.manager

import android.content.Context
import android.util.Log
import com.cap.locktask.utils.SharedPreferencesUtils
import model.Preset

object LockPresetManager {

    private const val TAG = "LockPresetManager"
    private const val KEY = "runpreset"

    /** 현재 잠금 중인 프리셋 가져오기 */
    fun getRunPreset(context: Context): Preset? {
        return SharedPreferencesUtils.loadPreset(context, KEY)
    }

    /** 프리셋이 존재하지 않을 경우에만 복사 저장 */
    fun saveIfNotExists(context: Context, preset: Preset) {
        val existing = getRunPreset(context)
        if (existing == null) {
            Log.d(TAG, "💾 저장됨 → ${preset.name}, unlocknum = ${preset.unlocknum}")
            SharedPreferencesUtils.savePreset(context, KEY, preset.copy())
        } else {
            Log.d(TAG, "⛔ 저장 안함 → 이미 존재함: ${existing.name}, unlocknum = ${existing.unlocknum}")
        }
    }


    /** 긴급 해제 횟수 감소 → true면 성공, false면 실패 */
    fun consumeUnlock(context: Context): Boolean {
        val preset = getRunPreset(context) ?: return false
        val current = preset.unlocknum ?: 0
        if (current <= 0) return false

        val updated = preset.copy(unlocknum = (current - 1).coerceAtLeast(0))
        SharedPreferencesUtils.savePreset(context, KEY, updated)
        Log.d(TAG, "🔓 긴급 해제 수행 - 남은 횟수: ${updated.unlocknum}")
        return true
    }


    /** 긴급 해제 가능 여부 */
    fun canUnlock(context: Context): Boolean {
        val preset = getRunPreset(context)
        return (preset?.unlocknum ?: 0) > 0
    }

    /** 잠금 해제 시 초기화 */
    fun clear(context: Context) {
        SharedPreferencesUtils.deletePreset(context, KEY)
        Log.d(TAG, "🗑️ runpreset 삭제 완료")
    }
}
