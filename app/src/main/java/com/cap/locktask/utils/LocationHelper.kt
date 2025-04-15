package com.cap.locktask.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

object LocationHelper {

    private const val TAG = "LocationHelper"

    /**
     * 📍 최근 위치 우선 → 없으면 실시간 위치 요청
     */
    suspend fun getCurrentLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) {
            Log.w(TAG, "🚫 위치 권한 없음")
            return null
        }

        if (!isLocationServiceEnabled(context)) {
            Log.w(TAG, "⚠️ GPS 꺼져 있음")
            return null
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val tokenSource = CancellationTokenSource()

        return try {
            suspendCancellableCoroutine { cont ->
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    tokenSource.token
                ).addOnSuccessListener { location ->
                    cont.resume(location)
                }.addOnFailureListener {
                    cont.resume(null)
                }

                cont.invokeOnCancellation {
                    tokenSource.cancel()
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "🚫 위치 권한 예외: ${e.message}", e)
            null
        }

    }



    private fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationServiceEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestFreshLocation(
        context: Context,
        client: FusedLocationProviderClient
    ): Location? {
        Log.d(TAG, "📍 실시간 위치 요청 시도")

        val request = LocationRequest.create().apply {
            priority = Priority.PRIORITY_HIGH_ACCURACY
            numUpdates = 1
            interval = 1000L
            fastestInterval = 500L
            maxWaitTime = 3000L
        }

        return withTimeoutOrNull(20000) { // 타임아웃 20초
            suspendCancellableCoroutine { cont ->
                val callback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        val location = result.lastLocation
                        Log.d(TAG, "✅ 실시간 위치 획득: ${location?.latitude}, ${location?.longitude}")
                        Log.d(TAG, "🤖 가상 위치 여부: ${location?.isFromMockProvider}")
                        client.removeLocationUpdates(this)
                        cont.resume(location)
                    }

                    override fun onLocationAvailability(availability: LocationAvailability) {
                        Log.d(TAG, "📡 LocationAvailability: ${availability.isLocationAvailable}")
                        if (!availability.isLocationAvailable) {
                            Log.w(TAG, "❌ 위치 사용 불가 (LocationAvailability: false)")
                        }
                    }
                }

                client.requestLocationUpdates(
                    request,
                    callback,
                    Looper.getMainLooper()
                ).addOnSuccessListener {
                    Log.d(TAG, "📬 requestLocationUpdates 등록 성공")
                }.addOnFailureListener {
                    Log.e(TAG, "❌ requestLocationUpdates 등록 실패: ${it.message}", it)
                }

                cont.invokeOnCancellation {
                    client.removeLocationUpdates(callback)
                    Log.d(TAG, "🛑 위치 요청 취소됨")
                }
            }
        }.also {
            if (it == null) Log.w(TAG, "⏰ 위치 요청 실패 또는 타임아웃")
        }
    }

    // Task<Location> 안전하게 suspend 처리
    private suspend fun com.google.android.gms.tasks.Task<Location>.awaitSafe(): Location? {
        return suspendCancellableCoroutine { cont ->
            addOnSuccessListener { cont.resume(it) }
            addOnFailureListener { cont.resume(null) }
        }
    }
}
