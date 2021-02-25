package com.hailiao.gaodemapdemo.activity

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.hailiao.gaodemapdemo.R
import com.hailiao.gaodemapdemo.databinding.ActDeviceStatusBinding
import com.hailiao.gaodemapdemo.utils.getSystemBattery
import com.hailiao.gaodemapdemo.utils.toDateStr
import kotlinx.coroutines.*


class DeviceStatusActivity : AppCompatActivity() {

    private lateinit var binding: ActDeviceStatusBinding

    private var locationClient: AMapLocationClient? = null

    private var sensorManager: SensorManager? = null

    private var checkBatteryJob: Job = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.act_device_status)

        initSatellite()

        initLocation()

        initSensor()

        initBattery()
    }

    override fun onDestroy() {
        locationClient?.stopLocation()
        locationClient?.onDestroy()
        sensorManager?.unregisterListener(pressureSensorEventListener)
        checkBatteryJob.cancel()
        super.onDestroy()
    }

    private fun initLocation() {
        val mLocationClient = AMapLocationClient(application)
        val mLocationOption = AMapLocationClientOption().apply {
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            interval = 5000
        }
        mLocationClient.setLocationOption(mLocationOption)
        mLocationClient.setLocationListener {
            val builder = StringBuilder()
                .append("经度=${it.longitude}\n")
                .append("纬度=${it.latitude}\n")
                .append("高度=${it.altitude}\n")
                .append("卫星授时=${it.time.toDateStr()}\n")
            binding.locationStatusText = builder.toString()
        }
        mLocationClient.startLocation()
        locationClient = mLocationClient
    }

    @SuppressLint("MissingPermission")
    private fun initSatellite() {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            manager.registerGnssStatusCallback(object : GnssStatus.Callback(){
                override fun onSatelliteStatusChanged(status: GnssStatus?) {
                    super.onSatelliteStatusChanged(status)
                    status?: return
                    //解析组装卫星信息
                    makeGnssStatus(status, status.satelliteCount)
                }
            })
        }
        //1000位最小的时间间隔，1为最小位移变化；也就是说每隔1000ms会回调一次位置信息
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, object : LocationListener{
            override fun onLocationChanged(location: Location?) {
                location?: return
                val builder = StringBuilder()
                    .append("经度=${location.longitude}\n")
                    .append("纬度=${location.latitude}\n")
                    .append("高度=${location.altitude}\n")
                binding.locationStatusText = builder.toString()
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String?) {}
            override fun onProviderDisabled(provider: String?) {}
        })
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun makeGnssStatus(status: GnssStatus, satelliteCount: Int) {
        //当前可以获取到的卫星总数，然后遍历
        var unknown = 0
        var gps = 0
        var sbas = 0
        var glonass = 0
        var qzss = 0
        var beidou = 0
        var galileo = 0
        var irnss = 0
        if (satelliteCount > 0) {
            for (i in 0 until satelliteCount) {
                //GnssStatus的大部分方法参数传入的就是卫星数量的角标
                //获取卫星类型
                val type = status.getConstellationType(i)
                val hz = status.getCn0DbHz(i)
                when(type) {
                    GnssStatus.CONSTELLATION_GPS -> gps ++
                    GnssStatus.CONSTELLATION_SBAS -> sbas ++
                    GnssStatus.CONSTELLATION_GLONASS -> glonass ++
                    GnssStatus.CONSTELLATION_QZSS -> qzss ++
                    GnssStatus.CONSTELLATION_BEIDOU -> beidou ++
                    GnssStatus.CONSTELLATION_GALILEO -> galileo ++
                    GnssStatus.CONSTELLATION_IRNSS -> irnss ++
                    else -> unknown ++
                }
                Log.e("测试", "卫星index=$i, 卫星类型=$type, 信号=$hz")
            }
        }
        val builder = StringBuilder()
            .append("卫星总数=$satelliteCount\n")
            .append("GPS卫星=$gps\n")
            .append("SBAS卫星=$sbas\n")
            .append("GLONASS卫星=$glonass\n")
            .append("QZSS卫星=$qzss\n")
            .append("BEIDOU卫星=$beidou\n")
            .append("GALILEO卫星=$galileo\n")
            .append("IRNSS卫星=$irnss\n")
            .append("未知卫星=$unknown\n")
        binding.satelliteStatusText = builder.toString()
    }

    private fun initSensor() {
        val mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val pressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        mSensorManager.registerListener(pressureSensorEventListener, pressure, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private val pressureSensorEventListener = object : SensorEventListener{
        override fun onSensorChanged(event: SensorEvent?) {
            event?: return
            val millibarsOfPressure = event.values[0]
            binding.sensorStatusText = "气压=$millibarsOfPressure"
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private fun initBattery() {
        checkBatteryJob = GlobalScope.launch {
            while (isActive) {
                val battery = getSystemBattery()
                withContext(Dispatchers.Main) {
                    binding.batteryText = "设备电量=$battery"
                }
                delay(10 * 1000)
            }
        }
    }
}