package com.exork.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.exork.app.model.Note
import com.exork.app.ui.theme.*
import com.exork.app.util.SoundManager
import com.exork.app.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HunterNotesScreen(
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }

    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var isEditing by remember { mutableStateOf(false) }

    ExorkTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("HUNTER NOTES", style = ExorkTypography.headlineMedium, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = {
                            soundManager.playClick()
                            onNavigateBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = ObsidianVoid,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        soundManager.playClick()
                        selectedNote = Note(title = "", content = "")
                        isEditing = true
                    },
                    containerColor = ChromeSilver,
                    contentColor = ObsidianVoid,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Note")
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        placeholder = { Text("Search notes...", color = TitaniumGray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ChromeSilver) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChromeSilver,
                            unfocusedBorderColor = ChromeSilver.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = ChromeSilver
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    if (notes.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.HistoryEdu,
                                    contentDescription = null,
                                    tint = TitaniumGray.copy(alpha = 0.3f),
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Your notebook is empty.",
                                    style = ExorkTypography.bodyLarge,
                                    color = TitaniumGray,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    "Write today's hunter thoughts...",
                                    style = ExorkTypography.labelMedium,
                                    color = TitaniumGray.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 120.dp)
                        ) {
                            items(notes) { note ->
                                NoteItem(
                                    note = note,
                                    onClick = {
                                        soundManager.playClick()
                                        selectedNote = note
                                        isEditing = true
                                    },
                                    onDelete = {
                                        viewModel.deleteNote(note)
                                        soundManager.playClick()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isEditing && selectedNote != null) {
            NoteEditorDialog(
                note = selectedNote!!,
                onDismiss = { isEditing = false },
                onSave = { updatedNote ->
                    if (updatedNote.title.isNotBlank() || updatedNote.content.isNotBlank()) {
                        if (updatedNote.id == 0) {
                            viewModel.addNote(updatedNote.title, updatedNote.content)
                        } else {
                            viewModel.updateNote(updatedNote)
                        }
                    }
                    isEditing = false
                }
            )
        }
    }
}

@Composable
fun NoteItem(note: Note, onClick: () -> Unit, onDelete: () -> Unit) {
    ExorkNeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (note.title.isBlank()) "Untitled" else note.title,
                    style = ExorkTypography.titleLarge,
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray.copy(alpha = 0.5f))
                }
            }
            
            Text(
                text = note.content,
                style = ExorkTypography.bodyLarge,
                color = TitaniumGray,
                maxLines = 3,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            val dateStr = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(note.timestamp))
            Text(
                text = dateStr,
                style = ExorkTypography.labelMedium,
                color = ChromeSilver.copy(alpha = 0.6f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorDialog(
    note: Note,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit
) {
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }

    Dialog(
        onDismissRequest = { 
            onSave(note.copy(title = title, content = content, timestamp = System.currentTimeMillis()))
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = ObsidianVoid
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { 
                        onSave(note.copy(title = title, content = content, timestamp = System.currentTimeMillis()))
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                    
                    Text("HUNTER NOTE", style = ExorkTypography.labelLarge, color = ChromeSilver)
                    
                    TextButton(onClick = {
                        onSave(note.copy(title = title, content = content, timestamp = System.currentTimeMillis()))
                    }) {
                        Text("SAVE", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }

                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Title", style = ExorkTypography.headlineMedium, color = TitaniumGray) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = ExorkTypography.headlineMedium.copy(color = Color.White),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = ChromeSilver
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("Write today's hunter thoughts...", style = ExorkTypography.bodyLarge, color = TitaniumGray) },
                    modifier = Modifier.fillMaxSize().weight(1f),
                    textStyle = ExorkTypography.bodyLarge.copy(color = Color.White),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = ChromeSilver
                    )
                )
            }
        }
    }
}
