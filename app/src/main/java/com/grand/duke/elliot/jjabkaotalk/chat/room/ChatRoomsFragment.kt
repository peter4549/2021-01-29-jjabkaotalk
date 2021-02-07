package com.grand.duke.elliot.jjabkaotalk.chat.room

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
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
import com.grand.duke.elliot.jjabkaotalk.profile.ProfileCreationActivity
import com.grand.duke.elliot.jjabkaotalk.settings.SettingsActivity
import com.grand.duke.elliot.jjabkaotalk.util.blank
import com.grand.duke.elliot.jjabkaotalk.util.isNull
import com.grand.duke.elliot.jjabkaotalk.util.view.SimpleItem
import com.grand.duke.elliot.jjabkaotalk.util.view.SimpleListDialogFragment

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

        binding.toolbar.title = blank
        initLocationSelector()

        setOnOptionsMenu(
                binding.toolbar,
                R.menu.menu_open_chat_rooms,
                arrayOf(
                        R.id.item_create_open_chat_room to {
                            ChatRoomCreationDialogFragment().show(requireActivity().supportFragmentManager, null)
                        },
                        R.id.item_settings to {
                            startSettingsActivity()
                        }
                )
        )

        /** User. */
        MainApplication.user.observe(viewLifecycleOwner, Observer { user ->
            user?.let {
                if (listenerRegistration.isNull()) {
                    listenerRegistration = viewModel.registerChatRoomSnapshotListener(user)
                    chatRoomAdapter = ChatRoomAdapter(ChatRoomAdapter.Type.public)
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

        /** Location. */
        MainApplication.location.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.textLocation.text = it
            }
        })

        /** Open Chat Rooms. */
        viewModel.displayChatRooms.observe(viewLifecycleOwner, Observer { chatRooms ->
            chatRooms?.let {
                chatRoomAdapter?.addDateAndSubmitList(it.chatRoomList)
                it.modifiedChatRoom?.let { chatRoom ->
                    chatRoomAdapter?.update(chatRoom)
                }
            }
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

    private fun startSettingsActivity() {
        val intent = Intent(requireActivity(), SettingsActivity::class.java)
        startActivity(intent)
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

    private fun initLocationSelector() {
        binding.textLocation.setOnClickListener {
            val simpleListDialog = SimpleListDialogFragment()
            val counties = resources.getStringArray(R.array.counties)
            val simpleItems = counties.map { SimpleItem(it, it) } as ArrayList
            simpleListDialog.setItems(simpleItems)
            simpleListDialog.show(requireActivity().supportFragmentManager, null)
        }
    }

    private fun clear() {
        chatRoomAdapter = null
        binding.recyclerView.adapter = chatRoomAdapter
        viewModel.clear()
        listenerRegistration?.remove()
        listenerRegistration = null
    }

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