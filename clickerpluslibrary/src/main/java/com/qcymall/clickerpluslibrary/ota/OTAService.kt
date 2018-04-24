package com.qcymall.clickerpluslibrary.ota

import android.app.Service
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.inuker.bluetooth.library.BluetoothClient
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse
import com.inuker.bluetooth.library.connect.response.BleWriteResponse
import com.inuker.bluetooth.library.utils.ByteUtils
import com.qcymall.clickerpluslibrary.ClickerPlus
import java.io.*
import java.util.*
import kotlin.experimental.xor

/**
 * Created by lanmi on 2018/4/14.
 */
class OTAService(mClien: BluetoothClient, macString: String) {
    private val TAG = "OTAService"
    // Todo: 添加OTA升级的服务号
    private val SPOTA_SERVICE_UUID = UUID.fromString("0000fef5-0000-1000-8000-00805f9b34fb")

    private val SPOTA_MEM_DEV_UUID = UUID.fromString("8082caa8-41a6-4021-91c6-56f9b954cc34") // step1
    private val SPOTA_GPIO_MAP_UUID = UUID.fromString("724249f0-5eC3-4b5f-8804-42345af08651") // step2
    private val SPOTA_PATCH_LEN_UUID = UUID.fromString("9d84b9a3-000c-49d8-9183-855b673fda31") // step3
    private val SPOTA_PATCH_DATA_UUID = UUID.fromString("457871e8-d516-4ca1-9116-57d0b17b9cb2") // step4
    private val SPOTA_SERV_STATUS_UUID = UUID.fromString("5f78df94-798c-46f5-990a-b3eb6a065c88") // step5

    private var listener: OTAListener? = null
    private val mBluetoothClient: BluetoothClient = mClien
    private val mMacString: String = macString
    private var file: File? = null
    private var instream: InputStream? = null

