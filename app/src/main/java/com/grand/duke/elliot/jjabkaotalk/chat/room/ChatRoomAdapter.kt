package com.grand.duke.elliot.jjabkaotalk.chat.room

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.ImageView
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
import java.util.*

class ChatRoomAdapter(private val type: Int): ListAdapter<AdapterItem, ChatRoomAdapter.ViewHolder>(
    AdapterItemDiffCallback()
) {
    private lateinit var recyclerView: RecyclerView
    private var onItemClickListener: OnItemClickListener? = null
    private var searchWord = blank
    private var chatRoomList = arrayListOf<ChatRoom>()

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onClick(chatRoom: ChatRoom)
    }

    object Type {
        const val private = 0
        const val public = 1
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

                    // Profile Photos.
                    setProfilePhotos(chatRoom)

                    if (chatRoom.type == ChatRoom.TYPE_PRIVATE)
                        binding.textUserNames.text = userNames
                    else
                        binding.textUserNames.text = chatRoom.name

                    binding.textLastMessage.text = chatRoom.lastMessage.message

                    binding.textUserCount.text = users.count().toString()

                    binding.textTime.text = chatRoom.lastMessage.time.toLocalTimeString()

                    val unreadCount = chatRoom.unreadCounter[MainApplication.user.value?.uid]
                    unreadCount?.let {
                        if (it > 0) {
                            binding.textUnreadCount.visibility = View.VISIBLE
                            binding.textUnreadCount.text = it.toString()
                        } else
                            binding.textUnreadCount.visibility = View.GONE
                    } ?: run {
                        binding as ItemChatRoomBinding
                        binding.textUnreadCount.visibility = View.GONE
                    }

                    binding.root.setOnClickListener {
                        onItemClickListener?.onClick(adapterItem.chatRoom)
                    }
                }
            }
        }

        private fun setProfilePhotos(chatRoom: ChatRoom) {
            binding as ItemChatRoomBinding
            val users =  chatRoom.users

            when {
                users.count() == 1 -> {
                    binding.imageProfilePhotos.visibility = View.VISIBLE
                    binding.constraintLayout2profilePhotos.visibility = View.GONE
                    binding.constraintLayout3profilePhotos.visibility = View.GONE
                    binding.constraintLayout4profilePhotos.visibility = View.GONE
                    users[0].profilePhotoUris.let {
                        if (it.isNotEmpty())
                            setImage(binding.imageProfilePhotos, it[0])
                    }
                }
                users.count() == 2 -> {
                    binding.imageProfilePhotos.visibility = View.GONE
                    binding.constraintLayout2profilePhotos.visibility = View.VISIBLE
                    binding.constraintLayout3profilePhotos.visibility = View.GONE
                    binding.constraintLayout4profilePhotos.visibility = View.GONE

                    val user0 = users[0]
                    val user1 = users[1]

                    if (user0.profilePhotoUris.isNotEmpty())
                        setImage(binding.image2profilePhotos1, user0.profilePhotoUris[0])

                    if (user1.profilePhotoUris.isNotEmpty())
                        setImage(binding.image2profilePhotos2, user1.profilePhotoUris[0])
                }
                users.count() == 3 -> {
                    binding.imageProfilePhotos.visibility = View.GONE
                    binding.constraintLayout2profilePhotos.visibility = View.GONE
                    binding.constraintLayout3profilePhotos.visibility = View.VISIBLE
                    binding.constraintLayout4profilePhotos.visibility = View.GONE

                    val user0 = users[0]
                    val user1 = users[1]
                    val user2 = users[1]

                    if (user0.profilePhotoUris.isNotEmpty())
                        setImage(binding.image3profilePhotos1, user0.profilePhotoUris[0])

                    if (user1.profilePhotoUris.isNotEmpty())
                        setImage(binding.image3profilePhotos2, user1.profilePhotoUris[0])

                    if (user2.profilePhotoUris.isNotEmpty())
                        setImage(binding.image3profilePhotos3, user2.profilePhotoUris[0])
                }
                users.count() >= 4 -> {
                    binding.imageProfilePhotos.visibility = View.GONE
                    binding.constraintLayout2profilePhotos.visibility = View.GONE
                    binding.constraintLayout3profilePhotos.visibility = View.GONE
                    binding.constraintLayout4profilePhotos.visibility = View.VISIBLE

                    val user0 = users[0]
                    val user1 = users[1]
                    val user2 = users[2]
                    val user3 = users[3]

                    if (user0.profilePhotoUris.isNotEmpty())
                        setImage(binding.image4profilePhotos1, user0.profilePhotoUris[0])

                    if (user1.profilePhotoUris.isNotEmpty())
                        setImage(binding.image4profilePhotos2, user1.profilePhotoUris[0])

                    if (user2.profilePhotoUris.isNotEmpty())
                        setImage(binding.image4profilePhotos3, user2.profilePhotoUris[0])

                    if (user3.profilePhotoUris.isNotEmpty())
                        setImage(binding.image4profilePhotos4, user3.profilePhotoUris[0])
                }
            }
        }

        private fun setImage(imageView: ImageView, uri: String) {
            Glide.with(imageView.context)
                .load(uri)
                .centerCrop()
                .placeholder(R.drawable.ic_round_account_circle_96)
                .transition(DrawableTransitionOptions.withCrossFade())
                .transform(CircleCrop())
                .into(imageView)
        }
    }

    private fun filtering(item: ChatRoom): ChatRoom? {
        if (item.name.contains(searchWord))
            return item

        return null
    }

    fun filter(): Filter {
        var itemsFiltered: MutableList<ChatRoom>

        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                searchWord = charSequence.toString()
                itemsFiltered =
                        if (searchWord.isBlank())
                            chatRoomList
                        else {
                            val itemsFiltering = mutableListOf<ChatRoom>()

                            for (item in chatRoomList)
                                filtering(item)?.let { itemsFiltering.add(it) }

                            itemsFiltering
                        }

                return FilterResults().apply {
                    values = itemsFiltered
                }
            }

            override fun publishResults(charSequence: CharSequence?, results: FilterResults?) {
                @Suppress("UNCHECKED_CAST")
                results?.values?.let { values ->
                    submitListInFilter(values as List<ChatRoom>)
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

    fun addDateAndSubmitList(list: List<ChatRoom>) {
        sort(list)

        val items = arrayListOf<AdapterItem>()
        var yearMonth = blank

        for ((index, item) in list.withIndex()) {
            item.time.toDateFormat(
                    recyclerView.context.getString(R.string.year_month_date_format)
            ).let {
                if (it != yearMonth) {
                    yearMonth = it
                    items.add(AdapterItem.DateItem(index.toString(), yearMonth))
                }
            }

            items.add(AdapterItem.OpenChatRoomItem(item.id, item))
        }

        chatRoomList.clear()
        val chatRoomItemList = items.filterIsInstance<AdapterItem.OpenChatRoomItem>()
        chatRoomList.addAll(chatRoomItemList.map { it.chatRoom })

        recyclerView.scheduleLayoutAnimation()
        submitList(items)
    }

    private fun submitListInFilter(list: List<ChatRoom>) {
        sort(list)

        val items = arrayListOf<AdapterItem>()
        var yearMonth = blank

        for ((index, item) in list.withIndex()) {
            item.time.toDateFormat(
                    recyclerView.context.getString(R.string.year_month_date_format)
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

    fun update(chatRoom: ChatRoom) {
        for ((index, item) in currentList.withIndex()) {
            if (item is AdapterItem.OpenChatRoomItem) {
                if (item.chatRoom.id == chatRoom.id) {
                    notifyItemChanged(index)
                }
            }
        }
    }

    @Suppress("JavaCollectionsStaticMethodOnImmutableList")
    private fun sort(list: List<ChatRoom>) {
        Collections.sort(list
        ) { o1: ChatRoom, o2: ChatRoom ->
            when (type) {
                Type.private -> (o2.lastMessage.time - o1.lastMessage.time).toInt()
                Type.public -> (o2.time - o1.time).toInt()
                else -> 0
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView

        // Disable animation.
        recyclerView.itemAnimator = null
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