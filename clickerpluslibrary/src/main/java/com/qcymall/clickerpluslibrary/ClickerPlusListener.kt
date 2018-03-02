package com.qcymall.clickerpluslibrary

import android.bluetooth.BluetoothDevice

/**
 * Created by lanmi on 2018/2/26.
 */
interface ClickerPlusListener {
    fun onConnect(deviceMac: String)
    fun onDisconnect(deviceMac: String)
    fun onPair(state: ClickerPlus.ClickerPlusState)
    fun onCancelPair(state: ClickerPlus.ClickerPlusState)
    fun onConnectBack(state: ClickerPlus.ClickerPlusState)
    fun onClick()
    fun onDoubleClick()
    fun onLongPress()
    fun onWeakup()
    fun onIdeaCapsule()
    fun onVoicePCMStart()
    fun onVoicePCM(data: ByteArray, index: Int)
    fun onVoicePCMEnd()
    fun onIdeaPCMStart(header: String)
    fun onIdeaPCM(data: ByteArray, index: Int)
    fun onIdeaPCMEnd(info: ByteArray?)
    fun onBatteryChange(percent: Int)

    fun onOTAStart(deviceMac: String)
    fun onOTAProgressChanged(deviceMac: String, percent: Int, speed: Float, avgSpeed: Float, currentPart: Int, partsTotal: Int)
    fun onOTACompleted(deviceMac: String)
    fun onOTAError(deviceMac: String, error: Int, errorType: Int, message: String?)
}