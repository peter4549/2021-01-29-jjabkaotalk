package com.grand.duke.elliot.jjabkaotalk.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.firebase.FireStoreHelper

class FriendsViewModel: ViewModel() {

    private val fireStoreHelper = FireStoreHelper()
    private val friendIds = mutableListOf<String>()

    private val _friends = MutableLiveData<List<User>>()
    val friends: LiveData<List<User>>
        get() = _friends

    fun update(user: User) {
        if (friendIds.isEmpty()) {
            friendIds.addAll(user.friendIds.map { it }.toMutableList())
        } else {
            friendIds.addAll(friendIds.minus(user.friendIds))
            friendIds.removeAll(user.friendIds.minus(friendIds))
        }

        fireStoreHelper.getUsers(friendIds) { users ->
            _friends.value = users
        }
    }
}