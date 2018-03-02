package com.qcymall.clickerplus

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.inuker.bluetooth.library.utils.ByteUtils
import com.qcymall.clickerpluslibrary.ClickerPlus
import com.qcymall.clickerpluslibrary.ClickerPlusListener
import java.util.HashMap

/**
 * Created by lanmi on 2018/2/27.
 */
class LibraryDemoActivity: AppCompatActivity()  {

    private val TAG = "LibraryDemoActivity"
    private var mDeviceMAC: String? = null
    private var mDevice: BluetoothDevice? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_librarydemo)
        val mapData = intent.getSerializableExtra("data") as HashMap<String, Any>
        mDeviceMAC = mapData["mac"] as String
        mDevice = mapData["btdevice"] as BluetoothDevice
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

    fun ota4Click(v: View){
        ClickerPlus.otaDFU(this, "/storage/emulated/0/360Download/sdk13_app_4(1).zip")
    }
    fun ota5Click(v: View){
        ClickerPlus.otaDFU(this, "/storage/emulated/0/360Download/sdk13_app_5(1).zip")
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
            Toast.makeText(this@LibraryDemoActivity, "单击", Toast.LENGTH_SHORT).show()
        }

        override fun onDoubleClick() {
            Log.e(TAG, "onDoubleClick")
            Toast.makeText(this@LibraryDemoActivity, "双击", Toast.LENGTH_SHORT).show()
        }

        override fun onLongPress() {
            Log.e(TAG, "onLongPress")
            Toast.makeText(this@LibraryDemoActivity, "长按", Toast.LENGTH_SHORT).show()
        }

        override fun onWeakup() {
            Log.e(TAG, "onWeakup")
            Toast.makeText(this@LibraryDemoActivity, "音箱唤醒", Toast.LENGTH_SHORT).show()
        }

        override fun onIdeaCapsule() {
            Log.e(TAG, "onIdeaCapsule")
            Toast.makeText(this@LibraryDemoActivity, "闪念胶囊", Toast.LENGTH_SHORT).show()
        }
        override fun onVoicePCMStart() {
            Log.e(TAG, "onVoicePCMStart")
            Toast.makeText(this@LibraryDemoActivity, "PCM数据开始", Toast.LENGTH_SHORT).show()
        }

        override fun onVoicePCMEnd() {
            Log.e(TAG, "onVoicePCMEnd")
            Toast.makeText(this@LibraryDemoActivity, "PCM数据结束", Toast.LENGTH_SHORT).show()
        }
        override fun onVoicePCM(data: ByteArray, index: Int) {
            Log.e(TAG, String.format("onVoicePCM  current Index = %d, Voice PCM Data: %s", index, ByteUtils.byteToString(data)))
        }

        override fun onIdeaPCMStart(header: String) {
            Log.e(TAG, "onIdeaPCMStart " + header)
            Toast.makeText(this@LibraryDemoActivity, "闪念胶囊PCM数据开始", Toast.LENGTH_SHORT).show()
        }

        override fun onIdeaPCMEnd(info: ByteArray?) {
            Log.e(TAG, "onIdeaPCMEnd " + ByteUtils.byteToString(info))
            Toast.makeText(this@LibraryDemoActivity, "闪念胶囊PCM数据结束", Toast.LENGTH_SHORT).show()
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