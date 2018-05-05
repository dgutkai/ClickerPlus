package com.qcymall.clickerpluslibrary.utils

import com.inuker.bluetooth.library.utils.ByteUtils
import com.qcymall.clickerpluslibrary.adpcm.AdpcmUtils
import java.util.*
import kotlin.experimental.and

/**
 * Created by lanmi on 2018/2/28.
 */
object BLECMDUtil {

    enum class ConnectState{
        fail,
        success,
        request
    }

    private val VERIFY_CODE1 = 0xFE.toByte() // 指令头（验证码1）
    private val VERIFY_CODE2 = 0xCF.toByte() // 指令头（验证码2）

    val CMDID_PAIR = 0x5001       // 配对连接
    val CMDID_UNPAIR = 0x5002       // 取消配对连接
    val CMDID_CONNECTBACK = 0x5003      // 回连
    val CMDID_FIND = 0x5010        // 查找设备
    val CMDID_BATTERY = 0x5011      // 获取电池电量
    val CMDID_INCREASE = 0x5018      // 获取MIC增益
    val CMDID_VERSION  = 0x500F      // 获取版本号
    val CMDID_CHANGE_MAC = 0x5019       // 更改MAC

    val CMDID_OTA = 0x5012      // OTA升级
    val CMDID_TIME = 0x5013     // 时间同步
    val CMDID_FINDPHONE = 0x5014    // 寻找手机

    val CMDID_WEAKUP = 0x5004       // 唤醒
    val CMDID_CLICK = 0x5005        // 单击
    val CMDID_DOUBLECLICK = 0x5006      // 双击
    val CMDID_LONGPRESS = 0x5007        // 长按
    val CMDID_IDEA = 0x5008     // 闪念胶囊
    val CMDID_VOICESTART = 0x5009     // 语音PCM数据开始
    val CMDID_VOICEPCM = 0x500A     // 语音PCM数据
    val CMDID_VOICEEND = 0x500B     // 语音PCM数据结束
    val CMDID_IDEASTART = 0x500C     // 闪念PCM数据开始
    val CMDID_IDEAPCM = 0x500D     // 闪念PCM数据
    val CMDID_IDEAEND = 0x500E     // 闪念PCM数据结束

    val CMDID_VOICE_TMP_START = 0x5015     // 闪念PCM数据开始
    val CMDID_VOICE_TMP_PCM = 0x5016     // 闪念PCM数据
    val CMDID_VOICE_TMP_END = 0x5017     // 闪念PCM数据结束

    private var cmdIndex = 0
    /**
     * 指令解析结果
     */
    class ParseResult {
        var version: Int = 0 // 版本号
        var id: Int = 0   // 指令ID
        var index: Int = 0   // 序号
        var error: Int = 0   // 错误码
        var data: ByteArray? = null // 数据

    }

    /**
     * 打包指令帧
     * @param cmdID 指令类型编号
     * @param data 指令数据
     * @return 打包后的指令字符数组
     */
    fun packageCMD(cmdID: Int, data: ByteArray?): ByteArray {
        var datatemp = data
        if (data == null) {
            datatemp = ByteArray(0)
        }
        val datalen = 12 + datatemp!!.size
        val packData = ByteArray(12 + datatemp.size)
        packData[0] = VERIFY_CODE1
        packData[1] = VERIFY_CODE2
        packData[2] = 0x00.toByte()
        packData[3] = 0x01.toByte()
        packData[4] = (datalen and 0xff00 shr 8).toByte()
        packData[5] = (datalen and 0xff).toByte()
        packData[6] = (cmdID and 0xff00 shr 8).toByte()
        packData[7] = (cmdID and 0xff).toByte()
        packData[8] = (cmdIndex and 0xff00 shr 8).toByte()
        packData[9] = (cmdIndex and 0xff).toByte()
        packData[10] = 0x00.toByte()
        packData[11] = 0x00.toByte()
        System.arraycopy(datatemp, 0, packData, 12, datatemp.size)
        cmdIndex += 1
        return packData
    }

    /**
     * 解析一条指令数据
     * @param cmd 指令数据
     * @return 解析结果
     */
    fun parseCMD(cmd: ByteArray?): ParseResult? {
        var parseResult: ParseResult? = null
        if (cmd != null && cmd.size > 6) {
            val datalen = getInt(cmd[4], cmd[5])
            if (cmd.size == datalen && cmd[0] == VERIFY_CODE1 && cmd[1] == VERIFY_CODE2) {
                parseResult = ParseResult()
                parseResult.version = getInt(cmd[2], cmd[3])
                parseResult.id = getInt(cmd[6], cmd[7])
                parseResult.index = getInt(cmd[8], cmd[9])
                parseResult.error = getInt(cmd[10], cmd[11])
                parseResult.data = ByteArray(datalen - 12)
                System.arraycopy(cmd, 12, parseResult.data, 0, datalen - 12)
                return parseResult
            }
        }
        return null
    }

    /**
     * 合字节为一个INT数据
     * @param d     Byte数据
     * @return Int数据
     */
    fun getInt(vararg d: Byte): Int {
        var result = 0
        if (d != null) {
            for (b in d) {
                result = (result shl 8) + (b.toInt() and 0xff)
            }
        }
        return result
    }

    /**
     * 创建一个配对的指令数据
     * @param deviceInfo 唯一识别码，用于设备绑定
     */
    fun createPariCMD(deviceInfo: String): ByteArray{
        val data = deviceInfo.toByteArray()
        val infoByte = ByteArray(8)
        var datalen = 7
        if (data.size.compareTo(datalen) < 0){
            datalen = data.size
        }
        System.arraycopy(data, 0, infoByte, 0, datalen)
        infoByte[7] = 1
        return packageCMD(CMDID_PAIR, infoByte)
    }

