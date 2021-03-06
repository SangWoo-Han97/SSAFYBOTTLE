package com.ssafy.smartstore.core.service

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ssafy.smartstore.application.MainActivity
import com.ssafy.smartstore.data.entitiy.Notification
import com.ssafy.smartstore.data.repository.Repository
import com.ssafy.smartstore.utils.getNotificationBuilder
import com.ssafy.smartstore.utils.getUserId
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FCMService : FirebaseMessagingService() {

    private lateinit var repository: Repository
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }

    override fun onCreate() {
        super.onCreate()

        repository = Repository.get()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCMService_싸피", "onNewToken: 토큰 갱신: $token")
        val userId = getUserId()
        CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            Repository.get().postToken(mapOf("userId" to userId, "token" to token))
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("FCMService_싸피", message.toString())

        message.notification?.let {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pIntent = PendingIntent.getActivity(this, 0, intent, 0)

            NotificationManagerCompat.from(this).notify(
                101,
                getNotificationBuilder(
                    "smart_store",
                    it.title ?: "",
                    it.body ?: "",
                    pIntent
                ).build()
            )
        }
    }
}