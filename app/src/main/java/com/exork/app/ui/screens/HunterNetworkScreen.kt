package com.exork.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.exork.app.ui.theme.*
import com.exork.app.viewmodel.HunterNetworkViewModel
import com.exork.app.viewmodel.HunterProfile
import com.exork.app.viewmodel.NetworkTab
import com.exork.app.viewmodel.NetworkUiEvent
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HunterNetworkScreen(
    viewModel: HunterNetworkViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val allies by viewModel.allies.collectAsState()
    val sentRequestIds by viewModel.sentRequestIds.collectAsState()
    val incomingRequests by viewModel.incomingRequests.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is NetworkUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HUNTER NETWORK", style = ExorkTypography.titleLarge.copy(fontWeight = FontWeight.Black)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFF0E1013)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Tab Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianVoid)
                    .padding(4.dp)
            ) {
                TabItem(
                    text = "FIND HUNTERS",
                    isSelected = currentTab == NetworkTab.FIND,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setTab(NetworkTab.FIND) }
                )
                TabItem(
                    text = "MY ALLIES",
                    badgeCount = incomingRequests.size,
                    isSelected = currentTab == NetworkTab.ALLIES,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setTab(NetworkTab.ALLIES) }
                )
            }

            if (currentTab == NetworkTab.FIND) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search by Hunter Name...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = ElectricCyan) },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = ObsidianVoid,
                        unfocusedContainerColor = ObsidianVoid,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                if (isSearching) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ElectricCyan)
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(searchResults) { hunter ->
                        val isAlly = allies.any { it.userId == hunter.userId }
                        val isRequested = sentRequestIds.contains(hunter.userId)
                        
                        HunterResultCard(
                            hunter = hunter,
                            isAlly = isAlly,
                            isRequested = isRequested
                        ) {
                            if (!isAlly && !isRequested) {
                                viewModel.sendAllyRequest(hunter)
                            }
                        }
                    }
                }
            } else {
                // Allies List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (incomingRequests.isNotEmpty()) {
                        item {
                            Text(
                                "PENDING ALLY REQUESTS",
                                style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = ElectricCyan,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(incomingRequests) { request ->
                            IncomingRequestCard(
                                hunter = request,
                                onAccept = { viewModel.acceptRequest(request) },
                                onDecline = { viewModel.declineRequest(request) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    item {
                        Text(
                            "YOUR ALLIES",
                            style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = TitaniumGray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    if (allies.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Text("NO ALLIES IN YOUR NETWORK", color = Color.Gray)
                            }
                        }
                    }
                    items(allies) { ally ->
                        HunterResultCard(ally, isAlly = true) {}
                    }
                }
            }
        }
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean, modifier: Modifier, badgeCount: Int = 0, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) ElectricCyan else Color.Transparent
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = text,
                style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) Color.Black else Color.Gray
            )
            if (badgeCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = if (isSelected) Color.Black else ElectricCyan,
                    shape = CircleShape,
                    modifier = Modifier.size(18.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = badgeCount.toString(),
                            style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = if (isSelected) ElectricCyan else Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HunterAvatar(hunter: HunterProfile, modifier: Modifier = Modifier) {
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid }
    val photoSource = remember(hunter) {
        hunter.photoUrl.takeIf { !it.isNullOrBlank() } ?: 
        if (hunter.userId == currentUserId) FirebaseAuth.getInstance().currentUser?.photoUrl?.toString() else null
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(ObsidianVoid)
            .border(1.5.dp, ChromeSilver.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!photoSource.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoSource)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = "Hunter Profile Avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = TitaniumGray.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxSize(0.6f)
            )
        }
    }
}

@Composable
fun IncomingRequestCard(hunter: HunterProfile, onAccept: () -> Unit, onDecline: () -> Unit) {
    ExorkNeumorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HunterAvatar(
                hunter = hunter,
                modifier = Modifier.size(50.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = (hunter.username ?: hunter.displayName).uppercase(),
                    style = ExorkTypography.labelLarge.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
                Text(
                    text = "${hunter.hunterRank} • LVL ${hunter.hunterLevel}",
                    style = ExorkTypography.labelSmall,
                    color = TitaniumGray
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onDecline,
                    modifier = Modifier.size(36.dp).background(Color.DarkGray, CircleShape)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = onAccept,
                    modifier = Modifier.size(36.dp).background(ElectricCyan, CircleShape)
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun HunterResultCard(
    hunter: HunterProfile,
    isAlly: Boolean = false,
    isRequested: Boolean = false,
    onAdd: () -> Unit
) {
    ExorkNeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isAlly) Color.Green.copy(alpha = 0.3f) else null
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HunterAvatar(
                hunter = hunter,
                modifier = Modifier.size(50.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = (hunter.username ?: hunter.displayName).uppercase(),
                    style = ExorkTypography.labelLarge.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
                Text(
                    text = "${hunter.hunterRank} • LVL ${hunter.hunterLevel}",
                    style = ExorkTypography.labelSmall,
                    color = TitaniumGray
                )
            }
            
            when {
                isAlly -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, null, tint = Color.Green, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ALLY", color = Color.Green, style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
                isRequested -> {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.Gray),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("PENDING", color = Color.Gray, style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
                else -> {
                    Button(
                        onClick = onAdd,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("ADD ALLY", style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}
