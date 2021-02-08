package com.grand.duke.elliot.jjabkaotalk.shared_preferences

import android.content.Context
import com.grand.duke.elliot.jjabkaotalk.util.DefaultLocation

object SharedPreferencesManager {
    private object Name {
        const val Location = "com.grand.duke.elliot.jjabkaotalk.shared_preferences" +
                ".SharedPreferencesManager.Name.Location"
    }

    private object Key {
        const val Location = "com.grand.duke.elliot.jjabkaotalk.shared_preferences" +
                ".SharedPreferencesManager.Key.Location"
    }


    fun saveLocation(context: Context, location: String) {
        val preferences = context.getSharedPreferences(Name.Location, Context.MODE_PRIVATE)
        preferences.edit().putString(Key.Location, location).apply()
    }

    fun loadLocation(context: Context): String {
        val preferences = context.getSharedPreferences(Name.Location, Context.MODE_PRIVATE)
        return preferences.getString(Key.Location, DefaultLocation) ?: DefaultLocation
    }
}