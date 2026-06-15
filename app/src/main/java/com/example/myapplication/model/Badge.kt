package com.example.myapplication.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.myapplication.R

enum class BadgeRarity(val color: Color, val displayName: String) {
    COMMON(Color.Gray, "Common"),
    RARE(Color(0xFF6200EE), "Rare"),
    EPIC(Color(0xFFBB86FC), "Epic"),
    LEGENDARY(Color(0xFFFFD700), "Legendary")
}

data class Badge(
    val name: String,
    val requiredLevel: Int,
    val rarity: BadgeRarity,
    val description: String,
    @DrawableRes val imageRes: Int,
    val isUnlocked: Boolean = false
)

object BadgeData {
    val allBadges = listOf(
        Badge("Initiate Hunter", 1, BadgeRarity.COMMON, "The journey of a thousand miles begins with a single push-up.", R.drawable.initiate_hunter),
        Badge("Iron Body", 5, BadgeRarity.COMMON, "Your muscles are beginning to harden like iron.", R.drawable.iron_body),
        Badge("Shadow Trainee", 10, BadgeRarity.RARE, "You have stepped into the path of shadows.", R.drawable.shadow_trainee),
        Badge("Elite Hunter", 15, BadgeRarity.RARE, "Recognized among the ranks of capable hunters.", R.drawable.elite_hunter),
        Badge("Beast Crusher", 20, BadgeRarity.RARE, "No beast can stand against your overwhelming strength.", R.drawable.beast_crusher),
        Badge("Phantom Warrior", 25, BadgeRarity.EPIC, "Swift and deadly, you strike like a phantom.", R.drawable.phantom_warrior),
        Badge("Abyss Walker", 30, BadgeRarity.EPIC, "The darkness of the abyss is your new home.", R.drawable.abyss_walker),
        Badge("Monarch Candidate", 40, BadgeRarity.EPIC, "You are now a potential heir to the throne of shadows.", R.drawable.monarch_candidate),
        Badge("Shadow Monarch", 50, BadgeRarity.RARE, "The shadows bow to their new king.", R.drawable.shadow_monarch),
        Badge("Assianite", 60, BadgeRarity.RARE, "A title given to those who mastered the art of hidden strength.", R.drawable.assianite),
        Badge("Night Sentinel", 65, BadgeRarity.EPIC, "Guardian of the eternal night.", R.drawable.night_sentinel),
        Badge("Void Reaper", 70, BadgeRarity.RARE, "Harvesting the essence of the void itself.", R.drawable.void_reaper),
        Badge("Eclipse Sovereign", 80, BadgeRarity.EPIC, "The sun and moon align to honor your power.", R.drawable.eclipse_sovereign),
        Badge("Chaos Executioner", 90, BadgeRarity.LEGENDARY, "Dealer of ultimate justice in the heart of chaos.", R.drawable.chaos_executioner),
        Badge("Silent Killer", 100, BadgeRarity.LEGENDARY, "The final breath of your enemies is the only sound you leave behind.", R.drawable.silent_killer)
    )
}
