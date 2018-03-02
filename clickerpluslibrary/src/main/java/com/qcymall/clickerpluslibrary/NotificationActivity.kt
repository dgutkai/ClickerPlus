package com.qcymall.clickerpluslibrary

import android.support.v7.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log


/**
 * Created by lanmi on 2018/3/2.
 */
class NotificationActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If this activity is the root activity of the task, the app is not running
        if (isTaskRoot) {
            // Start the app before finishing
//            val intent = Intent(this, MyActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            intent.putExtras(getIntent().extras!!) // copy all extras
//            startActivity(intent)
        }
        Log.e("NotificationActivity", "OnCreate ")
        // Now finish, which will drop you to the activity at which you were at the top of the task stack
        finish()
    }
}