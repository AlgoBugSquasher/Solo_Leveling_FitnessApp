package com.exork.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exork.app.data.FitnessRepository
import com.exork.app.model.HunterProfile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class NetworkTab { FIND, ALLIES }

sealed class NetworkUiEvent {
    data class ShowToast(val message: String) : NetworkUiEvent()
}

class HunterNetworkViewModel(private val repository: FitnessRepository) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    val currentUserId: String? = auth.currentUser?.uid

    private val _currentTab = MutableStateFlow(NetworkTab.FIND)
    val currentTab: StateFlow<NetworkTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<HunterProfile>>(emptyList())
    val searchResults: StateFlow<List<HunterProfile>> = _searchResults.asStateFlow()

    private val _allies = MutableStateFlow<List<HunterProfile>>(emptyList())
    val allies: StateFlow<List<HunterProfile>> = _allies.asStateFlow()

    private val _sentRequestIds = MutableStateFlow<List<String>>(emptyList())
    val sentRequestIds: StateFlow<List<String>> = _sentRequestIds.asStateFlow()

    private val _incomingRequests = MutableStateFlow<List<HunterProfile>>(emptyList())
    val incomingRequests: StateFlow<List<HunterProfile>> = _incomingRequests.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _suggestedHunters = MutableStateFlow<List<HunterProfile>>(emptyList())
    val suggestedHunters: StateFlow<List<HunterProfile>> = _suggestedHunters.asStateFlow()

    private val _incomingManaCount = MutableStateFlow(0)
    val incomingManaCount: StateFlow<Int> = _incomingManaCount.asStateFlow()

    private val _uiEvent = MutableSharedFlow<NetworkUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var searchJob: Job? = null

    private val _currentUserProfile = MutableStateFlow<HunterProfile?>(null)

    init {
        viewModelScope.launch {
            val uid = currentUserId
            if (uid != null) {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                try {
                    val doc = db.collection("users").document(uid).get().await()
                    _currentUserProfile.value = doc.toObject(HunterProfile::class.java)
                } catch (e: Exception) {
                    android.util.Log.e("HunterNetworkViewModel", "Failed to fetch current user profile", e)
                }
            }
        }
        viewModelScope.launch {
            repository.getAlliesFlow().collectLatest {
                _allies.value = it
            }
        }
        viewModelScope.launch {
            repository.getSentRequestsFlow().collectLatest {
                _sentRequestIds.value = it
            }
        }
        viewModelScope.launch {
            repository.getIncomingRequestsFlow().collectLatest {
                _incomingRequests.value = it
            }
        }
        
        startManaListener()
        fetchSuggestedHunters()
    }

    private fun fetchSuggestedHunters() {
        viewModelScope.launch {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            try {
                val snapshot = db.collection("users")
                    .orderBy("totalXp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(40)
                    .get()
                    .await()
                
                val hunters = snapshot.documents.mapNotNull { doc ->
                    val profile = doc.toObject(HunterProfile::class.java) ?: HunterProfile()
                    val photo = doc.getString("photoUrl") 
                        ?: doc.getString("profilePicture") 
                        ?: doc.getString("avatarUrl") 
                        ?: profile.photoUrl
                    
                    profile.copy(
                        userId = doc.id,
                        hunterLevel = doc.getLong("hunterLevel")?.toInt() ?: profile.hunterLevel,
                        totalXp = doc.getLong("totalXp")?.toInt() ?: profile.totalXp,
                        hunterRank = doc.getString("hunterRank") ?: profile.hunterRank,
                        photoUrl = photo
                    )
                }.filter { it.userId != currentUserId }.shuffled().take(20)
                
                _suggestedHunters.value = hunters
            } catch (e: Exception) {
                android.util.Log.e("HunterNetworkViewModel", "Failed to fetch suggestions", e)
            }
        }
    }

    private var manaListener: com.google.firebase.firestore.ListenerRegistration? = null

    private fun startManaListener() {
        val uid = currentUserId ?: return
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        manaListener = db.collection("users").document(uid)
            .collection("incoming_mana")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                _incomingManaCount.value = snapshot?.size() ?: 0
            }
    }

    override fun onCleared() {
        super.onCleared()
        manaListener?.remove()
    }

    fun setTab(tab: NetworkTab) {
        _currentTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            _isSearching.value = true
            delay(500) // Debounce
            val results = repository.searchHunters(query)
            results.forEach { hunter ->
                android.util.Log.d("SEARCH_BUG", "Emitted User: ${hunter.username}, Level: ${hunter.hunterLevel}, Photo: ${hunter.photoUrl}")
            }
            // Filter out current user
            _searchResults.value = results.filter { it.userId != currentUserId }
            _isSearching.value = false
        }
    }

    fun sendAllyRequest(hunter: HunterProfile) {
        viewModelScope.launch {
            val fromUsername = _currentUserProfile.value?.username ?: auth.currentUser?.displayName ?: "Hunter"
            repository.sendAllyRequest(hunter.userId, fromUsername)
            _uiEvent.emit(NetworkUiEvent.ShowToast("Ally Request Sent to ${hunter.username ?: hunter.displayName}!"))
        }
    }

    fun acceptRequest(hunter: HunterProfile) {
        viewModelScope.launch {
            repository.acceptAllyRequest(hunter.userId)
            _uiEvent.emit(NetworkUiEvent.ShowToast("Accepted request from ${hunter.username ?: hunter.displayName}!"))
        }
    }

    fun declineRequest(hunter: HunterProfile) {
        viewModelScope.launch {
            repository.declineAllyRequest(hunter.userId)
            _uiEvent.emit(NetworkUiEvent.ShowToast("Declined request."))
        }
    }

    fun removeAlly(hunter: HunterProfile) {
        viewModelScope.launch {
            repository.removeAlly(hunter.userId)
            _uiEvent.emit(NetworkUiEvent.ShowToast("Removed ${hunter.username ?: hunter.displayName} from allies."))
        }
    }

    fun sendMana(hunter: HunterProfile) {
        viewModelScope.launch {
            val fromUsername = _currentUserProfile.value?.username ?: auth.currentUser?.displayName ?: "Hunter"
            val success = repository.sendManaToAlly(hunter.userId, fromUsername)
            if (success) {
                _uiEvent.emit(NetworkUiEvent.ShowToast("Mana Sent! (+10 XP to Ally)"))
            } else {
                _uiEvent.emit(NetworkUiEvent.ShowToast("You have already sent Mana to this Hunter today."))
            }
        }
    }

    fun claimMana() {
        viewModelScope.launch {
            val amount = repository.claimIncomingMana()
            if (amount > 0) {
                _uiEvent.emit(NetworkUiEvent.ShowToast("Mana Infusion! Gained +$amount XP from Allies."))
            }
        }
    }
}
