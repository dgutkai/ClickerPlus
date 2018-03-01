package com.qcymall.clickerplus

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.qcymall.clickerpluslibrary.ClickerPlus
import com.qcymall.clickerpluslibrary.ClickerPlusListener
import com.qcymall.clickerpluslibrary.SharedPreferencesUtils
import java.util.HashMap

/**
 * Created by lanmi on 2018/2/27.
 */
class LibraryDemoActivity: AppCompatActivity()  {

    private val TAG = "LibraryDemoActivity"
    private var mDeviceMAC: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_librarydemo)
        val mapData = intent.getSerializableExtra("data") as HashMap<String, Any>
        mDeviceMAC = mapData["mac"] as String
        Log.e(TAG, mapData["name"] as String?)
        ClickerPlus.mClickerPlusListener = mListener
    }

    fun pairClick(v: View){
        val result = ClickerPlus.pairDevice(mDeviceMAC!!, "123456")
        if (!result){
            Toast.makeText(this, "当前已经连接设备，无需配对", Toast.LENGTH_SHORT).show()
        }
    }

    fun unpairClick(v: View){
        ClickerPlus.unPairDevice(false)
    }
    fun findClick(v: View){
        ClickerPlus.findDevice()
    }
    fun batteryClick(v: View){
        ClickerPlus.getBattery()
    }

    val mListener = object: ClickerPlusListener {
        override fun onConnect(deviceMac: String) {
            Log.e(TAG, deviceMac + " OnConnect")
        }

        override fun onDisconnect(deviceMac: String) {
            Log.e(TAG, deviceMac + " onDisconnect")
        }

        override fun onPair(state: ClickerPlus.ClickerPlusState) {
            Log.e(TAG, "onPair " + state.name)
            when(state){
                ClickerPlus.ClickerPlusState.success ->{
                    Toast.makeText(this@LibraryDemoActivity, "配对成功", Toast.LENGTH_SHORT).show()
                }
                ClickerPlus.ClickerPlusState.pairing ->{
                    Toast.makeText(this@LibraryDemoActivity, "正在配对，请按一下设备上的按键。", Toast.LENGTH_SHORT).show()
                }
                ClickerPlus.ClickerPlusState.fail -> {
                    Toast.makeText(this@LibraryDemoActivity, "配对超时", Toast.LENGTH_SHORT).show()
                }
            }

        }

        override fun onCancelPair(state: ClickerPlus.ClickerPlusState) {
            Log.e(TAG, "onCancelPair " + state.name)
            when(state){
                ClickerPlus.ClickerPlusState.success ->{
                    Toast.makeText(this@LibraryDemoActivity, "取消配对成功", Toast.LENGTH_SHORT).show()
                }
                ClickerPlus.ClickerPlusState.fail -> {
                    Toast.makeText(this@LibraryDemoActivity, "取消配对失败", Toast.LENGTH_SHORT).show()
                }
                else -> {
                }
            }
        }

        override fun onConnectBack(state: ClickerPlus.ClickerPlusState) {
            Log.e(TAG, "onConnectBack " + state.name)

            when(state){
                ClickerPlus.ClickerPlusState.success ->{
                    Toast.makeText(this@LibraryDemoActivity, "回连成功", Toast.LENGTH_SHORT).show()
                }
                ClickerPlus.ClickerPlusState.fail -> {
                    Toast.makeText(this@LibraryDemoActivity, "回连失败", Toast.LENGTH_SHORT).show()
                }
                else -> {
                }
            }
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
}