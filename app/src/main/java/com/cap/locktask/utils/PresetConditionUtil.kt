package com.cap.locktask.utils

import android.content.Context
import android.location.Location
import android.util.Log
import model.Preset
import java.util.*

//private fun isTimeMatched(preset: Preset): Boolean  프리셋 삽입해서 내부의 시간
//조건안에 현재 시간이 있는지 파악하여 참거짓 반한

//fun isLocationMatched(preset: Preset, userLocation: Location?): Boolean {
//사용자가 프리셋 안에 설정된 범위 안에 존재하고 있는가?에대한 응답 함수

//private fun isDayOfWeekMatched(preset: Preset): Boolean {
//현재 요일이 일치하는가데 대한 함수


object PresetConditionUtil {

    fun isPresetMatched(context: Context, preset: Preset, location: Location?): Boolean
    {
//만약 프리셋이 활동 중이지 않으면 이거 가동할 가치가 없어
        if (!preset.isactivity) {
            Log.d("PresetConditionUtil", "❌ 비활성화 상태: ${preset.name}")
            return false
        }
        //만약 프리셋 시간 비교해서 틀리면 (시간 범위 내에 없으면 작동하지마
        if (!isTimeMatched(preset)) {
            Log.d("PresetConditionUtil", "⏰ 시간 조건 불만족 - ${preset.name}")
            Log.d("PresetConditionUtil", "도대체 뭘 리턴하는거지${isTimeMatched(preset)}")
            return false
        }else
        {Log.d("PresetConditionUtil", "도대체 뭘 리턴하는거지${isTimeMatched(preset)}")}

        //만약 활성화 요일 아니면 잠금 구동하지마
        if (!isDayOfWeekMatched(preset)) {
            Log.d("PresetConditionUtil", "📅 요일 조건 불만족 - ${preset.name}")
            return false
        }


        if (preset.lockType == "어플 사용량 잠금") {
            val maxMillis = convertTimeToMillis(preset.Time)
            val totalUsed = preset.selectedApps.orEmpty().sumOf { app ->
                AppUsageTracker.getUsageTime(context, app)
            }

            val currentApp = getForegroundPackageName(context)

            if (preset.selectedApps?.contains(currentApp) == true) {
                Log.d("PresetConditionUtil", "✅ 감시 대상 앱 사용 중 → 잠금 유지 안 함")
                return false
            }

            Log.d("PresetConditionUtil", "🚫 어플 사용량 검사: 총 $totalUsed / 목표 $maxMillis")

            if (totalUsed < maxMillis) {
                Log.d("PresetConditionUtil", "⛔ 아직 부족 → 잠금 유지")
                return true
            } else {
                Log.d("PresetConditionUtil", "✅ 사용량 만족 → 잠금 해제")
                return false
            }
        }


        if (preset.lockType == "어플 할당량 잠금") {
            val requiredMillis = convertTimeToMillis(preset.Time)
            val totalUsed = preset.selectedApps.orEmpty().sumOf { app ->
                AppUsageTracker.getUsageTime(context, app)
            }

            Log.d("PresetConditionUtil", "🔍 어플 사용량 총합: ${totalUsed / 1000}초 / 목표: ${requiredMillis / 1000}초")

            if (totalUsed < requiredMillis) {
                Log.d("PresetConditionUtil", "⛔ 사용량 부족 → 잠금 유지")
                return true // 잠금 유지
            } else {
                Log.d("PresetConditionUtil", "✅ 충분히 사용 → 잠금 해제")
                return false
            }
        }

        //목적지 잠금 해제야?
        //그럼 복잡해지는데

        if (preset.lockType == "목적지 잠금 해제") {
            val presetName = preset.name ?: return false
            val inside = isLocationMatched(preset, location)
            LockStateManager.updateStayTime(context, presetName, inside)


            val requiredMillis = convertTimeToMillis(preset.Time)
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
    fun getForegroundPackageName(context: Context): String? {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val taskInfo = am.getRunningTasks(1)
        return taskInfo.firstOrNull()?.topActivity?.packageName
    }

    private fun convertTimeToMillis(time: String?): Long {
        val minutes = time?.toIntOrNull() ?: return 0L
        return minutes * 60_000L
    }

//시간 정보 비교해주는 코드
//val result 안에는 설정 시간 안에 현재 시간이 있는지 파악해주는 결과가 반환됨
    private fun isTimeMatched(preset: Preset): Boolean {
        //일단 캘린더에서 지금에 대한 정보 뺴오기
        val now = Calendar.getInstance()
        //now에서 시간 분 빼오기
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)

        //시간:분인걸 분으로 통합
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


//이건 string으로 받은 시작,종료 시간을 변환해주는 코드
    private fun convertTimeToMinutes(time: String?): Int? {
        return time?.split(":")?.let {
            if (it.size != 2) return null
            val h = it[0].toIntOrNull()
            val m = it[1].toIntOrNull()
            if (h == null || m == null) return null
            h * 60 + m
        }
    }

    //프리셋에서 위치 조건 비교하는 코드
    fun isLocationMatched(preset: Preset, userLocation: Location?): Boolean {
        if (preset.latitude == null || preset.longitude == null || preset.radius == null || userLocation == null) return true

        val presetLocation = Location("").apply {
            latitude = preset.latitude!!
            longitude = preset.longitude!!
        }

        val distance = userLocation.distanceTo(presetLocation) // 미터 단위
        Log.d("PresetConditionUtil", "📏 거리 계산됨: $distance / 기준: ${preset.radius}")

        return distance <= preset.radius!!
    }

//요일 맞는가에 대한 비교 함수
    private fun isDayOfWeekMatched(preset: Preset): Boolean {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) // 1 (일요일) ~ 7 (토요일)
        val koreanDays = listOf("일", "월", "화", "수", "목", "금", "토")
        val todayKorean = koreanDays[today - 1] // 0-based 인덱스

        return preset.week?.contains(todayKorean) ?: true
    }



}
