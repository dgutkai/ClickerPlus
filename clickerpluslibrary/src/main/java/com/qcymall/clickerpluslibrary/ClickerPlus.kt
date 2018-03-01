package com.qcymall.clickerpluslibrary

import android.annotation.SuppressLint
import android.app.Notification
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import android.util.Log
import com.inuker.bluetooth.library.BluetoothClient
import com.inuker.bluetooth.library.Code.REQUEST_SUCCESS
import com.inuker.bluetooth.library.Constants.STATUS_CONNECTED
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener
import com.inuker.bluetooth.library.connect.options.BleConnectOptions
import com.inuker.bluetooth.library.connect.response.BleConnectResponse
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse
import com.inuker.bluetooth.library.connect.response.BleWriteResponse
import com.inuker.bluetooth.library.model.BleGattCharacter
import com.inuker.bluetooth.library.model.BleGattProfile
import com.inuker.bluetooth.library.utils.BluetoothLog
import com.inuker.bluetooth.library.utils.ByteUtils
import com.qcymall.clickerpluslibrary.adpcm.AdpcmUtils
import com.qcymall.clickerpluslibrary.utils.BLECMDUtil
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by lanmi on 2018/2/26.
 */
object ClickerPlus {

    private val TAG = "ClickerPlus"
    private val SERVICE_UUID = UUID.fromString("0000caa1-0000-1000-8000-00805f9b34fb")//  服务号
    private val WRITE_UUID = UUID.fromString("0000cab1-0000-1000-8000-00805f9b34fb")//  写特征号
    private val NOTIFICATION_UUID = UUID.fromString("0000cab2-0000-1000-8000-00805f9b34fb")//  写特征号
    enum class ClickerPlusState{
        pairing,
        success,
        fail
    }


    private val SP_NAME = "ClickerPlus_sp"
    private var mBluetoothClien: BluetoothClient? = null
    var isConnect: Boolean = false
        get() = field
    private var mContext: WeakReference<Context>? = null
    private var mFlagID: String? = null
    private var isSendFlag: Boolean = false
    var mClickerPlusListener: ClickerPlusListener? = null
        set(value) {
            field = value
        }

    private var mCurrentDevice: BluetoothDevice? = null
//    private var mWriteCharacteristic:BleGattCharacter? = null
//    private var mNotificationCharacteristic: BleGattCharacter? = null
    fun initClicker(context: Context) {
        mBluetoothClien = BluetoothClient(context)
        mContext = WeakReference(context)
        val sp_macString = getStringValueFromSP(CommonAction.SP_CLICKER_PAIRED_MAC)
        val sp_flaginfo = getStringValueFromSP(CommonAction.SP_CLICKER_FLAGINFO)
        if (sp_macString != ""){
            isSendFlag = false
            connect(sp_macString)
        }

    }

    private fun connect(macString: String){

        val options = BleConnectOptions.Builder()
                .setConnectRetry(3)   // 连接如果失败重试3次
                .setConnectTimeout(30000)   // 连接超时30s
                .setServiceDiscoverRetry(3)  // 发现服务如果失败重试3次
                .setServiceDiscoverTimeout(20000)  // 发现服务超时20s
                .build()
        mBluetoothClien!!.registerConnectStatusListener(macString, mConnectStatusListener)
        mBluetoothClien!!.connect(macString, options, BleConnectResponse { code, data ->
            BluetoothLog.e("error code = " + code + ", GattProfile = " + data.toString())
            val service = data.getService(SERVICE_UUID)
            val characters = service.characters
            for (character in characters) {
                if (character.uuid == WRITE_UUID){
//                    mWriteCharacteristic = character
                }else if (character.uuid == NOTIFICATION_UUID){
//                    mNotificationCharacteristic = character
                    mBluetoothClien!!.notify(macString, SERVICE_UUID, NOTIFICATION_UUID, mNotifyRsp)
                }
            }
        })


    }
    fun connectDevice(btDevice: BluetoothDevice?, flagID: String): Boolean{
        if (mBluetoothClien == null){
            return false
        }
        if (btDevice == null){
            return false
        }
        mFlagID = flagID
        mCurrentDevice = btDevice
        connect(btDevice.address)

        return true
    }

    private val mConnectStatusListener = object : BleConnectStatusListener() {
        override fun onConnectStatusChanged(mac: String, status: Int) {
            BluetoothLog.e(String.format("DeviceDetailActivity onConnectStatusChanged %d in %s",
                    status, Thread.currentThread().name))

            isConnect = status == STATUS_CONNECTED
            if (mClickerPlusListener != null){
                if (isConnect) {
                    mBluetoothClien!!.write(mCurrentDevice!!.address, SERVICE_UUID, WRITE_UUID, BLECMDUtil.createPariCMD(mFlagID!!), response)
                    val h = Handler()
                    h.post { mClickerPlusListener!!.onConnect(mCurrentDevice!!) }

                }else{
                    val h = Handler()
                    h.post { mClickerPlusListener!!.onDisconnect(mCurrentDevice!!) }

                }
            }
        }
    }

    private val response = BleWriteResponse { code ->
        Log.e(TAG, "Write OnResponse code " + code)
    }
    private val mNotifyRsp = object : BleNotifyResponse {
        override fun onNotify(service: UUID, character: UUID, value: ByteArray) {
            if (service == SERVICE_UUID && character == NOTIFICATION_UUID) {
               Log.e(TAG, String.format("Notify: %s \n%d", ByteUtils.byteToString(value), Date().time))
                //                synchronized (BLEDetialActivity.this) {
                //                    byte[] resultData = new byte[value.length * 4];
                //                    int result2 = AdpcmUtils.shareInstance().adpcmDecoder(value, resultData, value.length);
                //                    player.write(resultData, 0, resultData.length);
                //                }

                // ========分割线==============
                //                int cpylen = adpcmBuff.length-offset>value.length? value.length: adpcmBuff.length-offset;
                //                System.arraycopy(value, 0, adpcmBuff, offset, cpylen);
                //                offset += value.length;
                //                if (offset >= adpcmBuff.length) {
                //                    offset = 0;
                //                    byte[] resultData = new byte[adpcmBuff.length * 4];
                //                    int result2 = AdpcmUtils.shareInstance().adpcmDecoder(value, resultData, value.length);
                //                    player.write(resultData, 0, resultData.length);
                //                    Log.e("BLEDetialActivity", "playing");
                //                }


                // ========分割线==============
                //                try {
                //                    // 往文件所在的缓冲输出流中写byte数据
                //                    bufferedOutputStream.write(value);
                //                    bufferedOutputStream.flush();
                //
                //                }catch (Exception e){
                //
                //                }


            }
        }

        override fun onResponse(code: Int) {
            if (code == REQUEST_SUCCESS) {
                Log.e(TAG, "BleNotifyResponse success" + code)
            } else {
                Log.e(TAG, "BleNotifyResponse failed" + code)
            }
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun setStringValueToSP(key: String, value: String){
        if (mContext == null){
            return
        }
        mContext!!.get()!!.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).edit().putString(key, value).apply()
    }
    private fun getStringValueFromSP(key: String): String{
        if (mContext == null){
            return ""
        }
        return mContext!!.get()!!.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
                .getString(key, "");
    }


}