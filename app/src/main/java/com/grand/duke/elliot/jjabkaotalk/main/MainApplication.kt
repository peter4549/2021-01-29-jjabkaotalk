package com.grand.duke.elliot.jjabkaotalk.main

import android.app.Application
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.grand.duke.elliot.jjabkaotalk.R
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.module.getNetWorkModule
import com.grand.duke.elliot.jjabkaotalk.shared_preferences.SharedPreferencesManager
import com.grand.duke.elliot.jjabkaotalk.util.DefaultLocation
import com.grand.duke.elliot.jjabkaotalk.util.blank
import com.kakao.sdk.common.KakaoSdk
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class MainApplication: Application() {

    private val sharedPreferencesManager = SharedPreferencesManager.instance(this)

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        primaryColor = ContextCompat.getColor(this, R.color.primary_color)
        secondaryColor = ContextCompat.getColor(this, R.color.secondary_color)
        location.value = sharedPreferencesManager.loadLocation()
        // printHashKey(this)

        FirebaseApp.initializeApp(this)
        KakaoSdk.init(this, "bc81e292da6e7b7f70d9b44118abc16c")
        startKoin {
            androidContext(this@MainApplication)
            modules(getNetWorkModule("https://asia-northeast1-jjabkaotalk.cloudfunctions.net/"))
        }
    }

    companion object {
        @ColorInt
        var primaryColor = 0
        @ColorInt
        var secondaryColor = 0

        @Volatile
        private var firebaseAuth: FirebaseAuth? = null

        fun getFirebaseAuthInstance(): FirebaseAuth {
            synchronized(this) {
                var instance = this.firebaseAuth

                if (instance == null) {
                    instance = FirebaseAuth.getInstance()
                    firebaseAuth = instance
                }

                return instance
            }
        }

        var user = MutableLiveData<User?>(null)
        var location = MutableLiveData<String>(DefaultLocation)
        var currentChatRoomId = blank
    }

    /*
    @SuppressLint("PackageManagerGetSignatures")
    private fun printHashKey(context: Context) {
        try {
            val info: PackageInfo =
                    context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val messageDigest: MessageDigest = MessageDigest.getInstance("SHA")
                messageDigest.update(signature.toByteArray())
                val hashKey = String(android.util.Base64.encode(messageDigest.digest(), 0))
                println("printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            println("printHashKey() $e")
        } catch (e: Exception) {
            println("printHashKey() $e")
        }
    }
     */
}