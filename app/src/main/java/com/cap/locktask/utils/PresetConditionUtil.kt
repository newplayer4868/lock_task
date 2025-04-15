package com.cap.locktask.utils

import android.content.Context
import android.location.Location
import android.util.Log
import model.Preset
import java.util.*

object PresetConditionUtil {

    fun isPresetMatched(context: Context, preset: Preset, location: Location?): Boolean {

    if (!preset.isactivity) {
            Log.d("PresetConditionUtil", "❌ 비활성화 상태: ${preset.name}")
            return false
        }

        if (!isTimeMatched(preset)) {
            Log.d("PresetConditionUtil", "⏰ 시간 조건 불만족 - ${preset.name}")
            return false
        }
        if (!isDayOfWeekMatched(preset)) {
            Log.d("PresetConditionUtil", "📅 요일 조건 불만족 - ${preset.name}")
            return false
        }

        if (preset.lockType == "목적지 잠금 해제") {
            val presetName = preset.name ?: return false
            val inside = isLocationMatched(preset, location)
            LockStateManager.updateStayTime(context, presetName, inside)


            val requiredMillis = convertTimeToMillis(preset.Time)
            val stayedMillis = System.currentTimeMillis() -
                    (LockStateManager.getState(presetName).stayStartTime ?: System.currentTimeMillis())
            val state = LockStateManager.getState(presetName)
            val start = state.stayStartTime
            val elapsed = if (start != null) System.currentTimeMillis() - start else 0L
            val total = state.totalStayMillis + elapsed

            Log.d("PresetConditionUtil", "🔍 [${preset.name}] 위치 상태: ${if (inside) "📍안에 있음" else "📍밖에 있음"}")
            Log.d("PresetConditionUtil", "🕒 필요 머문 시간: ${requiredMillis / 1000}초")
            Log.d("PresetConditionUtil", "⏱️ 현재 누적 머문 시간: ${total / 1000}초")
            //조건 만족시 잠금 해제
            if (inside && LockStateManager.hasStayedEnough(presetName, requiredMillis)) {
                Log.d("PresetConditionUtil", "✅ 목적지 도착 + 충분히 머묾 → 해제 조건 충족")
                return false
            }

            // 아직 해제 조건은 충족하지 않았지만, 시간 조건 내이므로 잠금 유지
            val nowInTimeRange = isTimeMatched(preset)
            if (nowInTimeRange) {
                Log.d("PresetConditionUtil", "🔒 시간 범위 내, 해제 조건 미충족 → 잠금 유지")
                return true
            }

            // 시간 조건 벗어난 경우 → 잠금도 해제 안 됨, 적용 대상 아님
            Log.d("PresetConditionUtil", "❌ 시간 범위 밖 → 프리셋 무시")
            return false
        }



        if (preset.lockType == "목적지 잠금") {
            if (!isLocationMatched(preset, location)) {
                Log.d("PresetConditionUtil", "📍목적지 잠금 위치 조건 불만족 - ${preset.name}")
                return false
            }
        }



        Log.d("PresetConditionUtil", "✅ 모든 조건 만족: ${preset.name}")
        return true
    }

    private fun convertTimeToMillis(time: String?): Long {
        val minutes = time?.toIntOrNull() ?: return 0L
        return minutes * 60_000L
    }


    private fun isTimeMatched(preset: Preset): Boolean {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        val current = hour * 60 + minute

        val start = convertTimeToMinutes(preset.startTime)
        val end = convertTimeToMinutes(preset.endTime)

        if (start == null || end == null) {
            Log.d("PresetConditionUtil", "⏰ 시간 정보 없음 - 조건 검사 통과")
            return true
        }

        val result = if (start <= end) {
            current in start..end
        } else {
            // 자정 넘김 처리 (예: 22:00 ~ 02:00)
            current in start..1439 || current in 0..end
        }

        val currentStr = String.format("%02d:%02d", hour, minute)
        val startStr = preset.startTime ?: "?"
        val endStr = preset.endTime ?: "?"

        Log.d(
            "PresetConditionUtil",
            if (result)
                "⏰ 시간 조건 만족 ($currentStr ∈ [$startStr ~ $endStr])"
            else
                "⛔ 시간 조건 불만족 ($currentStr ∉ [$startStr ~ $endStr])"
        )

        return result
    }



    private fun convertTimeToMinutes(time: String?): Int? {
        return time?.split(":")?.let {
            if (it.size != 2) return null
            val h = it[0].toIntOrNull()
            val m = it[1].toIntOrNull()
            if (h == null || m == null) return null
            h * 60 + m
        }
    }
    fun isLocationMatched(preset: Preset, userLocation: Location?): Boolean {
        if (preset.latitude == null || preset.longitude == null || preset.radius == null || userLocation == null) return true

        val presetLocation = Location("").apply {
            latitude = preset.latitude
            longitude = preset.longitude
        }

        val distance = userLocation.distanceTo(presetLocation) // 미터 단위
        Log.d("PresetConditionUtil", "📏 거리 계산됨: $distance / 기준: ${preset.radius}")

        return distance <= preset.radius
    }


    private fun isDayOfWeekMatched(preset: Preset): Boolean {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) // 1 (일요일) ~ 7 (토요일)
        val koreanDays = listOf("일", "월", "화", "수", "목", "금", "토")
        val todayKorean = koreanDays[today - 1] // 0-based 인덱스

        return preset.week?.contains(todayKorean) ?: true
    }


    private fun isLocationMatched(preset: Preset): Boolean {
        // TODO: 나중에 현재 위치가 preset 범위 안에 있는지 비교
        return true // 지금은 무조건 통과
    }


}
