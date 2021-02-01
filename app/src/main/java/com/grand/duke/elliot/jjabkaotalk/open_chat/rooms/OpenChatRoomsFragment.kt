package com.grand.duke.elliot.jjabkaotalk.open_chat.rooms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentChange
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.base.BaseFragment
import com.grand.duke.elliot.jjabkaotalk.data.OpenChatRoom
import com.grand.duke.elliot.jjabkaotalk.databinding.FragmentOpenChatRoomsBinding
import com.grand.duke.elliot.jjabkaotalk.firebase.FireStoreHelper
import timber.log.Timber

class OpenChatRoomsFragment private constructor(): BaseFragment(), FireStoreHelper.OnOpenChatRoomSnapshotListener {

    private lateinit var viewModel: OpenChatRoomsViewModel
    private lateinit var binding: FragmentOpenChatRoomsBinding
    private val fireStoreHelper = FireStoreHelper()
    private val openChatRoomsAdapter = OpenChatRoomAdapter()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(viewModelStore, OpenChatViewRoomsModelFactory())[OpenChatRoomsViewModel::class.java]
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

        openChatRoomsAdapter.addDateAndSubmitList(viewModel.openChatRooms)

        fireStoreHelper.setupOpenChatRoomSnapshotListener("busan") // todo. test city.
        fireStoreHelper.setOnOpenChatRoomSnapshotListener(this)

        binding.recyclerView.apply {
            adapter = openChatRoomsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        return binding.root
    }

    /** FireStoreHelper.OnOpenChatRoomSnapshotListener */
    override fun onOpenChatRoomDocumentSnapshot(documentChanges: List<DocumentChange>) {
        showToast(documentChanges.map { fireStoreHelper.convertToOpenChatRoom(it.document.data).name }.toString())
        documentChanges.forEach { documentChange ->
            val openChatRoom = fireStoreHelper.convertToOpenChatRoom(documentChange.document.data)

            when(documentChange.type) {
                DocumentChange.Type.ADDED -> {
                    viewModel.openChatRooms.add(openChatRoom)
                    openChatRoomsAdapter.addDateAndSubmitList(viewModel.openChatRooms)
                }
                DocumentChange.Type.MODIFIED -> update(openChatRoom)
                DocumentChange.Type.REMOVED -> {
                    remove(openChatRoom)
                    openChatRoomsAdapter.addDateAndSubmitList(viewModel.openChatRooms)
                }
            }
        }
    }

    private fun update(openChatRoom: OpenChatRoom) {
        val item = viewModel.openChatRooms.find { it.id == openChatRoom.id }
        val index = viewModel.openChatRooms.indexOf(item)

        if (index == -1)
            return

        viewModel.openChatRooms[index] = openChatRoom
        openChatRoomsAdapter.notifyItemChanged(index)
    }

    private fun remove(openChatRoom: OpenChatRoom) {
        val item = viewModel.openChatRooms.find { it.id == openChatRoom.id }
        val index = viewModel.openChatRooms.indexOf(item)

        if (index == -1)
            return

        viewModel.openChatRooms.removeAt(index)
    }

    override fun onException(exception: Exception) {
        Timber.e(exception)
        showToast(getString(R.string.failed_to_load_open_chat_rooms))
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