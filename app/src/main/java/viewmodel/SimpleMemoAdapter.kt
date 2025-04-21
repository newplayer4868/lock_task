package viewmodel

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cap.locktask.R
import model.MemoItem

class SimpleMemoAdapter(private val items: List<MemoItem>) :
    RecyclerView.Adapter<SimpleMemoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTitle: TextView = view.findViewById(R.id.textMemoTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_simple_memo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textTitle.text = items[position].title
    }
    override fun getItemCount(): Int {
        Log.d("SimpleMemoAdapter", "아이템 개수: ${items.size}")
        return items.size
    }


}
