package com.android.applens.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.android.applens.model.AppInfo
import com.android.applens.R
import com.android.applens.utils.drawableToBitmap
import java.util.concurrent.TimeUnit

class AppRepository(private val context: Context) {

    fun getInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        val usageManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(30)

        val usageStats = usageManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        val usageMap = usageStats.associateBy { it.packageName }

        return apps
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            .map {
                val lastUsed = usageMap[it.packageName]?.lastTimeUsed ?: getLastModifiedTime(it.packageName)
                val versionName = try {
                    pm.getPackageInfo(it.packageName, 0).versionName ?: "N/A"
                } catch (e: Exception) {
                    "N/A"
                }

                val iconDrawable = try {
                    pm.getApplicationIcon(it)
                } catch (e: Exception) {
                    ContextCompat.getDrawable(context, R.drawable.ic_default_app)
                }

                val iconBitmap = iconDrawable?.let { drawableToBitmap(it) }

                AppInfo(
                    appName = pm.getApplicationLabel(it).toString(),
                    packageName = it.packageName,
                    lastUsedTime = lastUsed,
                    versionName = versionName
                ).also { info ->
                    info.iconBitmap = iconBitmap
                }
            }
            .sortedBy { it.appName.lowercase() }
    }

    private fun getLastModifiedTime(packageName: String): Long {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getPackageInfo(packageName, 0)
            appInfo.lastUpdateTime
        } catch (e: Exception) {
            0L
        }
    }
}
