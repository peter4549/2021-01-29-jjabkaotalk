package com.grand.duke.elliot.jjabkaotalk.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.grand.duke.elliot.jjabkaotalk.chat.ChatRoomsFragment
import com.grand.duke.elliot.jjabkaotalk.friends.FriendsFragment
import com.grand.duke.elliot.jjabkaotalk.open_chat.rooms.OpenChatRoomsFragment

class FragmentStateAdapter(fragmentActivity: FragmentActivity):
    androidx.viewpager2.adapter.FragmentStateAdapter(fragmentActivity) {
    private val pageCount = 3

    override fun getItemCount(): Int = pageCount

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OpenChatRoomsFragment.getInstance()
            1 -> ChatRoomsFragment.getInstance()
            2 -> FriendsFragment.getInstance()
            else -> throw Exception("Invalid position.")
        }
    }
}