package com.grand.duke.elliot.jjabkaotalk.splash

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.base.BaseActivity
import com.grand.duke.elliot.jjabkaotalk.cloud_messaging.CLICK_ACTION
import com.grand.duke.elliot.jjabkaotalk.cloud_messaging.CloudMessagingService
import com.grand.duke.elliot.jjabkaotalk.main.MainActivity
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import com.grand.duke.elliot.jjabkaotalk.main.TabFragmentDirections
import com.grand.duke.elliot.jjabkaotalk.util.blank
import com.grand.duke.elliot.jjabkaotalk.util.lightenColor
import timber.log.Timber

class SplashActivity: BaseActivity() {
    private lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.statusBarColor = MainApplication.secondaryColor

        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.BOTTOM_TOP,
            intArrayOf(
                lightenColor(MainApplication.primaryColor, 0.25F),
                MainApplication.primaryColor
            )
        )
        gradientDrawable.cornerRadius = 0F

        findViewById<ConstraintLayout>(R.id.constraintLayout).background = gradientDrawable
    }

    override fun onStart() {
        super.onStart()
        startApplicationWithFirebaseRemoteConfig()
    }

    private fun startApplicationWithFirebaseRemoteConfig() {
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config)
        firebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                val mainIntent = Intent(this, MainActivity::class.java)
                if (intent.action == CLICK_ACTION) {
                    val chatRoomId = intent.extras?.get("chatRoomId").toString()
                    mainIntent.putExtra(CloudMessagingService.EXTRA_NAME_CHAT_ROOM_ID, chatRoomId)
                }


                if (task.isSuccessful) {
                    /**
                     * Keys:
                     * version_name: String
                     * notification_title: String
                     * notification_message: String
                     */
                    val versionName =
                        firebaseRemoteConfig.getString("version_name")
                    val notificationTitle =
                        firebaseRemoteConfig.getString("notification_title")
                    val notificationMessage =
                        firebaseRemoteConfig.getString("notification_message")

                    latestVersionName = versionName
                    startActivity(mainIntent)
                    finish()
                } else {
                    Timber.e(task.exception)
                    startActivity(mainIntent)
                    finish()
                }
            }
    }

    companion object {
        var latestVersionName = blank
    }
}