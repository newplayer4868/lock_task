package com.cap.locktask

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        val locationBtn = findViewById<Button>(R.id.btn_request_A_permissions)
        val overlayBtn = findViewById<Button>(R.id.btn_request_B_permissions)

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

    override fun onResume() {
        super.onResume()

        val locationGranted = isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        val overlayGranted = Settings.canDrawOverlays(this)

        updatePermissionButtonState(findViewById(R.id.btn_request_A_permissions), locationGranted)
        updatePermissionButtonState(findViewById(R.id.btn_request_B_permissions), overlayGranted)

        if (locationGranted && overlayGranted) {
            navigateToMain()
        }
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
