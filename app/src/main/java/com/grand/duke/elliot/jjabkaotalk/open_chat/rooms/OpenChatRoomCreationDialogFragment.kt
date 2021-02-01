package com.grand.duke.elliot.jjabkaotalk.open_chat.rooms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.data.OpenChatRoom
import com.grand.duke.elliot.jjabkaotalk.databinding.FragmentOpenChatRoomCreationDialogBinding
import com.grand.duke.elliot.jjabkaotalk.firebase.FireStoreHelper
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import com.grand.duke.elliot.jjabkaotalk.util.hashString

class OpenChatRoomCreationDialogFragment: DialogFragment() {

    private lateinit var binding: FragmentOpenChatRoomCreationDialogBinding
    private val fireStoreHelper = FireStoreHelper()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOpenChatRoomCreationDialogBinding.inflate(inflater, container, false)

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
            fireStoreHelper.setOpenChatRoom(createOpenChatRoom(name), message)
            dismiss()
        }

        return binding.root
    }

    private fun createOpenChatRoom(name: String): OpenChatRoom {
        val user = MainApplication.user ?: throw NullPointerException("MainApplication.user?.uid is null.")
        val uid = user.uid
        val creationTime = System.currentTimeMillis()
        val id =  hashString(uid + creationTime).chunked(32)[0]

        return OpenChatRoom(
                id = id,
                name = name,
                location = "busan", // todo. change here.
                users = mutableListOf(user),
                time = creationTime
        )
    }
}