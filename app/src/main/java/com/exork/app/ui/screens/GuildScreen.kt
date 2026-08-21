package com.exork.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.exork.app.model.Guild
import com.exork.app.model.GuildMember
import com.exork.app.ui.theme.*
import com.exork.app.ui.theme.parseAvatarToBitmap
import com.exork.app.viewmodel.GuildUiEvent
import com.exork.app.viewmodel.GuildViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuildScreen(
    viewModel: GuildViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit
) {
    val context = LocalContext.current
    val currentGuild by viewModel.currentGuild.collectAsState()
    
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showDisbandConfirm by remember { mutableStateOf(false) }
    var showEditNoticeDialog by remember { mutableStateOf(false) }
    var selectedMemberForAction by remember { mutableStateOf<GuildMember?>(null) }
    var memberToKick by remember { mutableStateOf<GuildMember?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is GuildUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    Scaffold(
        containerColor = ObsidianVoid
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianVoid)
                .statusBarsPadding()
        ) {
            // 1. Top Back + Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    coroutineScope.launch {
                        onNavigateBack()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ChromeSilver
                    )
                }
                Text(
                    text = "HUNTER GUILD",
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
                            .fillMaxWidth()
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

                        // Tab Labels
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Page 0: MY GUILD
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
                                    text = "MY GUILD",
                                    style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = if (pagerState.currentPage == 0 && pagerState.currentPageOffsetFraction < 0.5f) 
                                        Color(0xFF0A0A0E) else TitaniumGray
                                )
                            }

                            // Page 1: BROWSE GUILDS
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
                                Text(
                                    text = "BROWSE GUILDS",
                                    style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = if (pagerState.currentPage == 1 || pagerState.currentPageOffsetFraction >= 0.5f) 
                                        Color(0xFF0A0A0E) else TitaniumGray
                                )
                            }
                        }
                    }
                }
            }

            // 3. Horizontal Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> MyGuildPageContent(
                        viewModel = viewModel,
                        onEditNotice = { showEditNoticeDialog = true },
                        onDisband = { showDisbandConfirm = true },
                        onMemberClick = { selectedMemberForAction = it }
                    )
                    1 -> BrowseGuildsPageContent(
                        viewModel = viewModel,
                        onCreateNew = { showCreateDialog = true }
                    )
                }
            }
        }
    }

    if (showDisbandConfirm) {
        AlertDialog(
            onDismissRequest = { showDisbandConfirm = false },
            containerColor = ObsidianVoid,
            title = { Text("DISBAND GUILD?", color = Color.White, fontWeight = FontWeight.Black) },
            text = { Text("This will permanently dissolve the guild and remove all members. This action cannot be undone.", color = TitaniumGray) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.disbandGuild()
                    showDisbandConfirm = false
                }) {
                    Text("DISBAND", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisbandConfirm = false }) {
                    Text("CANCEL", color = ChromeSilver)
                }
            }
        )
    }

    if (selectedMemberForAction != null && currentGuild != null) {
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        val isCurrentUserLeader = currentUserId == currentGuild!!.masterId
        val masterId = currentGuild!!.masterId

        Dialog(onDismissRequest = { selectedMemberForAction = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF111118),
                border = BorderStroke(1.dp, Color(0xFF262638)),
                modifier = Modifier.width(250.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Member Name Header
                    Text(
                        text = selectedMemberForAction?.username?.uppercase() ?: "HUNTER",
                        style = ExorkTypography.labelLarge.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )

                    HorizontalDivider(color = Color(0xFF222230), thickness = 1.dp, modifier = Modifier.padding(vertical = 2.dp))

                    // 1. VIEW PROFILE
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF181824),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val memberId = selectedMemberForAction?.userId
                                selectedMemberForAction = null
                                if (memberId != null) {
                                    onNavigateToUserProfile(memberId)
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "VIEW PROFILE",
                                style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElectricCyan
                            )
                        }
                    }

                    // 2. KICK FROM GUILD (Only for Leader)
                    if (isCurrentUserLeader && selectedMemberForAction?.userId != masterId) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x22FF3344),
                            border = BorderStroke(1.dp, Color(0x66FF3344)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    memberToKick = selectedMemberForAction
                                    selectedMemberForAction = null
                                }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "KICK FROM GUILD",
                                    style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFF4455)
                                )
                            }
                        }
                    }

                    // 3. CANCEL / CLOSE
                    Text(
                        text = "CANCEL",
                        style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TitaniumGray,
                        modifier = Modifier
                            .clickable { selectedMemberForAction = null }
                            .padding(top = 4.dp, bottom = 2.dp)
                    )
                }
            }
        }
    }

    if (memberToKick != null) {
        AlertDialog(
            onDismissRequest = { memberToKick = null },
            containerColor = ObsidianVoid,
            title = { Text("KICK MEMBER?", color = Color.White, fontWeight = FontWeight.Black) },
            text = { Text("Are you sure you want to remove ${memberToKick?.username} from the guild?", color = TitaniumGray) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.kickMember(memberToKick!!.userId)
                    memberToKick = null
                }) {
                    Text("KICK", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToKick = null }) {
                    Text("CANCEL", color = ChromeSilver)
                }
            }
        )
    }

    if (showCreateDialog) {
        CreateGuildDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, tag, icon ->
                viewModel.createGuild(name, tag, icon)
                showCreateDialog = false
            }
        )
    }

    if (showEditNoticeDialog && currentGuild != null) {
        EditNoticeDialog(
            currentNotice = currentGuild!!.notice,
            onDismiss = { showEditNoticeDialog = false },
            onSave = { 
                viewModel.updateNotice(it)
                showEditNoticeDialog = false
            }
        )
    }
}

