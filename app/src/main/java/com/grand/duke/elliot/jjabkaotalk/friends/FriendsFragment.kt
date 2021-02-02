package com.grand.duke.elliot.jjabkaotalk.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.grand.duke.elliot.jjabkaotalk.base.BaseFragment
import com.grand.duke.elliot.jjabkaotalk.databinding.FragmentFriendsBinding
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication

class FriendsFragment private constructor(): BaseFragment() {

    private lateinit var viewModel: FriendsViewModel
    private lateinit var binding: FragmentFriendsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel = ViewModelProvider(viewModelStore, FriendsViewModelFactory())[FriendsViewModel::class.java]
        binding = FragmentFriendsBinding.inflate(inflater, container, false)

        MainApplication.user?.let {

        }

        return binding.root
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