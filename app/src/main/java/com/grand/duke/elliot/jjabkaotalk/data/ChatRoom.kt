package com.grand.duke.elliot.jjabkaotalk.data

class ChatRoom (
    val id: Long,
    var name: String,
    var users: Array<User> // TODO, check, is able to be list?
)