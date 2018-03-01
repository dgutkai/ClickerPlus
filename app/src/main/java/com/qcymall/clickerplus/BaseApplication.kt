package com.qcymall.clickerplus

import android.app.Application
import com.qcymall.clickerpluslibrary.ClickerPlus

/**
 * Created by lanmi on 2018/2/26.
 */
class BaseApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        ClickerPlus.initClicker(this)
    }
}