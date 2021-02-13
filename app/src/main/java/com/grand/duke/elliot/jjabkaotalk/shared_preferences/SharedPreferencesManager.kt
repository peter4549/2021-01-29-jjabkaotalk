package com.grand.duke.elliot.jjabkaotalk.shared_preferences

import android.app.Application
import android.content.Context
import com.grand.duke.elliot.jjabkaotalk.chat.room.ChatRoomsFragment
import com.grand.duke.elliot.jjabkaotalk.util.DefaultLocation

class SharedPreferencesManager private constructor(private val application: Application) {
    private object Name {
        const val Location = "com.grand.duke.elliot.jjabkaotalk.shared_preferences" +
                ".SharedPreferencesManager.Name.Location"
        const val NotificationSettings = "com.grand.duke.elliot.jjabkaotalk.shared_preferences" +
                ".SharedPreferencesManager.Name.NotificationSettings"
    }

    private object Key {
        const val Location = "com.grand.duke.elliot.jjabkaotalk.shared_preferences" +
                ".SharedPreferencesManager.Key.Location"
        const val NotificationSettings = "com.grand.duke.elliot.jjabkaotalk.shared_preferences" +
                ".SharedPreferencesManager.Key.NotificationSettings"

    }

    object NotificationSettings {
        const val Silent = 0
        const val Sound = 1
        const val Vibration = 2
    }


    fun saveLocation(location: String) {
        val preferences = application.getSharedPreferences(Name.Location, Context.MODE_PRIVATE)
        preferences.edit().putString(Key.Location, location).apply()
    }

    fun loadLocation(): String {
        val preferences = application.getSharedPreferences(Name.Location, Context.MODE_PRIVATE)
        return preferences.getString(Key.Location, DefaultLocation) ?: DefaultLocation
    }

    fun putNotificationSettings(notificationSettings: Int) {
        application.getSharedPreferences(Name.NotificationSettings, Context.MODE_PRIVATE)
                .edit()
                .putInt(Key.NotificationSettings, notificationSettings)
                .apply()
    }

    fun getNotificationSettings(): Int {
        return application.getSharedPreferences(Name.NotificationSettings, Context.MODE_PRIVATE)
                .getInt(Key.NotificationSettings, NotificationSettings.Silent)
    }

    companion object {
        @Volatile
        private var INSTANCE: SharedPreferencesManager? = null

        fun instance(application: Application): SharedPreferencesManager {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = SharedPreferencesManager(application)
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}