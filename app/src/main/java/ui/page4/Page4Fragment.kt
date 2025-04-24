package ui.page4

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cap.locktask.R
import com.cap.locktask.utils.UsageStatsHelper
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import viewmodel.UsageStatData
import viewmodel.UsageStatsAdapter

class Page4Fragment : Fragment(R.layout.b_fragment_page4) {

    private lateinit var usageBarChart: BarChart
    private lateinit var usageListRecyclerView: RecyclerView
    private lateinit var adapter: UsageStatsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        usageBarChart = view.findViewById(R.id.usageBarChart)
        usageListRecyclerView = view.findViewById(R.id.usageListRecyclerView)

        val usageStatsList = UsageStatsHelper.getUsageStats(requireContext())
            .sortedByDescending { it.usageTime }


        setupBarChart(usageStatsList)

        adapter = UsageStatsAdapter(requireContext(), usageStatsList)
        usageListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        usageListRecyclerView.adapter = adapter
    }

    private fun setupBarChart(data: List<UsageStatData>) {
        val entries = data.mapIndexed { index, usage ->
            BarEntry(index.toFloat(), usage.usageTime.toFloat() / 60000f) // minutes
        }

        val barDataSet = BarDataSet(entries, "앱 사용 시간 (분)").apply {
            valueTextSize = 12f
            color = resources.getColor(R.color.teal_700, null)
        }

        val barData = BarData(barDataSet)

        // ✅ 막대 간격 및 너비 조정
        barData.barWidth = 0.6f

        usageBarChart.data = barData

        val xAxis = usageBarChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(data.map { it.appName })
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.labelRotationAngle = -45f // 라벨이 많을 경우 기울이기

        usageBarChart.axisLeft.axisMinimum = 0f
        usageBarChart.axisRight.isEnabled = false
        usageBarChart.description.isEnabled = false
        usageBarChart.legend.isEnabled = true

        usageBarChart.setVisibleXRangeMaximum(6f)  // 한 화면에 보여줄 막대 수
        usageBarChart.moveViewToX(0f) // 시작 위치

        usageBarChart.invalidate()
    }

}
