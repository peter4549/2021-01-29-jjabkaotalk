package com.grand.duke.elliot.jjabkaotalk.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.grand.duke.elliot.jjabkaotalk.chat.mine.MyChatRoomsFragment
import com.grand.duke.elliot.jjabkaotalk.friends.FriendsFragment
import com.grand.duke.elliot.jjabkaotalk.chat.room.ChatRoomsFragment

class FragmentStateAdapter(fragmentActivity: FragmentActivity):
    androidx.viewpager2.adapter.FragmentStateAdapter(fragmentActivity) {
    private val pageCount = 3

    override fun getItemCount(): Int = pageCount

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ChatRoomsFragment.getInstance()
            1 -> MyChatRoomsFragment.getInstance()
            2 -> FriendsFragment.getInstance()
            else -> throw Exception("Invalid position.")
        }
    }
}