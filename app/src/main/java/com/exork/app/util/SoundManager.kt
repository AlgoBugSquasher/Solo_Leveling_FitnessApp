package com.exork.app.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.exork.app.R
import com.exork.app.model.BadgeRarity

class SoundManager private constructor(context: Context) {
    private val soundPool: SoundPool
    private val sounds = mutableMapOf<String, Int>()
    private val rawResIds = mutableMapOf<String, Int>()
    private val loadedSounds = mutableSetOf<Int>()
    private var isEnabled = true
    private val context: Context = context.applicationContext

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSounds.add(sampleId)
                Log.d("SoundManager", "Sound Loaded Successfully: ID $sampleId")
            } else {
                Log.e("SoundManager", "Failed to load sound: ID $sampleId, status: $status")
            }
        }

        // Preload sounds
        loadSound(context, "click", R.raw.click_subtle)
        loadSound(context, "level_up", R.raw.level_up)
        loadSound(context, "quest_complete", R.raw.quest_complete)
        loadSound(context, "promotion", R.raw.rank_promotion)
        loadSound(context, "rank_promotion", R.raw.rank_promotion) // Same as promotion or specific one if exists
        loadSound(context, "badge_common", R.raw.badge_common)
        loadSound(context, "badge_rare", R.raw.badge_rare)
        loadSound(context, "badge_epic", R.raw.badge_epic)
        loadSound(context, "badge_legendary", R.raw.badge_legendary)
        loadSound(context, "system_popup", R.raw.system_popup)
        loadSound(context, "system_accept", R.raw.system_accept)
        loadSound(context, "system_decline", R.raw.system_decline)
    }

    private fun loadSound(context: Context, key: String, resId: Int) {
        try {
            val id = soundPool.load(context, resId, 1)
            sounds[key] = id
            rawResIds[key] = resId
        } catch (e: Exception) {
            Log.e("SoundManager", "Error loading sound $key: ${e.message}")
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    private fun play(key: String) {
        if (!isEnabled) return
        val id = sounds[key]
        if (id != null) {
            if (loadedSounds.contains(id)) {
                soundPool.play(id, 1f, 1f, 1, 0, 1f)
            } else {
                Log.w("SoundManager", "Sound not loaded yet: $key. Falling back to MediaPlayer.")
                playFallback(key)
            }
        } else {
            Log.w("SoundManager", "Sound key not found: $key")
        }
    }

    private fun playFallback(key: String) {
        val resId = rawResIds[key] ?: return
        try {
            android.media.MediaPlayer.create(context, resId).apply {
                setOnCompletionListener { release() }
                start()
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "MediaPlayer fallback failed for $key: ${e.message}")
        }
    }

    fun playClick() = play("click")
    fun playLevelUp() = play("level_up")
    fun playQuestComplete() = play("quest_complete")
    fun playPromotion() = play("promotion")
    fun playRankPromotion() = play("rank_promotion")
    fun playPersonalRecord() = play("promotion")
    
    fun playBadgeUnlock(rarity: BadgeRarity) {
        when (rarity) {
            BadgeRarity.COMMON -> play("badge_common")
            BadgeRarity.RARE -> play("badge_rare")
            BadgeRarity.EPIC -> play("badge_epic")
            BadgeRarity.LEGENDARY -> play("badge_legendary")
        }
    }

    fun playSystemPopupSound() = play("system_popup")
    fun playSystemAcceptSound() = play("system_accept")
    fun playSystemDeclineSound() = play("system_decline")

    companion object {
        @Volatile
        private var instance: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return instance ?: synchronized(this) {
                instance ?: SoundManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
