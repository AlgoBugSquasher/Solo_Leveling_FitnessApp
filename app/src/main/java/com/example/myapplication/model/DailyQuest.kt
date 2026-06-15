package com.example.myapplication.model

data class DailyQuest(
    val id: Int,
    val title: String,
    val goal: String,
    val xpReward: Int,
    val isCompleted: Boolean = false
)
