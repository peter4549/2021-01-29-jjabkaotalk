package com.grand.duke.elliot.jjabkaotalk.chat.open_chat.room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.data.ChatMessage
import com.grand.duke.elliot.jjabkaotalk.data.OpenChatRoom
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.databinding.FragmentOpenChatRoomCreationDialogBinding
import com.grand.duke.elliot.jjabkaotalk.firebase.FireStoreHelper
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import com.grand.duke.elliot.jjabkaotalk.util.hashString

class OpenChatRoomCreationDialogFragment: DialogFragment(), FireStoreHelper.OnSetOpenChatRoomListener {

    private lateinit var binding: FragmentOpenChatRoomCreationDialogBinding
    private lateinit var uid: String
    private lateinit var user: User
    private val fireStoreHelper = FireStoreHelper()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOpenChatRoomCreationDialogBinding.inflate(inflater, container, false)

        fireStoreHelper.setOnSetOpenChatRoomListener(this)
        user = MainApplication.user ?: throw NullPointerException("MainApplication.user?.uid is null.")
        uid = user.uid

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonComplete.setOnClickListener {
            val name = binding.textInputEditTextOpenChatRoomName.text.toString()

            if (name.isBlank()) {
                // todo set error text.
                return@setOnClickListener
            }

            var message = getString(R.string.chat_room_entry_message)
            message = MainApplication.user?.name + message

            val chatMessage = ChatMessage(
                message = message,
                readerIds = mutableListOf(uid),
                senderId = uid,
                time = System.currentTimeMillis()
            )

            fireStoreHelper.setOpenChatRoom(createOpenChatRoom(name, chatMessage), chatMessage)
            dismiss()
        }

        return binding.root
    }

    private fun createOpenChatRoom(name: String, chatMessage: ChatMessage): OpenChatRoom {
        val creationTime = System.currentTimeMillis()
        val id =  hashString(uid + creationTime).chunked(32)[0]

        return OpenChatRoom(
            id = id,
            name = name,
            lastMessage = chatMessage,
            location = "busan", // todo. change here.
            unreadCounter = mutableMapOf(uid to 0),
            users = mutableListOf(user),
            time = creationTime
        )
    }

    override fun onSuccess(openChatRoomId: String) {
        fireStoreHelper.userArrayUnion(user.uid, User.FIELD_OPEN_CHAT_ROOMS, openChatRoomId)
    }

    override fun onFailure() {
        Toast.makeText(requireContext(), getString(R.string.failed_to_create_open_chat_room), Toast.LENGTH_LONG).show()
    }
}