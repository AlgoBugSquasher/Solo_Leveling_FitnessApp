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

    fun getLastNotifiedDeletionTimestamp(): Long {
        return sharedPreferences.getLong("last_notified_deletion_timestamp", 0L)
    }

    fun setLastNotifiedDeletionTimestamp(timestamp: Long) {
        sharedPreferences.edit(commit = false) {
            putLong("last_notified_deletion_timestamp", timestamp)
        }
    }

    fun getLastReviewMilestone(): Int {
        return sharedPreferences.getInt("last_review_milestone", 0)
    }

    fun setLastReviewMilestone(milestone: Int) {
        sharedPreferences.edit(commit = false) {
            putInt("last_review_milestone", milestone)
        }
    }

    fun getSfxVolume(): Float = sharedPreferences.getFloat("sfx_volume", 1.0f)
    fun setSfxVolume(volume: Float) {
        sharedPreferences.edit(commit = false) {
            putFloat("sfx_volume", volume)
        }
    }

    fun getBgmVolume(): Float = sharedPreferences.getFloat("bgm_volume", 0.8f)
    fun setBgmVolume(volume: Float) {
        sharedPreferences.edit(commit = false) {
            putFloat("bgm_volume", volume)
        }
    }

    fun getVoiceVolume(): Float = sharedPreferences.getFloat("voice_volume", 1.0f)
    fun setVoiceVolume(volume: Float) {
        sharedPreferences.edit(commit = false) {
            putFloat("voice_volume", volume)
        }
    }

    fun isHapticsEnabled(): Boolean = sharedPreferences.getBoolean("haptics_enabled", true)
    fun setHapticsEnabled(enabled: Boolean) {
        sharedPreferences.edit(commit = false) {
            putBoolean("haptics_enabled", enabled)
        }
    }
}
