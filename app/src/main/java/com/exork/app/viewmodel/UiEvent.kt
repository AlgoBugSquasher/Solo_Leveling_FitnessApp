package com.exork.app.viewmodel

import com.exork.app.model.Ability
import com.exork.app.model.Achievement
import com.exork.app.model.Badge
import com.exork.app.model.Title

/**
 * Events for the UI to handle, such as showing animations or dialogs.
 */
sealed class UiEvent {
    data class LevelUp(val oldLevel: Int, val newLevel: Int) : UiEvent()
    data class AbilityUnlocked(val ability: Ability) : UiEvent()
    data class TitleUnlocked(val title: Title) : UiEvent()
    data class BadgeUnlocked(val badge: Badge) : UiEvent()
    data class AchievementUnlocked(val achievement: Achievement) : UiEvent()
    data class NewPersonalRecord(val recordName: String, val oldValue: Int, val newValue: Int) : UiEvent()
    data class RankPromotion(val oldRank: String, val newRank: String) : UiEvent()
    data class XpGained(val amount: Int) : UiEvent()
    data object PenaltyTriggered : UiEvent()
    data class BackupSuccess(val message: String) : UiEvent()
    data class BackupError(val message: String) : UiEvent()
    data object Logout : UiEvent()
    data object DeletionCancelled : UiEvent()
    data object RequestReview : UiEvent()
}
