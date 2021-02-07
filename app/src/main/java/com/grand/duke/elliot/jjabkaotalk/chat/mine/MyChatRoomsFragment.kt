package com.grand.duke.elliot.jjabkaotalk.chat.mine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.base.BaseFragment
import com.grand.duke.elliot.jjabkaotalk.chat.room.ChatRoomAdapter
import com.grand.duke.elliot.jjabkaotalk.data.ChatRoom
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.databinding.FragmentMyChatRoomsBinding
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import com.grand.duke.elliot.jjabkaotalk.main.TabFragmentDirections
import com.grand.duke.elliot.jjabkaotalk.util.isNull

class MyChatRoomsFragment: BaseFragment() {

    private lateinit var viewModel: MyChatRoomsViewModel
    private lateinit var binding: FragmentMyChatRoomsBinding
    private var chatRoomAdapter: ChatRoomAdapter? = null
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMyChatRoomsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(viewModelStore, MyChatRoomsViewModelFactory())[MyChatRoomsViewModel::class.java]

        viewModel.exception.observe(viewLifecycleOwner, Observer {
            showToast(getString(R.string.failed_to_load_chat_rooms) + ": ${it.message}")
        })

        MainApplication.user.observe(viewLifecycleOwner, Observer { user ->
            showToast(user?.name.toString())
            user?.let {
                if (listenerRegistration.isNull()) {
                    listenerRegistration = viewModel.registerMyChatRoomSnapshotListener(user)
                    chatRoomAdapter = ChatRoomAdapter(ChatRoomAdapter.Type.private)
                    chatRoomAdapter?.setOnItemClickListener(object :
                            ChatRoomAdapter.OnItemClickListener {
                        override fun onClick(chatRoom: ChatRoom) {
                            findNavController().navigate(
                                    TabFragmentDirections.actionTabFragmentToOpenChatFragment(
                                            chatRoom
                                    )
                            )
                        }
                    })

                    binding.recyclerView.apply {
                        adapter = chatRoomAdapter
                        layoutManager = LinearLayoutManager(requireContext())
                    }
                }
            } ?: run {
               clear()
            }
            //updateUi(user)
        })

        viewModel.displayChatRooms.observe(viewLifecycleOwner, Observer { chatRooms ->
            chatRooms?.let {
                chatRoomAdapter?.addDateAndSubmitList(it.chatRoomList)
                it.modifiedChatRoom?.let { chatRoom ->
                    chatRoomAdapter?.update(chatRoom)
                }
            }
        })

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    private fun clear() {
        chatRoomAdapter = null
        binding.recyclerView.adapter = chatRoomAdapter
        viewModel.clear()
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    /*
    private fun updateUi(user: User?) {
        user?.let {
            chatRoomAdapter = ChatRoomAdapter()
            binding.recyclerView.apply {
                adapter = chatRoomAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        } ?: run {
            chatRoomAdapter = null
            chatRoomAdapter = ChatRoomAdapter()
            binding.recyclerView.adapter = chatRoomAdapter
        }
    }

     */


    companion object {
        @Volatile
        private var INSTANCE: MyChatRoomsFragment? = null

        fun getInstance(): MyChatRoomsFragment {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = MyChatRoomsFragment()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}