package com.grand.duke.elliot.jjabkaotalk.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.data.ChatMessage
import com.grand.duke.elliot.jjabkaotalk.data.ChatRoom
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.databinding.ItemChatLeftBinding
import com.grand.duke.elliot.jjabkaotalk.databinding.ItemChatRightBinding
import com.grand.duke.elliot.jjabkaotalk.databinding.ItemDateBinding
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import com.grand.duke.elliot.jjabkaotalk.util.blank
import com.grand.duke.elliot.jjabkaotalk.util.toDateFormat
import com.grand.duke.elliot.jjabkaotalk.util.toLocalTimeString

class ChatMessageAdapter(private val chatRoom: ChatRoom):
        ListAdapter<AdapterItem, ChatMessageAdapter.ViewHolder>(
            AdapterItemDiffCallback()
        ) {
    private val user = MainApplication.user.value ?: throw NullPointerException("ChatMessageAdapter: MainApplication.user is null.")
    private val uidToUser = mutableMapOf<String, User>()
    private var recyclerView: RecyclerView? = null

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onChatMessageLongClick(chatMessage: ChatMessage)
        fun onProfileImageClick(sender: User)
    }

    init {
        chatRoom.users.forEach {
            uidToUser[it.uid] = it
        }
    }

    inner class ViewHolder(private val binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(adapterItem: AdapterItem) {
            when (adapterItem) {
                is AdapterItem.ChatMessageItem -> {
                    val chatMessage = adapterItem.chatMessage
                    val unreadCount = chatRoom.users.count() - chatMessage.readerIds.count()

                    if (adapterItem.chatMessage.senderId == user.uid) {
                        // My chat message.
                        binding as ItemChatRightBinding
                        binding.textMessage.text = adapterItem.chatMessage.message

                        println("EEEEEE: $chatMessage $unreadCount")
                        if (unreadCount > 0) {
                            binding.textUnreadCount.visibility = View.VISIBLE
                            binding.textUnreadCount.text = unreadCount.toString()
                        } else
                            binding.textUnreadCount.visibility = View.GONE

                        /*
                        Toast.makeText(
                            binding.root.context,
                            "ap: ${adapterPosition} mine: " + chatMessage.readerIds + " ${chatMessage.message}",
                            Toast.LENGTH_SHORT
                        ).show()

                         */

                        binding.textTime.text = chatMessage.time.toLocalTimeString()
                    } else {
                        binding as ItemChatLeftBinding
                        binding.textMessage.text = chatMessage.message
                        binding.textName.text = uidToUser[chatMessage.senderId]?.name
                            ?: binding.root.context.getString(R.string.unknown)

                        if (unreadCount > 0) {
                            binding.textUnreadCount.visibility = View.VISIBLE
                            binding.textUnreadCount.text = unreadCount.toString()
                        } else
                            binding.textUnreadCount.visibility = View.GONE

                        binding.textTime.text = chatMessage.time.toLocalTimeString()
                        /*
                        Toast.makeText(
                            binding.root.context,
                            "ap: ${adapterPosition} others: " + chatMessage.readerIds + " ${chatMessage.message}",
                            Toast.LENGTH_SHORT
                        ).show()

                         */

                        binding.imageProfile.setOnClickListener {
                            uidToUser[chatMessage.senderId]?.let { sender ->
                                onItemClickListener?.onProfileImageClick(sender)
                            }
                        }

                        binding.textMessage.setOnLongClickListener {
                            onItemClickListener?.onChatMessageLongClick(chatMessage)
                            true
                        }
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

    fun update(chatMessage: ChatMessage) {
        val item = currentList.find {
            it is AdapterItem.ChatMessageItem
                    && it.chatMessage.senderId == chatMessage.senderId
                    && it.chatMessage.time == chatMessage.time
        } ?: return

        println("IIIIIIII: $item")

        val position = getPosition(item.id)
        notifyItemChanged(position)
    }

    private fun getPosition(id: Long): Int {
        for ((index, item) in currentList.withIndex())
            if (item.id == id)
                return index

        return 0
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
        // scrollTo?.let { recyclerView?.smoothScrollToPosition(it) }
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
        var chatMessage: ChatMessage
    ): AdapterItem()

    abstract val id: Long
}