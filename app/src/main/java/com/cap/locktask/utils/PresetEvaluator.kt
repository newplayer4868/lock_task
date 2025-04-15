package com.cap.locktask.utils

import android.content.Context
import android.util.Log
import com.cap.locktask.manager.LockPresetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.Preset

object PresetEvaluator {

    suspend fun evaluateAllPresets(context: Context): Preset? {
        return withContext(Dispatchers.IO) {
            Log.d("PresetEvaluator", "🧠 evaluateAllPresets() 진입 완료")
            val allPresetNames = SharedPreferencesUtils.getAllPresetNames(context)
            val location = try {
                Log.d("PresetEvaluator", "🌍 현재 위치 획득 시도")
                val loc = LocationHelper.getCurrentLocation(context)

                if (loc != null) {
                    Log.d("PresetEvaluator", "✅ 위치 획득 성공: ${loc.latitude}, ${loc.longitude}")
                } else {
                    Log.w("PresetEvaluator", "⚠️ 위치 획득 실패 - null 반환됨")
                }

                loc
            } catch (e: Exception) {
                Log.e("PresetEvaluator", "❌ 위치 획득 실패: ${e.message}", e)
                null
            }

            val now = java.util.Calendar.getInstance()
            val currentHour = now.get(java.util.Calendar.HOUR_OF_DAY)
            val currentMinute = now.get(java.util.Calendar.MINUTE)
            val currentDayOfWeek = now.get(java.util.Calendar.DAY_OF_WEEK) // 1 = 일요일

            Log.d("PresetEvaluator", "📋 전체 프리셋 이름: $allPresetNames")
            Log.d("PresetEvaluator", "🕓 현재 시각: $currentHour:$currentMinute")
            Log.d("PresetEvaluator", "📅 현재 요일: $currentDayOfWeek")
            Log.d("PresetEvaluator", "📍 현재 위치: ${location?.latitude}, ${location?.longitude}")

            val currentRunPreset = LockPresetManager.getRunPreset(context)

            for (name in allPresetNames) {
                LockStateManager.restoreStayTime(context, name)
                val isRunPreset = currentRunPreset?.name == name
                if (isRunPreset) {
                    Log.d("PresetEvaluator", "⚠️ 실행 중인 프리셋 [$name] 평가 제외됨")
                    continue
                }
                try {
                    Log.d("PresetEvaluator", "🧪 [$name] 프리셋 로드 시도")
                    val preset = SharedPreferencesUtils.loadPreset(context, name)

                    if (preset == null) {
                        Log.w("PresetEvaluator", "⚠️ [$name] 프리셋 로드 실패 → null")
                        continue
                    }

                    Log.d("PresetEvaluator", "🔍 프리셋 평가 중: ${preset.name}")
                    Log.d("PresetEvaluator", "   ├─ 시작 시간: ${preset.startTime}")
                    Log.d("PresetEvaluator", "   ├─ 종료 시간: ${preset.endTime}")
                    Log.d("PresetEvaluator", "   ├─ 요일: ${preset.week}")
                    Log.d("PresetEvaluator", "   ├─ 위치 조건: ${preset.latitude}")
                    Log.d("PresetEvaluator", "   ├─ 위치 조건: ${preset.longitude}")
                    Log.d("PresetEvaluator", "   └─ 잠금 타입: ${preset.lockType}")

                    val matched = PresetConditionUtil.isPresetMatched(context, preset, location)


                    Log.d("PresetEvaluator", "🔎 평가 결과 - ${preset.name}: $matched")

                    if (matched) {
                        Log.d("PresetEvaluator", "✅ 조건 일치 프리셋 발견 → '${preset.name}' 반환")
                        return@withContext preset
                    }

                } catch (e: Exception) {
                    Log.e("PresetEvaluator", "❌ 예외 발생 - [$name]: ${e.message}", e)
                }
            }

            Log.d("PresetEvaluator", "🚫 조건 만족하는 프리셋 없음")
            return@withContext null
        }
    }
}
