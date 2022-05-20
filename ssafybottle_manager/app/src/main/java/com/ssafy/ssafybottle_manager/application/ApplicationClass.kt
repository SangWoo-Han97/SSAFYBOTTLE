package com.ssafy.ssafybottle_manager.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.messaging.FirebaseMessaging

class ApplicationClass : Application() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        initFCMMessageAccept()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initFCMMessageAccept() {
        // FCM 토큰 받아오기
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.d("Application_싸피", "onCreate: FCM 토큰 얻기 실패", it.exception)
                return@addOnCompleteListener
            }
            // 새로운 FCM 등록 토큰을 얻음
            Log.d("Application_싸피", "onCreate: 새로운 등록 토큰: ${it.result}")
            //saveToken(it.result)
            createNotificationChannel("smart_store", "smart_store")
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(id: String, name: String) {
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(
                NotificationChannel(
                    id,
                    name,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
    }
}