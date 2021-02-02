package com.grand.duke.elliot.jjabkaotalk.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat
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
import com.grand.duke.elliot.jjabkaotalk.firebase.CloudStorageHelper
import com.grand.duke.elliot.jjabkaotalk.firebase.FireStoreHelper
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import com.grand.duke.elliot.jjabkaotalk.util.blank
import com.grand.duke.elliot.jjabkaotalk.util.view.SimpleItem
import com.grand.duke.elliot.jjabkaotalk.util.view.SimpleListDialogFragment
import java.lang.NullPointerException

class ProfileCreationActivity: BaseActivity(), SimpleListDialogFragment.FragmentContainer,
        PhoneAuthenticationFragment.FragmentContainer {

    private lateinit var viewModel: ProfileCreationViewModel
    private lateinit var binding: ActivityProfileCreationBinding
    private val firebaseUser = MainApplication.getFirebaseAuthInstance().currentUser
    private val fireStoreHelper = FireStoreHelper().apply {
        setOnSetUserListener(object: FireStoreHelper.OnSetUserListener {
            override fun onSuccess() {
                finish()
            }

            override fun onFailure() {
                showToast(getString(R.string.profile_creation_failure_message))
            }
        })
    }
    private val cloudStorageHelper = CloudStorageHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, ProfileCreationViewModelFactory())[ProfileCreationViewModel::class.java]
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile_creation)

        binding.imageProfilePhoto.setOnClickListener {
            val simpleListDialogFragment = SimpleListDialogFragment()
            simpleListDialogFragment.setItems(
                    arrayListOf(
                            SimpleItem(
                                    IMAGE_CAPTURE,
                                    getString(R.string.photo_shoot),
                                    ContextCompat.getDrawable(this, R.drawable.ic_round_photo_camera_48)),
                            SimpleItem(
                                    IMAGE_PICK,
                                    getString(R.string.select_from_album),
                                    ContextCompat.getDrawable(this, R.drawable.ic_round_insert_photo_48))
                    )
            )
            simpleListDialogFragment.show(supportFragmentManager, null)
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

        binding.buttonComplete.setOnClickListener {
            if (checkProfile()) {
                viewModel.deviceProfilePhotoUri?.let {
                    /** deviceProfilePhotoUri is not null. */
                    cloudStorageHelper.storeProfilePhoto(it) { uri ->
                        uri?.let { viewModel.profilePhotoUri = uri.toString() }
                        fireStoreHelper.setUser(createUser())
                    }
                } ?: run {
                    /** deviceProfilePhotoUri is null. */
                    fireStoreHelper.setUser(createUser())
                }
            }
        }


        setOnOptionsMenu(
                menuRes = R.menu.menu_activity_profile_creation,
                optionsItemIdToOnSelected = arrayOf(
                        R.id.item_complete to {
                            if (checkProfile()) {
                                // TODO 프로필 업로드하기. 버튼 클릭과 유사동작
                                createUser().let { fireStoreHelper.setUser(it) }
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

    private fun createUser(): User {
        val uid = firebaseUser?.uid ?: throw NullPointerException("viewModel.uid() is null.")
        val name = viewModel.name
        val profilePhotoUri = viewModel.profilePhotoUri
        val profilePhotoUris = mutableListOf<String>()

        profilePhotoUri?.let { profilePhotoUris.add(it) }

        return User (
                uid = uid,
                name = name,
                profilePhotoUris = profilePhotoUris,
                location = blank,
                friends = mutableListOf(),
                chatRooms = mutableListOf(),
                openChatRooms = mutableListOf(),
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

    override fun onRequestOnVerifiedListener(): PhoneAuthenticationFragment.OnVerifiedListener? =
            object: PhoneAuthenticationFragment.OnVerifiedListener {
                override fun onVerified() {
                    onBackPressed()
                    viewModel.verified = true
                    binding.checkView.check()
                    binding.buttonVerification.isEnabled = false
                    binding.buttonComplete.isEnabled = true
                }
            }

    override fun onRequestOnItemSelectedListener():
            SimpleListDialogFragment.OnItemSelectedListener =
            object: SimpleListDialogFragment.OnItemSelectedListener {
                override fun onItemSelected(dialogFragment: DialogFragment, simpleItem: SimpleItem) {
                    when(simpleItem.id) {
                        IMAGE_CAPTURE -> PhotoManager.dispatchImageCaptureIntent(this@ProfileCreationActivity)
                        IMAGE_PICK -> PhotoManager.dispatchImagePickerIntent(this@ProfileCreationActivity)
                    }

                    dialogFragment.dismiss()
                }
            }

    override fun onRequestOnScrollReachedBottom():
            SimpleListDialogFragment.OnScrollReachedBottomListener? = null

    companion object {
        private const val IMAGE_CAPTURE = "com.grand.duke.elliot.jjabkaotalk.profile" +
                ".image_capture"
        private const val IMAGE_PICK = "com.grand.duke.elliot.jjabkaotalk.profile" +
                ".image_pick"
    }
}