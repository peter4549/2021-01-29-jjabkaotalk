package com.grand.duke.elliot.jjabkaotalk.open_chat.rooms

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.data.OpenChatRoom
import com.grand.duke.elliot.jjabkaotalk.databinding.ItemChatRoomBinding
import com.grand.duke.elliot.jjabkaotalk.databinding.ItemDateBinding
import com.grand.duke.elliot.jjabkaotalk.util.blank
import com.grand.duke.elliot.jjabkaotalk.util.toDateFormat

class OpenChatRoomAdapter: ListAdapter<AdapterItem, OpenChatRoomAdapter.ViewHolder>(AdapterItemDiffCallback()) {

    private lateinit var recyclerView: RecyclerView

    inner class ViewHolder constructor(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(adapterItem: AdapterItem) {
            when(adapterItem) {
                is AdapterItem.DateItem -> {}
                is AdapterItem.OpenChatRoomItem -> {
                    binding as ItemChatRoomBinding
                    val users = adapterItem.openChatRoom.users
                    val userNames = users.joinToString(", ") { it.name }

                    // todo. change to name..
                    binding.textUserNames.text = adapterItem.openChatRoom.name
                    // binding.textUserNames.text = userNames
                    binding.textUserCount.text = users.count().toString()
                }
            }
        }
    }

    private fun from(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = when(viewType) {
            VIEW_TYPE_DATE_ITEM -> ItemDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            VIEW_TYPE_OPEN_CHAT_ROOM_ITEM -> ItemChatRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            else -> throw IllegalArgumentException("Invalid viewType.")
        }

        return ViewHolder(binding)
    }

    fun addDateAndSubmitList(list: List<OpenChatRoom>) {
        if (list.isNullOrEmpty())
            return

        val items = arrayListOf<AdapterItem>()
        var yearMonth = blank

        for ((index, item) in list.withIndex()) {
            item.time.toDateFormat(
                    recyclerView.context.getString(R.string.year_month_format)
            ).let {
                if (it != yearMonth) {
                    yearMonth = it
                    items.add(AdapterItem.DateItem(index.toString(), yearMonth))
                }
            }

            items.add(AdapterItem.OpenChatRoomItem(item.id, item))
        }

        recyclerView.scheduleLayoutAnimation()
        submitList(items)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AdapterItem.DateItem -> VIEW_TYPE_DATE_ITEM
            is AdapterItem.OpenChatRoomItem -> VIEW_TYPE_OPEN_CHAT_ROOM_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return from(parent, viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private const val VIEW_TYPE_DATE_ITEM = 0
        private const val VIEW_TYPE_OPEN_CHAT_ROOM_ITEM = 1
    }
}

class AdapterItemDiffCallback: DiffUtil.ItemCallback<AdapterItem>() {
    override fun areItemsTheSame(oldItem: AdapterItem, newItem: AdapterItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: AdapterItem, newItem: AdapterItem): Boolean {
        return oldItem == newItem
    }
}

sealed class AdapterItem {
    data class DateItem(
            override val id: String,
            val yearMonth: String
    ): AdapterItem()

    data class OpenChatRoomItem(
            override val id: String,
            val openChatRoom: OpenChatRoom
    ): AdapterItem()

    abstract val id: String
}