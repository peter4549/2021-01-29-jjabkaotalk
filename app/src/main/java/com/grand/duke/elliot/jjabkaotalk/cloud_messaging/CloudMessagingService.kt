package com.grand.duke.elliot.jjabkaotalk.cloud_messaging

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.main.MainActivity
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import com.grand.duke.elliot.jjabkaotalk.shared_preferences.SharedPreferencesManager
import com.grand.duke.elliot.jjabkaotalk.util.blank

class CloudMessagingService: FirebaseMessagingService() {
    private lateinit var chatRoomId: String
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        sharedPreferencesManager = SharedPreferencesManager.instance(application)

        remoteMessage.data.isNotEmpty().let {
            chatRoomId = remoteMessage.data["chatRoomId"] ?: blank
            val message = remoteMessage.data["message"] ?: getString(R.string.unknown)
            val senderName = remoteMessage.data["senderName"] ?: getString(R.string.unknown)
            if (MainApplication.currentChatRoomId != chatRoomId)
                sendNotification(senderName, message)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    private fun sendNotification(senderPublicName: String, message: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.action = ACTION_CHAT_NOTIFICATION
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(EXTRA_NAME_CHAT_ROOM_ID, chatRoomId)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT)

        val channelId = CHANNEL_ID
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setContentText(message)
            .setContentTitle(senderPublicName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_molecule_24)
            .setSound(defaultSoundUri)

        /*
        when(sharedPreferencesManager.getNotificationSettings()) {
            SharedPreferencesManager.NotificationSettings.Silent -> notificationBuilder.setVibrate(null)
            SharedPreferencesManager.NotificationSettings.Sound -> notificationBuilder.setSound(defaultSoundUri)
            SharedPreferencesManager.NotificationSettings.Vibration -> notificationBuilder.setVibrate(longArrayOf(100L))
        }
         */

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                CHANNEL_TITLE,
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationManager.createNotificationChannel(channel)
        }

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(0, notificationBuilder.build())
        }
    }

    companion object {
        const val ACTION_CHAT_NOTIFICATION = "com.grand.duke.elliot.jjabkaotalk.cloud_messaging" +
                ".action_chat_notification"
        const val EXTRA_NAME_CHAT_ROOM_ID = "com.grand.duke.elliot.jjabkaotalk.cloud_messaging" +
                "extra_name_chat_room_id"
        private const val CHANNEL_ID = "default" // Always use this..
        private const val CHANNEL_TITLE = "com.grand.duke.elliot.jjabkaotalk.cloud_messaging" +
                ".channel_title"
    }
}