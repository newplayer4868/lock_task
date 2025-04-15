package model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Preset(
    val name: String,  // 프리셋 이름
    val latitude: Double? = null,  // 좌표 1
    val longitude: Double? = null,  // 좌표 2
    val radius: Int? = null,  // 원 둘레
    val Time: String? = null,  // 활성화 시간
    val startTime: String? = null,  // 시작 시간
    val endTime: String? = null,  // 종료 시간
    val selectedApps: List<String>? = null,  // 설정된 앱 리스트 (null 가능)
    val lockType: String? = "Default Lock Type",  // 잠금 종류, 기본값 설정
    val week: List<String>?=null,
    val unlocknum:Int?=3,
    var isactivity:Boolean=true,
    val requiredStayMillis: Long = 60_000L
) : Parcelable
