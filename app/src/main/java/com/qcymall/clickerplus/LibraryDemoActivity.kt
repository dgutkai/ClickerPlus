package com.qcymall.clickerplus

import android.bluetooth.BluetoothDevice
import android.content.DialogInterface
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.inuker.bluetooth.library.connect.listener.ReadRssiListener
import com.inuker.bluetooth.library.connect.response.BleReadRssiResponse
import com.inuker.bluetooth.library.utils.ByteUtils
import com.qcymall.clickerpluslibrary.ClickerPlus
import com.qcymall.clickerpluslibrary.ClickerPlusListener
import java.util.*

/**
 * Created by lanmi on 2018/2/27.
 */
class LibraryDemoActivity: AppCompatActivity()  {

//    var outputStream: FileOutputStream? = null
    // 创建BufferedOutputStream对象
//    var bufferedOutputStream: BufferedOutputStream? = null

    private val TAG = "LibraryDemoActivity"
    private var mDeviceMAC: String? = null
    private var mDevice: BluetoothDevice? = null
    private var audioBufSize: Int = 0
    private var player: AudioTrack? = null // 播放PCM数据的播放器
    private lateinit var infoText: TextView
    private lateinit var rssiText: Button
    private val rssiThread: Thread = Thread({
        while (true) {
            Thread.sleep(1000)
            if (ClickerPlus.isConnect) {
                ClickerPlus.readRss(object: BleReadRssiResponse {
                    override fun onResponse(code: Int, data: Int?) {
                        if (data != null) {
                            val h = Handler()
                            h.post {
                                rssiText.setText("RSSI = " + data)

//                        Toast.makeText(this@LibraryDemoActivity, "RSSI = " + data, Toast.LENGTH_SHORT).show()
                            }


                        }
                    }

                })
            }
        }
    })
    //    private var byteArray: ByteArray = ByteArray(230 * 4 * 100)
    private var buffList: ArrayList<ByteArray> = ArrayList<ByteArray>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_librarydemo)
        val mapData = intent.getSerializableExtra("data") as HashMap<String, Any>
        mDeviceMAC = mapData["mac"] as String
        mDevice = mapData["btdevice"] as BluetoothDevice
