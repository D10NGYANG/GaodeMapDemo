package com.hailiao.gaodemapdemo.activity

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.eazypermissions.common.model.PermissionResult
import com.eazypermissions.coroutinespermission.PermissionManager
import com.hailiao.gaodemapdemo.R
import com.hailiao.gaodemapdemo.databinding.ActMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import android.content.Intent
import com.amap.api.maps.offlinemap.OfflineMapActivity
import com.hailiao.gaodemapdemo.utils.goTo


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.act_main)

        binding.mapView.onCreate(savedInstanceState)

        val myLocationStyle = MyLocationStyle().apply {
            interval(1000)
            myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER)
            showMyLocation(true)
        }
        var first = true
        binding.mapView.map.apply {
            this.myLocationStyle = myLocationStyle
            this.isMyLocationEnabled = true
            this.isTrafficEnabled = false
        }.setOnMyLocationChangeListener {
            if (first) {
                first = false
                val cameraUpdate = CameraUpdateFactory.newCameraPosition(CameraPosition(LatLng(it.latitude, it.longitude), 18f, 0f, 0f))
                binding.mapView.map.animateCamera(cameraUpdate)
            }
        }

        // 获取定位权限
        GlobalScope.launch {
            checkLocationPermission()
        }

        // 点击定位
        binding.btnLocation.setOnClickListener {
            val map = binding.mapView.map
            val locData = map.myLocation
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(CameraPosition(LatLng(locData.latitude, locData.longitude), 18f, 0f, 0f))
            map.animateCamera(cameraUpdate)
        }

        // 切换地图样式
        binding.togLayer.setOnCheckedChangeListener { _, isChecked ->
            binding.mapView.map.mapType = if (isChecked) {
                AMap.MAP_TYPE_SATELLITE
            } else {
                AMap.MAP_TYPE_NORMAL
            }
        }

        // 点击离线地图
        binding.btnOffline.setOnClickListener {
            startActivity(Intent(this.applicationContext, OfflineMapActivity::class.java))
        }

        // 点击设备状态
        binding.btnDeviceStatus.setOnClickListener {
            goTo(DeviceStatusActivity::class.java)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }
}

/**
 * 检查定位权限
 * @receiver BaseActivity
 * @return Boolean
 */
suspend fun AppCompatActivity.checkLocationPermission(): Boolean {
    return suspendCoroutine { cont ->
        GlobalScope.launch {
            // 请求定位权限
            val permissionResult = PermissionManager.requestPermissions(
                this@checkLocationPermission,
                1,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (permissionResult is PermissionResult.PermissionGranted) {
                // 请求成功
                cont.resume(true)
            } else {
                cont.resume(false)
            }
        }
    }
}