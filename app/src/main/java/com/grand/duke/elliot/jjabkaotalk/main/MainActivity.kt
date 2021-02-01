package com.grand.duke.elliot.jjabkaotalk.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseUser
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.base.BaseActivity
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.databinding.ActivityMainBinding
import com.grand.duke.elliot.jjabkaotalk.firebase.FireStoreHelper
import com.grand.duke.elliot.jjabkaotalk.profile.ProfileCreationActivity
import com.grand.duke.elliot.jjabkaotalk.sign_in.SignInActivity
import timber.log.Timber

class MainActivity : AppCompatActivity(), FireStoreHelper.OnUserDocumentSnapshotListener {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    private val fireStoreHelper = FireStoreHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this, MainViewModelFactory())[MainViewModel::class.java]

        setupAuthStateListener()
        initUi()
    }

    private fun setupAuthStateListener() {
        MainApplication.getFirebaseAuthInstance().addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser.isNotNull()) {
                firebaseAuth.currentUser?.uid?.let {
                    fireStoreHelper.setOnDocumentSnapshotListener(this)
                    fireStoreHelper.setupUserSnapshotListener(it)
                }
            } else {
                startSignInActivity()
            }
        }
    }

    private fun startSignInActivity() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_SIGN_IN_ACTIVITY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            REQUEST_CODE_SIGN_IN_ACTIVITY -> {
                if (resultCode == RESULT_OK) {
                    Timber.d("Google sign-in success.")
                }
            }
        }
    }

    /** FireStoreHelper.OnDocumentSnapshotListener */
    override fun onUserDocumentSnapshot(user: User) {
        showToast("WHAT THE?")
        if (user.verified) {
            showToast("WORKWELL!")
            MainApplication.user = user
            // TODO: load info, 정상적으로 앱 이용가능상태.
        } else {
            // TODO. 프로필로 이동, 번호인증하라고 메시지 띄워주기.
            startProfileCreationActivity(user)
        }
    }

    override fun onException(exception: Exception) {
        Timber.e(exception)
    }

    override fun onNoUserDocumentSnapshot() {
        showToast("WHAT FFFFF?")
        // TODO 작성된 프로필 없음 => 프로필 작성 액티비티로 이동,
        startProfileCreationActivity(null)
    }

    private fun startProfileCreationActivity(user: User?) {
        val intent = Intent(this, ProfileCreationActivity::class.java)
        user?.let { intent.putExtra(EXTRA_NAME_USER, it) }
        startActivity(intent)
    }

    private fun initUi() {
        val tabIconDrawableIds = arrayOf(
            R.drawable.ic_communication_48px,
            R.drawable.ic_speech_bubble_48,
            R.drawable.ic_round_person_24
        )

        binding.viewPager2.adapter = FragmentStateAdapter(this)
        binding.viewPager2.isUserInputEnabled = false

        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.tag = position
            tab.setIcon(tabIconDrawableIds[position])
        }.attach()
    }

    private fun FirebaseUser?.isNotNull() = this != null

    protected fun showToast(text: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(this, text, duration).show()
    }

    companion object {
        private const val REQUEST_CODE_SIGN_IN_ACTIVITY = 139

        const val EXTRA_NAME_USER = "com.grand.duke.elliot.jjabkaotalk.main" +
                ".main_activity.extra_name_user"
    }
}