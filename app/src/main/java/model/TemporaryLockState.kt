package model


object TemporaryLockState {
    var ison: Boolean = false
    var nopecall: Boolean = false
    var lockStartTime: Long? = 10
    var cntul: Int = 3
    var replyMessage: String = "현재 집중 모드 기능 활용으로 인해 무음 실행, 전화를 못 받을 수도 있습니다" // 🔥 추가
}

