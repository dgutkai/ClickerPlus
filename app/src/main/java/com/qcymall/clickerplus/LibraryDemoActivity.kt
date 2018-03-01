package com.qcymall.clickerplus

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.qcymall.clickerpluslibrary.ClickerPlus
import com.qcymall.clickerpluslibrary.SharedPreferencesUtils
import java.util.HashMap

/**
 * Created by lanmi on 2018/2/27.
 */
class LibraryDemoActivity: AppCompatActivity()  {

    private var mDevice: BluetoothDevice? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_librarydemo)
        val mapData = intent.getSerializableExtra("data") as HashMap<String, Any>
        mDevice = mapData["btdevice"] as BluetoothDevice
        Log.e("TAG", mDevice!!.name)
    }

    fun pairClick(v: View){
        ClickerPlus.connectDevice(mDevice, "123456")
    }

    fun unpairClick(v: View){

    }
}