//        Log.e(TAG, mapData["name"] as String?)
        ClickerPlus.mClickerPlusListener = mListener
        audioBufSize = 230 * 16
        player = AudioTrack(AudioManager.STREAM_MUSIC, 8000,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                audioBufSize,
                AudioTrack.MODE_STREAM)
        infoText = findViewById(R.id.info_txt)
        rssiText = findViewById(R.id.readRssi_btn)
        rssiThread.start()
    }

    fun clean(v: View){
        infoText.setText("")
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
    fun ota6Click(v: View){
        // Todo: 修改升级文件路径
        ClickerPlus.otaUpdate(this, "/sdcard/A001/Smartisan_Clicker.img")
    }
    fun ota7Click(v: View){
        // Todo: 修改升级文件路径
        ClickerPlus.otaUpdate(this, "/sdcard/A001/Smartisan_Clicker.img")
    }
    fun changeMAC(v: View){
        val editText = findViewById<EditText>(R.id.mac_edit)
        ClickerPlus.changeMAC(editText.text.toString())
    }
    fun minincreaseClick(v: View){
        val editText = EditText(this)
        val dialog = AlertDialog.Builder(this).setTitle("输入值").setView(editText)
                .setPositiveButton("确定", object :DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        ClickerPlus.micIncrease(editText.text.toString().toInt())
                    }

                }).setNegativeButton("取消", null).create()
        dialog.show()
    }
    fun readRssiClick(v: View){
        ClickerPlus.readRss(object:BleReadRssiResponse {
            override fun onResponse(code: Int, data: Int?) {
                if (data != null) {
                    val h = Handler()
                    h.post {
                        Toast.makeText(this@LibraryDemoActivity, "RSSI = " + data, Toast.LENGTH_SHORT).show()
                    }


                }
            }

        })
    }
    fun switchClick(v: View){
        val tag = v.tag
        if (tag == "0") {
            v.tag = "1"
            ClickerPlus.switchFind(true)
            (v as Button).text = "关闭防丢"
        }else{
            v.tag = "0"
            ClickerPlus.switchFind(false)
            (v as Button).text = "打开防丢"
        }
    }
    fun readVersionClick(v: View){
        ClickerPlus.getVersion();
    }
    fun disconnect(v: View){
        ClickerPlus.disconnect()
    }
    override fun onBackPressed() {
        super.onBackPressed()
//         关闭创建的流对象
//        if (outputStream != null) {
//            try {
//                outputStream!!.close();
//            } catch (e: Exception) {
//                e.printStackTrace();
//            }
//
//        }
//        if (bufferedOutputStream != null) {
//            try {
//                bufferedOutputStream!!.close();
//            } catch (e2: Exception) {
//                e2.printStackTrace();
//            }
//
//        }
    }
    val mListener = object: ClickerPlusListener {
//        override fun onDataReceive(info: String) {
//            infoText.text = info + "\n" + infoText.text
//        }

        // 语音指令缓存上传
        override fun onVoiceTmpPCMStart(header: String) {

            Log.e(TAG, "onVoiceTmpPCMStart " + header)

//            val speakpath = "/sdcard/DCS/PCM/";
//            val file2 = File(speakpath, "abc2.pcm");
//
//            // 如果文件存在则删除
//            if (file2.exists()) {
//                file2.delete();
//            }
//            // 在文件系统中根据路径创建一个新的空文件
//            try {
//                file2.createNewFile();
//                // 获取FileOutputStream对象
//                outputStream = FileOutputStream(file2);
//                // 获取BufferedOutputStream对象
//                bufferedOutputStream = BufferedOutputStream(outputStream);
//            } catch (e: Exception) {
//                e.printStackTrace();
//            }

            player!!.play()
            Thread({
                while (player!!.playState == AudioTrack.PLAYSTATE_PLAYING){
                    val bytes = ByteArray(audioBufSize)
                    Arrays.fill(bytes, 0)
                    synchronized(this@LibraryDemoActivity, {
                        if (buffList.size > 4){
                            System.arraycopy(buffList.removeAt(0), 0, bytes, 0, audioBufSize/4)
                            System.arraycopy(buffList.removeAt(0), 0, bytes, 230*4, audioBufSize/4)
                            System.arraycopy(buffList.removeAt(0), 0, bytes, 230*8, audioBufSize/4)
                            System.arraycopy(buffList.removeAt(0), 0, bytes, 230*12, audioBufSize/4)
                        }
                    })
                    player!!.write(bytes, 0, audioBufSize)
                    Thread.sleep(10)
                }
            }).start()
        }

        override fun onVoiceTmpPCM(data: ByteArray, index: Int) {
            Log.e(TAG, "onVoiceTmpPCM " + index)
            synchronized(this@LibraryDemoActivity, {
                buffList.add(data);
            })
//            try {
//                    // 往文件所在的缓冲输出流中写byte数据
//                    bufferedOutputStream!!.write(data);
//                    bufferedOutputStream!!.flush();
//
//                }catch (e: Exception){
//
//                }
        }

        override fun onVoiceTmpPCMEnd(info: ByteArray?) {
            Log.e(TAG, "onVoiceTmpPCMEnd ")
            player!!.stop()
//        if (outputStream != null) {
//            try {
//                outputStream!!.close();
//            } catch (e: Exception) {
//                e.printStackTrace();
//            }
//
//        }
//        if (bufferedOutputStream != null) {
//            try {
//                bufferedOutputStream!!.close();
//            } catch (e2: Exception) {
//                e2.printStackTrace();
//            }
//
//        }
        }

        override fun onFindPhone() {
            Toast.makeText(this@LibraryDemoActivity, "查找手机，手机进行震动响铃", Toast.LENGTH_SHORT).show()
        }

        override fun onConnect(deviceMac: String) {
            Log.e(TAG, deviceMac + " OnConnect")
            title = "已连接"

        }

        override fun onDisconnect(deviceMac: String) {
            Log.e(TAG, deviceMac + " onDisconnect")
            title = "连接断开了"
        }

        override fun onPair(state: ClickerPlus.ClickerPlusState) {
            Log.e(TAG, "onPair " + state.name)
            title = "已配对"
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
            title = "已回连"
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
//            val speakpath = "/sdcard/DCS/PCM/";
//            val file2 = File(speakpath, "abc1.pcm");
//
//            // 如果文件存在则删除
//            if (file2.exists()) {
//                file2.delete();
//            }
//            // 在文件系统中根据路径创建一个新的空文件
//            try {
//                file2.createNewFile();
//                // 获取FileOutputStream对象
//                outputStream = FileOutputStream(file2);
//                // 获取BufferedOutputStream对象
//                bufferedOutputStream = BufferedOutputStream(outputStream);
//            } catch (e: Exception) {
//                e.printStackTrace();
//            }

            Log.e(TAG, "onVoicePCMStart")
            Toast.makeText(this@LibraryDemoActivity, "PCM数据开始", Toast.LENGTH_SHORT).show()
            player!!.play()
            Thread({
                while (player!!.playState == AudioTrack.PLAYSTATE_PLAYING){
                    val bytes = ByteArray(audioBufSize)
                    Arrays.fill(bytes, 0)
                    synchronized(this@LibraryDemoActivity, {
                        if (buffList.size > 4){
                            System.arraycopy(buffList.removeAt(0), 0, bytes, 0, audioBufSize/4)
                            System.arraycopy(buffList.removeAt(0), 0, bytes, 230*4, audioBufSize/4)
                            System.arraycopy(buffList.removeAt(0), 0, bytes, 230*8, audioBufSize/4)
                            System.arraycopy(buffList.removeAt(0), 0, bytes, 230*12, audioBufSize/4)
                        }
                    })
                    player!!.write(bytes, 0, audioBufSize)
                    Thread.sleep(10)
                }
            }).start()
        }

        override fun onVoicePCMEnd() {
            Log.e(TAG, "onVoicePCMEnd")
            Toast.makeText(this@LibraryDemoActivity, "PCM数据结束", Toast.LENGTH_SHORT).show()
            player!!.stop()
        }
        override fun onVoicePCM(data: ByteArray, index: Int) {
            Log.e(TAG, String.format("onVoicePCM  current Index = %d, Voice PCM Data: %s", index, ByteUtils.byteToString(data)))
//                try {
//                    // 往文件所在的缓冲输出流中写byte数据
//                    bufferedOutputStream!!.write(data);
//                    bufferedOutputStream!!.flush();
//
//                }catch (e: Exception){
//
//                }
            synchronized(this@LibraryDemoActivity, {
                buffList.add(data);
            })

        }

        override fun onIdeaPCMStart(header: String) {
            Log.e(TAG, "onIdeaPCMStart " + header)
            Toast.makeText(this@LibraryDemoActivity, "闪念胶囊PCM数据开始", Toast.LENGTH_SHORT).show()
            player!!.play()
            Thread({
                while (player!!.playState == AudioTrack.PLAYSTATE_PLAYING){
                    val bytes = ByteArray(audioBufSize)
                    Arrays.fill(bytes, 0)
                    synchronized(this@LibraryDemoActivity, {
                        if (buffList.size > 4){
                            System.arraycopy(buffList.removeAt(0), 0, bytes, 0, audioBufSize/4)
                            System.arraycopy(buffList.removeAt(0), 0, bytes, 230*4, audioBufSize/4)
                            System.arraycopy(buffList.removeAt(0), 0, bytes, 230*8, audioBufSize/4)
                            System.arraycopy(buffList.removeAt(0), 0, bytes, 230*12, audioBufSize/4)
                        }
                    })
                    player!!.write(bytes, 0, audioBufSize)
                    Thread.sleep(10)
                }
            }).start()
        }

        override fun onIdeaPCMEnd(info: ByteArray?) {
            Log.e(TAG, "onIdeaPCMEnd " + ByteUtils.byteToString(info))
            Toast.makeText(this@LibraryDemoActivity, "闪念胶囊PCM数据结束", Toast.LENGTH_SHORT).show()
            player!!.stop()
        }
        override fun onIdeaPCM(data: ByteArray, index: Int) {
            Log.e(TAG, String.format("onIdeaPCM  current Index = %d, Voice PCM Data: %s", index, ByteUtils.byteToString(data)))
            synchronized(this@LibraryDemoActivity, {
                buffList.add(data);
            })
        }

        override fun onBatteryChange(percent: Int) {
            Log.e(TAG, "onBatteryChange percent = " + percent)
            infoText.text = "电池电量：" + percent + "%\n" + infoText.text
        }

        override fun onVersion(version: String) {
            Log.e(TAG, "onVersion " + version)
            infoText.text = "软件版本：" + version + "\n" + infoText.text
        }

        override fun onOTAStart(deviceMac: String) {
            Log.e(TAG, deviceMac + " onOTAStart")
            infoText.text = "OTA升级开始" + "\n" + infoText.text
        }

        override fun onOTAProgressChanged(deviceMac: String, percent: Int) {
            Log.e(TAG, "onProgressChanged " + deviceMac + " progress:" + percent)
            infoText.text = "OTA升级进度 " + percent + "%\n" + infoText.text
        }

        override fun onOTACompleted(deviceMac: String) {
            Log.e(TAG, deviceMac + " onOTACompleted")
            infoText.text = "OTA升级完成" + "\n" + infoText.text
        }

        override fun onOTAError(deviceMac: String, error: Int, errorType: Int, message: String?) {
            Log.e(TAG, "onError " + deviceMac + " error:" + error + " errorType:" + errorType + " message:" + message)
            infoText.text = "OTA升级出错" + "\n" + infoText.text
        }


    }
}