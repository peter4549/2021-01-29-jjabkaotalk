package com.grand.duke.elliot.jjabkaotalk.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.databinding.ItemFriendBinding

class FriendAdapter: ListAdapter<User, FriendAdapter.ViewHolder>(AdapterItemDiffCallback()) {

    class ViewHolder(private val binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: User) {
            binding as ItemFriendBinding

            if (friend.profilePhotoUris.isNotEmpty()) {
                Glide.with(binding.imageProfilePhoto.context)
                        .load(friend.profilePhotoUris[0])
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.ic_round_account_circle_96)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .transform(CircleCrop())
                        .into(binding.imageProfilePhoto)
            }

            binding.textName.text = friend.name
        }
    }

    private fun from(parent: ViewGroup): ViewHolder {
        val binding = ItemFriendBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
        )

        return ViewHolder(binding)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class AdapterItemDiffCallback: DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}