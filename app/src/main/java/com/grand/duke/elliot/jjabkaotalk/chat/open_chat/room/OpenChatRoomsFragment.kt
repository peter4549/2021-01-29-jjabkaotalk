package com.grand.duke.elliot.jjabkaotalk.chat.open_chat.room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.base.BaseFragment
import com.grand.duke.elliot.jjabkaotalk.data.OpenChatRoom
import com.grand.duke.elliot.jjabkaotalk.databinding.FragmentOpenChatRoomsBinding
import com.grand.duke.elliot.jjabkaotalk.firebase.FireStoreHelper
import com.grand.duke.elliot.jjabkaotalk.main.TabFragmentDirections
import timber.log.Timber

class OpenChatRoomsFragment private constructor(): BaseFragment(), FireStoreHelper.OnOpenChatRoomSnapshotListener {

    private lateinit var viewModel: OpenChatRoomsViewModel
    private lateinit var binding: FragmentOpenChatRoomsBinding
    private lateinit var openChatRoomAdapter: OpenChatRoomAdapter
    private lateinit var listenerRegistration: ListenerRegistration
    private val fireStoreHelper = FireStoreHelper()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(viewModelStore, OpenChatRoomsViewModelFactory())[OpenChatRoomsViewModel::class.java]
        binding = FragmentOpenChatRoomsBinding.inflate(inflater, container, false)

        setOnOptionsMenu(
                binding.toolbar,
                R.menu.menu_open_chat_rooms,
                arrayOf(
                        R.id.item_create_open_chat_room to {
                            showToast("what?")
                            OpenChatRoomCreationDialogFragment().show(requireActivity().supportFragmentManager, null)
                        }
                )
        )

        openChatRoomAdapter = OpenChatRoomAdapter()
        openChatRoomAdapter.addDateAndSubmitList(viewModel.openChatRooms)
        openChatRoomAdapter.setOnItemClickListener(object:
            OpenChatRoomAdapter.OnItemClickListener {
            override fun onClick(openChatRoom: OpenChatRoom) {
                findNavController().navigate(TabFragmentDirections.actionTabFragmentToOpenChatFragment(openChatRoom))
            }
        })

        fireStoreHelper.setOnOpenChatRoomSnapshotListener(this)

        binding.recyclerView.apply {
            adapter = openChatRoomAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        listenerRegistration = fireStoreHelper.registerOpenChatRoomSnapshotListener("busan") // todo. test city.
    }

    override fun onStop() {
        super.onStop()
        if (this::listenerRegistration.isInitialized)
            listenerRegistration.remove()
    }

    private fun update(openChatRoom: OpenChatRoom) {
        val item = viewModel.openChatRooms.find { it.id == openChatRoom.id }
        val index = viewModel.openChatRooms.indexOf(item)

        if (index == -1)
            return

        viewModel.openChatRooms[index] = openChatRoom
        openChatRoomAdapter.notifyItemChanged(index)
    }

    private fun remove(openChatRoom: OpenChatRoom) {
        val item = viewModel.openChatRooms.find { it.id == openChatRoom.id }
        val index = viewModel.openChatRooms.indexOf(item)

        if (index == -1)
            return

        viewModel.openChatRooms.removeAt(index)
    }

    /** FireStoreHelper.OnOpenChatRoomSnapshotListener */
    override fun onOpenChatRoomDocumentSnapshot(documentChanges: List<DocumentChange>) {
        showToast(documentChanges.map { fireStoreHelper.convertToOpenChatRoom(it.document.data).name }.toString())
        documentChanges.forEach { documentChange ->
            val openChatRoom = fireStoreHelper.convertToOpenChatRoom(documentChange.document.data)

            fireStoreHelper.getUsers(openChatRoom.users.map { it.uid }) { users ->
                openChatRoom.users = users.toMutableList()  // Update users.

                when(documentChange.type) {
                    DocumentChange.Type.ADDED -> {
                        viewModel.openChatRooms.add(openChatRoom)
                        openChatRoomAdapter.addDateAndSubmitList(viewModel.openChatRooms)
                    }
                    DocumentChange.Type.MODIFIED -> update(openChatRoom)
                    DocumentChange.Type.REMOVED -> {
                        remove(openChatRoom)
                        openChatRoomAdapter.addDateAndSubmitList(viewModel.openChatRooms)
                    }
                }
            }
        }
    }

    override fun onException(exception: Exception) {
        Timber.e(exception)
        showToast(getString(R.string.failed_to_load_open_chat_rooms) + ": ${exception.message}")
    }

    companion object {
        @Volatile
        private var INSTANCE: OpenChatRoomsFragment? = null

        fun getInstance(): OpenChatRoomsFragment {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = OpenChatRoomsFragment()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}