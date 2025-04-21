package ui.page2

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cap.locktask.R
import com.cap.locktask.utils.SharedPreferencesUtils
import model.MemoItem
import viewmodel.SimpleMemoAdapter

class Page2_2 : Fragment(R.layout.b_fragment_page2_2) {

    // 💡 RecyclerView 참조 변수들 (onResume에서 다시 바인딩)
    private lateinit var recyclerTopLeft: RecyclerView
    private lateinit var recyclerTopRight: RecyclerView
    private lateinit var recyclerBottomLeft: RecyclerView
    private lateinit var recyclerBottomRight: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 💡 RecyclerView 초기화
        recyclerTopLeft = view.findViewById(R.id.recyclerTopLeft)
        recyclerTopRight = view.findViewById(R.id.recyclerTopRight)
        recyclerBottomLeft = view.findViewById(R.id.recyclerBottomLeft)
        recyclerBottomRight = view.findViewById(R.id.recyclerBottomRight)

        recyclerTopLeft.layoutManager = LinearLayoutManager(requireContext())
        recyclerTopRight.layoutManager = LinearLayoutManager(requireContext())
        recyclerBottomLeft.layoutManager = LinearLayoutManager(requireContext())
        recyclerBottomRight.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onResume() {
        super.onResume()

        val context = requireContext()
        val allMemos: List<MemoItem> = SharedPreferencesUtils.loadMemoList(context)

        // 🔍 카테고리별 필터링
        val urgentImportant = allMemos.filter { it.category == "긴급and중요" }
        val urgent = allMemos.filter { it.category == "긴급" }
        val important = allMemos.filter { it.category == "중요" }
        val none = allMemos.filter { it.category == "둘 다 아님" }

        // 📌 어댑터 연결 (항상 최신 리스트로)
        recyclerTopLeft.adapter = SimpleMemoAdapter(urgentImportant)
        recyclerTopRight.adapter = SimpleMemoAdapter(urgent)
        recyclerBottomLeft.adapter = SimpleMemoAdapter(important)
        recyclerBottomRight.adapter = SimpleMemoAdapter(none)
    }
}
