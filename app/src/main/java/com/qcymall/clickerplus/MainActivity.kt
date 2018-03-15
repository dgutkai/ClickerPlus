package com.qcymall.clickerplus

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
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
import android.widget.Toast
import com.inuker.bluetooth.library.BluetoothClient
import com.inuker.bluetooth.library.beacon.Beacon
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener
import com.inuker.bluetooth.library.search.SearchRequest
import com.inuker.bluetooth.library.search.SearchResult
import com.inuker.bluetooth.library.search.response.SearchResponse
import com.inuker.bluetooth.library.utils.BluetoothLog
import com.inuker.bluetooth.library.utils.ByteUtils
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
        ClickerPlus.scanDevice(object : SearchResponse {
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
                refreshLayout.isRefreshing = false
            }

            override fun onSearchCanceled() {
                refreshLayout.isRefreshing = false
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
            if (mac == data["mac"]) {
                return true
            }
        }
        return false
    }



    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1, 2 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

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

    val mListener = object: ClickerPlusListener {
        override fun onFindPhone() {
            Log.e(TAG, "寻找手机中 ")
        }

        @SuppressLint("SetTextI18n")
        override fun onDataReceive(info: String) {
        }

        override fun onConnect(deviceMac: String) {
            Log.e(TAG, deviceMac + " OnConnect")
            val detialIntent = Intent(baseContext, LibraryDemoActivity::class.java)
            val btAdapter = BluetoothAdapter.getDefaultAdapter()
            val device = btAdapter.getRemoteDevice(deviceMac)
            val data = HashMap<String, Any>()
            data.put("name", device.name)
            data.put("mac", device.address)
            data.put("btdevice", device)
            detialIntent.putExtra("data", data)
            startActivity(detialIntent)
        }

        override fun onDisconnect(deviceMac: String) {
            Log.e(TAG, deviceMac + " onDisconnect")
        }

        override fun onPair(state: ClickerPlus.ClickerPlusState) {
            Log.e(TAG, "onPair " + state.name)


        }

        override fun onCancelPair(state: ClickerPlus.ClickerPlusState) {
            Log.e(TAG, "onCancelPair " + state.name)

        }

        override fun onConnectBack(state: ClickerPlus.ClickerPlusState) {
            Log.e(TAG, "onConnectBack " + state.name)

        }

        override fun onClick() {
            Log.e(TAG, "onClick")
        }

        override fun onDoubleClick() {
            Log.e(TAG, "onDoubleClick")
        }

        override fun onLongPress() {
            Log.e(TAG, "onLongPress")
        }

        override fun onWeakup() {
            Log.e(TAG, "onWeakup")
        }

        override fun onIdeaCapsule() {
            Log.e(TAG, "onIdeaCapsule")
        }
        override fun onVoicePCMStart() {
            Log.e(TAG, "onVoicePCMStart")
        }

        override fun onVoicePCMEnd() {
            Log.e(TAG, "onVoicePCMEnd")
        }
        override fun onVoicePCM(data: ByteArray, index: Int) {
            Log.e(TAG, String.format("onVoicePCM  current Index = %d, Voice PCM Data: %s", index, ByteUtils.byteToString(data)))

        }

        override fun onIdeaPCMStart(header: String) {
            Log.e(TAG, "onIdeaPCMStart " + header)
        }

        override fun onIdeaPCMEnd(info: ByteArray?) {
            Log.e(TAG, "onIdeaPCMEnd " + ByteUtils.byteToString(info))
        }
        override fun onIdeaPCM(data: ByteArray, index: Int) {
            Log.e(TAG, String.format("onIdeaPCM  current Index = %d, Voice PCM Data: %s", index, ByteUtils.byteToString(data)))
        }

        override fun onBatteryChange(percent: Int) {
            Log.e(TAG, "onBatteryChange percent = " + percent)
        }

        override fun onOTAStart(deviceMac: String) {
            Log.e(TAG, deviceMac + " onOTAStart")
        }

        override fun onOTAProgressChanged(deviceMac: String, percent: Int, speed: Float, avgSpeed: Float, currentPart: Int, partsTotal: Int) {
            Log.e(TAG, "onProgressChanged " + deviceMac + " progress:" + percent + " speed:" + speed +
                    " avgSpeed:" + avgSpeed + " currentPart:" + currentPart + " partsTotal:" + partsTotal)
        }

        override fun onOTACompleted(deviceMac: String) {
            Log.e(TAG, deviceMac + " onOTACompleted")
        }

        override fun onOTAError(deviceMac: String, error: Int, errorType: Int, message: String?) {
            Log.e(TAG, "onError " + deviceMac + " error:" + error + " errorType:" + errorType + " message:" + message)
        }
    }
}
