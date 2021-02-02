package com.grand.duke.elliot.jjabkaotalk.cloud_messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.main.MainActivity
import com.grand.duke.elliot.jjabkaotalk.util.blank

class CloudMessagingService: FirebaseMessagingService() {
    private lateinit var chatRoomId: String

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.isNotEmpty().let {

            chatRoomId = remoteMessage.data["chatRoomId"] ?: blank
            val message = remoteMessage.data["message"] ?: getString(R.string.unknown)
            val senderName = remoteMessage.data["senderName"] ?: getString(R.string.unknown)
            sendNotification(senderName, message)
            //if (MainActivity.currentChatRoomId != roomId) todo check here.
            //    sendNotification(senderPublicName, message)
        }
    }

    // 여기서 업데이트 해주면됨. 유저의 토큰 등록..
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
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setContentText(message)
            .setContentTitle(senderPublicName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_snail_512px)
            .setSound(defaultSoundUri)  // 사운드 조정 필요.

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                CHANNEL_TITLE,
                NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build()) // id 부분, 만약 메시지 다른데서 왓어도 정상동작할런지.
    }

    companion object {
        const val ACTION_CHAT_NOTIFICATION = "com.grand.duke.elliot.jjabkaotalk.cloud_messaging" +
                ".action_chat_notification"
        const val EXTRA_NAME_CHAT_ROOM_ID = "com.grand.duke.elliot.jjabkaotalk.cloud_messaging" +
                "extra_name_chat_room_id"
        private const val CHANNEL_ID = "com.grand.duke.elliot.jjabkaotalk.cloud_messaging" +
                ".channel_id"
        private const val CHANNEL_TITLE = "com.grand.duke.elliot.jjabkaotalk.cloud_messaging" +
                ".channel_title"
    }

}