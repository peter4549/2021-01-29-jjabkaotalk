package com.grand.duke.elliot.jjabkaotalk.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayoutMediator
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.base.BaseFragment
import com.grand.duke.elliot.jjabkaotalk.databinding.FragmentTabBinding

class TabFragment: BaseFragment() {

    private lateinit var binding: FragmentTabBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTabBinding.inflate(inflater, container, false)

        initUi()

        return binding.root
    }

    private fun initUi() {
        val tabIconDrawableIds = arrayOf(
            R.drawable.ic_communication_48px,
            R.drawable.ic_speech_bubble_48,
            R.drawable.ic_round_person_24
        )

        binding.viewPager2.adapter = FragmentStateAdapter(requireActivity())
        binding.viewPager2.isUserInputEnabled = false

        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.tag = position
            tab.setIcon(tabIconDrawableIds[position])
        }.attach()
    }
}