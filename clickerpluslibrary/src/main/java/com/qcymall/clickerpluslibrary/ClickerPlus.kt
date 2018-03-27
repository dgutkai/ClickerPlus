package com.qcymall.clickerpluslibrary

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.Log
import com.inuker.bluetooth.library.BluetoothClient
import com.inuker.bluetooth.library.Code.REQUEST_SUCCESS
import com.inuker.bluetooth.library.Constants.STATUS_CONNECTED
import com.inuker.bluetooth.library.beacon.Beacon
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener
import com.inuker.bluetooth.library.connect.options.BleConnectOptions
import com.inuker.bluetooth.library.connect.response.BleConnectResponse
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse
import com.inuker.bluetooth.library.connect.response.BleReadRssiResponse
import com.inuker.bluetooth.library.connect.response.BleWriteResponse
import com.inuker.bluetooth.library.search.SearchRequest
import com.inuker.bluetooth.library.search.SearchResult
import com.inuker.bluetooth.library.search.response.SearchResponse
import com.inuker.bluetooth.library.utils.BluetoothLog
import com.inuker.bluetooth.library.utils.ByteUtils
import com.qcymall.clickerpluslibrary.dfu.DFUService
import com.qcymall.clickerpluslibrary.utils.BLECMDUtil
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.dfu.DfuServiceListenerHelper
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by lanmi on 2018/2/26.
 */
object ClickerPlus {

    private val TAG = "ClickerPlus"
    private val SERVICE_UUID = UUID.fromString("6e40caa1-b5a3-f393-e0a9-e50e24dcca9e")//  服务号
    private val WRITE_UUID = UUID.fromString("6e40cab1-b5a3-f393-e0a9-e50e24dcca9e")//  写特征号
    private val NOTIFICATION_UUID = UUID.fromString("6e40cab2-b5a3-f393-e0a9-e50e24dcca9e")//  写特征号
    private val PAIR_TIMEOUT = 10000L
    enum class ClickerPlusState{
        pairing,
        success,
        fail
    }


    private val SP_NAME = "ClickerPlus_sp"
    private var mBluetoothClien: BluetoothClient? = null
    var isConnect: Boolean = false
    var isPair: Boolean = false
    private var mContext: WeakReference<Context>? = null
    private var mFlagID: String? = null
    private var isSendFlag: Boolean = false
    var mClickerPlusListener: ClickerPlusListener? = null

    private var mCurrentMac: String? = null
//    private var mCurrentDevice: BluetoothDevice? = null
//    private var mWriteCharacteristic:BleGattCharacter? = null
//    private var mNotificationCharacteristic: BleGattCharacter? = null



    var outputStream: FileOutputStream? = null
    // 创建BufferedOutputStream对象
    var bufferedOutputStream: BufferedOutputStream? = null

    var outputStream2: FileOutputStream? = null
    // 创建BufferedOutputStream对象
    var bufferedOutputStream2: BufferedOutputStream? = null


    /**
     * 初始化库，该方法必须在最开始调用，只有调用了初始化方法之后，其他方法放可以调用。
     * @param context
     */
    fun initClicker(context: Context) {
        mBluetoothClien = BluetoothClient(context)
        mContext = WeakReference(context)
        connectDevice()

    }
    fun readRss(response: BleReadRssiResponse){
        if (mCurrentMac != null) {
            return mBluetoothClien!!.readRssi(mCurrentMac, response)
        }
    }

    // TODO: 发布的时候删掉
    fun disconnect(){
        Log.e(TAG, "disconnect()")
        mBluetoothClien!!.disconnect(mCurrentMac);
        mCurrentMac = null;
    }
    fun scanDevice(searchResponse: SearchResponse){
        val request = SearchRequest.Builder()
                .searchBluetoothLeDevice(3000, 3)   // 先扫BLE设备3次，每次3s
                .build()

        mBluetoothClien!!.search(request, object : SearchResponse {
            override fun onSearchStarted() {
                Log.e(TAG, "onSearchStarted")
                searchResponse.onSearchStarted()
            }

            override fun onDeviceFounded(device: SearchResult) {
                Log.e(TAG, "onDeviceFounded " + device.name + " " + device.address)
                val beancom = Beacon(device.scanRecord)
//                Log.e(TAG, "onDeviceFounded " + beancom.toString())
                if (device.name == "Smartisan Clicker+"){
                    val paritype = beancom.mBytes.last()
                    if (paritype == 0x01.toByte()){
                        searchResponse.onDeviceFounded(device)
                    }
                }


            }

            override fun onSearchStopped() {
                searchResponse.onSearchStopped()
            }

            override fun onSearchCanceled() {
                searchResponse.onSearchCanceled()
            }
        })
    }
    private fun connect(macString: String): Boolean{
        if (isConnect && macString == mCurrentMac){
            return false
        }
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
        return true
    }

