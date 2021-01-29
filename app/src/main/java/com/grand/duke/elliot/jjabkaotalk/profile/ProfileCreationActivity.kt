package com.grand.duke.elliot.jjabkaotalk.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.authentication.PhoneAuthenticationFragment
import com.grand.duke.elliot.jjabkaotalk.base.BaseActivity
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.databinding.ActivityProfileCreationBinding
import com.grand.duke.elliot.jjabkaotalk.util.blank
import com.grand.duke.elliot.jjabkaotalk.util.view.SimpleItem
import com.grand.duke.elliot.jjabkaotalk.util.view.SimpleListDialogFragment

class ProfileCreationActivity: BaseActivity(), SimpleListDialogFragment.FragmentContainer {

    private lateinit var viewModel: ProfileCreationViewModel
    private lateinit var binding: ActivityProfileCreationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(viewModelStore, ProfileCreationViewModelFactory())[ProfileCreationViewModel::class.java]
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile_creation)

        binding.imageProfilePhoto.setOnClickListener {
            val simpleDialgo
            PhotoManager.dispatchImageCaptureIntent(this@ProfileCreationActivity)
        }

        binding.buttonVerification.setOnClickListener {
            val containerViewId = R.id.frameLayout
            val fragment = PhoneAuthenticationFragment()
            val tag = fragment.tag

            supportFragmentManager.beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(
                    R.anim.anim_slide_in_left,
                    R.anim.anim_slide_out_left,
                    R.anim.anim_slide_in_right,
                    R.anim.anim_slide_out_left
                ).replace(containerViewId, PhoneAuthenticationFragment(), tag).commit()
        }

        setOnOptionsMenu(
            menuRes = R.menu.menu_activity_profile_creation,
            optionsItemIdToOnSelected = *arrayOf(
                R.id.item_complete to {
                    if (checkProfile()) {
                        // TODO 프로필 업로드하기.
                        createUser()?.let { viewModel.setUser(it) } ?: run {
                            // TODO 유저를 찾을수 없습니다 띄워주기. ,, error.
                        }
                    }
                }
            )
        )
    }



    private fun checkProfile(): Boolean {
        val name = binding.textInputEditTextName.text.toString()

        if (name.isBlank())
            return false
        else
            viewModel.name = name

        if (viewModel.verified.not())
            return false

        return true
    }

    private fun createUser(): User? {
        val uid = viewModel.uid() ?: return null
        val name = viewModel.name
        val profilePhotoUri = viewModel.profilePhotoUri
        return User(
            uid = uid,
            name = name,
            profilePhotoUri = profilePhotoUri,
            location = blank,
            friends = arrayOf(),
            chatRooms = arrayOf(),
            verified = viewModel.verified
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when(requestCode) {
                PhotoManager.REQUEST_CODE_IMAGE_CAPTURE -> {
                    PhotoManager.photoUri?.let {
                        setProfilePhoto(it)
                    }
                }
                PhotoManager.REQUEST_CODE_IMAGE_PICKER -> {
                    data?.let { it.data?.let { uri -> setProfilePhoto(uri) } }
                }
            }
        }
    }

    private fun setProfilePhoto(uri: Uri) {
        Glide.with(binding.imageProfilePhoto.context)
            .load(uri)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.ic_round_account_circle_96)
            .transition(DrawableTransitionOptions.withCrossFade())
            .transform(CircleCrop())
            .into(binding.imageProfilePhoto)
        viewModel.deviceProfilePhotoUri = uri
    }

    override fun onRequestOnItemSelectedListener():
            SimpleListDialogFragment.OnItemSelectedListener =
        object: SimpleListDialogFragment.OnItemSelectedListener {
            override fun onItemSelected(dialogFragment: DialogFragment, simpleItem: SimpleItem) {
                TODO("Not yet implemented")
            }
        }

    override fun onRequestOnScrollReachedBottom():
            SimpleListDialogFragment.OnScrollReachedBottomListener? = null
}