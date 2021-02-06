package com.grand.duke.elliot.jjabkaotalk.cloud_messaging

import com.grand.duke.elliot.jjabkaotalk.data.ChatRoom
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.squareup.okhttp.ResponseBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class CloudMessagingHelper {

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    fun sendCloudMessage(message: String, chatRoom: ChatRoom, sender: User) {
        val tokens = chatRoom.users
            .filter { it.token != sender.token }
            .map { it.token }

        val cloudMessage = CloudMessage(tokens)
        cloudMessage.notification.title = sender.name
        cloudMessage.notification.body = message
        cloudMessage.notification.click_action = "action.ad.astra.cloud.message.click"
        cloudMessage.notification.tag = chatRoom.id

        cloudMessage.data.message = message
        cloudMessage.data.chatRoomId = chatRoom.id
        cloudMessage.data.senderName = sender.name
        coroutineScope.launch {
            CloudMessageApi.getCloudMessagingService()
                    .request(requestBody = cloudMessage)
                    .enqueue(object: Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if (response.isSuccessful)
                                Timber.d("Cloud message sent.")
                            else
                                Timber.w("Cloud message sent failed.")
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            Timber.w("Cloud message sent failed.")
                        }
                    })
        }
    }
}