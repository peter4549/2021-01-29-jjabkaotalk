package com.grand.duke.elliot.jjabkaotalk.friends

import com.grand.duke.elliot.jjabkaotalk.base.BaseFragment

class FriendsFragment private constructor(): BaseFragment() {

    companion object {
        @Volatile
        private var INSTANCE: FriendsFragment? = null

        fun getInstance(): FriendsFragment {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = FriendsFragment()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}