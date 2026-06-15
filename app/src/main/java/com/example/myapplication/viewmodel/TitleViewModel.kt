package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FitnessRepository
import com.example.myapplication.model.Title
import com.example.myapplication.model.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TitleViewModel(private val repository: FitnessRepository) : ViewModel() {

    val user: StateFlow<User?> = repository.user
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val titles: StateFlow<List<Title>> = repository.allTitles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun equipTitle(titleName: String) {
        viewModelScope.launch {
            val currentUser = user.value ?: return@launch
            // Toggle or equip
            val newTitle = if (currentUser.activeTitle == titleName) null else titleName
            repository.updateUser(currentUser.copy(activeTitle = newTitle))
        }
    }
}
