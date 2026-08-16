package com.exork.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exork.app.data.FitnessRepository
import com.exork.app.model.Guild
import com.exork.app.model.GuildMember
import com.exork.app.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class GuildUiEvent {
    data class ShowToast(val message: String) : GuildUiEvent()
    data class NavigateToGuild(val guildId: String) : GuildUiEvent()
}

class GuildViewModel(private val repository: FitnessRepository) : ViewModel() {

    val user: StateFlow<User?> = repository.user
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _currentGuild = MutableStateFlow<Guild?>(null)
    val currentGuild: StateFlow<Guild?> = _currentGuild.asStateFlow()

    private val _guildMembers = MutableStateFlow<List<GuildMember>>(emptyList())
    val guildMembers: StateFlow<List<GuildMember>> = _guildMembers.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Guild>>(emptyList())
    val searchResults: StateFlow<List<Guild>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _uiEvent = MutableSharedFlow<GuildUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var guildJob: Job? = null
    private var searchJob: Job? = null

    init {
        loadDiscoverGuilds()
        viewModelScope.launch {
            user.collectLatest { u ->
                var gid = u?.guildId
                
                // Fallback check: if user.guildId is null, try to recover from Firestore
                if (gid == null) {
                    val auth = FirebaseAuth.getInstance()
                    val currentUserId = auth.currentUser?.uid
                    if (currentUserId != null) {
                        try {
                            val db = FirebaseFirestore.getInstance()
                            // Check if user document in Firestore has guildId
                            val userDoc = db.collection("users").document(currentUserId).get().await()
                            val recoveredGid = userDoc.getString("guildId")
                            if (recoveredGid != null) {
                                gid = recoveredGid
                                // Update local user with recovered guildId
                                u?.let { repository.updateUser(it.copy(
                                    guildId = recoveredGid,
                                    guildName = userDoc.getString("guildName"),
                                    guildTag = userDoc.getString("guildTag")
                                )) }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("GuildViewModel", "Guild recovery failed", e)
                        }
                    }
                }

                guildJob?.cancel()
                if (gid != null) {
                    guildJob = viewModelScope.launch {
                        combine(
                            repository.getGuildFlow(gid),
                            repository.getGuildMembersFlow(gid)
                        ) { guild, members ->
                            _currentGuild.value = guild
                            _guildMembers.value = members
                        }.collect()
                    }
                } else {
                    _currentGuild.value = null
                    _guildMembers.value = emptyList()
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            loadDiscoverGuilds()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            _isSearching.value = true
            delay(500)
            _searchResults.value = repository.searchGuilds(query)
            _isSearching.value = false
        }
    }

    private var discoverJob: Job? = null
    private fun loadDiscoverGuilds() {
        discoverJob?.cancel()
        discoverJob = viewModelScope.launch {
            _isSearching.value = true
            repository.getPublicGuilds().collectLatest { results ->
                if (_searchQuery.value.isBlank()) {
                    _searchResults.value = results
                    _isSearching.value = false
                }
            }
        }
    }

    suspend fun getHunterProfile(userId: String): com.exork.app.model.HunterProfile? {
        return repository.getHunterProfile(userId)
    }

    fun createGuild(name: String, tag: String, badgeIcon: String) {
        viewModelScope.launch {
            val result = repository.createGuild(name, tag, badgeIcon)
            if (result.isSuccess) {
                _uiEvent.emit(GuildUiEvent.ShowToast("Guild '$name' Created!"))
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to create guild."
                _uiEvent.emit(GuildUiEvent.ShowToast(error))
            }
        }
    }

    fun joinGuild(guild: Guild) {
        viewModelScope.launch {
            val result = repository.joinGuild(guild.id)
            if (result.isSuccess) {
                _uiEvent.emit(GuildUiEvent.ShowToast("Joined ${guild.name}!"))
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to join guild."
                _uiEvent.emit(GuildUiEvent.ShowToast(error))
            }
        }
    }

    fun leaveGuild() {
        val gid = user.value?.guildId ?: return
        viewModelScope.launch {
            val success = repository.leaveGuild(gid)
            if (success) {
                _uiEvent.emit(GuildUiEvent.ShowToast("Left the guild."))
            } else {
                _uiEvent.emit(GuildUiEvent.ShowToast("Master cannot leave. Disband the guild instead."))
            }
        }
    }

    fun disbandGuild() {
        val gid = user.value?.guildId ?: return
        viewModelScope.launch {
            val result = repository.disbandGuild(gid)
            if (result.isSuccess) {
                _uiEvent.emit(GuildUiEvent.ShowToast("Guild disbanded."))
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to disband guild."
                _uiEvent.emit(GuildUiEvent.ShowToast(error))
            }
        }
    }

    fun updateNotice(newNotice: String) {
        val gid = user.value?.guildId ?: return
        viewModelScope.launch {
            val result = repository.updateGuildNotice(gid, newNotice)
            if (result.isSuccess) {
                _uiEvent.emit(GuildUiEvent.ShowToast("Guild notice updated!"))
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to update notice."
                _uiEvent.emit(GuildUiEvent.ShowToast(error))
            }
        }
    }

    fun kickMember(memberId: String) {
        val gid = user.value?.guildId ?: return
        viewModelScope.launch {
            val result = repository.kickMember(gid, memberId)
            if (result.isSuccess) {
                _uiEvent.emit(GuildUiEvent.ShowToast("Member kicked from guild."))
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to kick member."
                _uiEvent.emit(GuildUiEvent.ShowToast(error))
            }
        }
    }
}
