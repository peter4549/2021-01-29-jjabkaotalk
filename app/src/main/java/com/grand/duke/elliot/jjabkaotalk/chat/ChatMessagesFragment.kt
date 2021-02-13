package com.grand.duke.elliot.jjabkaotalk.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.*
import com.grand.duke.elliot.jjabkaotalk.base.BaseFragment
import com.grand.duke.elliot.jjabkaotalk.cloud_messaging.CloudMessagingHelper
import com.grand.duke.elliot.jjabkaotalk.data.ChatMessage
import com.grand.duke.elliot.jjabkaotalk.data.ChatRoom
import com.grand.duke.elliot.jjabkaotalk.data.ChatRoom.Companion.FIELD_LAST_MESSAGE
import com.grand.duke.elliot.jjabkaotalk.data.ChatRoom.Companion.FIELD_UNREAD_COUNTER
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.databinding.FragmentChatBinding
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import com.grand.duke.elliot.jjabkaotalk.R
import timber.log.Timber

class ChatMessagesFragment: BaseFragment(), ChatMessageAdapter.OnItemClickListener {

    private lateinit var viewModel: ChatMessagesViewModel
    private lateinit var binding: FragmentChatBinding
    private lateinit var chatMessageAdapter: ChatMessageAdapter
    private lateinit var chatRoom: ChatRoom

    private val cloudMessagingHelper = CloudMessagingHelper()
    private val user = MainApplication.user.value ?: throw NullPointerException("ChatFragment: MainApplication.user is null.")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        val chatFragmentArgs by navArgs<ChatMessagesFragmentArgs>()
        chatRoom = chatFragmentArgs.chatRoom  // Initial value.
        binding.toolbar.title = chatRoom.name

        setOnOptionsMenu(binding.toolbar, R.menu.menu_chat, arrayOf (
                R.id.item_exit to {
                    leaveChatRoom(user, chatRoom)
                })
        )

        viewModel = ViewModelProvider(viewModelStore, ChatMessagesViewModelFactory(chatRoom))[ChatMessagesViewModel::class.java]
        chatMessageAdapter = ChatMessageAdapter(chatRoom).apply {
            setOnItemClickListener(this@ChatMessagesFragment)
        }
        binding.recyclerView.apply {
            adapter = chatMessageAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.chatRoom.observe(viewLifecycleOwner, Observer {
            chatRoom = it
            chatMessageAdapter.updateChatRoom(chatRoom)
        })

        viewModel.displayChatMessages.observe(viewLifecycleOwner, Observer { displayChatMessages ->
            val chatMessages = displayChatMessages.chatMessages

            when(displayChatMessages.type) {
                DocumentChange.Type.ADDED -> chatMessageAdapter.addDateAndSubmitList(chatMessages, 0)
                DocumentChange.Type.MODIFIED -> {
                    chatMessageAdapter.addDateAndSubmitList(chatMessages, 0)
                    displayChatMessages.changedChatMessage?.let { chatMessageAdapter.update(it) }
                }
                DocumentChange.Type.REMOVED -> chatMessageAdapter.addDateAndSubmitList(chatMessages, 0)
            }
        })

        binding.buttonSend.setOnClickListener {
            val message = binding.editTextChat.text.toString()

            if (message.isBlank())
                return@setOnClickListener

            sendMessage(message)
            binding.editTextChat.text = null
        }

        return binding.root
    }

    private fun sendMessage(message: String) {
        binding.buttonSend.isEnabled = false
        val chatMessage = createChatMessage(message, System.currentTimeMillis())

        // Update chatRoom.
        if (chatRoom.users.map { it.uid }.contains(user.uid).not())
            viewModel.fireStoreHelper.addUserToChatRoom(chatRoom.id, user) {
                setChatMessage(chatMessage)
            }
        else
            setChatMessage(chatMessage)
    }

    private fun setChatMessage(chatMessage: ChatMessage) {
        viewModel.chatMessageCollectionReference.add(chatMessage)
                .addOnSuccessListener {
                    binding.buttonSend.isEnabled = true
                    val userIds = chatRoom.users.map { it.uid }

                    // Update user.
                    if (user.chatRooms.contains(chatRoom.id).not())
                        viewModel.fireStoreHelper.userArrayUnion(user.uid, User.FIELD_CHAT_ROOMS, chatRoom.id)



                    // Send cloud message.
                    cloudMessagingHelper.sendCloudMessage(chatMessage.message, chatRoom, user)

                    // Update unreadCounter.
                    for (uid in userIds) {
                        if (uid != user.uid) {
                            chatRoom.let {
                                it.unreadCounter[uid] = it.unreadCounter[uid]?.plus(1) ?: 0
                            }
                        }
                    }

                    // Update lastMessage.
                    chatRoom.lastMessage = chatMessage
                    viewModel.openChatRoomDocumentReference
                            .update(mapOf(
                                    FIELD_LAST_MESSAGE to chatRoom.lastMessage,
                                    FIELD_UNREAD_COUNTER to chatRoom.unreadCounter
                            ))
                            .addOnSuccessListener {
                                Timber.d("lastMessage and unreadCounter updated.")
                            }
                            .addOnFailureListener {
                                Timber.w("lastMessage and unreadCounter update failed.")
                            }
                }
                .addOnFailureListener {
                    Timber.e(it)
                    showToast(getString(R.string.failed_to_send_message) + ": ${it.message}")
                    binding.buttonSend.isEnabled = true
                }
    }

    private fun createChatMessage(message: String, time: Long): ChatMessage =
        ChatMessage(
            message = message,
            readerIds = mutableListOf(user.uid),
            senderId = user.uid,
            time = time
        )

    private fun leaveChatRoom(user: User, chatRoom: ChatRoom) {
        var delete = false

        if (chatRoom.users.count() < 2)
            delete = true

        viewModel.fireStoreHelper.leaveChatRoom(user, chatRoom, delete) {
            it?.message?.let { message -> showToast(message) }
            requireActivity().onBackPressed()
        }
    }

    override fun onStop() {
        super.onStop()

        // Update unreadCounter.
        chatRoom.unreadCounter[user.uid] = 0
        viewModel.openChatRoomDocumentReference
            .update(mapOf(FIELD_UNREAD_COUNTER to chatRoom.unreadCounter))
            .addOnSuccessListener {
                Timber.d("unreadCounter updated.")
            }
            .addOnFailureListener {
                Timber.e(it, "Failed to update unreadCounter.")
            }
    }

    override fun onChatMessageLongClick(chatMessage: ChatMessage) {

    }

    override fun onProfileImageClick(sender: User) {
        findNavController().navigate(ChatMessagesFragmentDirections.actionOpenChatFragmentToProfileFragment(sender))
    }
}