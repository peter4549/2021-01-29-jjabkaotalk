package com.grand.duke.elliot.jjabkaotalk.main

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.chat.mine.MyChatRoomsFragment
import com.grand.duke.elliot.jjabkaotalk.chat.room.ChatRoomsFragment
import com.grand.duke.elliot.jjabkaotalk.cloud_messaging.CLICK_ACTION
import com.grand.duke.elliot.jjabkaotalk.cloud_messaging.CloudMessagingHelper
import com.grand.duke.elliot.jjabkaotalk.cloud_messaging.CloudMessagingService
import com.grand.duke.elliot.jjabkaotalk.cloud_messaging.CloudMessagingService.Companion.EXTRA_NAME_CHAT_ROOM_ID
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.data.User.Companion.FIELD_TOKEN
import com.grand.duke.elliot.jjabkaotalk.databinding.ActivityMainBinding
import com.grand.duke.elliot.jjabkaotalk.firebase.Collection
import com.grand.duke.elliot.jjabkaotalk.firebase.FireStoreHelper
import com.grand.duke.elliot.jjabkaotalk.friends.FriendsFragment
import com.grand.duke.elliot.jjabkaotalk.profile.ProfileCreationActivity
import com.grand.duke.elliot.jjabkaotalk.sign_in.SignInActivity
import com.grand.duke.elliot.jjabkaotalk.util.view.SimpleItem
import com.grand.duke.elliot.jjabkaotalk.util.view.SimpleListDialogFragment
import timber.log.Timber

class MainActivity : AppCompatActivity(), FireStoreHelper.OnUserDocumentSnapshotListener,
        SimpleListDialogFragment.FragmentContainer{

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var listenerRegistration: ListenerRegistration
    private val fireStoreHelper = FireStoreHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this, MainViewModelFactory())[MainViewModel::class.java]

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = MainApplication.secondaryColor

        setupAuthStateListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::listenerRegistration.isInitialized)
            listenerRegistration.remove()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action != null) {
            when (intent.action) {
                CloudMessagingService.ACTION_CHAT_NOTIFICATION -> {
                    val chatRoomId =
                        intent.getStringExtra(EXTRA_NAME_CHAT_ROOM_ID)
                    if (chatRoomId != null && chatRoomId.isNotBlank())
                        enterChatRoom(chatRoomId)
                }
            }
        }
    }

    private fun setupAuthStateListener() {
        MainApplication.getFirebaseAuthInstance().addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser.isNotNull()) {
                firebaseAuth.currentUser?.uid?.let {
                    fireStoreHelper.setOnUserDocumentSnapshotListener(this)
                    listenerRegistration = fireStoreHelper.registerUserSnapshotListener(it)
                }
            } else {
                if (this::listenerRegistration.isInitialized)
                    listenerRegistration.remove()

                MainApplication.user.value = null
                startSignInActivity()
            }
        }
    }

    fun startSignInActivity() {
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

    /** FireStoreHelper.OnUserDocumentSnapshotListener */
    override fun onUserDocumentSnapshot(user: User) {
        MainApplication.user.value = user
        updateToken(user)
        val chatRoomId = intent.getStringExtra(EXTRA_NAME_CHAT_ROOM_ID) ?: return
        enterChatRoom(chatRoomId)
    }

    override fun onNoUserDocumentSnapshot() {
        MainApplication.user.value = null
        startProfileCreationActivity(null)
    }

    override fun onException(exception: Exception) {
        Timber.e(exception)
        MainApplication.user.value = null
    }

    private fun updateToken(user: User) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (task.isSuccessful.not()) {
                    Timber.w(task.exception, "Fetching FCM registration token failed.")
                    return@OnCompleteListener
                }

                val token = task.result

                if (user.token == token)
                    return@OnCompleteListener

                val map = mapOf(FIELD_TOKEN to token)

                FirebaseFirestore.getInstance()
                        .collection(Collection.Users)
                        .document(user.uid)
                        .update(map).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful)
                                Timber.d("Token updated.")
                            else
                                Timber.w("Token update failed.")
                        }
            })
    }

    private fun startProfileCreationActivity(user: User?) {
        val intent = Intent(this, ProfileCreationActivity::class.java)
        user?.let { intent.putExtra(EXTRA_NAME_USER, it) }
        startActivity(intent)
    }

    private fun enterChatRoom(chatRoomId: String) {
        fireStoreHelper.getChatRoom(chatRoomId) { chatRoom ->
            chatRoom?.let {
                if (MainApplication.currentChatRoomId.isNotBlank())
                    supportFragmentManager.popBackStackImmediate()

                findNavController(R.id.nav_host_fragment)
                    .navigate(TabFragmentDirections.actionTabFragmentToOpenChatFragment(it))
            }
        }
    }

    private fun FirebaseUser?.isNotNull() = this != null

    companion object {
        private const val REQUEST_CODE_SIGN_IN_ACTIVITY = 139

        const val EXTRA_NAME_USER = "com.grand.duke.elliot.jjabkaotalk.main" +
                ".main_activity.extra_name_user"
    }

    override fun onRequestOnItemSelectedListener():
            SimpleListDialogFragment.OnItemSelectedListener =  object: SimpleListDialogFragment.OnItemSelectedListener {
        override fun onItemSelected(dialogFragment: DialogFragment, simpleItem: SimpleItem) {
            MainApplication.location.value = simpleItem.name
            dialogFragment.dismiss()
        }
    }

    override fun onRequestOnScrollReachedBottom():
            SimpleListDialogFragment.OnScrollReachedBottomListener? = null
}