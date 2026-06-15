package com.example.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ability_table")
data class Ability(
    @PrimaryKey val name: String,
    val isUnlocked: Boolean = false,
    val requiredPushups: Int = 0,
    val requiredPullups: Int = 0,
    val requiredPlankTime: Int = 0, // in seconds
    val requiredLevel: Int = 0,
    val requiredStreak: Int = 0
)
