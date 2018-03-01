package com.qcymall.clickerpluslibrary

import android.content.Context

/**
 * Created by lanmi on 2018/2/26.
 */
object SharedPreferencesUtils {
    private val SP_NAME = "ClickerPlus_sp"

    fun getStringValueFromSP(context: Context, key: String): String{
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
                .getString(key, "");
    }
}