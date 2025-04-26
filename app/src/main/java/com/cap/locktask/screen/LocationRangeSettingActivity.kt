package com.cap.locktask.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.cap.locktask.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.SupportMapFragment

class LocationRangeSettingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var searchView: SearchView
    private lateinit var radiusTextView: TextView
    private lateinit var seekBar: SeekBar

    private var selectedLocation: LatLng? = null
    private var rangeCircle: Circle? = null

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.m_activity_location_range_setting)

        // 지도 준비
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        searchView = findViewById(R.id.search_view)
        radiusTextView = findViewById(R.id.radiusTextView)
        seekBar = findViewById(R.id.rangeSeekBar)

        // 기본값
        seekBar.max = 500
        seekBar.min = 10
        seekBar.progress = 100
        radiusTextView.text = "범위: 100m"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                radiusTextView.text = "범위: ${progress}m"
                updateRangeCircle()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    searchLocation(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?) = false
        })

        // 위치 확인 → 결과 반환
        findViewById<Button>(R.id.confirm_button).setOnClickListener {
            returnLocationResult()
        }

        findViewById<Button>(R.id.confirmRangeButton).setOnClickListener {
            returnLocationResult()
        }
    }

    private fun returnLocationResult() {
        if (selectedLocation != null) {
            val resultIntent = Intent().apply {
                putExtra("latitude", selectedLocation!!.latitude)
                putExtra("longitude", selectedLocation!!.longitude)
                putExtra("radius", seekBar.progress)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        } else {
            Toast.makeText(this, "위치를 선택해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchLocation(query: String) {
        val geocoder = android.location.Geocoder(this)
        val addresses = geocoder.getFromLocationName(query, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val latLng = LatLng(address.latitude, address.longitude)
            moveToLocation(latLng)
        } else {
            Toast.makeText(this, "위치를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun moveToLocation(latLng: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(latLng).title("선택된 위치"))
        selectedLocation = latLng
        updateRangeCircle()
    }

    private fun updateRangeCircle() {
        if (selectedLocation != null) {
            rangeCircle?.remove()
            rangeCircle = mMap.addCircle(
                CircleOptions()
                    .center(selectedLocation!!)
                    .radius(seekBar.progress.toDouble())
                    .strokeColor(0x550000FF)
                    .fillColor(0x220000FF)
            )
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val seoul = LatLng(37.5665, 126.9780)
        mMap.addMarker(MarkerOptions().position(seoul).title("서울"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 20f))

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("선택된 위치"))
            selectedLocation = latLng
            updateRangeCircle()
        }
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        mMap.isMyLocationEnabled = true
    }
}
