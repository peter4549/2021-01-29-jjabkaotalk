package com.grand.duke.elliot.jjabkaotalk.friends

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.base.BaseFragment
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.databinding.FragmentFriendsBinding
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import com.grand.duke.elliot.jjabkaotalk.settings.SettingsActivity

class FriendsFragment: BaseFragment() {

    private lateinit var viewModel: FriendsViewModel
    private lateinit var binding: FragmentFriendsBinding
    private val friendsAdapter = FriendAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel = ViewModelProvider(viewModelStore, FriendsViewModelFactory())[FriendsViewModel::class.java]
        binding = FragmentFriendsBinding.inflate(inflater, container, false)

        setOnOptionsMenu(binding.toolbar, R.menu.menu_friends_fragment, arrayOf(
                R.id.item_settings to {
                    startSettingsActivity()
                }
        ))

        MainApplication.user.observe(viewLifecycleOwner, Observer { user ->
            user?.let {
                updateUi(it)
                viewModel.update(it)
            }
        })

        viewModel.friends.observe(viewLifecycleOwner, Observer { friends ->
            binding.recyclerView.apply {
                adapter = friendsAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
            friendsAdapter.submitList(friends)
        })

        return binding.root
    }

    private fun startSettingsActivity() {
        val intent = Intent(requireActivity(), SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun updateUi(user: User) {
        if (this::binding.isInitialized) {
            if (user.profilePhotoUris.isNotEmpty()) {
                Glide.with(binding.imageProfilePhoto.context)
                        .load(user.profilePhotoUris[0])
                        .centerCrop()
                        // .diskCacheStrategy(DiskCacheStrategy.NONE)
                        // .skipMemoryCache(true)
                        .placeholder(R.drawable.ic_round_account_circle_96)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .transform(CircleCrop())
                        .into(binding.imageProfilePhoto)
            }
            binding.textName.text = user.name
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: FriendsFragment? = null

        fun getInstance(): FriendsFragment {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = FriendsFragment()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}