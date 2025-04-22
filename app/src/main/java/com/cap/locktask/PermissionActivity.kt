package com.cap.locktask

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import service.AppMonitorService

class PermissionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        val locationBtn = findViewById<Button>(R.id.btn_request_A_permissions)
        val overlayBtn = findViewById<Button>(R.id.btn_request_B_permissions)
        val accessibilityBtn = findViewById<Button>(R.id.btn_request_C_permissions)
        val usageStatsBtn = findViewById<Button>(R.id.btn_request_D_permissions)

        // ✅ 모든 버튼 초기 색상 반영
        updatePermissionButtonState(locationBtn, isGranted(Manifest.permission.ACCESS_FINE_LOCATION))
        updatePermissionButtonState(overlayBtn, Settings.canDrawOverlays(this))
        updatePermissionButtonState(accessibilityBtn, isAccessibilityServiceEnabled())
        updatePermissionButtonState(usageStatsBtn, isUsageStatsPermissionGranted())
        usageStatsBtn.setOnClickListener {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // 꼭 추가
            }
            startActivity(intent)
        }


        accessibilityBtn.setOnClickListener {
            openAccessibilitySettings()
        }
        // 초기 색상
        updatePermissionButtonState(locationBtn, isGranted(Manifest.permission.ACCESS_FINE_LOCATION))
        updatePermissionButtonState(overlayBtn, Settings.canDrawOverlays(this))

        // 위치 권한 요청
        locationBtn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // 먼저 ACCESS_FINE_LOCATION 요청
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            } else {
                // Android 9 이하는 바로 허용
                navigateToMain()
            }
        }


        // 오버레이 권한 요청
        overlayBtn.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 101) {
            val granted = grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
            val btn = findViewById<Button>(R.id.btn_request_A_permissions)
            updatePermissionButtonState(btn, granted)

            // 만약 Android 10+ 이고 권한이 허용되었지만 Background 권한이 필요하다면 안내
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && granted) {
                Toast.makeText(this, "항상 허용으로 설정하려면 권한 설정 화면으로 이동해주세요.", Toast.LENGTH_LONG).show()
                openLocationPermissionSettings() // 🔥 설정 앱으로 이동
            }
        }

    }
    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName = ComponentName(this, AppMonitorService::class.java.name)
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.split(":")?.any { it == expectedComponentName.flattenToString() } == true
    }


    override fun onResume() {
        super.onResume()

        Handler(Looper.getMainLooper()).postDelayed({
            val locationGranted = isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
            val overlayGranted = Settings.canDrawOverlays(this)
            val accessibilityGranted = isAccessibilityServiceEnabled()
            val usageGranted = isUsageStatsPermissionGranted()

            updatePermissionButtonState(findViewById(R.id.btn_request_A_permissions), locationGranted)
            updatePermissionButtonState(findViewById(R.id.btn_request_B_permissions), overlayGranted)
            updatePermissionButtonState(findViewById(R.id.btn_request_C_permissions), accessibilityGranted)
            updatePermissionButtonState(findViewById(R.id.btn_request_D_permissions), usageGranted)

            if (locationGranted && overlayGranted && accessibilityGranted && usageGranted) {
                navigateToMain()
            }
        }, 500)
    }

    fun isUsageStatsPermissionGranted(): Boolean {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = end - 1000L * 60 * 60 * 24 * 7  // 최근 7일 사용 기록 확인

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, start, end
        )

        stats?.forEach {
            Log.d("PermissionCheck", "📱 앱: ${it.packageName}, 사용시간: ${it.totalTimeInForeground / 1000}s")
        }

        val granted = stats != null && stats.any { it.totalTimeInForeground > 0 }
        Log.d("PermissionCheck", "✅ 사용량 권한 granted: $granted")

        return granted
    }


    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    private fun openLocationPermissionSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }


    private fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun updatePermissionButtonState(button: Button, granted: Boolean) {
        val color = if (granted) 0xFF2196F3.toInt() else 0xFF4CAF50.toInt()
        button.setBackgroundColor(color)
    }
}
