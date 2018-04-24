package com.qcymall.clickerpluslibrary.ota

/**
 * Created by lanmi on 2018/4/17.
 */
interface OTAListener {
    fun otaStart(devicemac: String)
    fun otaCompleted(devicemac: String)
    fun otaProgressChanged(devicemac: String, percent: Int)
    fun otaError(devicemac: String, error:Int)

}