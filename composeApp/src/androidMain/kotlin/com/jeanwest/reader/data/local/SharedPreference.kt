package com.jeanwest.reader.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.jeanwest.reader.models.Device
import com.jeanwest.reader.models.ERPData
import com.jeanwest.reader.models.User
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreference @Inject constructor(
    @ApplicationContext context: Context,
) {

    private var appMemory: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
    private var appMemoryEditor: SharedPreferences.Editor = appMemory.edit()

    var user = User()
    var erpData = ERPData()
    var device = Device()

    init {
        refresh()
    }

    private fun refresh() {
        try {
            user = Gson().fromJson(
                appMemory.getString("user", ""),
                user.javaClass
            ) ?: User()
        } catch (e: Exception) {
            return
        }
        erpData = Gson().fromJson(
            appMemory.getString("erpData", ""),
            erpData.javaClass
        ) ?: ERPData()

        device = Gson().fromJson(
            appMemory.getString("device", ""),
            device.javaClass
        ) ?: Device()

        //Log.e("navid data", device.toString())
    }

    fun setAppDataImmediately() {
        appMemoryEditor.putString(
            "user",
            Gson().toJson(user).toString()
        )
        appMemoryEditor.putString(
            "erpData",
            Gson().toJson(erpData).toString()
        )
        appMemoryEditor.putString(
            "device",
            Gson().toJson(device).toString()
        )
        appMemoryEditor.commit()
        refresh()
    }
}