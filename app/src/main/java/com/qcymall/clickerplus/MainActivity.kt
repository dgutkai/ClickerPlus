package com.qcymall.clickerplus

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import com.inuker.bluetooth.library.BluetoothClient
import com.inuker.bluetooth.library.beacon.Beacon
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener
import com.inuker.bluetooth.library.search.SearchRequest
import com.inuker.bluetooth.library.search.SearchResult
import com.inuker.bluetooth.library.search.response.SearchResponse
import com.inuker.bluetooth.library.utils.BluetoothLog
import com.qcymall.clickerpluslibrary.ClickerPlus
import com.qcymall.clickerpluslibrary.ClickerPlusListener
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var deviceListView: ListView
    private lateinit var deviceAdapter: SimpleAdapter
    private lateinit var deviceData: ArrayList<HashMap<String, Any>>

    lateinit var mBluetoothClien: BluetoothClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2)

        }

        mBluetoothClien = BluetoothClient(this)
        ClickerPlus.mClickerPlusListener = mListener

        initDeviceListView()
        scanBleDevice()
    }

    private fun initDeviceListView() {
        refreshLayout = findViewById(R.id.swipelayout)
        refreshLayout.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener { scanBleDevice() })
        deviceListView = findViewById(R.id.devicelist)
        deviceData = ArrayList()
        deviceAdapter = SimpleAdapter(this, deviceData, R.layout.item_devicelist, arrayOf("name", "mac"),
                intArrayOf(R.id.name, R.id.mac))
        deviceListView.setAdapter(deviceAdapter)
        deviceListView.setOnItemClickListener(AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val detialIntent = Intent(baseContext, LibraryDemoActivity::class.java)
            detialIntent.putExtra("data", deviceData.get(i))
            startActivity(detialIntent)
        })
    }

    private fun scanBleDevice() {
        deviceData.clear()
        deviceAdapter.notifyDataSetChanged()
        refreshLayout.setRefreshing(true)
        val request = SearchRequest.Builder()
                .searchBluetoothLeDevice(3000, 3)   // 先扫BLE设备3次，每次3s
                //                .searchBluetoothClassicDevice(5000) // 再扫经典蓝牙5s
                //                .searchBluetoothLeDevice(2000)      // 再扫BLE设备2s
                .build()

        mBluetoothClien.search(request, object : SearchResponse {
            override fun onSearchStarted() {

            }

            override fun onDeviceFounded(device: SearchResult) {
                val beacon = Beacon(device.scanRecord)
                if (!isHaveDevice(device.address)) {
                    val data = HashMap<String, Any>()
                    data.put("name", device.name)
                    data.put("mac", device.address)
                    data.put("btdevice", device.device)
                    deviceData.add(data)
                    deviceAdapter.notifyDataSetChanged()
                    BluetoothLog.e(String.format("beacon for %s\n%s", device.address, beacon.toString()))
                }
            }

            override fun onSearchStopped() {
                refreshLayout.setRefreshing(false)
            }

            override fun onSearchCanceled() {
                refreshLayout.setRefreshing(false)
            }
        })
    }

    private val mBluetoothStateListener = object : BluetoothStateListener() {
        override fun onBluetoothStateChanged(openOrClosed: Boolean) {
            if (openOrClosed) {
                scanBleDevice()
            }
            mBluetoothClien.unregisterBluetoothStateListener(this)
        }

    }

    private fun isHaveDevice(mac: String?): Boolean {
        if (mac == null) {
            return false
        }
        for (data in deviceData) {
            if (mac == data.get("mac")) {
                return true
            }
        }
        return false
    }

    val mListener = object: ClickerPlusListener{
        override fun onConnect(device: BluetoothDevice) {
            Log.e(TAG, device.name + ":" + device.address + " OnConnect")
        }

        override fun onDisconnect(device: BluetoothDevice) {
            Log.e(TAG, device.name + ":" + device.address + " onDisconnect")
        }

        override fun onPair(state: ClickerPlus.ClickerPlusState) {
            Log.e(TAG, "onPair")
        }

        override fun onCancelPair(state: ClickerPlus.ClickerPlusState) {
            Log.e(TAG, "onCancelPair")
        }

        override fun onConnectBack(state: ClickerPlus.ClickerPlusState) {
            Log.e(TAG, "onConnectBack")
        }

        override fun onClick() {
            Log.e(TAG, "onClick")
        }

        override fun onDoubleClick() {
            Log.e(TAG, "onDoubleClick")
        }

        override fun onLongClick() {
            Log.e(TAG, "onLongClick")
        }

        override fun onWeakup() {
            Log.e(TAG, "onWeakup")
        }

        override fun onIdeaCapsule() {
            Log.e(TAG, "onIdeaCapsule")
        }

        override fun onVoicePCM(data: ByteArray, isEnd: Boolean) {
            Log.e(TAG, "onVoicePCM datalength = " + data.size)
        }

        override fun onIdeaPCM(data: ByteArray, isEnd: Boolean) {
            Log.e(TAG, "onIdeaPCM datalength = " + data.size)
        }

        override fun onBatteryChange(percent: Int) {
            Log.e(TAG, "onBatteryChange percent = " + percent)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1, 2 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    println("User granted permission")

                } else {
                    println("User didn't grante permission")

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
        }
    }
}
