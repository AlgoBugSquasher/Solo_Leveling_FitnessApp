package com.example.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "title_table")
data class Title(
    @PrimaryKey val name: String,
    val requiredStreak: Int,
    val isUnlocked: Boolean = false
)

object TitleData {
    val allTitles = listOf(
        Title("Consistent", 3),
        Title("Disciplined Hunter", 7),
        Title("Relentless Warrior", 15),
        Title("Iron Discipline", 30),
        Title("Shadow Disciple", 50),
        Title("Unbreakable Monarch", 100)
    )
}
