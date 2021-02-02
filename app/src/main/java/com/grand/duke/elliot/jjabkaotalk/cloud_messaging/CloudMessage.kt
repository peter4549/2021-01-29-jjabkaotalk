package com.grand.duke.elliot.jjabkaotalk.cloud_messaging

import com.grand.duke.elliot.jjabkaotalk.util.blank

data class CloudMessage(val registration_ids: List<String>) {

    var data: Data = Data()
    var notification: Notification = Notification()

    @Suppress("PropertyName")
    class Notification {
        var body: String = blank
        var click_action: String = blank
        var tag: String = blank
        var title: String = blank
    }

    class Data {
        var chatRoomId: String = blank
        var message: String = blank
        var senderName: String = blank
    }
}