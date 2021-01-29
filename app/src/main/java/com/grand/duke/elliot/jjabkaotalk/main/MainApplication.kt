package com.grand.duke.elliot.jjabkaotalk.main

import android.app.Application
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.grand.duke.elliot.jjabkaotalk.R
import timber.log.Timber

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        primaryColor = ContextCompat.getColor(this, R.color.primary_color)
        secondaryColor = ContextCompat.getColor(this, R.color.secondary_color)
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
    }
}