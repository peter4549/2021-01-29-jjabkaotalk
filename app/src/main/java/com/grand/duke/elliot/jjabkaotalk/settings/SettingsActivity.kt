package com.grand.duke.elliot.jjabkaotalk.settings

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.base.BaseActivity
import com.grand.duke.elliot.jjabkaotalk.databinding.ActivitySettingsBinding
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import com.grand.duke.elliot.jjabkaotalk.util.SettingItem
import com.grand.duke.elliot.jjabkaotalk.util.SettingItemAdapter

class SettingsActivity: BaseActivity(), SettingItemAdapter.OnItemClickListener {

    object SettingItemId {
        const val SignOut = 0
    }

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)

        val settingItems = arrayListOf(
                SettingItem(SettingItemId.SignOut,
                        title = getString(R.string.sign_out),
                        drawable = ContextCompat.getDrawable(this, R.drawable.ic_outline_exit_to_app_24)
                )
        )

        binding.recyclerView.apply {
            adapter = SettingItemAdapter(settingItems).apply {
                setOnItemClickListener(this@SettingsActivity)
            }
            layoutManager = LinearLayoutManager(this@SettingsActivity)
        }
    }

    override fun onClick(id: Int) {
        when(id) {
            SettingItemId.SignOut -> {
                MainApplication.getFirebaseAuthInstance().signOut()
                finish()
            }
        }
    }
}