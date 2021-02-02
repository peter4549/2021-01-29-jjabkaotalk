package com.grand.duke.elliot.jjabkaotalk.chat.open_chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.*
import com.grand.duke.elliot.jjabkaotalk.base.BaseFragment
import com.grand.duke.elliot.jjabkaotalk.cloud_messaging.CloudMessagingHelper
import com.grand.duke.elliot.jjabkaotalk.data.ChatMessage
import com.grand.duke.elliot.jjabkaotalk.data.OpenChatRoom
import com.grand.duke.elliot.jjabkaotalk.data.OpenChatRoom.Companion.FIELD_LAST_MESSAGE
import com.grand.duke.elliot.jjabkaotalk.data.OpenChatRoom.Companion.FIELD_UNREAD_COUNTER
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.databinding.FragmentChatBinding
import com.grand.duke.elliot.jjabkaotalk.firebase.Collection.Messages
import com.grand.duke.elliot.jjabkaotalk.firebase.Collection.OpenChatRooms
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import timber.log.Timber

class OpenChatFragment: BaseFragment() {

    private lateinit var viewModel: OpenChatViewModel
    private lateinit var binding: FragmentChatBinding
    private lateinit var messageCollectionReference: CollectionReference
    private lateinit var openChatMessageAdapter: OpenChatMessageAdapter
    private lateinit var openChatRoom: OpenChatRoom
    private lateinit var openChatRoomDocumentReference: DocumentReference

    private val cloudMessagingHelper = CloudMessagingHelper()
    private val user = MainApplication.user ?: throw NullPointerException("OpenChatFragment: MainApplication.user is null.")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(viewModelStore, OpenChatViewModelFactory())[OpenChatViewModel::class.java]
        binding = FragmentChatBinding.inflate(inflater, container, false)

        val openChatFragmentArgs by navArgs<OpenChatFragmentArgs>()
        openChatRoom = openChatFragmentArgs.openChatRoom
        openChatRoom.let {
            val openChatRoomCollectionReference = FirebaseFirestore.getInstance().collection(OpenChatRooms)
            openChatRoomDocumentReference = openChatRoomCollectionReference.document(it.id)
            messageCollectionReference = openChatRoomDocumentReference.collection(Messages)

            openChatMessageAdapter = OpenChatMessageAdapter(it)
            binding.recyclerView.apply {
                adapter = openChatMessageAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }

            viewModel.setChatMessageSnapshotListener(it)
            viewModel.chatMessages.observe(viewLifecycleOwner, Observer { chatMessages ->
                val scrollTo =
                    if (chatMessages.count() > openChatMessageAdapter.itemCount)
                        0
                    else
                        null
                openChatMessageAdapter.addDateAndSubmitList(chatMessages, scrollTo)
            })
        }

        binding.buttonSend.setOnClickListener {
            val message = binding.editTextChat.text.toString()

            if (message.isBlank())
                return@setOnClickListener

            sendMessage(message)
        }

        return binding.root
    }

    private fun sendMessage(message: String) {
        binding.buttonSend.isEnabled = false
        val chatMessage = createChatMessage(message, System.currentTimeMillis())

        messageCollectionReference.add(chatMessage)
            .addOnSuccessListener {
                binding.buttonSend.isEnabled = true
                val userIds = openChatRoom.users.map { it.uid }

                // Update user.
                if (user.openChatRooms.contains(openChatRoom.id).not())
                    viewModel.fireStoreHelper.userArrayUnion(user.uid, User.FIELD_OPEN_CHAT_ROOMS, openChatRoom.id)

                // Update openChatRoom.
                if (openChatRoom.users.map { it.uid }.contains(user.uid).not())
                    viewModel.fireStoreHelper.chatRoomArrayUnion(openChatRoom.id, OpenChatRoom.FIELD_USERS, user)

                // Send cloud message.
                cloudMessagingHelper.sendCloudMessage(message, openChatRoom, user)

                // Update unreadCounter.
                for (uid in userIds) {
                    if (uid != user.uid) {
                        openChatRoom.let {
                            it.unreadCounter[uid] = it.unreadCounter[uid]?.plus(1) ?: 0
                        }
                    }
                }

                // Update lastMessage.
                openChatRoom.lastMessage = chatMessage
                openChatRoomDocumentReference
                    .update(mapOf(
                        FIELD_LAST_MESSAGE to openChatRoom.lastMessage,
                        FIELD_UNREAD_COUNTER to openChatRoom.unreadCounter
                    ))
                    .addOnSuccessListener {
                        Timber.d("lastMessage and unreadCounter updated.")
                    }
                    .addOnFailureListener {
                        Timber.w("lastMessage and unreadCounter update failed.")
                    }
            }
            .addOnFailureListener {
                binding.buttonSend.isEnabled = false
            }
    }

    private fun createChatMessage(message: String, time: Long): ChatMessage =
        ChatMessage(
            message = message,
            readerIds = mutableListOf(user.uid),
            senderId = user.uid,
            time = time
        )

    override fun onStop() {
        super.onStop()
        // Update unreadCounter.
        openChatRoom.unreadCounter[user.uid] = 0
        openChatRoomDocumentReference
            .update(mapOf(FIELD_UNREAD_COUNTER to openChatRoom.unreadCounter))
            .addOnSuccessListener {
                Timber.d("unreadCounter updated.")
            }
            .addOnFailureListener {
                Timber.e(it, "Failed to update unreadCounter.")
            }
    }
}