    /**
     * 配对设备，设备进入配对模式的前提下可以连接并配对设备。
     * @param deviceMac 配对设备的MAC地址
     * @param flagID 用户唯一的字符串，用于绑定设备时作唯一识别。
     * @return 库没有初始化时返回false。
     */
    fun pairDevice(deviceMac: String, flagID: String): Boolean{
        if (mBluetoothClien == null){
            return false
        }
        mFlagID = flagID
        isSendFlag = true
        return connect(deviceMac)
    }
    fun connectDevice(): Boolean{
        if (mBluetoothClien == null){
            return false
        }
        val spMacString = getStringValueFromSP(CommonAction.SP_CLICKER_PAIRED_MAC)
        val spFlaginfo = getStringValueFromSP(CommonAction.SP_CLICKER_FLAGINFO)
        if (spMacString != ""){
            mFlagID = spFlaginfo
            isSendFlag = false
            connect(spMacString)
            return true
        }else{
            return false
        }

    }

    /**
     * @param isAll 是否解除设备的所有绑定
     */
    fun unPairDevice(isAll: Boolean): Boolean{
        if (!isPair){
            return false
        }
        if (!isConnect || mCurrentMac == null){
            return false
        }
        mBluetoothClien!!.write(mCurrentMac, SERVICE_UUID, WRITE_UUID,
                BLECMDUtil.createUnpariCMD(mFlagID!!, isAll), response)
        return true
    }

    fun findDevice(): Boolean{
        if (!isPair){
            return false
        }
        if (!isConnect || mCurrentMac == null){
            return false
        }
        mBluetoothClien!!.write(mCurrentMac, SERVICE_UUID, WRITE_UUID,
                BLECMDUtil.createFindCMD(), response)
        return true
    }

    fun getBattery(): Boolean{
        if (!isPair){
            return false
        }
        if (!isConnect || mCurrentMac == null){
            return false
        }
        mBluetoothClien!!.write(mCurrentMac, SERVICE_UUID, WRITE_UUID,
                BLECMDUtil.createBatteryCMD(), response)
        return true

    }
    fun micIncrease(value: Int): Boolean{
        if (!isPair){
            return false
        }
        if (!isConnect || mCurrentMac == null){
            return false
        }
        mBluetoothClien!!.write(mCurrentMac, SERVICE_UUID, WRITE_UUID,
                BLECMDUtil.createIncreaseCMD(value), response)
        return true

    }
    fun otaDFU(context: Context, filePath: String): Boolean{
        if (!isPair){
            return false
        }
        if (!isConnect || mCurrentMac == null){
            return false
        }
        mBluetoothClien!!.write(mCurrentMac, SERVICE_UUID, WRITE_UUID,
                BLECMDUtil.createOTACMD(), response)
        val h = Handler()
        h.postDelayed({
            val request = SearchRequest.Builder()
                    .searchBluetoothLeDevice(3000, 3)   // 先扫BLE设备3次，每次3s
                    .build()

            mBluetoothClien!!.search(request, object : SearchResponse {
                override fun onSearchStarted() {
                    Log.e(TAG, "onSearchStarted")
                }

                override fun onDeviceFounded(device: SearchResult) {
                    Log.e(TAG, "onDeviceFounded " + device.name + " " + device.address)
                    if (device.name == "DfuTarg"){ // device.address == mCurrentMac &&
                        mBluetoothClien!!.stopSearch()
                        update(context, device.address, device.name, filePath)
                    }

                }

                override fun onSearchStopped() {
                }

                override fun onSearchCanceled() {
                }
            })
        }, 10000)
        return true

    }

