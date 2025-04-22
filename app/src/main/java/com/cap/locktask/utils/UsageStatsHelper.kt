package com.cap.locktask.utils

import android.app.usage.UsageStatsManager
import android.content.Context
import viewmodel.UsageStatData

object UsageStatsHelper {
    fun getUsageStats(context: Context): List<UsageStatData> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = end - 1000 * 60 * 60 * 24

        return usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
            .filter { it.totalTimeInForeground > 0 }
            .mapNotNull {
                val appName = try {
                    val appInfo = context.packageManager.getApplicationInfo(it.packageName, 0)
                    context.packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    return@mapNotNull null
                }
                val icon = context.packageManager.getApplicationIcon(it.packageName)
                UsageStatData(appName, icon, it.totalTimeInForeground)
            }
    }
}
