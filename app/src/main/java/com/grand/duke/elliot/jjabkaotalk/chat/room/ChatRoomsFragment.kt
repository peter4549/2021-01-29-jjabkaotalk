package com.grand.duke.elliot.jjabkaotalk.chat.room

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
import com.grand.duke.elliot.jjabkaotalk.data.ChatRoom
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.databinding.FragmentOpenChatRoomsBinding
import com.grand.duke.elliot.jjabkaotalk.main.MainActivity
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import com.grand.duke.elliot.jjabkaotalk.main.TabFragmentDirections

class ChatRoomsFragment: BaseFragment() {

    private lateinit var viewModel: ChatRoomsViewModel
    private lateinit var binding: FragmentOpenChatRoomsBinding
    private var chatRoomAdapter: ChatRoomAdapter? = null

    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(viewModelStore, ChatRoomsViewModelFactory())[ChatRoomsViewModel::class.java]
        binding = FragmentOpenChatRoomsBinding.inflate(inflater, container, false)

        setOnOptionsMenu(
                binding.toolbar,
                R.menu.menu_open_chat_rooms,
                arrayOf(
                        R.id.item_create_open_chat_room to {
                            ChatRoomCreationDialogFragment().show(requireActivity().supportFragmentManager, null)
                        }
                )
        )

        MainApplication.user.observe(viewLifecycleOwner, Observer { user ->
            user?.let {
                if (listenerRegistration.isNull()) {
                    listenerRegistration = viewModel.registerChatRoomSnapshotListener()
                    chatRoomAdapter = ChatRoomAdapter()
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
            updateUi(user)
        })

        viewModel.chatRooms.observe(viewLifecycleOwner, Observer { chatRooms ->
            chatRooms?.let { chatRoomAdapter?.addDateAndSubmitList(it) }
        })

        viewModel.updatedPosition.observe(viewLifecycleOwner, Observer {
            if (it == -1)
                return@Observer
            showToast(it.toString())
            chatRoomAdapter?.notifyItemChanged(it)
        })

        viewModel.exception.observe(viewLifecycleOwner, Observer {
            showToast(getString(R.string.open_chat_room_not_found) + ": ${it.message}")
        })

        binding.buttonSignIn.setOnClickListener {
            if (requireActivity() is MainActivity)
                (requireActivity() as MainActivity).startSignInActivity()
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    private fun updateUi(user: User?) {
        user?.let {
            binding.recyclerView.visibility = View.VISIBLE
            binding.linearLayoutRequestSignIn.visibility = View.GONE
        } ?: run {
            binding.recyclerView.visibility = View.GONE
            binding.linearLayoutRequestSignIn.visibility = View.VISIBLE
        }
    }

    private fun clear() {
        chatRoomAdapter = null
        binding.recyclerView.adapter = chatRoomAdapter
        viewModel.clear()
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    private fun ListenerRegistration?.isNull() = this == null

    companion object {
        @Volatile
        private var INSTANCE: ChatRoomsFragment? = null

        fun getInstance(): ChatRoomsFragment {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = ChatRoomsFragment()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}