    private fun update(context: Context, deviceMac: String, deviceName: String, path: String){
        DfuServiceListenerHelper.registerProgressListener(context, mDfuProgressListener)
        val starter = DfuServiceInitiator(deviceMac)
                .setDeviceName(deviceName)
                .setKeepBond(false)
                .setForceDfu(false)
                .setPacketsReceiptNotificationsEnabled(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                .setPacketsReceiptNotificationsValue(DfuServiceInitiator.DEFAULT_PRN_VALUE)
                .setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true)
        starter.setZip(null, path)

        starter.start(context, DFUService::class.java)
    }

    private val mDfuProgressListener = object : DfuProgressListenerAdapter() {
        override fun onDeviceConnecting(deviceAddress: String?) {
            Log.e("mDfuProgressListener", "onDeviceConnecting " + deviceAddress )
        }

        override fun onDfuProcessStarting(deviceAddress: String?) {
            Log.e("mDfuProgressListener", "onDfuProcessStarting " + deviceAddress )
            if (mClickerPlusListener != null) {
                val h = Handler()
                h.post {
                    mClickerPlusListener!!.onOTAStart(deviceAddress!!)
                }
            }
        }

        override fun onEnablingDfuMode(deviceAddress: String?) {
            Log.e("mDfuProgressListener", "onEnablingDfuMode " + deviceAddress )
        }

        override fun onFirmwareValidating(deviceAddress: String?) {
            Log.e("mDfuProgressListener", "onFirmwareValidating " + deviceAddress )
        }

        override fun onDeviceDisconnecting(deviceAddress: String?) {
            Log.e("mDfuProgressListener", "onDeviceDisconnecting " + deviceAddress )
        }

        override fun onDfuCompleted(deviceAddress: String?) {

            Log.e("mDfuProgressListener", "onDfuCompleted " + deviceAddress )
            if (mClickerPlusListener != null) {
                val h = Handler()
                h.post {
                    mClickerPlusListener!!.onOTACompleted(deviceAddress!!)
                }
            }
            val connectHandler = Handler()
            connectHandler.postDelayed({ connectDevice() }, 500)

        }

        override fun onDfuAborted(deviceAddress: String?) {
            Log.e("mDfuProgressListener", "onDfuAborted " + deviceAddress )
        }

        override fun onProgressChanged(deviceAddress: String?, percent: Int, speed: Float, avgSpeed: Float, currentPart: Int, partsTotal: Int) {
            Log.e("mDfuProgressListener", "onProgressChanged " + deviceAddress + " progress:" + percent + " speed:" + speed +
                    " avgSpeed:" + avgSpeed + " currentPart:" + currentPart + " partsTotal:" + partsTotal)
            if (mClickerPlusListener != null) {
                val h = Handler()
                h.post {
                    mClickerPlusListener!!.onOTAProgressChanged(deviceAddress!!, percent, speed, avgSpeed, currentPart, partsTotal)
                }
            }
        }

        override fun onError(deviceAddress: String?, error: Int, errorType: Int, message: String?) {
            Log.e("mDfuProgressListener", "onError " + deviceAddress + " error:" + error + " errorType:" + errorType + " message:" + message)
            if (mClickerPlusListener != null) {
                val h = Handler()
                h.post {
                    mClickerPlusListener!!.onOTAError(deviceAddress!!, error, errorType, message)
                }
            }
        }
    }

