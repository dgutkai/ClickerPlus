package com.qcymall.clickerpluslibrary.adpcm

/**
 * Created by lin on 2018/2/4.
 */

object AdpcmUtils {

    init {
        System.loadLibrary("native-lib")
        adpcmReset()
    }

    external fun adpcmReset()
    external fun adpcmCoder(indata: ByteArray, outdata: ByteArray, len: Int): Int
    external fun adpcmDecoder(indata: ByteArray, outdata: ByteArray, len: Int): Int
    external fun add(a1: Int, b1: Int): Int

}
