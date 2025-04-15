package com.cap.locktask.utils

import android.content.Context

object LockStateManager {
    private val activeStates = mutableMapOf<String, LockState>()

    data class LockState(
        val presetName: String,
        var isLocked: Boolean = false,
        var stayStartTime: Long? = null,
        var invalidUntil: Long? = null,
        var totalStayMillis: Long = 0L,
        var remainingUnlocks: Int = 3

    )
    fun updateStayTime(context: Context, presetName: String, isInside: Boolean) {
        val state = getState(presetName)

        if (isInside) {
            if (state.stayStartTime == null) {
                val now = System.currentTimeMillis()
                state.stayStartTime = now
                SharedPreferencesUtils.putLong(context, "${presetName}_stayStart", now)
                SharedPreferencesUtils.putLong(context, "${presetName}_stayTotal", state.totalStayMillis)
            }
        } else {
            // 머문 시간 누적
            val start = state.stayStartTime
            if (start != null) {
                val now = System.currentTimeMillis()
                val session = now - start
                state.totalStayMillis += session
                SharedPreferencesUtils.putLong(context, "${presetName}_stayTotal", state.totalStayMillis)
            }

            // 초기화
            state.stayStartTime = null
            SharedPreferencesUtils.remove(context, "${presetName}_stayStart")
        }
    }


    fun restoreStayTime(context: Context, presetName: String) {
        val savedStart = SharedPreferencesUtils.getLong(context, "${presetName}_stayStart", -1L)
        val savedTotal = SharedPreferencesUtils.getLong(context, "${presetName}_stayTotal", 0L)

        val state = getState(presetName)
        if (savedStart > 0) state.stayStartTime = savedStart
        state.totalStayMillis = savedTotal
    }



    fun getState(presetName: String): LockState =
        activeStates.getOrPut(presetName) { LockState(presetName) }

    fun markInvalid(presetName: String, durationMillis: Long = 5 * 60 * 1000L) {
        getState(presetName).invalidUntil = System.currentTimeMillis() + durationMillis
    }

    fun isCurrentlyInvalid(presetName: String): Boolean {
        val invalidUntil = getState(presetName).invalidUntil
        return invalidUntil != null && System.currentTimeMillis() < invalidUntil
    }

    fun updateStayTime(presetName: String, isInside: Boolean) {
        val state = getState(presetName)
        if (isInside) {
            if (state.stayStartTime == null) state.stayStartTime = System.currentTimeMillis()
        } else {
            state.stayStartTime = null // 나갔다가 다시 들어오면 초기화
        }
    }

    fun hasStayedEnough(presetName: String, requiredMillis: Long): Boolean {
        val state = getState(presetName)
        val start = state.stayStartTime
        val elapsed = if (start != null) System.currentTimeMillis() - start else 0L
        val total = state.totalStayMillis + elapsed
        return total >= requiredMillis
    }

}