    private val mConnectStatusListener = object : BleConnectStatusListener() {
        override fun onConnectStatusChanged(mac: String, status: Int) {
            BluetoothLog.e(String.format("DeviceDetailActivity onConnectStatusChanged %d in %s",
                    status, Thread.currentThread().name))

            isConnect = status == STATUS_CONNECTED
            if (isConnect){
                mCurrentMac = mac
//                if (isSendFlag){
//                    mBluetoothClien!!.write(mCurrentMac, SERVICE_UUID, WRITE_UUID, BLECMDUtil.createPariCMD(mFlagID!!), response)
//                }else{
//                    mBluetoothClien!!.write(mCurrentMac, SERVICE_UUID, WRITE_UUID, BLECMDUtil.createConnectBackCMD(mFlagID!!), response)
//                }
                val timeoutHandler = Handler()
                timeoutHandler.postDelayed({
                    if (!isPair && isConnect){
                        Log.e(TAG, "timeoutHandler.postDelayed")
                        mBluetoothClien!!.disconnect(mCurrentMac)
                        if (mClickerPlusListener != null) {
                            if (isSendFlag) {
                                mClickerPlusListener!!.onPair(ClickerPlusState.fail)
                            } else {
                                mClickerPlusListener!!.onConnectBack(ClickerPlusState.fail)
                            }
                        }
                    }
                }, PAIR_TIMEOUT)
                if (mClickerPlusListener != null) {
                    val h = Handler()
                    h.post {
                        mClickerPlusListener!!.onConnect(mCurrentMac!!)
                        mClickerPlusListener!!.onPair(ClickerPlusState.pairing)
                    }
                }
            }else{
                isPair = false
                if (mClickerPlusListener != null) {
                    val h = Handler()
                    h.post {
                        mClickerPlusListener!!.onDisconnect(mac)
                        mCurrentMac = null
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream!!.close();
                    } catch (e: Exception) {
                        e.printStackTrace();
                    }

                }
                if (bufferedOutputStream != null) {
                    try {
                        bufferedOutputStream!!.close();
                    } catch (e2: Exception) {
                        e2.printStackTrace();
                    }

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

                if (mClickerPlusListener != null){
                    val h = Handler()
//                    h.post { mClickerPlusListener!!.onDataReceive(String.format("Notify: %s -> %d\n", ByteUtils.byteToString(value), Date().time)) }
                }
                val parseResult = BLECMDUtil.parseCMD(value) ?: return
//                Log.e(TAG, String.format("Notify: %s %d \n%d", ByteUtils.byteToString(parseResult.data), parseResult.id, Date().time))
                when (parseResult.id){

                    BLECMDUtil.CMDID_PAIR ->{
                        val result = BLECMDUtil.parsePairCMD(parseResult.data)
                        val h = Handler()
                        when(result){
                            BLECMDUtil.ConnectState.fail -> {
                                isPair = false
                                Log.e(TAG, "BLECMDUtil.CMDID_PAIR BLECMDUtil.ConnectState.fail")
                                mBluetoothClien!!.disconnect(mCurrentMac)
                                if (mClickerPlusListener != null) {
                                    h.post { mClickerPlusListener!!.onPair(ClickerPlusState.fail) }
                                }
                            }
                            BLECMDUtil.ConnectState.success -> {
                                isPair = true
                                mBluetoothClien!!.write(mCurrentMac, SERVICE_UUID, WRITE_UUID,
                                        BLECMDUtil.createTimeCMD(), response)
                                setStringValueToSP(CommonAction.SP_CLICKER_PAIRED_MAC, mCurrentMac!!)
                                setStringValueToSP(CommonAction.SP_CLICKER_FLAGINFO, mFlagID!!)
                                if (mClickerPlusListener != null) {
                                    h.post { mClickerPlusListener!!.onPair(ClickerPlusState.success) }
                                }
                            }
                            BLECMDUtil.ConnectState.request -> {
                                mBluetoothClien!!.write(mCurrentMac, SERVICE_UUID, WRITE_UUID, BLECMDUtil.createPariCMD(mFlagID!!), response)
                            }
                        }
                    }
                    BLECMDUtil.CMDID_UNPAIR ->{
                        val result = BLECMDUtil.parseUnpairCMD(parseResult.data)
                        val h = Handler()
                        if (result){
                            isPair = false
                            setStringValueToSP(CommonAction.SP_CLICKER_PAIRED_MAC, "")
                            setStringValueToSP(CommonAction.SP_CLICKER_FLAGINFO, "")
                            Log.e(TAG, "BLECMDUtil.CMDID_UNPAIR")
                            mBluetoothClien!!.disconnect(mCurrentMac)
                            if (mClickerPlusListener != null) {
                                h.post { mClickerPlusListener!!.onCancelPair(ClickerPlusState.success) }
                            }
                        }else{
                            if (mClickerPlusListener != null) {
                                h.post { mClickerPlusListener!!.onCancelPair(ClickerPlusState.fail) }
                            }
                        }
                    }
                    BLECMDUtil.CMDID_CONNECTBACK ->{
                        val result = BLECMDUtil.parseConnectbackCMD(parseResult.data)
                        val h = Handler()
                        when(result){
                            BLECMDUtil.ConnectState.fail -> {
                                isPair = false
                                Log.e(TAG, "BLECMDUtil.CMDID_CONNECTBACK BLECMDUtil.ConnectState.fail")
                                mBluetoothClien!!.disconnect(mCurrentMac)
                                if (mClickerPlusListener != null) {
                                    h.post { mClickerPlusListener!!.onConnectBack(ClickerPlusState.fail) }
                                }
                            }
                            BLECMDUtil.ConnectState.success -> {
                                isPair = true
                                mBluetoothClien!!.write(mCurrentMac, SERVICE_UUID, WRITE_UUID,
                                        BLECMDUtil.createTimeCMD(), response)
                                if (mClickerPlusListener != null) {
                                    h.post { mClickerPlusListener!!.onConnectBack(ClickerPlusState.success) }
                                }
                            }
                            BLECMDUtil.ConnectState.request -> {
                                mBluetoothClien!!.write(mCurrentMac, SERVICE_UUID, WRITE_UUID, BLECMDUtil.createConnectBackCMD(mFlagID!!), response)
                            }
                        }
                    }
                    BLECMDUtil.CMDID_WEAKUP -> {
                        if (mClickerPlusListener != null) {
                            val h = Handler()
                            h.post { mClickerPlusListener!!.onWeakup() }
                        }
                    }
                    BLECMDUtil.CMDID_CLICK -> {
                        if (mClickerPlusListener != null) {
                            val h = Handler()
                            h.post { mClickerPlusListener!!.onClick() }
                        }
                    }
                    BLECMDUtil.CMDID_DOUBLECLICK -> {
                        if (mClickerPlusListener != null) {
                            val h = Handler()
                            h.post { mClickerPlusListener!!.onDoubleClick() }
                        }
                    }
                    BLECMDUtil.CMDID_LONGPRESS -> {
                        if (mClickerPlusListener != null) {
                            val h = Handler()
                            h.post { mClickerPlusListener!!.onLongPress() }
                        }
                    }
                    BLECMDUtil.CMDID_IDEA -> {
                        if (mClickerPlusListener != null) {
                            val h = Handler()
                            h.post { mClickerPlusListener!!.onIdeaCapsule() }
                        }
                    }
                    BLECMDUtil.CMDID_VOICESTART -> {
                        if (mClickerPlusListener != null) {
                            val h = Handler()
                            h.post { mClickerPlusListener!!.onVoicePCMStart() }
                        }
                        val speakpath = "/sdcard/DCS/PCM/";
                        val file2 = File(speakpath, "abc1.pcm");

                        // 如果文件存在则删除
                        if (file2.exists()) {
                            file2.delete();
                        }
                        // 在文件系统中根据路径创建一个新的空文件
                        try {
                            file2.createNewFile();
                            // 获取FileOutputStream对象
                            outputStream = FileOutputStream(file2);
                            // 获取BufferedOutputStream对象
                            bufferedOutputStream = BufferedOutputStream(outputStream);
                        } catch (e: Exception) {
                            e.printStackTrace();
                        }
                    }
                    BLECMDUtil.CMDID_VOICEPCM -> {
                        val result = BLECMDUtil.parsePCMData(parseResult.data)
                        if (mClickerPlusListener != null) {
                            val h = Handler()
                            h.post { mClickerPlusListener!!.onVoicePCM(result["pcmData"] as ByteArray, result["index"] as Int) }
                        }
                        val pcmData = ByteArray(parseResult.data!!.size - 2)
                        System.arraycopy(parseResult.data, 2, pcmData, 0, pcmData.size)
                        try {
                            // 往文件所在的缓冲输出流中写byte数据
                            bufferedOutputStream!!.write(pcmData);
                            bufferedOutputStream!!.flush();

                        }catch (e: Exception){

                        }
                    }
                    BLECMDUtil.CMDID_VOICEEND -> {
                        if (mClickerPlusListener != null) {
                            val h = Handler()
                            h.post { mClickerPlusListener!!.onVoicePCMEnd() }
                        }

                    }
                    BLECMDUtil.CMDID_IDEASTART -> {
                        val result = BLECMDUtil.parseIdeaHeader(parseResult.data)
                        if (mClickerPlusListener != null) {
                            val h = Handler()
                            h.post { mClickerPlusListener!!.onIdeaPCMStart(result) }
                        }
                    }
                    BLECMDUtil.CMDID_IDEAPCM -> {
                        val result = BLECMDUtil.parsePCMData(parseResult.data)
                        if (mClickerPlusListener != null) {
                            val h = Handler()
                            h.post { mClickerPlusListener!!.onIdeaPCM(result["pcmData"] as ByteArray, result["index"] as Int) }
                        }
                    }
                    BLECMDUtil.CMDID_IDEAEND -> {

                        if (mClickerPlusListener != null) {
                            val h = Handler()
                            h.post { mClickerPlusListener!!.onIdeaPCMEnd(parseResult.data) }
                        }
                    }
                    BLECMDUtil.CMDID_VOICE_TMP_START -> {
                        val result = BLECMDUtil.parseVoiceTmpHeader(parseResult.data)
                        if (mClickerPlusListener != null) {
                            val h = Handler()
                            h.post { mClickerPlusListener!!.onVoiceTmpPCMStart(result) }
                        }

                        val speakpath = "/sdcard/DCS/PCM/";
                        val file2 = File(speakpath, "abc2.pcm");

                        // 如果文件存在则删除
                        if (file2.exists()) {
                            file2.delete();
                        }
                        // 在文件系统中根据路径创建一个新的空文件
                        try {
                            file2.createNewFile();
                            // 获取FileOutputStream对象
                            outputStream2 = FileOutputStream(file2);
                            // 获取BufferedOutputStream对象
                            bufferedOutputStream2 = BufferedOutputStream(outputStream2);
                        } catch (e: Exception) {
                            e.printStackTrace();
                        }

                    }
                    BLECMDUtil.CMDID_VOICE_TMP_PCM -> {
                        val result = BLECMDUtil.parsePCMData(parseResult.data)
                        if (mClickerPlusListener != null) {
                            val h = Handler()
                            h.post { mClickerPlusListener!!.onVoiceTmpPCM(result["pcmData"] as ByteArray, result["index"] as Int) }
                        }

                        val pcmData = ByteArray(parseResult.data!!.size - 2)
                        System.arraycopy(parseResult.data, 2, pcmData, 0, pcmData.size)
                        try {
                            // 往文件所在的缓冲输出流中写byte数据
                            bufferedOutputStream2!!.write(pcmData);
                            bufferedOutputStream2!!.flush();

                        }catch (e: Exception){

                        }
                    }
                    BLECMDUtil.CMDID_VOICE_TMP_END -> {

                        if (mClickerPlusListener != null) {
                            val h = Handler()
                            h.post { mClickerPlusListener!!.onVoiceTmpPCMEnd(parseResult.data) }
                        }
                        if (outputStream2 != null) {
                            try {
                                outputStream2!!.close()
                            } catch (e: Exception) {
                                e.printStackTrace();
                            }

                        }
                        if (bufferedOutputStream2 != null) {
                            try {
                                bufferedOutputStream2!!.close();
                            } catch (e2: Exception) {
                                e2.printStackTrace();
                            }

                        }

                    }

                    BLECMDUtil.CMDID_BATTERY -> {
                        val result = BLECMDUtil.parseBatteryCMD(parseResult.data)
                        if (mClickerPlusListener != null) {
                            val h = Handler()
                            h.post { mClickerPlusListener!!.onBatteryChange(result) }
                        }
                    }
                    BLECMDUtil.CMDID_TIME -> {
                        val result = BLECMDUtil.parseTimeBackCMD(parseResult.data)
                        if (result){
                            // 时间配置正确
                        }
                    }
                    BLECMDUtil.CMDID_FINDPHONE -> {
                        if (mClickerPlusListener != null) {
                            val h = Handler()
                            h.post { mClickerPlusListener!!.onFindPhone() }
                        }
                    }

                }
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