@Composable
fun MyGuildPageContent(
    viewModel: GuildViewModel,
    onEditNotice: () -> Unit,
    onDisband: () -> Unit,
    onMemberClick: (GuildMember) -> Unit
) {
    val currentGuild by viewModel.currentGuild.collectAsState()
    val members by viewModel.guildMembers.collectAsState()

    if (currentGuild == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Shield, null, tint = TitaniumGray, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("YOU ARE NOT IN A GUILD", color = ChromeSilver, fontWeight = FontWeight.Bold)
                Text("Join or Establish one in the Browse tab.", color = TitaniumGray, style = ExorkTypography.labelSmall)
            }
        }
    } else {
        val guild = currentGuild!!
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                GuildHeaderCard(guild)
            }

            item {
                val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                val isMaster = currentUserId == guild.masterId
                
                ExorkNeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("GUILD NOTICE", style = ExorkTypography.labelSmall, color = ElectricCyan)
                            if (isMaster) {
                                IconButton(
                                    onClick = onEditNotice,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Edit, "Edit Notice", tint = ChromeSilver, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(guild.notice, style = ExorkTypography.bodyLarge, color = Color.White)
                    }
                }
            }

            item {
                Text(
                    "MEMBER ROSTER (${members.size}/${guild.maxMembers})",
                    style = ExorkTypography.labelLarge.copy(fontWeight = FontWeight.Black),
                    color = ChromeSilver
                )
            }

            item {
                MemberRosterTable(
                    members = members,
                    masterId = guild.masterId,
                    onMemberClick = onMemberClick
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                val isMaster = currentUserId == guild.masterId
                if (isMaster) {
                    Button(
                        onClick = onDisband,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f), contentColor = Color.Red),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("DISBAND GUILD", fontWeight = FontWeight.Black)
                    }
                } else {
                    OutlinedButton(
                        onClick = { viewModel.leaveGuild() },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Text("LEAVE GUILD")
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun MemberRosterTable(
    members: List<GuildMember>,
    masterId: String,
    onMemberClick: (GuildMember) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F0F16),
        border = BorderStroke(1.dp, Color(0xFF22222E)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            members.forEachIndexed { index, member ->
                val isLeader = member.userId == masterId
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMemberClick(member) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small Avatar
                    val memberBitmap = remember(member.photoUrl) {
                        parseAvatarToBitmap(member.photoUrl)
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ObsidianVoid)
                            .border(1.dp, if (isLeader) Color(0xFFFFD700) else ElectricCyan.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (memberBitmap != null) {
                            androidx.compose.foundation.Image(
                                bitmap = memberBitmap.asImageBitmap(),
                                contentDescription = member.username,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = ChromeSilver,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Member Name & Rank/Level
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = member.username,
                            style = ExorkTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1
                        )
                        Text(
                            text = "${member.rank} • LVL ${member.level}",
                            style = ExorkTypography.labelSmall,
                            color = TitaniumGray
                        )
                    }

                    // XP Display
                    Text(
                        text = "${member.totalXp} XP",
                        style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Role Badge (LEADER in Gold/Cyan vs MEMBER)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isLeader) Color(0xFFFFD700).copy(alpha = 0.15f) else Color(0xFF1E1E28),
                        border = BorderStroke(1.dp, if (isLeader) Color(0xFFFFD700) else Color(0xFF333344))
                    ) {
                        Text(
                            text = if (isLeader) "LEADER" else "MEMBER",
                            color = if (isLeader) Color(0xFFFFD700) else TitaniumGray,
                            style = ExorkTypography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Row Divider
                if (index < members.lastIndex) {
                    HorizontalDivider(
                        color = Color(0xFF1B1B26),
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BrowseGuildsPageContent(
    viewModel: GuildViewModel,
    onCreateNew: () -> Unit
) {
    val context = LocalContext.current
    val currentGuild by viewModel.currentGuild.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search Guilds...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = ElectricCyan) },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isSearching) {
            CircularProgressIndicator(
                color = ElectricCyan,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else if (searchQuery.isNotBlank() && searchResults.isEmpty()) {
            Text(
                "No guilds found matching '$searchQuery'",
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(searchResults) { guild ->
                GuildSearchCard(guild) {
                    if (currentGuild == null) {
                        viewModel.joinGuild(guild)
                    } else {
                        Toast.makeText(context, "Leave your current guild first!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        if (currentGuild == null) {
            Spacer(modifier = Modifier.height(16.dp))
            ExorkChromeButton(
                text = "ESTABLISH NEW GUILD",
                onClick = onCreateNew,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun EditNoticeDialog(currentNotice: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var noticeText by remember { mutableStateOf(currentNotice) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianVoid,
        title = { Text("EDIT GUILD NOTICE", color = Color.White, fontWeight = FontWeight.Black) },
        text = {
            Column {
                OutlinedTextField(
                    value = noticeText,
                    onValueChange = { input ->
                        if (input.length <= 50) {
                            noticeText = input
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("Enter guild notice...", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Text(
                    text = "${noticeText.length}/50",
                    style = ExorkTypography.labelSmall,
                    color = if (noticeText.length >= 50) Color.Red else TitaniumGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    textAlign = TextAlign.End
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(noticeText) },
                enabled = noticeText.isNotBlank() && noticeText.length <= 50
            ) {
                val color = if (noticeText.isNotBlank() && noticeText.length <= 50) ElectricCyan else Color.Gray
                Text("SAVE", color = color, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.Gray)
            }
        }
    )
}

@Composable
fun GuildHeaderCard(guild: Guild) {
    ExorkNeumorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(1.dp, ElectricCyan.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(guild.badgeIcon, fontSize = 32.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = guild.name.uppercase(),
                    style = ExorkTypography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ElectricCyan.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "[ ${guild.tag} ]",
                            style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Master: ${guild.masterName.lowercase()}",
                        style = ExorkTypography.labelSmall,
                        color = TitaniumGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    "TOTAL GUILD XP: ${guild.totalGuildXp} XP",
                    style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = ElectricCyan
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { 1f }, 
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = ElectricCyan,
                    trackColor = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun GuildSearchCard(guild: Guild, onJoin: () -> Unit) {
    ExorkNeumorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.3f))
                    .border(1.dp, ChromeSilver.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(guild.badgeIcon, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = guild.name.uppercase(),
                        style = ExorkTypography.labelLarge.copy(fontWeight = FontWeight.Black),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ElectricCyan.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.2f)),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text(
                            text = guild.tag,
                            style = ExorkTypography.labelSmall.copy(fontSize = 10.sp),
                            color = ElectricCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${guild.memberCount}/${guild.maxMembers} Members • ${guild.totalGuildXp} XP",
                    style = ExorkTypography.labelSmall,
                    color = TitaniumGray
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onJoin,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("JOIN", style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun CreateGuildDialog(onDismiss: () -> Unit, onCreate: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("⚔️") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianVoid,
        title = { Text("ESTABLISH GUILD", color = Color.White, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Guild Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tag,
                    onValueChange = { if (it.length <= 6) tag = it.uppercase() },
                    label = { Text("Tag (Max 6 chars)") },
                    modifier = Modifier.fillMaxWidth()
                )
                // Expanded RPG Icon List
                val guildIcons = listOf("⚔️", "🛡️", "🔥", "⚡", "🦅", "👑", "🐉", "🐺", "💀", "🗡️", "🏹", "🌌")
                Text("Select Emblem:", style = ExorkTypography.labelSmall, color = TitaniumGray)
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(guildIcons) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (icon == emoji) ElectricCyan.copy(alpha = 0.2f) else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (icon == emoji) ElectricCyan else Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { icon = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 24.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank() && tag.isNotBlank()) onCreate(name, tag, icon) },
                enabled = name.isNotBlank() && tag.isNotBlank()
            ) {
                Text("CREATE", color = ElectricCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.Gray)
            }
        }
    )
}
