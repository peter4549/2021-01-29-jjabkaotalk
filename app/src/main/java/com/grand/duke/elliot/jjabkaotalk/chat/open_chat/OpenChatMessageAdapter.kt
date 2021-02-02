package com.grand.duke.elliot.jjabkaotalk.chat.open_chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.data.ChatMessage
import com.grand.duke.elliot.jjabkaotalk.data.OpenChatRoom
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.databinding.ItemChatLeftBinding
import com.grand.duke.elliot.jjabkaotalk.databinding.ItemChatRightBinding
import com.grand.duke.elliot.jjabkaotalk.databinding.ItemDateBinding
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import com.grand.duke.elliot.jjabkaotalk.util.blank
import com.grand.duke.elliot.jjabkaotalk.util.toDateFormat
import com.grand.duke.elliot.jjabkaotalk.util.toLocalTimeString

class OpenChatMessageAdapter(private val openChatRoom: OpenChatRoom): ListAdapter<AdapterItem, OpenChatMessageAdapter.ViewHolder>(
    AdapterItemDiffCallback()
) {
    private val user = MainApplication.user ?: throw NullPointerException("ChatMessageAdapter: MainApplication.user is null.")
    private val uidToUser = mutableMapOf<String, User>()
    private var recyclerView: RecyclerView? = null

    init {
        openChatRoom.users.forEach {
            uidToUser[it.uid] = it
        }
    }

    inner class ViewHolder(private val binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(adapterItem: AdapterItem) {
            when (adapterItem) {
                is AdapterItem.ChatMessageItem -> {
                    if (adapterItem.chatMessage.senderId == user.uid) {
                        binding as ItemChatRightBinding
                        binding.textMessage.text = adapterItem.chatMessage.message
                    } else {
                        binding as ItemChatLeftBinding
                        val chatMessage = adapterItem.chatMessage
                        val unreadCount = openChatRoom.users.count() - chatMessage.readerIds.count()
                        binding.textMessage.text = chatMessage.message
                        println("LLLLLLLLL: ${uidToUser.values}")
                        binding.textName.text = uidToUser[chatMessage.senderId]?.name
                                ?: binding.root.context.getString(R.string.unknown) // todo change to res..
                        binding.textUnreadCount.text = unreadCount.toString()
                        binding.textTime.text = chatMessage.time.toLocalTimeString()
                    }
                }
                is AdapterItem.DateItem -> {
                    binding as ItemDateBinding
                    binding.textDate.text = adapterItem.yearMonth
                }
            }
        }
    }

    private fun from(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = when(viewType) {
            VIEW_TYPE_CHAT_MESSAGE -> ItemChatLeftBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            VIEW_TYPE_DATE -> ItemDateBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            VIEW_TYPE_MY_CHAT_MESSAGE -> ItemChatRightBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            else -> throw IllegalArgumentException("Invalid viewType.")
        }

        return ViewHolder(binding)
    }

    fun addDateAndSubmitList(list: List<ChatMessage>, scrollTo: Int? = null) {
        val context = recyclerView?.context ?: return

        if (list.isNullOrEmpty())
            return

        val items = arrayListOf<AdapterItem>()
        var yearMonth = blank

        for ((index, item) in list.withIndex()) {
            item.time.toDateFormat(
                context.getString(R.string.year_month_format)
            ).let {
                if (it != yearMonth) {
                    yearMonth = it
                    items.add(AdapterItem.DateItem(index.toLong(), yearMonth))
                }
            }

            items.add(AdapterItem.ChatMessageItem(index.toLong(), item))
        }

        recyclerView?.scheduleLayoutAnimation()
        submitList(items)
        scrollTo?.let { recyclerView?.smoothScrollToPosition(it) }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is AdapterItem.DateItem -> VIEW_TYPE_DATE
            is AdapterItem.ChatMessageItem -> {
                if (item.chatMessage.senderId == user.uid)
                    VIEW_TYPE_MY_CHAT_MESSAGE
                else
                    VIEW_TYPE_CHAT_MESSAGE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return from(parent, viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private const val VIEW_TYPE_CHAT_MESSAGE = 0
        private const val VIEW_TYPE_DATE = 1
        private const val VIEW_TYPE_MY_CHAT_MESSAGE = 2
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
        override val id: Long,
        val yearMonth: String
    ): AdapterItem()

    data class ChatMessageItem(
        override val id: Long,
        val chatMessage: ChatMessage
    ): AdapterItem()

    abstract val id: Long
}