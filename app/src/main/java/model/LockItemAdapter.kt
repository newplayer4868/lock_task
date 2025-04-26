package model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.cap.locktask.R

class LockItemAdapter(
    private val items: MutableList<Preset>
) : RecyclerView.Adapter<LockItemAdapter.LockViewHolder>() {

    private var isEditMode = false

    inner class LockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lockName: TextView = itemView.findViewById(R.id.item_Lcok_Name)
        val description: EditText = itemView.findViewById(R.id.editTextDescription)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        val toggleButton: TextView = itemView.findViewById(R.id.item_Lock_toggle)
        val layoutSettings: LinearLayout = itemView.findViewById(R.id.layoutSettings)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lock, parent, false)
        return LockViewHolder(view)
    }

    override fun onBindViewHolder(holder: LockViewHolder, position: Int) {
        val item = items[position]

        // 이름 표시
        holder.lockName.text = item.name

        // 설명 표시
        holder.description.setText(item.description ?: "")

        // 체크박스: 편집 모드에 따라 보이기
        holder.checkBox.visibility = if (isEditMode) View.VISIBLE else View.GONE
        holder.checkBox.isChecked = item.isChecked ?: false

        // 상세 설정 토글
        holder.toggleButton.setOnClickListener {
            val visible = holder.layoutSettings.visibility == View.VISIBLE
            holder.layoutSettings.visibility = if (visible) View.GONE else View.VISIBLE
            holder.toggleButton.text = if (visible) "▼ 상세 설정" else "▲ 상세 설정"
        }

        // 이름 수정 감지
        holder.lockName.setOnClickListener {
            // 클릭하면 이름 수정 로직 추가할 수도 있음
        }

        // 설명 수정 감지
        holder.description.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                item.description = holder.description.text.toString()
            }
        }

        // 체크박스 체크 변화 감지
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            item.isChecked = isChecked
        }
    }

    override fun getItemCount(): Int = items.size

    // --- 편집 모드 on/off ---
    fun setEditMode(enabled: Boolean) {
        isEditMode = enabled
        notifyDataSetChanged()
    }

    // --- 전체 선택 / 선택 해제 ---
    fun selectAll(select: Boolean) {
        items.forEach { it.isChecked = select }
        notifyDataSetChanged()
    }

    // --- 체크된 항목 삭제 ---
    fun removeCheckedItems() {
        items.removeAll { it.isChecked == true }
        notifyDataSetChanged()
    }

    // --- 아이템 추가 ---
    fun addItem() {
        items.add(
            Preset(name = "새 프리셋")
        )
        notifyItemInserted(items.size - 1)
    }

    // --- 아이템 이동 (드래그) ---
    fun moveItem(fromPosition: Int, toPosition: Int) {
        val item = items.removeAt(fromPosition)
        items.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
    }
    fun updateItems(newItems: List<Preset>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    // --- 현재 아이템 리스트 리턴 ---
    fun getItems(): List<Preset> = items
}
