package com.exork.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("exork_prefs", Context.MODE_PRIVATE)

    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean("is_first_launch", true)
    }

    fun setFirstLaunch(isFirstLaunch: Boolean) {
        sharedPreferences.edit(commit = false) {
            putBoolean("is_first_launch", isFirstLaunch)
        }
    }

    fun hasAcceptedQualifications(): Boolean {
        return sharedPreferences.getBoolean("has_accepted_qualifications", false)
    }

    fun setHasAcceptedQualifications(accepted: Boolean) {
        sharedPreferences.edit(commit = false) {
            putBoolean("has_accepted_qualifications", accepted)
        }
    }

    fun getAvatarUri(): String? {
        return sharedPreferences.getString("avatar_uri", null)
    }

    fun setAvatarUri(uri: String?) {
        sharedPreferences.edit(commit = false) {
            if (uri == null) {
                remove("avatar_uri")
            } else {
                putString("avatar_uri", uri)
            }
        }
    }

    fun hasSeenDailyQuestPopUp(date: String): Boolean {
        return sharedPreferences.getString("last_quest_popup_date", "") == date
    }

    fun setHasSeenDailyQuestPopUp(date: String) {
        sharedPreferences.edit(commit = false) {
            putString("last_quest_popup_date", date)
        }
    }
}
