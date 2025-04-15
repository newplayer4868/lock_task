package com.cap.locktask.utils



import android.content.Context
import android.util.Log
import model.Preset
import com.google.gson.Gson
import java.io.File

object SharedPreferencesUtils {
    private const val PREFS_NAME = "ButtonPrefs"

    //중복 이름 검사를 위한 함수
    fun isPresetNameExists(context: Context, name: String): Boolean {
        val prefs = context.getSharedPreferences("ButtonPrefs", Context.MODE_PRIVATE)
        val buttonCount = prefs.getInt("buttonCount", 0)

        for (i in 1..buttonCount) {
            val existingName = prefs.getString("button_${i}_name", null)
            if (existingName == name) {
                return true
            }
        }

        return false
    }




    fun savePreset(context: Context, name: String, preset: Preset) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("preset_$name", Gson().toJson(preset))
        // 기존에 저장된 버튼 수 확인
        val currentCount = prefs.getInt("buttonCount", 0)

        // 같은 이름이 이미 있는지 확인 (중복 저장 방지용)
        var nameExists = false
        for (i in 1..currentCount) {
            val existingName = prefs.getString("button_${i}_name", null)
            if (existingName == name) {
                nameExists = true
                break
            }
        }

        // 없으면 새로운 이름 등록 + 카운트 증가
        if (!nameExists) {
            val newIndex = currentCount + 1
            editor.putString("button_${newIndex}_name", name)
            editor.putInt("buttonCount", newIndex)
        }

        editor.apply()
    }

    fun loadPreset(context: Context, name: String): Preset? {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val key = "preset_$name"
            val json = prefs.getString(key, null)

            Log.d("SharedPreferencesUtils", "📦 [$key] → JSON: $json")

            if (json == null) {
                Log.w("SharedPreferencesUtils", "⚠️ [$key] JSON이 null입니다.")
                return null
            }

            Gson().fromJson(json, Preset::class.java)

        } catch (e: Exception) {
            Log.e("SharedPreferencesUtils", "❌ [$name] 프리셋 파싱 실패: ${e.message}", e)
            null
        }
    }





    fun deletePreset(context: Context, name: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.remove("preset_$name")
        val buttonCount = prefs.getInt("buttonCount", 0)
        val newButtonNames = mutableListOf<String>()
        for (i in 1..buttonCount) {
            val savedName = prefs.getString("button_${i}_name", null)
            if (savedName != null && savedName != name) {
                newButtonNames.add(savedName)
            }
        }

        for (i in 1..buttonCount) {
            editor.remove("button_${i}_name")
        }

        // 남은 버튼 이름들 재저장
        newButtonNames.forEachIndexed { index, presetName ->
            editor.putString("button_${index + 1}_name", presetName)
        }
        editor.putInt("buttonCount", newButtonNames.size)

        editor.apply()
    }




    //이미지 경로 저장
    fun saveCroppedImagePath(context: Context, imagePath: String) {
        val prefs = context.getSharedPreferences("ImagePrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("lastCroppedImage", imagePath).apply()
    }

    //저장한 경로 불러오기
    fun loadCroppedImagePath(context: Context): String? {
        val prefs = context.getSharedPreferences("ImagePrefs", Context.MODE_PRIVATE)
        return prefs.getString("lastCroppedImage", null)
    }

    fun deleteSavedCroppedImage(context: Context) {
        val prefs = context.getSharedPreferences("ImagePrefs", Context.MODE_PRIVATE)
        val path = prefs.getString("lastCroppedImage", null)  // ← 수정됨
        path?.let {
            File(it).delete()
        }
        prefs.edit().remove("lastCroppedImage").apply()        // ← 수정됨
    }
    fun getAllPresetNames(context: Context): List<String> {
        val prefs = context.getSharedPreferences("ButtonPrefs", Context.MODE_PRIVATE)
        val count = prefs.getInt("buttonCount", 0)
        val names = mutableListOf<String>()
        for (i in 1..count) {
            prefs.getString("button_${i}_name", null)?.let { names.add(it) }
        }
        return names
    }



    //음원
    private const val MUSIC_PREFS = "MusicPrefs"
    private const val MUSIC_LIST_KEY = "musicList"
    fun saveMusicFile(context: Context, filePath: String) {
        val prefs = context.getSharedPreferences(MUSIC_PREFS, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val musicSet = prefs.getStringSet(MUSIC_LIST_KEY, mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        musicSet.add(filePath)

        editor.putStringSet(MUSIC_LIST_KEY, musicSet)
        editor.apply()
    }

    /**
     * 저장된 음악 경로 리스트 반환
     */
    fun getSavedMusicFiles(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(MUSIC_PREFS, Context.MODE_PRIVATE)
        return prefs.getStringSet(MUSIC_LIST_KEY, emptySet()) ?: emptySet()
    }

    /**
     * 음악 삭제 (SharedPreferences에서만)
     */
    fun deleteMusicFile(context: Context, filePath: String) {
        val prefs = context.getSharedPreferences(MUSIC_PREFS, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val musicSet = prefs.getStringSet(MUSIC_LIST_KEY, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        musicSet.remove(filePath)

        editor.putStringSet(MUSIC_LIST_KEY, musicSet)
        editor.apply()
    }

    /**
     * 전체 초기화
     */
    fun clearAllMusicFiles(context: Context) {
        val prefs = context.getSharedPreferences(MUSIC_PREFS, Context.MODE_PRIVATE)
        prefs.edit().remove(MUSIC_LIST_KEY).apply()
    }
    //이름 중복 검사
    fun isMusicFileNameExists(context: Context, fileName: String): Boolean {
        val savedPaths = getSavedMusicFiles(context)
        return savedPaths.any { File(it).name == fileName }
    }

    //경로 클린으로 인한 미아 처리기
    fun cleanUpInvalidMusicFiles(context: Context) {
        val prefs = context.getSharedPreferences("MusicPrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val validSet = prefs.getStringSet("musicList", mutableSetOf())?.filter { path ->
            File(path).exists()
        }?.toSet() ?: emptySet()

        editor.putStringSet("musicList", validSet)
        editor.apply()
    }

    fun putLong(context: Context, key: String, value: Long) {
        getPrefs(context).edit().putLong(key, value).apply()
    }

    fun getLong(context: Context, key: String, defValue: Long): Long {
        return getPrefs(context).getLong(key, defValue)
    }

    fun remove(context: Context, key: String) {
        getPrefs(context).edit().remove(key).apply()
    }

    private fun getPrefs(context: Context) =
        context.getSharedPreferences("lock_state", Context.MODE_PRIVATE)


}
