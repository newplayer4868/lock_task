package model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Preset(
    var name: String,  // 프리셋 이름
    var latitude: Double? = null,  // 좌표 1
    var longitude: Double? = null,  // 좌표 2
    var radius: Int? = null,  // 원 둘레
    var Time: String? = null,  // 활성화 시간
    var startTime: String? = null,  // 시작 시간
    var endTime: String? = null,  // 종료 시간
    var selectedApps: List<String>? = null,  // 설정된 앱 리스트 (null 가능)
    val lockType: String? = "Default Lock Type",  // 잠금 종류, 기본값 설정
    var week: List<String>?=null,//이거 언제 킬건데
    var unlocknum:Int?=3,//이거 잠금 해제 가능 횟수

    var onmemoQA:Boolean=false,//이거 메모에 생성할거임?//필요없게 만드는 방식 가능할거 가틍ㄴ데

    var isactivity:Boolean=true,//이거 활성화임?
    var description:String?=null,//잠금 묘사용 설명
    val requiredStayMillis: Long = 60_000L
) : Parcelable
