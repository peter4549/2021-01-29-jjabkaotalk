package com.grand.duke.elliot.jjabkaotalk.chat.room

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.data.ChatRoom
import com.grand.duke.elliot.jjabkaotalk.databinding.ItemChatRoomBinding
import com.grand.duke.elliot.jjabkaotalk.databinding.ItemDateBinding
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import com.grand.duke.elliot.jjabkaotalk.util.blank
import com.grand.duke.elliot.jjabkaotalk.util.toDateFormat
import com.grand.duke.elliot.jjabkaotalk.util.toLocalTimeString

class ChatRoomAdapter: ListAdapter<AdapterItem, ChatRoomAdapter.ViewHolder>(
    AdapterItemDiffCallback()
) {
    private lateinit var recyclerView: RecyclerView
    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onClick(chatRoom: ChatRoom)
    }

    inner class ViewHolder constructor(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(adapterItem: AdapterItem) {
            when(adapterItem) {
                is AdapterItem.DateItem -> {}
                is AdapterItem.OpenChatRoomItem -> {
                    binding as ItemChatRoomBinding
                    val chatRoom = adapterItem.chatRoom
                    val users = chatRoom.users
                    val userNames = users.joinToString(", ") { it.name }

                    // todo. change to name..

                    if (chatRoom.type == ChatRoom.TYPE_PRIVATE)
                        binding.textUserNames.text = userNames
                    else
                        binding.textUserNames.text = chatRoom.name

                    binding.textLastMessage.text = chatRoom.lastMessage.message
                    binding.textUserCount.text = users.count().toString()

                    binding.textTime.text = chatRoom.lastMessage.time.toLocalTimeString()
                    binding.textUnreadCount.text = chatRoom.unreadCounter[MainApplication.user.value?.uid].toString()

                    binding.root.setOnClickListener {
                        onItemClickListener?.onClick(adapterItem.chatRoom)
                    }
                }
            }
        }

        fun setProfilePhotos(chatRoom: ChatRoom) {
            binding as ItemChatRoomBinding

            if (chatRoom.users.count() == 1) {
                binding.imageProfilePhotos.visibility = View.VISIBLE
                binding.constraintLayout2profilePhotos.visibility = View.GONE
                binding.constraintLayout3profilePhotos.visibility = View.GONE
                binding.constraintLayout4profilePhotos.visibility = View.GONE
                chatRoom.users[0].profilePhotoUris.let {
                    if (it.isNotEmpty())
                        setImage(binding.imageProfilePhotos, it[0])
                }
            } else if (chatRoom.users.count() == 2) {
                binding.imageProfilePhotos.visibility = View.GONE
                binding.constraintLayout2profilePhotos.visibility = View.VISIBLE
                binding.constraintLayout3profilePhotos.visibility = View.GONE
                binding.constraintLayout4profilePhotos.visibility = View.GONE
            } else if (chatRoom.users.count() == 3) {
                binding.imageProfilePhotos.visibility = View.GONE
                binding.constraintLayout2profilePhotos.visibility = View.GONE
                binding.constraintLayout3profilePhotos.visibility = View.VISIBLE
                binding.constraintLayout4profilePhotos.visibility = View.GONE
            } else if (chatRoom.users.count() >= 4) {
                binding.imageProfilePhotos.visibility = View.GONE
                binding.constraintLayout2profilePhotos.visibility = View.GONE
                binding.constraintLayout3profilePhotos.visibility = View.GONE
                binding.constraintLayout4profilePhotos.visibility = View.VISIBLE
            }
        }

        fun setImage(imageView: ImageView, uri: String) {
            Glide.with(imageView.context)
                .load(uri)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_round_account_circle_96)
                .transition(DrawableTransitionOptions.withCrossFade())
                .transform(CircleCrop())
                .into(imageView)
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

    fun addDateAndSubmitList(list: List<ChatRoom>) {
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

            // TODO. load user data by userIds.
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
            val chatRoom: ChatRoom
    ): AdapterItem()

    abstract val id: String
}