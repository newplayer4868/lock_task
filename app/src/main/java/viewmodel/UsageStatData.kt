package viewmodel

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cap.locktask.R

data class UsageStatData(val appName: String, val icon: Drawable, val usageTime: Long)

class UsageStatsAdapter(
    private val context: Context,
    private val data: List<UsageStatData>
) : RecyclerView.Adapter<UsageStatsAdapter.UsageViewHolder>() {

    class UsageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.app_icon)
        val label: TextView = view.findViewById(R.id.app_name)
        val bar: ProgressBar = view.findViewById(R.id.usage_progress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_usage_stat, parent, false)
        return UsageViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsageViewHolder, position: Int) {
        val item = data[position]
        val max = data.maxOfOrNull { it.usageTime } ?: 1
        holder.icon.setImageDrawable(item.icon)
        holder.label.text = item.appName
        holder.bar.max = max.toInt()
        holder.bar.progress = item.usageTime.toInt()
    }

    override fun getItemCount(): Int = data.size
}
