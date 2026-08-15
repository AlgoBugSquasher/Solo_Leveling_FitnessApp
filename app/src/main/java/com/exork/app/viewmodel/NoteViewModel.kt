package com.exork.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exork.app.data.FitnessRepository
import com.exork.app.model.Note
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModel(private val repository: FitnessRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private var notesListener: com.google.firebase.firestore.ListenerRegistration? = null

    init {
        startRealTimeNotesListener()
    }

    private fun startRealTimeNotesListener() {
        val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        
        notesListener = db.collection("users").document(firebaseUser.uid)
            .collection("notes")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                
                val remoteNotes = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Note::class.java)
                }

                viewModelScope.launch {
                    val existingNotes = repository.allNotes.first()
                    
                    // 1. Sync remote additions/updates to local
                    remoteNotes.forEach { remoteNote ->
                        val localMatch = existingNotes.find { it.id == remoteNote.id }
                        if (localMatch == null || localMatch != remoteNote) {
                            repository.insertNoteLocal(remoteNote)
                        }
                    }
                    
                    // 2. Sync remote deletions to local
                    existingNotes.forEach { localNote ->
                        if (remoteNotes.none { it.id == localNote.id }) {
                            repository.deleteNoteLocal(localNote)
                        }
                    }
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        notesListener?.remove()
    }

    val notes: StateFlow<List<Note>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                repository.allNotes
            } else {
                repository.searchNotes(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            repository.insertNote(Note(title = title, content = content))
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }
}
