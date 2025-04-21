package model

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.cap.locktask.R
import java.util.*

class MemoAdapter(
    private val items: MutableList<MemoItem>
) : RecyclerView.Adapter<MemoAdapter.MemoViewHolder>() {

    inner class MemoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val editTextTitle: EditText = itemView.findViewById(R.id.editTextTitle)
        val editTextDescription: EditText = itemView.findViewById(R.id.editTextDescription)
        val textDueDate: TextView = itemView.findViewById(R.id.textDueDate)
        val textDueTime: TextView = itemView.findViewById(R.id.textDueTime)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)

        val textExpandToggle: TextView = itemView.findViewById(R.id.textExpandToggle)
        val layoutDetailSettings: LinearLayout = itemView.findViewById(R.id.layoutDetailSettings)

        val textCategory: TextView = itemView.findViewById(R.id.textCategory)
        val layoutCategoryOptions: LinearLayout = itemView.findViewById(R.id.layoutCategoryOptions)

        val optionUrgentImportant: TextView = itemView.findViewById(R.id.optionUrgentImportant)
        val optionUrgent: TextView = itemView.findViewById(R.id.optionUrgent)
        val optionImportant: TextView = itemView.findViewById(R.id.optionImportant)
        val optionNone: TextView = itemView.findViewById(R.id.optionNone)

        val allOptions = listOf(
            optionUrgentImportant,
            optionUrgent,
            optionImportant,
            optionNone
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_memo, parent, false)
        return MemoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        val item = items[position]

        holder.editTextTitle.setText(item.title)
        holder.editTextDescription.setText(item.description)
        holder.textDueDate.text = item.dueDate.ifEmpty { "기한 선택" }
        holder.textDueTime.text = item.dueTime.ifEmpty { "시간 선택" }
        holder.checkBox.isChecked = item.isChecked

        holder.textExpandToggle.setOnClickListener {
            val isVisible = holder.layoutDetailSettings.visibility == View.VISIBLE
            holder.layoutDetailSettings.visibility = if (isVisible) View.GONE else View.VISIBLE
            holder.textExpandToggle.text = if (isVisible) "▼ 상세 설정" else "▲ 상세 설정"
        }

        holder.textCategory.setOnClickListener {
            holder.layoutCategoryOptions.visibility =
                if (holder.layoutCategoryOptions.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        holder.allOptions.forEach { optionView ->
            optionView.setOnClickListener {
                val selectedCategory = optionView.text.toString()
                item.category = selectedCategory
                updateCategorySelection(holder, selectedCategory)
                holder.layoutCategoryOptions.visibility = View.GONE
            }
        }

        holder.editTextTitle.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                item.title = holder.editTextTitle.text.toString()
            }
        }

        holder.editTextDescription.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) item.description = holder.editTextDescription.text.toString()
        }

        holder.textDueDate.setOnClickListener {
            val context = holder.itemView.context
            val cal = Calendar.getInstance()
            DatePickerDialog(context,
                { _, year, month, day ->
                    val dateStr = "%04d-%02d-%02d".format(year, month + 1, day)
                    item.dueDate = dateStr
                    holder.textDueDate.text = dateStr
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        holder.textDueTime.setOnClickListener {
            val context = holder.itemView.context
            val cal = Calendar.getInstance()
            TimePickerDialog(context,
                { _, hour, minute ->
                    val timeStr = "%02d:%02d".format(hour, minute)
                    item.dueTime = timeStr
                    holder.textDueTime.text = timeStr
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            item.isChecked = isChecked
        }

        updateCategorySelection(holder, item.category)
    }

    private fun updateCategorySelection(holder: MemoViewHolder, selected: String) {
        holder.allOptions.forEach { option ->
            option.setBackgroundColor(
                if (option.text.toString() == selected)
                    Color.parseColor("#FFDD55")
                else
                    Color.parseColor("#E0E0E0")
            )
        }
        holder.textCategory.text = "분류: $selected"
    }

    override fun getItemCount(): Int = items.size
    fun updateItems(newList: List<MemoItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun addItem() {
        items.add(
            MemoItem(
                title = "새 메모",
                description = "",
                dueDate = "",
                dueTime = "",
                category = "둘 다 아님",
                order = items.size + 1
            )
        )
        notifyItemInserted(items.size - 1)
    }

    fun removeCheckedItems() {
        items.removeAll { it.isChecked }
        updateOrderValues()
        notifyDataSetChanged()
    }

    fun selectAll(select: Boolean) {
        items.forEach { it.isChecked = select }
        notifyDataSetChanged()
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val movedItem = items.removeAt(fromPosition)
        items.add(toPosition, movedItem)
        notifyItemMoved(fromPosition, toPosition)
        updateOrderValues() // ✔️ 순서 업데이트!
    }

    private fun updateOrderValues() {
        items.forEachIndexed { index, item ->
            item.order = index + 1
        }
    }
    fun getItems(): List<MemoItem> = items

}
