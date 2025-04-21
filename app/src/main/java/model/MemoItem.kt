package model

data class MemoItem(
    var title: String,
    var description: String,
    var dueDate: String,
    var dueTime: String,
    var category: String,
    var isChecked: Boolean = false,
    var order: Int = 0 // 👉 순서 필드 추가!
)
