package ui.page2

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cap.locktask.R
import com.cap.locktask.utils.SharedPreferencesUtils
import model.MemoAdapter
import model.MemoItem
import viewmodel.MemoSharedViewModel

class Page2_1 : Fragment(R.layout.b_fragment_page2_1) {

    private lateinit var adapter: MemoAdapter
    private var isAllSelected = false
    private lateinit var items: MutableList<MemoItem>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()

        // SharedPreferences에서 로드
        items = SharedPreferencesUtils.loadMemoList(context)

        adapter = MemoAdapter(items)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // 드래그 순서 이동
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                adapter.moveItem(vh.adapterPosition, target.adapterPosition)
                return true
            }

            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {}
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // ➕ 생성
        view.findViewById<Button>(R.id.addButton).setOnClickListener {
            adapter.addItem()
        }

        // ➖ 삭제
        view.findViewById<Button>(R.id.deleteButton).setOnClickListener {
            adapter.removeCheckedItems()
        }

        // ✅ 전체 선택/해제
        view.findViewById<Button>(R.id.selectAllButton).setOnClickListener {
            isAllSelected = !isAllSelected
            adapter.selectAll(isAllSelected)
        }
    }

    override fun onPause() {
        super.onPause()
        val currentItems = adapter.getItems()
        SharedPreferencesUtils.saveMemoList(requireContext(), currentItems)
        Log.d("MemoSave", "수정된 메모 저장 완료")
    }

}

