package com.grand.duke.elliot.jjabkaotalk.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.base.BaseFragment
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.databinding.FragmentProfileBinding
import com.grand.duke.elliot.jjabkaotalk.firebase.FireStoreHelper
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication

class ProfileFragment: BaseFragment() {

    private lateinit var binding: FragmentProfileBinding
    private val fireStoreHelper = FireStoreHelper()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        val profileFragmentArgs by navArgs<ProfileFragmentArgs>()
        val sender = profileFragmentArgs.user

        if (sender.profilePhotoUris.isNotEmpty()) {
            Glide.with(binding.imageProfilePhoto.context)
                    .load(sender.profilePhotoUris[0])
                    .centerCrop()
                    // .diskCacheStrategy(DiskCacheStrategy.NONE)
                    // .skipMemoryCache(true)
                    .placeholder(R.drawable.ic_round_account_circle_96)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .transform(CircleCrop())
                    .into(binding.imageProfilePhoto)
        }

        binding.textName.text = sender.name

        binding.textAddToFriends.setOnClickListener {
            MainApplication.user.value?.let {
                fireStoreHelper.userArrayUnion(it.uid, User.FIELD_FRIEND_IDS, sender.uid) {
                    showToast(getString(R.string.friend_added))
                    requireActivity().onBackPressed()
                }
            }
        }

        binding.textBlock.setOnClickListener {
            MainApplication.user.value?.let {
                fireStoreHelper.userArrayUnion(it.uid, User.FIELD_BLACKLIST, sender.uid) {
                    showToast(getString(R.string.added_to_blacklist))
                    requireActivity().onBackPressed()
                }
            }
        }

        return binding.root
    }
}