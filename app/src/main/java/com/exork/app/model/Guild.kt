package com.exork.app.model

data class Guild(
    val id: String = "",
    val name: String = "",
    val tag: String = "",
    val masterId: String = "",
    val masterName: String = "",
    val memberCount: Int = 1,
    val maxMembers: Int = 10,
    val totalGuildXp: Long = 0L,
    val memberIds: List<String> = emptyList(),
    val notice: String = "Welcome Hunters to our Guild!",
    val badgeIcon: String = "⚔️"
)

data class GuildMember(
    val userId: String = "",
    val username: String = "",
    val rank: String = "E-Rank Hunter",
    val level: Int = 1,
    val role: String = "MEMBER",
    val totalXp: Int = 0,
    val weeklyXp: Int = 0,
    val photoUrl: String = ""
)
