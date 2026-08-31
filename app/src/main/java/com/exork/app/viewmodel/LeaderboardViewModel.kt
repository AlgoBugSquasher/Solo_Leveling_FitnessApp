package com.exork.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exork.app.model.HunterProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LeaderboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    private val _topHunters = MutableStateFlow<List<HunterProfile>>(emptyList())
    val topHunters: StateFlow<List<HunterProfile>> = _topHunters.asStateFlow()

    private val _currentUserRank = MutableStateFlow<Int?>(null)
    val currentUserRank: StateFlow<Int?> = _currentUserRank.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<HunterProfile?>(null)
    val currentUserProfile: StateFlow<HunterProfile?> = _currentUserProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        startLeaderboardListener()
    }

    private fun startLeaderboardListener() {
        _isLoading.value = true
        val currentUserId = auth.currentUser?.uid
        
        listenerRegistration = db.collection("users")
            .orderBy("totalXp", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    android.util.Log.e("LeaderboardViewModel", "Listen failed", e)
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val hunters = snapshot.documents.mapNotNull { doc ->
                        val profile = doc.toObject(HunterProfile::class.java) ?: HunterProfile()
                        val photo = doc.getString("photoUrl") 
                            ?: doc.getString("profilePicture") 
                            ?: doc.getString("avatarUrl") 
                            ?: doc.getString("photo_url") 
                            ?: profile.photoUrl
                        
                        android.util.Log.d("LeaderboardAvatar", "User: ${doc.getString("username")}, photoUrl: $photo")
                        
                        profile.copy(
                            userId = doc.id,
                            hunterLevel = doc.getLong("hunterLevel")?.toInt() ?: profile.hunterLevel,
                            totalXp = doc.getLong("totalXp")?.toInt() ?: profile.totalXp,
                            hunterRank = doc.getString("hunterRank") ?: profile.hunterRank,
                            photoUrl = photo
                        )
                    }
                    android.util.Log.d("LeaderboardDebug", "Fetched users count: ${hunters.size}")
                    _topHunters.value = hunters
                    
                    if (currentUserId != null) {
                        val index = hunters.indexOfFirst { it.userId == currentUserId }
                        if (index != -1) {
                            _currentUserRank.value = index + 1
                            _currentUserProfile.value = hunters[index]
                        } else {
                            // If not in top 100, fetch user's doc and rank count
                        viewModelScope.launch {
                            try {
                                val userDoc = db.collection("users").document(currentUserId).get().await()
                                val profile = userDoc.toObject(HunterProfile::class.java) ?: HunterProfile()
                                val photo = userDoc.getString("photoUrl") 
                                    ?: userDoc.getString("profilePicture") 
                                    ?: userDoc.getString("avatarUrl") 
                                    ?: userDoc.getString("photo_url") 
                                    ?: profile.photoUrl
                                    
                                val finalProfile = profile.copy(
                                    userId = userDoc.id,
                                    hunterLevel = userDoc.getLong("hunterLevel")?.toInt() ?: profile.hunterLevel,
                                    totalXp = userDoc.getLong("totalXp")?.toInt() ?: profile.totalXp,
                                    photoUrl = photo
                                )
                                _currentUserProfile.value = finalProfile
                                
                                if (finalProfile != null) {
                                    val countSnapshot = db.collection("users")
                                        .whereGreaterThan("totalXp", finalProfile.totalXp)
                                        .get()
                                        .await()
                                    _currentUserRank.value = countSnapshot.size() + 1
                                }
                            } catch (ex: Exception) {
                                android.util.Log.e("LeaderboardViewModel", "User rank fetch failed", ex)
                            }
                        }
                        }
                    }
                }
                _isLoading.value = false
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