    /**
     * 创建一个取消配对的指令数据
     * @param deviceInfo 唯一识别码，用于设备绑定
     */
    fun createUnpariCMD(deviceInfo: String, isAll: Boolean): ByteArray{
        val data = deviceInfo.toByteArray()
        val infoByte = ByteArray(8)
        var datalen = 7
        if (data.size.compareTo(datalen) < 0){
            datalen = data.size
        }
        System.arraycopy(data, 0, infoByte, 0, datalen)
        if (isAll){
            infoByte[7] = 0xff.toByte()
        }else {
            infoByte[7] = 1
        }
        return packageCMD(CMDID_UNPAIR, infoByte)
    }

    fun createConnectBackCMD(deviceInfo: String): ByteArray{
        val data = deviceInfo.toByteArray()
        val infoByte = ByteArray(8)
        var datalen = 7
        if (data.size.compareTo(datalen) < 0){
            datalen = data.size
        }
        System.arraycopy(data, 0, infoByte, 0, datalen)
        infoByte[7] = 1
        return packageCMD(CMDID_CONNECTBACK, infoByte)
    }

    fun createFindCMD(): ByteArray{
        return packageCMD(CMDID_FIND, null)
    }

    fun createBatteryCMD(): ByteArray{
        return packageCMD(CMDID_BATTERY, null)
    }

    fun createVersionCMD(): ByteArray{
        return packageCMD(CMDID_VERSION, null)
    }

    fun createChangeMACCMD(mac: String): ByteArray{

        return packageCMD(CMDID_CHANGE_MAC, ByteUtils.stringToBytes(mac))
    }

    fun createIncreaseCMD(value: Int): ByteArray{
        var v = value
        if (v < 0){
            v = 0
        }else if(v > 0x50){
            v = 0x50
        }
        return packageCMD(CMDID_INCREASE, byteArrayOf(v.toByte()))
    }
    fun createOTACMD(): ByteArray{
        return packageCMD(CMDID_OTA, null)
    }

    fun createTimeCMD(): ByteArray{
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        var way = calendar.get(Calendar.DAY_OF_WEEK)
        // 默认是星期日为一个星期第一天，这里处理一下
        if (way > 1) {
            way -= 1
        } else {
            way = 7
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val rtc_data = ByteArray(8)
        rtc_data[0] = (year and 0xff00 shr 8).toByte()
        rtc_data[1] = (year and 0xff).toByte()
        rtc_data[2] = month.toByte()
        rtc_data[3] = day.toByte()
        rtc_data[4] = hour.toByte()
        rtc_data[5] = minute.toByte()
        rtc_data[6] = second.toByte()
        rtc_data[7] = way.toByte()
        return packageCMD(CMDID_TIME, rtc_data);
    }

    fun parsePairCMD(data: ByteArray?): ConnectState{
        if (data != null && data.isNotEmpty()){
            if (data[0] == 0.toByte()){
                return ConnectState.fail
            }else if (data[0] == 1.toByte()){
                return ConnectState.success
            }else if (data[0] == 2.toByte()){
                return ConnectState.request
            }
        }
        return ConnectState.fail
    }
    fun parseUnpairCMD(data: ByteArray?): Boolean{
        if (data != null && data.isNotEmpty()){
            return data[0] == 1.toByte()
        }
        return false
    }

    fun parseConnectbackCMD(data: ByteArray?): ConnectState{
        if (data != null && data.isNotEmpty()){
            if (data[0] == 0.toByte()){
                return ConnectState.fail
            }else if (data[0] == 1.toByte()){
                return ConnectState.success
            }else if (data[0] == 2.toByte()){
                return ConnectState.request
            }
        }
        return ConnectState.fail
    }
    fun parseTimeBackCMD(data: ByteArray?): Boolean{
        if (data != null && data.isNotEmpty()){
            return data[0] == 1.toByte()
        }
        return false
    }

    fun parseVersionCMD(data: ByteArray?): String{
        if (data != null && data.isNotEmpty()){
            return String(data, 1, data.size - 1)
        }
        return ""
    }

    fun parsePCMData(data: ByteArray?): HashMap<String, Any>{
        if (data == null || data.isEmpty()) {
            val resultMap = HashMap<String, Any>()
            resultMap.put("index", 0)
            resultMap.put("pcmData", ByteArray(0))
            return resultMap
        }
        val index = getInt(data!![0], data[1])
        val pcmData = ByteArray(data.size - 2)
        System.arraycopy(data, 2, pcmData, 0, pcmData.size)
        val resultData = ByteArray(pcmData.size * 4)
        AdpcmUtils.adpcmDecoder(pcmData, resultData, pcmData.size)
        val resultMap = HashMap<String, Any>()
        resultMap.put("index", index)
        resultMap.put("pcmData", resultData)
        return resultMap
    }
    fun parseVoiceTmpHeader(data: ByteArray?): String{
        if (data != null && data.isNotEmpty()) {
            val resultString = String(data!!)
            return resultString
        }else{
            return ""
        }
    }

    fun parseIdeaHeader(data: ByteArray?): String{
        if (data != null && data.isNotEmpty()) {
            val resultString = String(data!!)
            return resultString
        }else{
            return ""
        }
    }
    fun parseBatteryCMD(data: ByteArray?): Int{
        if (data != null && data.isNotEmpty()){
            return data[0].toInt()
        }
        return 0
    }
}

