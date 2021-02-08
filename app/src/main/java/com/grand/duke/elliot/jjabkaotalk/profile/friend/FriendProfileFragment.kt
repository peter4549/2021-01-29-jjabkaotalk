package com.grand.duke.elliot.jjabkaotalk.profile.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.base.BaseFragment
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.databinding.FragmentFriendProfileBinding
import com.grand.duke.elliot.jjabkaotalk.firebase.FireStoreHelper
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import timber.log.Timber

class FriendProfileFragment: BaseFragment() {

    private lateinit var binding: FragmentFriendProfileBinding
    private lateinit var friend: User
    private val fireStoreHelper = FireStoreHelper()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFriendProfileBinding.inflate(inflater, container, false)

        val friendProfileFragmentArgs by navArgs<FriendProfileFragmentArgs>()
        friend = friendProfileFragmentArgs.user

        if (friend.profilePhotoUris.isNotEmpty()) {
            Glide.with(binding.imageProfilePhoto.context)
                    .load(friend.profilePhotoUris[0])
                    .centerCrop()
                    // .diskCacheStrategy(DiskCacheStrategy.NONE)
                    // .skipMemoryCache(true)
                    .placeholder(R.drawable.ic_round_account_circle_96)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .transform(CircleCrop())
                    .into(binding.imageProfilePhoto)
        }

        binding.textName.text = friend.name
        binding.textUnFriend.setOnClickListener {
            MainApplication.user.value?.let { user ->
                fireStoreHelper.unFriend(user, friend) { exception ->
                    exception?.let {
                        Timber.e(it, "Un-friend failed.")
                    } ?: run {
                        Timber.d("Un-friend success.")
                    }

                    requireActivity().onBackPressed()
                }
            }
        }

        return binding.root
    }
}