    private var isEND = false
    private var byte_All: ByteArray? = null
    private var lenth: Long = 0
    private var input: InputStream? = null
    private var writeCount: Int = 1
    private var flag_num = 0
    private var isFinish = true
    private var shengYuNum = 1
    private var flag_all = true
    private var writeSize: Long = 0
    private var updatePercent: Int = -1
    fun otaUpdate(path: String, otalistener: OTAListener): Boolean{

        listener = otalistener
        writeCount = 1
        flag_num = 0
        writeSize = 0
        updatePercent = -1
        file = File(path)
        if (file == null){
            return false
        }
        try {
            instream = FileInputStream(file!!)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        Log.e(TAG, "start OTAUpadate path=$path")
        update_step0()
        if (listener != null){
            listener!!.otaStart(mMacString)
        }
        return true
    }


    private fun update_step0(){
        Log.e(TAG, "OTA Step0 ----> notify")
        mBluetoothClient.notify(mMacString, SPOTA_SERVICE_UUID, SPOTA_SERV_STATUS_UUID, mNotifyRsp)
    }
    private fun update_step1(){
        Log.e(TAG, "OTA Step1 ----> write Data <00 00 00 13>")
        mBluetoothClient.write(mMacString, SPOTA_SERVICE_UUID, SPOTA_MEM_DEV_UUID, byteArrayOf(0x00, 0x00, 0x00, 0x13), mWriteRsp1)

    }
    private fun update_step2(){
        Log.e(TAG, "OTA Step2 ----> write Data <00 03 06 05>")
        mBluetoothClient.write(mMacString, SPOTA_SERVICE_UUID, SPOTA_GPIO_MAP_UUID, byteArrayOf(0x00, 0x03, 0x06, 0x05), mWriteRsp2)
    }

    private fun update_step3(){
        Log.e(TAG, "OTA Step3 ----> read file, write Data <f0 00>")
        if (instream == null) {
            return
        }
        try {
            val bytesAvailable = instream!!.available()

            byte_All = ByteArray(bytesAvailable + 1)

            Log.d("LOGS", "bytesAvailable = $bytesAvailable")

            instream!!.read(byte_All)

            val crc = getCrc(bytesAvailable)
            byte_All!![bytesAvailable] = crc //这个数组中是加了rcr的新数组
            lenth = byte_All!!.size.toLong() //数组赋予新长度

            input = ByteArrayInputStream(byte_All)

        } catch (e: IOException) {
            e.printStackTrace()
        }

        mBluetoothClient.write(mMacString, SPOTA_SERVICE_UUID, SPOTA_PATCH_LEN_UUID, byteArrayOf(0xf0.toByte(), 0x00), mWriteRsp3)

    }
    private fun update_step4(){

                if (writeCount != byte_All!!.size / 240 + 1) {

                    if (flag_num < 12 && isFinish) {
                        val byte_now = ByteArray(20)
                        try {
                            val len = input!!.read(byte_now)

                            Log.e("temp", "$len")
                            if (len != -1) {
                                Log.e(TAG, "OTA Step4 ----> sendData, count=$writeCount, flagnum = $flag_num, length = $lenth")
                                if (listener != null){
                                    writeSize += len
                                    val percent = (writeSize*100)/lenth
                                    if (updatePercent < percent){
                                        updatePercent = percent.toInt()
                                        listener!!.otaProgressChanged(mMacString, updatePercent)
                                    }
                                }
                                mBluetoothClient.write(mMacString, SPOTA_SERVICE_UUID, SPOTA_PATCH_DATA_UUID, byte_now, mWriteRsp4)
                            }else{
                                Log.e(TAG, "OTA Step4 ----> data readEnd")
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    } else {

                        Log.e(TAG, "OTA Step4 ----> wait")
                    }
                } else {
                    val shengYuByte2 = byte_All!!.size % 240
                    shengYuNum = 1 //初始化下一包不满的数据
                    isEND = true
                    Log.e(TAG, "OTA Step4 ----> send other Data, shengYuByte = $shengYuByte2")
                    val bs = byteArrayOf((shengYuByte2 and 0xFF).toByte(), (shengYuByte2 shr 8 and 0xFF).toByte())
                    Log.e(TAG, "OTA Step4 ----> " + ByteUtils.byteToString(bs))
                    mBluetoothClient.write(mMacString, SPOTA_SERVICE_UUID, SPOTA_PATCH_LEN_UUID,
                            bs, mWriteRsp3)

                }
    }
    private fun update_step5(){
        val shengYuByte2 = byte_All!!.size % 240
        if (shengYuNum != shengYuByte2 / 20 + 1) {
            Log.e(TAG, "OTA Step5 ----> write last Data , writeCount = $writeCount, shengYuNum=$shengYuNum, length = $lenth")
            val byte_now = ByteArray(20)
            try {
                val len = input!!.read(byte_now)
                if (len != -1) {
                    if (listener != null){
                        writeSize += len
                        val percent = (writeSize*100)/lenth
                        if (updatePercent < percent){
                            updatePercent = percent.toInt()
                            listener!!.otaProgressChanged(mMacString, updatePercent)
                        }

                    }
                    mBluetoothClient.write(mMacString, SPOTA_SERVICE_UUID, SPOTA_PATCH_DATA_UUID, byte_now, mWriteRsp4)

                }
            } catch (e: IOException) {
                e.printStackTrace()
            }


        } else {

            val byte_now = ByteArray(shengYuByte2 % 20)
            try {
                val len = input!!.read(byte_now)
                Log.e(TAG, "OTA Step5 ----> write last Data len = $len")
                if (len != -1) {
                    if (listener != null){
                        writeSize += len
                        val percent = (writeSize*100)/lenth
                        if (updatePercent < percent){
                            updatePercent = percent.toInt()
                            listener!!.otaProgressChanged(mMacString, updatePercent)
                        }
                    }
                    mBluetoothClient.write(mMacString, SPOTA_SERVICE_UUID, SPOTA_PATCH_DATA_UUID, byte_now, mWriteRsp4)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

    }

    private fun update_step6(){
        Log.e(TAG, "OTA Step5 ----> end flage, writeData = <00 00 00 fe>")
        mBluetoothClient.write(mMacString, SPOTA_SERVICE_UUID, SPOTA_MEM_DEV_UUID, byteArrayOf(0x00, 0x00, 0x00, 0xfe.toByte()), mWriteRsp1)

        isFinish = false
    }
    private fun update_step7(){
        Log.e(TAG, "OTA Step7 ----> restart, writeData = <00 00 00 fd>")
        mBluetoothClient.write(mMacString, SPOTA_SERVICE_UUID, SPOTA_MEM_DEV_UUID, byteArrayOf(0x00, 0x00, 0x00, 0xfd.toByte()), mWriteRsp1)
        if (listener != null){
            listener!!.otaCompleted(mMacString)
        }
        isFinish = false
    }
    private val mWriteRsp1 = object : BleWriteResponse{
        override fun onResponse(code: Int) {
            Log.e(TAG, "OTA Step1 ----> write Data OK, isEnd=$isEND")
            if (!isEND) {
                when (code) {
                    133 -> Log.d(TAG, " 数据传输完毕........................开始重启")
                    0 -> {
                        Log.d(TAG, " OTA升级准备好--------，开始配置信息")
                        update_step2()
                    }
                }
            } else {
                Log.d(TAG, " ***************************所有结束，开始复位")

            }
        }

    }
    private val mWriteRsp2 = object : BleWriteResponse{
        override fun onResponse(code: Int) {
            Log.e(TAG, "OTA Step2 ----> write Data OK")
            update_step3()
        }

    }
    private val mWriteRsp3 = object : BleWriteResponse{
        override fun onResponse(code: Int) {
            Log.e(TAG, "OTA Step3 ----> write Data OK, isEnd=$isEND")
            if (isEND) {
                update_step5()
            } else {
                update_step4()
            }
        }

    }
    private val mWriteRsp4 = object : BleWriteResponse{
        override fun onResponse(code: Int) {
            if (flag_all && !isEND) {
                Log.e(TAG, "OTA Step4 ----> write Data OK, writeCount = $writeCount, flag_num=$flag_num")
                flag_num += 1//写入后增加一次
                update_step4()
            }
            if (isEND) {
                Log.e(TAG, "OTA Step4 ----> write other Data OK, writeCount = $writeCount, shengYuNum=$shengYuNum")
                shengYuNum++
                update_step5()

            }
        }
    }

    private val mNotifyRsp = object : BleNotifyResponse{
        override fun onResponse(code: Int) {
            Log.e(TAG, "OTA Step0 ----> notify OK $code")
            if (code == 0) {
                update_step1()
            }else{
                if (listener != null){
                    listener!!.otaError(mMacString, 101)
                }
            }

        }

        override fun onNotify(service: UUID?, character: UUID?, value: ByteArray?) {
            Log.e(TAG, "OTA Notify ----> value = " + ByteUtils.byteToString(value))
            if (SPOTA_SERV_STATUS_UUID.equals(character) && 2 == value!![0].toInt() ) {
                if (isFinish) {
                    Log.e(TAG, "OTA Notify ----> 第  $writeCount  包数据验证成功*******")

                    flag_all = true
                    flag_num = 0
                    writeCount++
                    if (isEND) {
                        Log.e(TAG, "OTA Notify ----> 数据发送完成，准备复位")
                        update_step6()
                    } else {
                        update_step4()
                    }
                }else{
                    update_step7()
                }
            }else if(SPOTA_SERV_STATUS_UUID.equals(character) && 21 == value!![0].toInt()){
                if (listener != null){
                    listener!!.otaError(mMacString, 102)
                }
            }
        }

    }

    @Throws(IOException::class)
    private fun getCrc(bytesAvailable: Int): Byte {
        var crc_code: Byte = 0
        for (i in 0 until bytesAvailable) {
            val byteValue = byte_All!![i]
            val intVal = byteValue.toInt()
            crc_code = crc_code xor intVal.toByte()
        }
        Log.d("LOGS", "crc =  " + String.format("%#10x", crc_code))
        return crc_code
    }
}