package com.qcymall.clickerpluslibrary

import android.bluetooth.BluetoothDevice

/**
 * Created by lanmi on 2018/2/26.
 */
interface ClickerPlusListener {
    fun onConnect(device: BluetoothDevice)
    fun onDisconnect(device: BluetoothDevice)
    fun onPair(state: ClickerPlus.ClickerPlusState)
    fun onCancelPair(state: ClickerPlus.ClickerPlusState)
    fun onConnectBack(state: ClickerPlus.ClickerPlusState)
    fun onClick()
    fun onDoubleClick()
    fun onLongClick()
    fun onWeakup()
    fun onIdeaCapsule()
    fun onVoicePCM(data: ByteArray, isEnd: Boolean)
    fun onIdeaPCM(data: ByteArray, isEnd: Boolean)
    fun onBatteryChange(percent: Int)
}