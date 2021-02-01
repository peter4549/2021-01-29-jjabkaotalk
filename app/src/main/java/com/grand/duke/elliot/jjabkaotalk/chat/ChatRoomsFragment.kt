package com.grand.duke.elliot.jjabkaotalk.chat

import com.google.firebase.auth.FirebaseAuth
import com.grand.duke.elliot.jjabkaotalk.base.BaseFragment

class ChatRoomsFragment private constructor(): BaseFragment() {

    companion object {
        @Volatile
        private var INSTANCE: ChatRoomsFragment? = null

        fun getInstance(): ChatRoomsFragment {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = ChatRoomsFragment()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}