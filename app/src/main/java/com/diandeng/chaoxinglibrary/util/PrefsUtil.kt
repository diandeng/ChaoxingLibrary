package com.diandeng.chaoxinglibrary.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefsUtil @Inject constructor(
    @ApplicationContext private val context:Context
){
    val prefs: SharedPreferences = context.getSharedPreferences("ChaoxingLibraryPrefs", Context.MODE_PRIVATE)
    val gson = Gson()

    fun <T> save(key: String, value: T) {
        val json = gson.toJson(value)
        prefs.edit { putString(key, json) }
    }

    inline fun <reified T> load(key: String, defaultValue: T): T {
        val json = prefs.getString(key, null)
        return if (json.isNullOrEmpty()) {
            defaultValue
        } else {
            try {
                val type = object : TypeToken<T>() {}.type
                gson.fromJson(json, type)
            } catch (e: Exception) {
                Log.e("PrefsUtil", "Failed to load $key: ${e.message}")
                defaultValue
            }
        }
    }
    // 清除特定 key 的数据
    fun clear(key: String) {
        prefs.edit { remove(key) }
    }

    // 清除所有数据
    fun clearAll() {
        prefs.edit { clear() }
    }
}