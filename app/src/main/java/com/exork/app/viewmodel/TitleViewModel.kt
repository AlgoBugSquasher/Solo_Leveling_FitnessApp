package com.exork.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exork.app.data.FitnessRepository
import com.exork.app.model.Title
import com.exork.app.model.User
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
