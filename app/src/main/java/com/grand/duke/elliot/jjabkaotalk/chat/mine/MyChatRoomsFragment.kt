package com.grand.duke.elliot.jjabkaotalk.chat.mine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.base.BaseFragment
import com.grand.duke.elliot.jjabkaotalk.chat.room.ChatRoomAdapter
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.databinding.FragmentMyChatRoomsBinding
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication

class MyChatRoomsFragment: BaseFragment() {

    private lateinit var viewModel: MyChatRoomsViewModel
    private lateinit var binding: FragmentMyChatRoomsBinding
    private var chatRoomAdapter: ChatRoomAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMyChatRoomsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(viewModelStore, MyChatRoomsViewModelFactory())[MyChatRoomsViewModel::class.java]

        viewModel.exception.observe(viewLifecycleOwner, Observer {
            showToast(getString(R.string.failed_to_load_chat_rooms) + ": ${it.message}")
        })

        viewModel.myChatRooms.observe(viewLifecycleOwner, Observer {
            chatRoomAdapter?.addDateAndSubmitList(it)
        })

        viewModel.updatedPosition.observe(viewLifecycleOwner, Observer {
            if (it != -1)
                chatRoomAdapter?.notifyItemChanged(it)
        })

        MainApplication.user.observe(viewLifecycleOwner, Observer { user ->
            user?.let {
                viewModel.registerMyChatRoomSnapshotListener(it)
            } ?: run {
                viewModel.unregisterMyChatRoomSnapshotListener()
            }

            updateUi(user)
        })

        return binding.root
    }

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