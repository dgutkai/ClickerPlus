package com.qcymall.clickerplus

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import com.inuker.bluetooth.library.search.SearchResult
import com.inuker.bluetooth.library.search.response.SearchResponse
import com.inuker.bluetooth.library.utils.ByteUtils
import com.qcymall.clickerpluslibrary.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var deviceListView: ListView
    private lateinit var deviceAdapter: SimpleAdapter
    private lateinit var deviceData: ArrayList<HashMap<String, Any>>

//    lateinit var mBluetoothClien: BluetoothClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2)

        }
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 2)

        }

//        mBluetoothClien = BluetoothClient(this)

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
        ClickerPlus.scanDevice(object: SearchResponse{

            override fun onSearchStarted() {
                Log.e(TAG, "start")
            }

            override fun onDeviceFounded(device: SearchResult) {
                if (!isHaveDevice(device.address)) {
                    val data = HashMap<String, Any>()
                    data.put("name", device.name)
                    data.put("mac", device.address)
                    data.put("btdevice", device.device)
                    deviceData.add(data)
                    deviceAdapter.notifyDataSetChanged()
                    Log.e(TAG, String.format("beacon for %s", device.address))
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

//    private val mBluetoothStateListener = object : BluetoothStateListener() {
//        override fun onBluetoothStateChanged(openOrClosed: Boolean) {
//            if (openOrClosed) {
//                scanBleDevice()
//            }
//            mBluetoothClien.unregisterBluetoothStateListener(this)
//        }
//
//    }

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
//        override fun onDataReceive(info: String) {
//
//        }

        // 语音指令缓存上传
        override fun onVoiceTmpPCMStart(header: String) {

            Log.e(TAG, "onVoiceTmpPCMStart " + header)
        }

        override fun onVoiceTmpPCM(data: ByteArray, index: Int) {
            Log.e(TAG, "onVoiceTmpPCM " + index)
        }

        override fun onVoiceTmpPCMEnd(info: ByteArray?) {
            Log.e(TAG, "onVoiceTmpPCMEnd ")
        }

        override fun onFindPhone() {
            Log.e(TAG, "寻找手机中 ")
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

        override fun onVersion(version: String) {
            Log.e(TAG, "onVersion " + version)
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

        override fun onOTAProgressChanged(deviceMac: String, percent: Int) {
            Log.e(TAG, "onProgressChanged " + deviceMac + " progress:" + percent)
        }

        override fun onOTACompleted(deviceMac: String) {
            Log.e(TAG, deviceMac + " onOTACompleted")
        }

        override fun onOTAError(deviceMac: String, error: Int, errorType: Int, message: String?) {
            Log.e(TAG, "onError " + deviceMac + " error:" + error + " errorType:" + errorType + " message:" + message)
        }
    }
}
