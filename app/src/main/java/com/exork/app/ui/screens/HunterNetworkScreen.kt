package com.exork.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.exork.app.model.HunterProfile
import com.exork.app.ui.components.AvatarImage
import com.exork.app.ui.components.HunterProfileInspectDialog
import com.exork.app.ui.theme.*
import com.exork.app.viewmodel.HunterNetworkViewModel
import com.exork.app.viewmodel.NetworkTab
import com.exork.app.viewmodel.NetworkUiEvent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

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
    val suggestedHunters by viewModel.suggestedHunters.collectAsState()
    val incomingManaCount by viewModel.incomingManaCount.collectAsState()

    val tabs = listOf("FIND HUNTERS", "MY ALLIES")
    val pagerState = rememberPagerState(initialPage = 0) { tabs.size }
    val coroutineScope = rememberCoroutineScope()

    var selectedHunter by remember { mutableStateOf<HunterProfile?>(null) }
    var showInspectDialog by remember { mutableStateOf(false) }
    var showAllyActions by remember { mutableStateOf(false) }
    var showRemoveConfirm by remember { mutableStateOf(false) }

    // Sync ViewModel Tab to Pager
    LaunchedEffect(currentTab) {
        val targetPage = if (currentTab == NetworkTab.FIND) 0 else 1
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    // Sync Pager to ViewModel Tab
    LaunchedEffect(pagerState.currentPage) {
        val targetTab = if (pagerState.currentPage == 0) NetworkTab.FIND else NetworkTab.ALLIES
        if (currentTab != targetTab) {
            viewModel.setTab(targetTab)
        }
    }

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
        containerColor = Color(0xFF0E1013)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0E1013))
        ) {
            // 1. Top Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "HUNTER NETWORK",
                    style = ExorkTypography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
            }

            // 2. Sliding Glassmorphic Tab Switcher
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF0F0F14),
                    border = BorderStroke(1.dp, Color(0xFF22222E))
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(42.dp)
                            .padding(3.dp)
                    ) {
                        val tabWidth = maxWidth / 2
                        val indicatorOffset = tabWidth * (pagerState.currentPage + pagerState.currentPageOffsetFraction)

                        // Smooth Sliding Glow Pill
                        Box(
                            modifier = Modifier
                                .offset(x = indicatorOffset)
                                .width(tabWidth)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(ElectricCyan, NeonBlue)
                                    )
                                )
                        )

                        // Tab Labels Layer
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Find Hunters Tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        coroutineScope.launch { pagerState.animateScrollToPage(0) }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "FIND HUNTERS",
                                    style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (pagerState.currentPage == 0 && pagerState.currentPageOffsetFraction < 0.5f) 
                                        Color(0xFF0A0A0E) else Color.Gray
                                )
                            }

                            // My Allies Tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        coroutineScope.launch { pagerState.animateScrollToPage(1) }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "MY ALLIES",
                                        style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (pagerState.currentPage == 1 || pagerState.currentPageOffsetFraction >= 0.5f) 
                                            Color(0xFF0A0A0E) else Color.Gray
                                    )
                                    if (incomingRequests.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .background(
                                                    if (pagerState.currentPage == 1 || pagerState.currentPageOffsetFraction >= 0.5f)
                                                        Color.Black else ElectricCyan,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = incomingRequests.size.toString(),
                                                style = ExorkTypography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                                                color = if (pagerState.currentPage == 1 || pagerState.currentPageOffsetFraction >= 0.5f)
                                                    ElectricCyan else Color.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Mana Infusion Banner
            AnimatedVisibility(visible = incomingManaCount > 0) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { viewModel.claimMana() },
                    color = ElectricCyan.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, null, tint = ElectricCyan)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "MANA INFUSION DETECTED",
                                style = ExorkTypography.labelLarge.copy(fontWeight = FontWeight.Black),
                                color = ElectricCyan
                            )
                        }
                        Text(
                            "CLAIM +${incomingManaCount * 10} XP",
                            style = ExorkTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            // 3. Horizontal Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                if (page == 0) {
                    // FIND TAB CONTENT
                    Column(modifier = Modifier.fillMaxSize()) {
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
                            if (searchQuery.isBlank()) {
                                item {
                                    Text(
                                        "ACTIVE HUNTERS IN REALM",
                                        style = ExorkTypography.labelMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                                        color = TitaniumGray,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                
                                val huntersToShow = if (suggestedHunters.isEmpty() && isSearching) emptyList() else suggestedHunters
                                items(huntersToShow) { hunter ->
                                    val isAlly = allies.any { it.userId == hunter.userId }
                                    val isRequested = sentRequestIds.contains(hunter.userId)
                                    
                                    HunterResultCard(
                                        hunter = hunter,
                                        isAlly = isAlly,
                                        isRequested = isRequested,
                                        onClick = {
                                            selectedHunter = hunter
                                            showInspectDialog = true
                                        },
                                        onSendMana = { viewModel.sendMana(hunter) }
                                    ) {
                                        if (!isAlly && !isRequested) {
                                            viewModel.sendAllyRequest(hunter)
                                        }
                                    }
                                }
                            } else {
                                items(searchResults) { hunter ->
                                    val isAlly = allies.any { it.userId == hunter.userId }
                                    val isRequested = sentRequestIds.contains(hunter.userId)
                                    
                                    HunterResultCard(
                                        hunter = hunter,
                                        isAlly = isAlly,
                                        isRequested = isRequested,
                                        onClick = {
                                            selectedHunter = hunter
                                            showInspectDialog = true
                                        },
                                        onSendMana = { viewModel.sendMana(hunter) }
                                    ) {
                                        if (!isAlly && !isRequested) {
                                            viewModel.sendAllyRequest(hunter)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ALLIES TAB CONTENT
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
                            HunterResultCard(
                                hunter = ally, 
                                isAlly = true, 
                                onClick = {
                                    selectedHunter = ally
                                    showAllyActions = true
                                },
                                onSendMana = { viewModel.sendMana(ally) }
                            ) {}
                        }
                    }
                }
            }
        }

        // Ally Actions Bottom Sheet
        if (showAllyActions && selectedHunter != null) {
            ModalBottomSheet(
                onDismissRequest = { showAllyActions = false },
                containerColor = LeatherDark,
                dragHandle = { BottomSheetDefaults.DragHandle(color = ChromeSilver.copy(alpha = 0.4f)) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .padding(bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        (selectedHunter!!.username ?: selectedHunter!!.displayName).uppercase(),
                        style = ExorkTypography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            showAllyActions = false
                            showInspectDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ChromeSilver, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("VIEW DOSSIER", style = ExorkTypography.labelLarge.copy(fontWeight = FontWeight.Black))
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = {
                            showAllyActions = false
                            showRemoveConfirm = true
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("REMOVE ALLY", color = Color.Red, style = ExorkTypography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ExorkChromeButton(
                        text = "SEND MANA (+10 XP)",
                        height = 56.dp,
                        onClick = {
                            showAllyActions = false
                            viewModel.sendMana(selectedHunter!!)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Inspect Dialog
        if (showInspectDialog && selectedHunter != null) {
            val hunter = selectedHunter!!
            val isAlly = allies.any { it.userId == hunter.userId }
            val isRequested = sentRequestIds.contains(hunter.userId)

            HunterProfileInspectDialog(
                profile = hunter,
                onDismiss = { showInspectDialog = false }
            ) {
                // Action Button inside dialog - CONDITIONAL LOGIC
                if (hunter.userId != viewModel.currentUserId && !isAlly) {
                    if (isRequested) {
                        // Subtle disabled "REQUEST PENDING" pill
                        Surface(
                            modifier = Modifier.height(44.dp).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = ObsidianVoid.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("REQUEST PENDING", style = ExorkTypography.labelLarge, color = Color.Gray)
                            }
                        }
                    } else {
                        // Compact "SEND ALLY REQUEST" button
                        ExorkChromeButton(
                            text = "SEND ALLY REQUEST", 
                            height = 44.dp,
                            onClick = {
                                viewModel.sendAllyRequest(hunter)
                            }, 
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Remove Confirmation
        if (showRemoveConfirm && selectedHunter != null) {
            AlertDialog(
                onDismissRequest = { showRemoveConfirm = false },
                containerColor = ObsidianVoid,
                title = { Text("REMOVE ALLY?", color = Color.White, fontWeight = FontWeight.Black) },
                text = { Text("Are you sure you want to remove ${selectedHunter!!.username ?: selectedHunter!!.displayName} from your network?", color = TitaniumGray) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.removeAlly(selectedHunter!!)
                        showRemoveConfirm = false
                    }) {
                        Text("CONFIRM", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRemoveConfirm = false }) {
                        Text("CANCEL", color = ChromeSilver)
                    }
                }
            )
        }
    }
}

@Composable
fun HunterAvatar(hunter: HunterProfile, modifier: Modifier = Modifier) {
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid }
    val photoSource = remember(hunter.photoUrl, hunter.userId) {
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
        AvatarImage(
            avatarData = photoSource,
            modifier = Modifier.fillMaxSize(),
            placeholderSize = 32.dp
        )
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
                    Icon(Icons.Default.Check, null, tint = MonarchSlate, modifier = Modifier.size(18.dp))
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
    onClick: () -> Unit = {},
    onSendMana: () -> Unit = {},
    onAdd: () -> Unit
) {
    ExorkNeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isAlly) Color.Green.copy(alpha = 0.3f) else null,
        onClick = onClick
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
                    IconButton(
                        onClick = onSendMana,
                        modifier = Modifier.size(40.dp).background(ElectricCyan.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Bolt, "Send Mana", tint = ElectricCyan, modifier = Modifier.size(20.dp))
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
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = MonarchSlate),
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
