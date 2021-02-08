package com.grand.duke.elliot.jjabkaotalk.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.firebase.FireStoreHelper

class FriendsViewModel: ViewModel() {

    private val fireStoreHelper = FireStoreHelper()
    private val existingFriendIds = mutableListOf<String>()

    private val _friends = MutableLiveData<MutableList<User>>()
    val friends: LiveData<MutableList<User>>
        get() = _friends

    fun update(user: User) {
        if (existingFriendIds.isEmpty()) {
            existingFriendIds.addAll(user.friendIds.map { it }.toMutableList())
            fireStoreHelper.getUsers(existingFriendIds) { users ->
                _friends.value = users as MutableList<User>
            }
        } else {
            // Added.
            if (user.friendIds.count() > existingFriendIds.count()) {
                val new = user.friendIds.minus(existingFriendIds)
                fireStoreHelper.getUsers(new) { users ->
                    val existingUsers = friends.value
                    existingUsers?.addAll(users)
                    _friends.value = existingUsers
                }
            }

            // Deleted.
            if (user.friendIds.count() < existingFriendIds.count()) {
                val deleted: List<String> = existingFriendIds.minus(user.friendIds)
                fireStoreHelper.getUsers(deleted) {
                    val existingUsers = friends.value
                    existingUsers?.removeAll(it)
                    _friends.value = existingUsers
                }

            }
        }
    }
}