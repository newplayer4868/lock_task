package viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import model.MemoItem

class MemoSharedViewModel : ViewModel() {

    private val _memoList = MutableLiveData<MutableList<MemoItem>>(mutableListOf())
    val memoList: LiveData<MutableList<MemoItem>> get() = _memoList

    fun addMemo(memo: MemoItem) {
        _memoList.value?.add(memo)
        _memoList.value = _memoList.value // trigger LiveData update
    }

    fun removeChecked() {
        _memoList.value = _memoList.value?.filter { !it.isChecked }?.toMutableList()
    }

    fun updateAll(select: Boolean) {
        _memoList.value?.forEach { it.isChecked = select }
        _memoList.value = _memoList.value // trigger update
    }

    fun moveItem(from: Int, to: Int) {
        _memoList.value?.let {
            val moved = it.removeAt(from)
            it.add(to, moved)
            it.forEachIndexed { index, item -> item.order = index + 1 }
            _memoList.value = it
        }
    }

    fun setInitialData(list: List<MemoItem>) {
        _memoList.value = list.toMutableList()
    }

    fun getCurrent(): List<MemoItem> = _memoList.value ?: emptyList()
}
