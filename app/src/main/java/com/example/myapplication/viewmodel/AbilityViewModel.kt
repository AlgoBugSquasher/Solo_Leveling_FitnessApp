package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FitnessRepository
import com.example.myapplication.model.Ability
import com.example.myapplication.model.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AbilityViewModel(private val repository: FitnessRepository) : ViewModel() {

    val user: StateFlow<User?> = repository.user
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val abilities: StateFlow<List<Ability>> = repository.abilities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        seedAbilities()
    }

    private fun seedAbilities() {
        viewModelScope.launch {
            val list = repository.abilities.first()
            if (list.isEmpty()) {
                val initialAbilities = listOf(
                    Ability("Handstand", requiredPushups = 300, requiredPlankTime = 120, requiredStreak = 3),
                    Ability("HSPU", requiredLevel = 5),
                    Ability("Planche", requiredPushups = 500, requiredLevel = 7),
                    Ability("Front Lever", requiredPullups = 100, requiredLevel = 6),
                    Ability("Muscle-Up", requiredPullups = 150, requiredLevel = 8)
                )
                repository.updateAbilities(initialAbilities)
            }
        }
    }

    fun checkAndUnlockAbilities(user: User) {
        viewModelScope.launch {
            val currentAbilities = abilities.value
            val updatedAbilities = currentAbilities.map { ability ->
                if (!ability.isUnlocked && meetsConditions(ability, user, currentAbilities)) {
                    ability.copy(isUnlocked = true)
                } else {
                    ability
                }
            }
            if (updatedAbilities != currentAbilities) {
                repository.updateAbilities(updatedAbilities)
            }
        }
    }

    private fun meetsConditions(ability: Ability, user: User, allAbilities: List<Ability>): Boolean {
        val baseStatsMet = user.pushups >= ability.requiredPushups &&
                user.pullups >= ability.requiredPullups &&
                user.plankTime >= ability.requiredPlankTime &&
                user.level >= ability.requiredLevel &&
                user.streak >= ability.requiredStreak

        val dependenciesMet = when (ability.name) {
            "HSPU" -> allAbilities.find { it.name == "Handstand" }?.isUnlocked == true
            else -> true
        }

        return baseStatsMet && dependenciesMet
    }
}
