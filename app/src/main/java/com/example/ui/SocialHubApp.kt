package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialHubApp(viewModel: SocialHubViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isDataFetching by viewModel.isDataFetching.collectAsStateWithLifecycle()
    val walletBalance by viewModel.walletBalance.collectAsStateWithLifecycle()
    val activeCheckoutInfo by viewModel.activeCheckoutInfo.collectAsStateWithLifecycle()
    val activeNotification by viewModel.activeNotification.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    var showPermissionsOnboarding by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                listOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.READ_MEDIA_IMAGES,
                    android.Manifest.permission.READ_MEDIA_VIDEO,
                    android.Manifest.permission.READ_MEDIA_AUDIO
                )
            } else {
                listOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }.any {
                androidx.core.content.ContextCompat.checkSelfPermission(context, it) != android.content.pm.PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val userName by viewModel.userProfileName.collectAsStateWithLifecycle()
    val userHandle by viewModel.userProfileHandle.collectAsStateWithLifecycle()

    var isHeaderSearchActive by remember { mutableStateOf(false) }
    var headerSearchQuery by remember { mutableStateOf("") }

    val creators by viewModel.creators.collectAsStateWithLifecycle(initialValue = emptyList())
    val posts by viewModel.posts.collectAsStateWithLifecycle(initialValue = emptyList())
    val followedPosts by viewModel.followedPosts.collectAsStateWithLifecycle(initialValue = emptyList())
    val subscriptions by viewModel.subscriptions.collectAsStateWithLifecycle(initialValue = emptyList())
    val transactions by viewModel.transactions.collectAsStateWithLifecycle(initialValue = emptyList())
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle(initialValue = emptyList())
    val events by viewModel.events.collectAsStateWithLifecycle(initialValue = emptyList())

    // Direct local state for splits
    var marioPizzaSplitPaid by remember { mutableStateOf(false) }

    val filteredPosts = if (headerSearchQuery.isBlank()) {
        followedPosts
    } else {
        followedPosts.filter {
            it.caption.contains(headerSearchQuery, ignoreCase = true) ||
            it.creatorName.contains(headerSearchQuery, ignoreCase = true) ||
            it.creatorHandle.contains(headerSearchQuery, ignoreCase = true)
        }
    }

    val filteredCreators = if (headerSearchQuery.isBlank()) {
        creators
    } else {
        creators.filter {
            it.name.contains(headerSearchQuery, ignoreCase = true) ||
            it.handle.contains(headerSearchQuery, ignoreCase = true) ||
            it.description.contains(headerSearchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            if (currentScreen !is Screen.Wallet) {
                TopAppBar(
                    title = {
                        if (isHeaderSearchActive) {
                            OutlinedTextField(
                                value = headerSearchQuery,
                                onValueChange = { headerSearchQuery = it },
                                placeholder = { Text("Search feed, creators...", color = GrayText, fontSize = 14.sp) },
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        headerSearchQuery = ""
                                        isHeaderSearchActive = false
                                    }) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close search", tint = LightText)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 15.sp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(RazorBlue, RazorTeal)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "S",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    )
                                }
                                Text(
                                    text = "SocialHub",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = LightText
                                )
                            }
                        }
                    },
                    actions = {
                        if (!isHeaderSearchActive && (currentScreen is Screen.Feed || currentScreen is Screen.Creators || currentScreen is Screen.LiveEvents || currentScreen is Screen.CreatorDetail)) {
                            IconButton(onClick = { viewModel.triggerRefresh() }) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Feed Cache", tint = RazorTeal)
                            }
                        }
                        if (!isHeaderSearchActive && currentScreen !is Screen.LiveEvents) {
                            IconButton(onClick = { isHeaderSearchActive = true }) {
                                Icon(imageVector = Icons.Default.Search, contentDescription = "Search hub", tint = LightText)
                            }
                        }
                        
                        // SECURE TECH GLOWING WALLET PILL - Page 1 & Page 5 (Marked inactive/SOON)
                        Box(contentAlignment = Alignment.Center) {
                            Surface(
                                onClick = { viewModel.navigateTo(Screen.Wallet) },
                                shape = RoundedCornerShape(20.dp),
                                color = ObsidianDark,
                                border = BorderStroke(1.5.dp, RazorBlue.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .testTag("app_quick_wallet")
                                    .alpha(0.4f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "$${String.format("%,.2f", walletBalance)}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = RazorBlue,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "₿",
                                            color = RazorBlue,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .offset(x = (-6).dp)
                                    .background(Color(0xFFEA739B), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("COMING SOON", color = Color.Black, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ObsidianDark,
                        titleContentColor = LightText,
                        actionIconContentColor = LightText
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MinimalSurfaceContainer,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .drawBehind {
                        drawLine(
                            color = MinimalBorder,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = currentScreen is Screen.Feed,
                    onClick = { viewModel.navigateTo(Screen.Feed) },
                    icon = { Icon(imageVector = if (currentScreen is Screen.Feed) Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Feed") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RazorBlue,
                        indicatorColor = MinimalLavenderContainer,
                        unselectedIconColor = GrayText
                    ),
                    modifier = Modifier.testTag("nav_feed")
                )
                NavigationBarItem(
                    selected = currentScreen is Screen.Creators || currentScreen is Screen.CreatorDetail,
                    onClick = { viewModel.navigateTo(Screen.Creators) },
                    icon = { Icon(imageVector = if (currentScreen is Screen.Creators) Icons.Filled.Explore else Icons.Outlined.Explore, contentDescription = "Creators") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RazorBlue,
                        indicatorColor = MinimalLavenderContainer,
                        unselectedIconColor = GrayText
                    ),
                    modifier = Modifier.testTag("nav_creators")
                )
                NavigationBarItem(
                    selected = currentScreen is Screen.LiveEvents,
                    onClick = { viewModel.navigateTo(Screen.LiveEvents) },
                    icon = { Icon(imageVector = if (currentScreen is Screen.LiveEvents) Icons.Filled.Storefront else Icons.Outlined.Storefront, contentDescription = "Market") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RazorBlue,
                        indicatorColor = MinimalLavenderContainer,
                        unselectedIconColor = GrayText
                    ),
                    modifier = Modifier.testTag("nav_tickets")
                )
                NavigationBarItem(
                    selected = currentScreen is Screen.Chat,
                    onClick = { viewModel.navigateTo(Screen.Chat()) },
                    icon = { Icon(imageVector = if (currentScreen is Screen.Chat) Icons.Filled.Mail else Icons.Outlined.Mail, contentDescription = "Secure Chats") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RazorBlue,
                        indicatorColor = MinimalLavenderContainer,
                        unselectedIconColor = GrayText
                    ),
                    modifier = Modifier.testTag("nav_chat")
                )
                NavigationBarItem(
                    selected = currentScreen is Screen.Wallet,
                    onClick = { viewModel.navigateTo(Screen.Wallet) },
                    icon = { Icon(imageVector = if (currentScreen is Screen.Wallet) Icons.Filled.CreditCard else Icons.Outlined.CreditCard, contentDescription = "Virtual Wallet") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RazorBlue,
                        indicatorColor = MinimalLavenderContainer,
                        unselectedIconColor = GrayText
                    ),
                    modifier = Modifier.testTag("nav_wallet")
                )
            }
        },
        containerColor = ObsidianDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentScreen,
                animationSpec = spring(),
                label = "ScreenSwitch"
            ) { target ->
                when (target) {
                    is Screen.Feed -> {
                        if (isDataFetching) {
                            FeedScreenSkeleton()
                        } else {
                            FeedScreen(
                                posts = filteredPosts,
                                creators = creators,
                                subscriptions = subscriptions,
                                onLike = { viewModel.likePost(it) },
                                onTip = { post, amt -> viewModel.triggerPostTip(post, amt) },
                                onUnlock = { creatorId ->
                                    viewModel.navigateTo(Screen.CreatorDetail(creatorId))
                                },
                                onPublishPost = { caption, creator, attachedMediaType -> viewModel.publishPost(caption, creator, attachedMediaType) },
                                onDiscoverCreators = { viewModel.navigateTo(Screen.Creators) }
                            )
                        }
                    }
                    is Screen.Creators -> {
                        if (isDataFetching) {
                            CreatorsScreenSkeleton()
                        } else {
                            CreatorsScreen(
                                creators = filteredCreators,
                                subscriptions = subscriptions,
                                onCreatorClick = { viewModel.navigateTo(Screen.CreatorDetail(it.id)) }
                            )
                        }
                    }
                    is Screen.CreatorDetail -> {
                        if (isDataFetching) {
                            CreatorDetailScreenSkeleton()
                        } else {
                            val creatorId = target.creatorId
                            val creator = creators.find { it.id == creatorId }
                            if (creator != null) {
                                CreatorDetailScreen(
                                    creator = creator,
                                    subscriptions = subscriptions,
                                    posts = posts.filter { it.creatorId == creatorId },
                                    events = events.filter { it.creatorId == creatorId },
                                    onSubscribeClick = { tier, price ->
                                        viewModel.triggerSubscriptionBuy(creator, tier, price)
                                    },
                                    onCancelSubscription = {
                                        viewModel.cancelSubscription(creator.id, creator.handle)
                                    },
                                    onFollowToggle = {
                                        viewModel.toggleFollow(creator)
                                    },
                                    onMessageClick = {
                                        viewModel.navigateTo(Screen.Chat(creator.name))
                                    },
                                    onLikePost = { viewModel.likePost(it) },
                                    onBack = { viewModel.navigateTo(Screen.Creators) }
                                )
                            } else {
                                Text("Creator Not Found", color = Color.White)
                            }
                        }
                    }
                    is Screen.LiveEvents -> {
                        if (isDataFetching) {
                            LiveEventsScreenSkeleton()
                        } else {
                            LiveEventsScreen(
                                events = events,
                                onBuyTicketClick = { viewModel.triggerTicketBuy(it) },
                                onCreatorDetail = { creatorId -> viewModel.navigateTo(Screen.CreatorDetail(creatorId)) },
                                walletBalance = walletBalance,
                                onPurchaseDirect = { name, amt ->
                                    viewModel.triggerPostTip(
                                        Post(creatorId = "pixel_queen", creatorName = "Market Item", creatorHandle = "shop", creatorAvatar = "", caption = "Purchased Product: $name", contentImage = "", isPremium = false),
                                        amt
                                    )
                                }
                            )
                        }
                    }
                    is Screen.Chat -> ChatScreen(
                        chatMessages = chatMessages,
                        creators = creators,
                        encryptionEnabled = viewModel.chatEncryptionEnabled.collectAsStateWithLifecycle().value,
                        onToggleEncryption = { viewModel.toggleChatEncryption() },
                        onSendMessage = { handle, msg -> viewModel.sendChatMessage(handle, msg) },
                        onPayInvoice = { chat -> viewModel.payChatInvoice(chat) },
                        onDeclineInvoice = { chat -> viewModel.declineChatInvoice(chat) },
                        initialRecipient = target.initialRecipient
                    )
                    is Screen.Wallet -> WalletScreen(
                        balance = walletBalance,
                        transactions = transactions,
                        userName = userName,
                        userHandle = userHandle,
                        onUpdateProfile = { name, handle -> viewModel.updateProfile(name, handle) },
                        onLoadFunds = { viewModel.addWalletFunds(it) },
                        onPayPizzaSplit = {
                            if (walletBalance >= 21.12) {
                                viewModel.triggerPostTip(
                                    Post(creatorId = "shop", creatorName = "Split Payment", creatorHandle = "bank", creatorAvatar = "", caption = "Mario's Pizza Co-Split Liability", contentImage = "", isPremium=false),
                                    21.12
                                )
                                marioPizzaSplitPaid = true
                            }
                        },
                        pizzaSplitPaid = marioPizzaSplitPaid,
                        posts = posts,
                        subscriptions = subscriptions,
                        creators = creators,
                        onLikePost = { viewModel.likePost(it) },
                        onCancelSubscription = { creatorId, handle -> viewModel.cancelSubscription(creatorId, handle) }
                    )
                }
            }

            activeCheckoutInfo?.let { checkout ->
                RazorpayCheckoutOverlay(
                    checkoutInfo = checkout,
                    onDismiss = { viewModel.dismissCheckout() }
                )
            }

            activeNotification?.let { alert ->
                SystemNotificationPopup(
                    title = alert.title,
                    message = alert.message,
                    onDismiss = { viewModel.clearNotification() }
                )
            }

            if (showPermissionsOnboarding) {
                PermissionsOnboardingDialog(
                    onDismiss = { showPermissionsOnboarding = false },
                    onPermissionsGranted = {
                        showPermissionsOnboarding = false
                    }
                )
            }
        }
    }
}

// ============================================
// SCREEN 1: FEED SCREEN WITH STORIES (Page 1)
// ============================================
@Composable
fun FeedScreen(
    posts: List<Post>,
    creators: List<Creator>,
    subscriptions: List<Subscription>,
    onLike: (Post) -> Unit,
    onTip: (Post, Double) -> Unit,
    onUnlock: (String) -> Unit,
    onPublishPost: (String, Creator?, String?) -> Unit,
    onDiscoverCreators: () -> Unit
) {
    var showStoryStudio by remember { mutableStateOf(false) }
    var showNewPostDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Beautiful futuristic icon / neon circle
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(RazorTeal.copy(alpha = 0.08f), CircleShape)
                            .border(1.5.dp, RazorTeal.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Empty Feed",
                            tint = RazorTeal,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Feed is Quiet 🌌",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Follow your favorite creators to see their tech insights, deep-dives, and community stories right here in your feed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrayText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Discover Creators Button!
                    Button(
                        onClick = onDiscoverCreators,
                        colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Discover Creators",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Horizontal Stories Bar - Page 1
                item {
                    StoriesBar(
                        onCreateStoryClick = { showStoryStudio = true }
                    )
                }

                items(posts, key = { it.id }) { post ->
                    val hasSubscription = subscriptions.any {
                        it.creatorId == post.creatorId &&
                        (it.tierName == "GOLD" || 
                         (it.tierName == "SILVER" && post.requiredTier != "GOLD") || 
                         (it.tierName == "BRONZE" && post.requiredTier == "BRONZE"))
                    }
                    val isLocked = post.isPremium && !hasSubscription

                    PostCard(
                        post = post,
                        isLocked = isLocked,
                        onLike = { onLike(post) },
                        onTip = { amt -> onTip(post, amt) },
                        onUnlock = { onUnlock(post.creatorId) }
                    )
                }
            }
        }

        // Floating Action Button to post dynamically
        FloatingActionButton(
            onClick = { showNewPostDialog = true },
            containerColor = RazorTeal,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 76.dp, end = 20.dp)
                .testTag("new_post_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Create,
                contentDescription = "New Post Icon",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    if (showStoryStudio) {
        CreativeStudioDialog(
            onDismiss = { showStoryStudio = false },
            onPublish = { onPublishPost(it, null, null) }
        )
    }

    if (showNewPostDialog) {
        NewPostDialog(
            creators = creators,
            onDismiss = { showNewPostDialog = false },
            onPublish = onPublishPost
        )
    }
}

@Composable
fun NewPostDialog(
    creators: List<Creator>,
    onDismiss: () -> Unit,
    onPublish: (String, Creator?, String?) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        var captionText by remember { mutableStateOf("") }
        var selectedCreator by remember { mutableStateOf<Creator?>(null) }
        var showCreatorDropdown by remember { mutableStateOf(false) }

        val context = androidx.compose.ui.platform.LocalContext.current
        var attachedMediaType by remember { mutableStateOf<String?>(null) } // "attached_image", "attached_video", "attached_audio"
        var permissionStatusMessage by remember { mutableStateOf<String?>(null) }
        var requestedPermissionType by remember { mutableStateOf<String?>(null) }

        // Core client-side file properties
        var realAttachedUri by remember { mutableStateOf<android.net.Uri?>(null) }
        var attachedFileName by remember { mutableStateOf<String?>(null) }
        var attachedFileSizeStr by remember { mutableStateOf<String?>(null) }
        var attachedFileMimeType by remember { mutableStateOf<String?>(null) }
        var fileValidationErrorMessage by remember { mutableStateOf<String?>(null) }

        // Simulation parameters for playground validation testing in the preview
        var simFileType by remember { mutableStateOf("image/png") }
        var simFileName by remember { mutableStateOf("cyberpunk_shot.png") }
        var simFileSizeMB by remember { mutableStateOf("3.5") }

        // Local client-side validator helper
        fun validateFile(
            fileName: String,
            fileSizeInBytes: Long,
            mimeType: String?
        ): Pair<Boolean, String> {
            val extension = fileName.substringAfterLast('.', "").lowercase()
            val resolvedType = mimeType ?: when (extension) {
                "png", "jpg", "jpeg", "webp", "gif" -> "image/$extension"
                "mp4", "mkv", "3gp", "webm", "mov" -> "video/$extension"
                "mp3", "wav", "ogg", "m4a" -> "audio/$extension"
                else -> "unknown/unknown"
            }

            val isImage = resolvedType.startsWith("image/") || listOf("png", "jpg", "jpeg", "webp", "gif").contains(extension)
            val isVideo = resolvedType.startsWith("video/") || listOf("mp4", "mkv", "3gp", "webm", "mov").contains(extension)
            val isAudio = resolvedType.startsWith("audio/") || listOf("mp3", "wav", "ogg", "m4a").contains(extension)

            if (!isImage && !isVideo && !isAudio) {
                return Pair(false, "Unsupported file format ($resolvedType). Only Image, Video, and Audio media types are permitted.")
            }

            val sizeInMB = fileSizeInBytes / (1024.0 * 1024.0)
            if (isImage) {
                if (fileSizeInBytes > 5 * 1024 * 1024) {
                    val formattedSize = String.format("%.2f", sizeInMB)
                    return Pair(false, "Image is too large ($formattedSize MB). Max limit is 5.0 MB to prevent client performance bottlenecks.")
                }
            } else if (isVideo) {
                if (fileSizeInBytes > 20 * 1024 * 1024) {
                    val formattedSize = String.format("%.2f", sizeInMB)
                    return Pair(false, "Video is too large ($formattedSize MB). Max limit is 20.0 MB to prevent play/buffer bottlenecks.")
                }
            } else if (isAudio) {
                if (fileSizeInBytes > 8 * 1024 * 1024) {
                    val formattedSize = String.format("%.2f", sizeInMB)
                    return Pair(false, "Audio is too large ($formattedSize MB). Max limit is 8.0 MB to minimize telemetry memory footprint.")
                }
            }

            return Pair(true, "Validation passed successfully!")
        }

        // Real media activity selector launcher
        val mediaPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
        ) { uri: android.net.Uri? ->
            if (uri != null) {
                val resolver = context.contentResolver
                var fileName = "imported_media_file"
                var fileSize: Long = 0
                val mimeType = resolver.getType(uri)

                try {
                    resolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                        if (cursor.moveToFirst()) {
                            if (nameIndex != -1) {
                                val retrievedName = cursor.getString(nameIndex)
                                if (!retrievedName.isNullOrBlank()) {
                                    fileName = retrievedName
                                }
                            }
                            if (sizeIndex != -1) {
                                fileSize = cursor.getLong(sizeIndex)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (fileSize <= 0) {
                    try {
                        resolver.openInputStream(uri)?.use { stream ->
                            fileSize = stream.available().toLong()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val (isValid, errorMsg) = validateFile(fileName, fileSize, mimeType)
                if (isValid) {
                    realAttachedUri = uri
                    attachedFileName = fileName
                    val sizeInMB = fileSize / (1024.0 * 1024.0)
                    attachedFileSizeStr = String.format("%.2f MB", sizeInMB)
                    attachedFileMimeType = mimeType ?: "unknown"
                    fileValidationErrorMessage = null

                    val ext = fileName.substringAfterLast('.', "").lowercase()
                    attachedMediaType = when {
                        mimeType?.startsWith("image/") == true || listOf("png", "jpg", "jpeg", "webp", "gif").contains(ext) -> "attached_image"
                        mimeType?.startsWith("video/") == true || listOf("mp4", "mkv", "3gp", "webm", "mov").contains(ext) -> "attached_video"
                        mimeType?.startsWith("audio/") == true || listOf("mp3", "wav", "ogg", "m4a").contains(ext) -> "attached_audio"
                        else -> "attached_image"
                    }
                    permissionStatusMessage = "✅ Validated & attached: $fileName ($attachedFileSizeStr)"
                } else {
                    fileValidationErrorMessage = errorMsg
                    permissionStatusMessage = "❌ Validation Failed: $errorMsg"
                    realAttachedUri = null
                    attachedFileName = null
                    attachedFileSizeStr = null
                    attachedFileMimeType = null
                    attachedMediaType = null
                }
            }
        }

        val singlePermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            val allGranted = results.values.all { it }
            if (allGranted) {
                permissionStatusMessage = "✅ Provisioned successfully! Initializing device engine."
                when (requestedPermissionType) {
                    "camera" -> {
                        attachedMediaType = "attached_image"
                        permissionStatusMessage = "📸 Captured live high-definition cyber snapshot!"
                    }
                    "media" -> {
                        try {
                            mediaPickerLauncher.launch("*/*")
                        } catch (e: Exception) {
                            permissionStatusMessage = "⚠️ Media warehouse launch failed: ${e.message}"
                        }
                    }
                    "mic" -> {
                        attachedMediaType = "attached_audio"
                        permissionStatusMessage = "🎙️ Synchronized voice telemetry note successfully!"
                    }
                    "video" -> {
                        attachedMediaType = "attached_video"
                        permissionStatusMessage = "📹 Recorded premium cybernetic cinematic reel stream!"
                    }
                }
            } else {
                permissionStatusMessage = "❌ Access requested was denied. Running sandbox fallback simulation!"
                when (requestedPermissionType) {
                    "camera" -> { attachedMediaType = "attached_image" }
                    "media" -> {
                        try {
                            mediaPickerLauncher.launch("*/*")
                        } catch (e: Exception) {
                            permissionStatusMessage = "⚠️ Media warehouse launch failed: ${e.message}"
                        }
                    }
                    "mic" -> { attachedMediaType = "attached_audio" }
                    "video" -> { attachedMediaType = "attached_video" }
                }
            }
        }

        val tryAttach: (String) -> Unit = { type ->
            requestedPermissionType = type
            val permissionsNeeded = when (type) {
                "camera" -> arrayOf(android.Manifest.permission.CAMERA)
                "mic" -> arrayOf(android.Manifest.permission.RECORD_AUDIO)
                "video" -> arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)
                "media" -> {
                    if (android.os.Build.VERSION.SDK_INT >= 33) {
                        arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
                    } else {
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
                else -> emptyArray()
            }
            
            val allGranted = permissionsNeeded.all {
                androidx.core.content.ContextCompat.checkSelfPermission(context, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
            }
            
            if (allGranted) {
                permissionStatusMessage = "✅ Access verified. Device ready."
                when (type) {
                    "camera" -> {
                        attachedMediaType = "attached_image"
                        permissionStatusMessage = "📸 Captured live high-definition cyber snapshot!"
                    }
                    "media" -> {
                        try {
                            mediaPickerLauncher.launch("*/*")
                        } catch (e: Exception) {
                            permissionStatusMessage = "⚠️ Media warehouse picking failed: ${e.message}"
                        }
                    }
                    "mic" -> {
                        attachedMediaType = "attached_audio"
                        permissionStatusMessage = "🎙️ Synchronized voice telemetry note successfully!"
                    }
                    "video" -> {
                        attachedMediaType = "attached_video"
                        permissionStatusMessage = "📹 Recorded premium cybernetic cinematic reel stream!"
                    }
                }
            } else {
                permissionStatusMessage = "📡 Requesting OS system permissions..."
                singlePermissionLauncher.launch(permissionsNeeded)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16112C)),
            border = BorderStroke(1.5.dp, RazorTeal.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(RazorTeal)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PUBLISH NEW REPORT",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close dialog",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                // Selector for "Post As" Account
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "POST AS AUTHOR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RazorBlue,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MinimalLavenderContainer)
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .clickable { showCreatorDropdown = !showCreatorDropdown }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (selectedCreator == null) RazorBlue else RazorTeal),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = selectedCreator?.name?.take(2) ?: "ME",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = selectedCreator?.name ?: "Current User Profile",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "@${selectedCreator?.handle ?: "me"}",
                                        color = GrayText,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (showCreatorDropdown) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }

                    if (showCreatorDropdown) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2254)),
                            border = BorderStroke(1.dp, RazorTeal.copy(alpha = 0.3f))
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                // Default current user option
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedCreator = null
                                                showCreatorDropdown = false
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(RazorBlue),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("ME", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Current User (Me)", color = Color.White, fontSize = 13.sp)
                                    }
                                }

                                // List of creators
                                items(creators) { creator ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedCreator = creator
                                                showCreatorDropdown = false
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(RazorTeal),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(creator.name.take(2).uppercase(), color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(creator.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("@${creator.handle}", color = GrayText, fontSize = 11.sp)
                                                if (creator.isFollowed) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("● Following", color = RazorTeal, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Caption Input Text Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "CAPTION & INSIGHT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RazorBlue,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F183C)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                            BasicTextField(
                                value = captionText,
                                onValueChange = { if (it.length <= 280) captionText = it },
                                textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 14.sp),
                                modifier = Modifier.fillMaxSize()
                            )
                            if (captionText.isEmpty()) {
                                Text(
                                    text = "Share some knowledge or make a statement to your feeds in the cloud...",
                                    color = GrayText,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // Character counter
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "${captionText.length}/280",
                            color = if (captionText.length >= 250) Color.Red else GrayText,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Cybernetic Hardware Media Attachment Hub
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MinimalDeepPurple)
                        .border(1.dp, RazorBlue.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "ATTACH HARDWARE MEDIA RESOURCE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = RazorTeal,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Triggers on-demand device authorization for high-fidelity captures.",
                        fontSize = 11.sp,
                        color = GrayText,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Buttons list row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // CAMERA SNAP
                        Button(
                            onClick = { tryAttach("camera") },
                            colors = ButtonDefaults.buttonColors(containerColor = if (attachedMediaType == "attached_image" && requestedPermissionType == "camera") RazorTeal else Color(0xFF1F163D)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📸 Snap", fontSize = 11.sp, color = if (attachedMediaType == "attached_image" && requestedPermissionType == "camera") Color.Black else Color.White, fontWeight = FontWeight.Bold)
                        }

                        // MIC AUDIO
                        Button(
                            onClick = { tryAttach("mic") },
                            colors = ButtonDefaults.buttonColors(containerColor = if (attachedMediaType == "attached_audio") RazorTeal else Color(0xFF1F163D)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🎙️ Mic", fontSize = 11.sp, color = if (attachedMediaType == "attached_audio") Color.Black else Color.White, fontWeight = FontWeight.Bold)
                        }

                        // VIDEO CAPTURE
                        Button(
                            onClick = { tryAttach("video") },
                            colors = ButtonDefaults.buttonColors(containerColor = if (attachedMediaType == "attached_video") RazorTeal else Color(0xFF1F163D)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📹 Video", fontSize = 11.sp, color = if (attachedMediaType == "attached_video") Color.Black else Color.White, fontWeight = FontWeight.Bold)
                        }

                        // GALLERY PICKER
                        Button(
                            onClick = { tryAttach("media") },
                            colors = ButtonDefaults.buttonColors(containerColor = if (attachedMediaType == "attached_image" && requestedPermissionType == "media") RazorTeal else Color(0xFF1F163D)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🖼️ Pick", fontSize = 11.sp, color = if (attachedMediaType == "attached_image" && requestedPermissionType == "media") Color.Black else Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // --- PLAYGROUND SHUNT ENGINE START ---
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "⚡ VALIDATION SYSTEM PLAYGROUND",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = RazorBlue,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Saves bandwidth & prevents client bottlenecks by vetting size (Image: 5MB, Audio: 8MB, Video: 20MB) & file formats.",
                        fontSize = 10.sp,
                        color = GrayText,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Simulated Type Segment Selection Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            Triple("image/png", "cyberpunk_shot.png", "🖼️ JPEG/PNG"),
                            Triple("video/mp4", "hype_vlog.mp4", "📹 Video (MP4)"),
                            Triple("audio/wav", "voice_memo.wav", "🎙️ Audio (WAV)"),
                            Triple("application/zip", "malicious_hack.zip", "⚠️ ZIP (Block)")
                        ).forEach { (mimeType, dName, label) ->
                            val isSelected = simFileType == mimeType
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) RazorBlue.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.3f))
                                    .border(1.dp, if (isSelected) RazorBlue else Color.Transparent, RoundedCornerShape(6.dp))
                                    .clickable {
                                        simFileType = mimeType
                                        simFileName = dName
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else GrayText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Simulated Size Segment Selection Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Simulate Size:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("1.5", "4.8", "7.9", "12.0", "32.0").forEach { preset ->
                                val isSelectedPreset = simFileSizeMB == preset
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelectedPreset) RazorTeal else Color.Black.copy(alpha = 0.2f))
                                        .border(1.dp, if (isSelectedPreset) RazorTeal else Color.White.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                        .clickable { simFileSizeMB = preset }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "$preset MB",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelectedPreset) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val parsedSizeMB = simFileSizeMB.toDoubleOrNull() ?: 3.5
                            val sizeBytes = (parsedSizeMB * 1024 * 1024).toLong()
                            val (isValid, errorMsg) = validateFile(simFileName, sizeBytes, simFileType)
                            
                            if (isValid) {
                                realAttachedUri = null // Reset real uri if using simulation
                                attachedFileName = simFileName
                                attachedFileSizeStr = "$parsedSizeMB MB"
                                attachedFileMimeType = simFileType
                                fileValidationErrorMessage = null
                                attachedMediaType = when {
                                    simFileType.startsWith("image/") -> "attached_image"
                                    simFileType.startsWith("video/") -> "attached_video"
                                    simFileType.startsWith("audio/") -> "attached_audio"
                                    else -> "attached_image"
                                }
                                permissionStatusMessage = "✅ Simulation: Verified $simFileName ($parsedSizeMB MB)"
                            } else {
                                fileValidationErrorMessage = errorMsg
                                permissionStatusMessage = "❌ Simulation Blocked: $errorMsg"
                                realAttachedUri = null
                                attachedFileName = null
                                attachedFileSizeStr = null
                                attachedFileMimeType = null
                                attachedMediaType = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RazorBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                    ) {
                        Text("🛡️ Validate & Apply Simulation", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                    // --- PLAYGROUND SHUNT ENGINE END ---

                    // Permission telemetry feedback banner
                    permissionStatusMessage?.let { status ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = status,
                                color = if (status.startsWith("❌") || status.contains("Blocked") || status.contains("Failed")) Color(0xFFFF5555) else RazorTeal,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Rich preview of active attachment
                    attachedMediaType?.let { type ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            border = BorderStroke(1.dp, RazorTeal.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    when (type) {
                                        "attached_image" -> {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Brush.radialGradient(listOf(RazorBlue, ObsidianDark))),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = RazorTeal, modifier = Modifier.size(16.dp))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(attachedFileName ?: "attachment_highfi_snapshot.png", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = if (attachedFileSizeStr != null) {
                                                        "VALIDATED • $attachedFileSizeStr • IMAGE BUFFER"
                                                    } else {
                                                        "RESOLVED • 4K IMAGE BUFFER"
                                                    },
                                                    color = RazorTeal,
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                        "attached_audio" -> {
                                            Row(
                                                modifier = Modifier
                                                    .width(40.dp)
                                                    .height(28.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color.Black.copy(alpha = 0.4f))
                                                    .padding(horizontal = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Box(modifier = Modifier.width(3.dp).height(12.dp).background(RazorTeal))
                                                Box(modifier = Modifier.width(3.dp).height(22.dp).background(RazorTeal))
                                                Box(modifier = Modifier.width(3.dp).height(16.dp).background(RazorTeal))
                                                Box(modifier = Modifier.width(3.dp).height(10.dp).background(RazorTeal))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(attachedFileName ?: "microphone_voice_stream.wav", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = if (attachedFileSizeStr != null) {
                                                        "VALIDATED • $attachedFileSizeStr • WAV/AUDIO"
                                                    } else {
                                                        "SAMPLE • 48KHZ STEREO SOUND"
                                                    },
                                                    color = RazorBlue,
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                        "attached_video" -> {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Brush.radialGradient(listOf(InstaPink, ObsidianDark))),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(attachedFileName ?: "vlog_video_stream.mp4", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = if (attachedFileSizeStr != null) {
                                                        "VALIDATED • $attachedFileSizeStr • VIDEO STREAM"
                                                    } else {
                                                        "REEL • H.265 DECODED CLUSTERS"
                                                    },
                                                    color = InstaPink,
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        attachedMediaType = null
                                        permissionStatusMessage = "🗑️ Attachment cleared."
                                        realAttachedUri = null
                                        attachedFileName = null
                                        attachedFileSizeStr = null
                                        attachedFileMimeType = null
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear attachment", tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // CTA Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D263B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (captionText.isNotBlank()) {
                                onPublish(captionText, selectedCreator, attachedMediaType)
                                onDismiss()
                            }
                        },
                        enabled = captionText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RazorTeal,
                            disabledContainerColor = RazorTeal.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Publish", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun StoriesBar(onCreateStoryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Your Story (with + click button) - Page 1
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onCreateStoryClick() }
                ) {
                    Box(modifier = Modifier.size(72.dp)) {
                        // Profile Avatar Frame
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MinimalSurfaceContainer)
                                .border(1.dp, GrayText, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Me", color = LightText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        // Plus Badge Circle - Page 1
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(RazorTeal)
                                .border(2.dp, ObsidianDark, CircleShape)
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Story", tint = Color.Black, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Your Story", color = GrayText, fontSize = 12.sp)
                }
            }

            // Sarah Story (With GLOWING CHANGER ring!) - Page 1
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color.Transparent)
                            .border(3.dp, RazorBlue, CircleShape), // Glowing Cyan Border
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8EAF6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("S", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sarah", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Alex Story - Page 1
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color.Transparent)
                            .border(1.5.dp, GrayText.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFF3E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("A", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Alex", color = LightText, fontSize = 12.sp)
                }
            }

            // Emily Story - Page 1
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color.Transparent)
                            .border(1.5.dp, GrayText.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F8E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("E", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Emily", color = LightText, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    isLocked: Boolean,
    onLike: () -> Unit,
    onTip: (Double) -> Unit,
    onUnlock: () -> Unit
) {
    var hasUserLiked by remember { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, MinimalBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar Circular Indicator
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(RazorBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.creatorName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.creatorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LightText
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified status checked",
                            tint = RazorTeal,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = "2 hours ago • San Francisco",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrayText
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(imageVector = Icons.Default.MoreHoriz, contentDescription = "More Options", tint = LightText)
            }

            if (isLocked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(MinimalLavenderContainer, ObsidianDark)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Hidden post contents",
                            tint = InstaPink,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Premium Locked Content",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = LightText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Unlock this subscriber post via secure premium routing subscription.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onUnlock,
                            colors = ButtonDefaults.buttonColors(containerColor = RazorBlue),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("unlock_sub_button")
                        ) {
                            Text("Unlock Portfolio Post", fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(270.dp)
                        .background(Color(0xFF130E22))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        
                        when (post.contentImage) {
                            "attached_image" -> {
                                // Cyber focus target design
                                val cx = canvasWidth / 2
                                val cy = canvasHeight / 2
                                drawCircle(
                                    color = RazorBlue.copy(alpha = 0.2f),
                                    radius = 100.dp.toPx(),
                                    center = Offset(cx, cy)
                                )
                                drawCircle(
                                    color = RazorTeal,
                                    radius = 70.dp.toPx(),
                                    center = Offset(cx, cy),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                                drawCircle(
                                    color = RazorTeal.copy(alpha = 0.5f),
                                    radius = 12.dp.toPx(),
                                    center = Offset(cx, cy)
                                )
                                // Crosshairs
                                drawLine(color = RazorTeal, start = Offset(cx - 90.dp.toPx(), cy), end = Offset(cx - 30.dp.toPx(), cy), strokeWidth = 3f)
                                drawLine(color = RazorTeal, start = Offset(cx + 30.dp.toPx(), cy), end = Offset(cx + 90.dp.toPx(), cy), strokeWidth = 3f)
                                drawLine(color = RazorTeal, start = Offset(cx, cy - 90.dp.toPx()), end = Offset(cx, cy - 30.dp.toPx()), strokeWidth = 3f)
                                drawLine(color = RazorTeal, start = Offset(cx, cy + 30.dp.toPx()), end = Offset(cx, cy + 90.dp.toPx()), strokeWidth = 3f)
                                
                                // corner brackets
                                drawLine(color = RazorBlue, start = Offset(10.dp.toPx(), 10.dp.toPx()), end = Offset(40.dp.toPx(), 10.dp.toPx()), strokeWidth = 5f)
                                drawLine(color = RazorBlue, start = Offset(10.dp.toPx(), 10.dp.toPx()), end = Offset(10.dp.toPx(), 40.dp.toPx()), strokeWidth = 5f)
                                drawLine(color = RazorBlue, start = Offset(canvasWidth - 40.dp.toPx(), 10.dp.toPx()), end = Offset(canvasWidth - 10.dp.toPx(), 10.dp.toPx()), strokeWidth = 5f)
                                drawLine(color = RazorBlue, start = Offset(canvasWidth - 10.dp.toPx(), 10.dp.toPx()), end = Offset(canvasWidth - 10.dp.toPx(), 40.dp.toPx()), strokeWidth = 5f)
                            }
                            "attached_audio" -> {
                                // Pulse frequency equalizer bars
                                val centerY = canvasHeight / 2
                                val barCount = 18
                                val gap = canvasWidth / (barCount + 1)
                                val baseHeights = listOf(30, 80, 110, 40, 90, 130, 150, 70, 100, 120, 80, 50, 90, 130, 110, 40, 60, 30)
                                for (i in 0 until barCount) {
                                    val barX = gap * (i + 1)
                                    val barH = baseHeights[i % baseHeights.size].dp.toPx()
                                    drawLine(
                                        color = RazorTeal,
                                        start = Offset(barX, centerY - barH / 2),
                                        end = Offset(barX, centerY + barH / 2),
                                        strokeWidth = 6.dp.toPx()
                                    )
                                }
                            }
                            "attached_video" -> {
                                // Cinematic look with play icon
                                val cx = canvasWidth / 2
                                val cy = canvasHeight / 2
                                
                                // top segment
                                drawRect(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    topLeft = Offset(0f, 0f),
                                    size = androidx.compose.ui.geometry.Size(canvasWidth, 30.dp.toPx())
                                )
                                // bottom segment
                                drawRect(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    topLeft = Offset(0f, canvasHeight - 30.dp.toPx()),
                                    size = androidx.compose.ui.geometry.Size(canvasWidth, 30.dp.toPx())
                                )
                                
                                // triangle play
                                val path = Path().apply {
                                    val side = 35.dp.toPx()
                                    val startX = cx - side / 3
                                    val startY = cy - side / 2
                                    moveTo(startX, startY)
                                    lineTo(startX + side, cy)
                                    lineTo(startX, startY + side)
                                    close()
                                }
                                drawPath(path = path, color = InstaPink)
                                drawCircle(
                                    color = InstaPink,
                                    radius = 32.dp.toPx(),
                                    center = Offset(cx, cy),
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            }
                            "latte_art" -> {
                                // Draw stylized coffee cup and espresso vector art on canvas - Page 1
                                val cupCenter = Offset(canvasWidth / 2, canvasHeight / 2)
                                val cupRadius = 90.dp.toPx()
                                
                                // Cup Shadow Background
                                drawCircle(
                                    color = Color(0xFF5D4037).copy(alpha = 0.6f),
                                    radius = cupRadius + 20f,
                                    center = cupCenter
                                )
                                // Cup Frame
                                drawCircle(
                                    color = Color(0xFFECEFF1),
                                    radius = cupRadius,
                                    center = cupCenter,
                                    style = Stroke(width = 8.dp.toPx())
                                )
                                // Espresso fill
                                drawCircle(
                                    color = Color(0xFF3E2723),
                                    radius = cupRadius - 5.dp.toPx(),
                                    center = cupCenter
                                )
                                // Crema Swirl Latte Art Details
                                for (i in 1..4) {
                                    drawCircle(
                                        color = Color(0xFFD7CCC8).copy(alpha = 0.2f * i),
                                        radius = (i * 18).dp.toPx(),
                                        center = cupCenter,
                                        style = Stroke(width = 4.dp.toPx())
                                    )
                                }
                            }
                            "music_studio" -> {
                                for (i in 0..10) {
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.08f),
                                        start = Offset((canvasWidth / 10) * i, 0f),
                                        end = Offset((canvasWidth / 10) * i, canvasHeight),
                                        strokeWidth = 2f
                                    )
                                }
                                val pathPoints = listOf(
                                    Offset(0f, canvasHeight * 0.7f),
                                    Offset(canvasWidth * 0.2f, canvasHeight * 0.4f),
                                    Offset(canvasWidth * 0.4f, canvasHeight * 0.8f),
                                    Offset(canvasWidth * 0.6f, canvasHeight * 0.3f),
                                    Offset(canvasWidth * 0.8f, canvasHeight * 0.6f),
                                    Offset(canvasWidth, canvasHeight * 0.5f)
                                )
                                for (i in 0 until pathPoints.size - 1) {
                                    drawLine(
                                        color = RazorBlue,
                                        start = pathPoints[i],
                                        end = pathPoints[i+1],
                                        strokeWidth = 6f
                                    )
                                }
                            }
                            else -> {
                                drawLine(
                                    color = Color.White.copy(alpha = 0.05f),
                                    start = Offset(0f, 0f),
                                    end = Offset(canvasWidth, canvasHeight),
                                    strokeWidth = 5f
                                )
                            }
                        }
                    }

                    // Floating overlays depending on content resource types
                    if (post.contentImage == "attached_image") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(14.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(ObsidianDark.copy(alpha = 0.75f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Camera check", tint = RazorTeal, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "LIVE SNAPSHOT 4K", color = RazorTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (post.contentImage == "attached_audio") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(30.dp))
                                .background(Color.Black.copy(alpha = 0.8f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayCircle, contentDescription = "Play sound stream", tint = RazorTeal, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "PLAY SOUND LOG (0:42)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (post.contentImage == "attached_video") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(ObsidianDark.copy(alpha = 0.75f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Red))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "HIGH-FI STREAM ACTIVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Page 1 Floating tag for Brand Location coffee place
                    if (post.contentImage == "latte_art") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(ObsidianDark.copy(alpha = 0.75f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.LocalOffer, contentDescription = "Tag symbol", tint = RazorTeal, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Blue Bottle", color = RazorTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Text(
                text = post.caption,
                style = MaterialTheme.typography.bodyMedium,
                color = LightText,
                modifier = Modifier.padding(14.dp)
            )

            // Dynamic bottom row items - Page 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            hasUserLiked = !hasUserLiked
                            onLike()
                            coroutineScope.launch {
                                scale.snapTo(1f)
                                scale.animateTo(
                                    targetValue = 1.4f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioHighBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                                scale.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (hasUserLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like button",
                        tint = if (hasUserLiked) InstaPink else GrayText,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale.value
                                scaleY = scale.value
                            }
                            .size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (post.likesCount >= 1000) {
                            String.format("%.1fk", post.likesCount / 1000.0)
                        } else {
                            post.likesCount.toString()
                        },
                        color = LightText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ChatBubbleOutline, contentDescription = "Comments button", tint = GrayText, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "84", color = LightText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Icon(imageVector = Icons.Default.Share, contentDescription = "Share post", tint = LightText, modifier = Modifier.size(20.dp))

                // Disabled Tips action button with Coming Soon sticker
                Box(contentAlignment = Alignment.TopEnd) {
                    Button(
                        onClick = { /* Inactive */ },
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RazorBlue.copy(alpha = 0.15f),
                            disabledContainerColor = RazorBlue.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp).testTag("pay_5_tip_btn")
                    ) {
                        Text(text = "Pay $5", color = GrayText, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                    
                    Box(
                        modifier = Modifier
                            .offset(x = 4.dp, y = (-6).dp)
                            .background(Color(0xFFEA739B), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text("SOON", color = Color.Black, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                text = "View all 84 comments",
                style = MaterialTheme.typography.bodySmall,
                color = GrayText,
                modifier = Modifier.padding(start = 14.dp, bottom = 14.dp)
            )
        }
    }
}

// ============================================
// SCREEN 2: CREATIVE STUDIO DIALOG PANEL (Page 2)
// ============================================
@Composable
fun CreativeStudioDialog(onDismiss: () -> Unit, onPublish: (String) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        var captionText by remember { mutableStateOf("") }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianDark)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top header bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Creative studio", tint = Color.White)
                    }
                    Text(
                        text = "CREATIVE STUDIO",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(RazorTeal)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "ACTIVE", color = RazorTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Centered skateboard overlay canvas preview - Page 2
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF150F26)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Drawing futuristic skater vector lines on Canvas
                        val w = size.width
                        val h = size.height
                        
                        // draw sunset rays in cyber tech layout
                        for (i in 0..10) {
                            drawLine(
                                color = Color(0xFFFF0055).copy(alpha = 0.05f * i),
                                start = Offset(w / 2, h),
                                end = Offset((w / 10) * i, 0f),
                                strokeWidth = 3f
                            )
                        }
                        
                        // Draw skater trajectory path
                        val trail = Path().apply {
                            moveTo(0f, h * 0.7f)
                            quadraticTo(w * 0.4f, h * 0.5f, w * 0.8f, h * 0.8f)
                            lineTo(w, h * 0.75f)
                        }
                        drawPath(
                            path = trail,
                            color = RazorBlue,
                            style = Stroke(width = 8f)
                        )
                    }

                    // Floating Right Toolbar with actions - Page 2
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(vertical = 12.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SidebarAction(Icons.Default.TextFields, "Sizes")
                        SidebarAction(Icons.Default.MusicNote, "Audio")
                        SidebarAction(Icons.Default.AutoFixHigh, "Effects")
                        SidebarAction(Icons.Default.Layers, "Layers")
                        SidebarAction(Icons.Default.CoPresent, "Co-author")
                    }
                    
                    Text(
                        text = "CREATIIVE STUDIO    ● OO UNE",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)
                    )
                }

                // Bottom Timeline Sequence selector - Page 2
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1C132E))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0:38", color = GrayText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text("0:23", color = RazorBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("0:30", color = GrayText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    
                    // Video blocks simulation row
                    Row(
                        modifier = Modifier
                             .fillMaxWidth()
                             .height(44.dp)
                             .border(1.dp, GrayText.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                             .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(Color(0xFF5D4037)))
                        Box(modifier = Modifier.weight(1.5f).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(RazorBlue.copy(alpha = 0.7f)))
                        Box(modifier = Modifier.weight(1.2f).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(Color(0xFF455A64)))
                    }
                }

                // Cyber Slate Story Caption Input Box
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E173C)),
                    border = BorderStroke(1.dp, MinimalBorder.copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "STORY CAPTION",
                            color = RazorBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box {
                            BasicTextField(
                                value = captionText,
                                onValueChange = { captionText = it },
                                textStyle = LocalTextStyle.current.copy(color = LightText),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (captionText.isEmpty()) {
                                Text(
                                    text = "Share what's on your mind... (e.g., Grinding core halfpipes! 🛹⚡)",
                                    color = GrayText,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Core dialog bottom actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (captionText.isNotBlank()) {
                                onPublish("[DRAFT SAVED] " + captionText)
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F293D)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Draft symbol", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Draft", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (captionText.isNotBlank()) {
                                onPublish(captionText)
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Next", color = Color.Black, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Forward step", tint = Color.Black, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SidebarAction(icon: ImageVector, title: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(20.dp))
        Text(text = title, color = GrayText, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

// ============================================
// SCREEN 3: CREATORS LISTING (Page 7 linkable)
// ============================================
@Composable
fun CreatorsScreen(
    creators: List<Creator>,
    subscriptions: List<Subscription>,
    onCreatorClick: (Creator) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MinimalSurfaceContainer)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Discover Global Talent",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = RazorBlue
                )
                Text(
                    text = "Micro-transactions enabled. Secure electronic subscriptions powered by local blockchain ledger routing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayText,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(creators, key = { it.id }) { creator ->
                val activeSub = subscriptions.find { it.creatorId == creator.id }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .clickable { onCreatorClick(creator) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, MinimalBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(RazorBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = creator.name.take(1).uppercase(),
                                color = RazorBlue,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = creator.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = LightText
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified creator",
                                    tint = RazorTeal,
                                    modifier = Modifier.size(16.dp)
                               )
                            }
                            Text(
                                text = "@${creator.handle}",
                                style = MaterialTheme.typography.bodySmall,
                                color = GrayText
                            )
                            Text(
                                text = creator.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = LightText.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            if (activeSub != null) {
                                Badge(
                                    containerColor = RazorTeal.copy(alpha = 0.2f),
                                    contentColor = RazorTeal
                                ) {
                                    Text(
                                        text = activeSub.tierName,
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "$${creator.bronzeTierPrice}/mo",
                                    color = RazorBlue,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = "Bronze",
                                    color = GrayText,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ========================================================
// SCREEN 4: MARKETPLACE - REPLACES TICKETS TAB (Page 5)
// ========================================================
@Composable
fun LiveEventsScreen(
    events: List<Event>,
    onBuyTicketClick: (Event) -> Unit,
    onCreatorDetail: (String) -> Unit,
    walletBalance: Double,
    onPurchaseDirect: (String, Double) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) } // "Trending", "Stickers", "VIP Access"

    val shopItems = listOf(
        Triple("Liquid Dreams #42", Pair("@VibeMaster", 25.00), Pair("liquid_dreams", "Trending")),
        Triple("Flow State", Pair("@ArtFlow", 10.00), Pair("flow_state", "Stickers")),
        Triple("Club 33 Premium", Pair("@ClubOwner", 500.00), Pair("club_33", "VIP Access")),
        Triple("Glitch Emoji Pack", Pair("@RetroBoy", 5.00), Pair("glitch_pack", "Stickers"))
    )

    val filteredItems = shopItems.filter { item ->
        val matchesCategory = selectedCategory == null || item.third.second == selectedCategory
        val matchesSearch = searchQuery.isBlank() || 
            item.first.contains(searchQuery, ignoreCase = true) || 
            item.second.first.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Drop head banner - Search creators, drops, stickers...
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search creators, drops, stickers...", color = GrayText) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GrayText) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RazorBlue,
                    unfocusedBorderColor = MinimalBorder,
                    unfocusedContainerColor = MinimalSurfaceContainer,
                    focusedContainerColor = MinimalSurfaceContainer
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
                readOnly = false
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // Category scroll tags - Page 5
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    AssistChip(
                        onClick = { selectedCategory = if (selectedCategory == "Trending") null else "Trending" },
                        label = { Text("🔥 Trending", color = if (selectedCategory == "Trending") Color.Black else LightText, fontWeight = FontWeight.Bold) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selectedCategory == "Trending") Color(0xFFFF6D00) else Color(0xFFFF6D00).copy(alpha = 0.15f)
                        )
                    )
                }
                item {
                    AssistChip(
                        onClick = { selectedCategory = if (selectedCategory == "Stickers") null else "Stickers" },
                        label = { Text("🔮 Stickers", color = if (selectedCategory == "Stickers") Color.Black else LightText, fontWeight = FontWeight.Bold) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selectedCategory == "Stickers") RazorBlue else MinimalSurfaceContainer
                        )
                    )
                }
                item {
                    AssistChip(
                        onClick = { selectedCategory = if (selectedCategory == "VIP Access") null else "VIP Access" },
                        label = { Text("⭐ VIP Access", color = if (selectedCategory == "VIP Access") Color.Black else LightText, fontWeight = FontWeight.Bold) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selectedCategory == "VIP Access") SafeGold else MinimalSurfaceContainer
                        )
                    )
                }
            }
        }

        // Featured Drop Card - Page 5
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.5.dp, RazorBlue)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(InstaPink, MinimalDeepPurple)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Badge(containerColor = SafeGold, contentColor = Color.Black) {
                            Text("LIMITED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Live Now", color = RazorTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Neon Genesis Collection",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "by @PixelQueen", color = GrayText, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { onCreatorDetail("pixel_queen") },
                        colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Explore", color = Color.Black, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Fresh vibes grid - Page 5
        Text(
            text = "Fresh Vibes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LightText,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 12.dp)
        )

        // Custom grid list layout representing virtual shop goods
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val chunkedItems = filteredItems.chunked(2)
            chunkedItems.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    rowItems.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            ShopItemCard(
                                title = item.first,
                                creator = item.second.first,
                                cost = item.second.second,
                                imageTemplate = item.third.first,
                                onBuy = { onPurchaseDirect(item.first, item.second.second) }
                            )
                        }
                    }
                    if (rowItems.size == 1) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ShopItemCard(
    title: String,
    creator: String,
    cost: Double,
    imageTemplate: String,
    onBuy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, MinimalBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFF130E22))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    when (imageTemplate) {
                        "liquid_dreams" -> {
                            // draw smooth organic waves
                            val p = Path().apply {
                                moveTo(0f, h * 0.7f)
                                cubicTo(w * 0.3f, h * 0.2f, w * 0.7f, h * 0.9f, w, h * 0.4f)
                                lineTo(w, h)
                                lineTo(0f, h)
                            }
                            drawPath(p, Brush.linearGradient(colors = listOf(RazorBlue, InstaPink)))
                        }
                        "flow_state" -> {
                            // draw neon loops
                            for (i in 1..5) {
                                drawCircle(
                                    color = RazorTeal.copy(alpha = 0.15f * i),
                                    radius = (i * 12).dp.toPx(),
                                    center = Offset(w / 2, h / 2),
                                    style = Stroke(width = 3f)
                                )
                            }
                        }
                        "club_33" -> {
                            // draw golden vip card layout
                            drawRoundRect(
                                color = SafeGold.copy(alpha = 0.8f),
                                topLeft = Offset(w * 0.15f, h * 0.15f),
                                size = this.size * 0.7f,
                                style = Stroke(width = 4f)
                            )
                            drawCircle(
                                color = SafeGold,
                                radius = 15f,
                                center = Offset(w/2, h/2)
                            )
                        }
                        "glitch_pack" -> {
                            // draw retro glitch layout
                            drawRect(
                                color = Color(0xFFFE2E93).copy(alpha = 0.5f),
                                topLeft = Offset(w * 0.2f, h * 0.2f),
                                size = this.size * 0.6f
                            )
                            drawLine(
                                color = RazorTeal,
                                start = Offset(0f, h/2),
                                end = Offset(w, h/2),
                                strokeWidth = 8f
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = LightText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = creator, color = GrayText, fontSize = 11.sp)
                
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format("%.2f", cost)}",
                        color = RazorTeal,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                    
                    Box(contentAlignment = Alignment.TopEnd) {
                        IconButton(
                            onClick = { /* Inactive */ },
                            enabled = false,
                            modifier = Modifier
                                .size(28.dp)
                                .background(MinimalLavenderContainer.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = RazorBlue.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                        }
                        
                        Box(
                            modifier = Modifier
                                .offset(x = 6.dp, y = (-6).dp)
                                .background(Color(0xFFEA739B), RoundedCornerShape(4.dp))
                                .padding(horizontal = 3.dp, vertical = 1.dp)
                        ) {
                            Text("SOON", color = Color.Black, fontSize = 5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// SCREEN 5: MESSAGES & GROUP PAYMENT (Page 3 & 4)
// ============================================
@Composable
fun ChatScreen(
    chatMessages: List<ChatMessage>,
    creators: List<Creator>,
    encryptionEnabled: Boolean,
    onToggleEncryption: () -> Unit,
    onSendMessage: (String, String) -> Unit,
    onPayInvoice: (ChatMessage) -> Unit,
    onDeclineInvoice: (ChatMessage) -> Unit,
    initialRecipient: String? = null
) {
    var activeThreadRecipient by remember(initialRecipient) { mutableStateOf<String?>(initialRecipient) }
    var inputMessage by remember { mutableStateOf("") }
    var settleUpStatePaid by remember { mutableStateOf(false) }

    if (activeThreadRecipient == null) {
        // Page 3: Screen of Recent chats and pending invoices
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Messages",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = LightText
                )
                Row {
                    IconButton(onClick = {}) { Icon(imageVector = Icons.Default.MoreVert, contentDescription = null, tint = LightText) }
                }
            }

            // Segment Tabs friends vs global
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1C132E))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2E1C44))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("FRIENDS", color = RazorTeal, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("GLOBAL", color = GrayText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Pending Action card - Request from Alex (Page 3)
            val pendingDineMsg = chatMessages.find { it.isFinancialRequest && it.paymentStatus == "NONE" }
            if (pendingDineMsg != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, RazorTeal.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2C1E4E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Payment, contentDescription = null, tint = RazorTeal)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Request from Alex", fontWeight = FontWeight.Bold, color = LightText, fontSize = 14.sp)
                                Text("$45.00 for Dinner", color = GrayText, fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = { onPayInvoice(pendingDineMsg) },
                            colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Pay Now", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }

            Text(
                text = "RECENT CHATS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GrayText,
                modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 8.dp)
            )

            // Dynamic listed chats mapping Page 3 mockup list of elements
            LazyColumn(modifier = Modifier.weight(1f)) {
                // Alex Rivera Chat Row
                item {
                    ChatConversationRow(
                        title = "ALEX RIVERA",
                        sub = "Got the transfer, thanks ...",
                        time = "10:42 AM",
                        indicator = RazorTeal,
                        onOpen = { activeThreadRecipient = "Alex Rivera" }
                    )
                }
                // Sarah Chen Chat Row
                item {
                    ChatConversationRow(
                        title = "SARAH CHEN",
                        sub = "Are we still meeting at 5 for ...",
                        time = "Yesterday",
                        indicator = Color(0xFFFFB300),
                        onOpen = { activeThreadRecipient = "Sarah Chen" }
                     )
                }
                // Group: Crypto Degens - Page 3
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { activeThreadRecipient = "Tokyo Trip" },
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.5.dp, RazorBlue)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF32235E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = "Group Locked symbol", tint = RazorBlue, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("CRYPTO DEGENS", fontWeight = FontWeight.Bold, color = LightText)
                                Text("@mihe: secure key updated for ...", color = RazorTeal, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("2m ago", color = GrayText, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(RazorTeal, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("3", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                // David Kim
                item {
                    ChatConversationRow(
                        title = "DAVID KIM",
                        sub = "Sent an attachment.",
                        time = "Tue",
                        indicator = null,
                        onOpen = { activeThreadRecipient = "David Kim" }
                    )
                }
                // Elena Rodriguez
                item {
                    ChatConversationRow(
                        title = "ELENA RODRIGUEZ",
                        sub = "See you at the workshop!",
                        time = "Mon",
                        indicator = null,
                        onOpen = { activeThreadRecipient = "Elena Rodriguez" }
                    )
                }
            }
        }
    } else {
        // Page 4: Active Chat Thread Room detail screen
        Column(modifier = Modifier.fillMaxSize()) {
            val recipientName = activeThreadRecipient ?: ""
            
            // Header Room thread
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B112D))
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { activeThreadRecipient = null }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back messages list", tint = Color.White)
                }
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (recipientName == "Tokyo Trip") "Tokyo Trip 🇯🇵" else recipientName,
                        fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp
                    )
                    Text(text = "ACTIVE GROUP", color = RazorTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                
                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Group Settings", tint = Color.White)
                }
            }

            // Stat indicators double column showing metrics - Page 4
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF130E22))
                    .padding(vertical = 10.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("TOTAL SPENT", color = GrayText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("$1,450.00", color = LightText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("YOU OWE", color = GrayText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (settleUpStatePaid) "$0.00" else "-$120.00",
                        color = Color(0xFFFFEB3B),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Filter messages for current recipient thread dynamically
            val threadMessages = chatMessages.filter {
                (it.senderName == recipientName && it.receiverName == "You") ||
                (it.senderName == "You" && it.receiverName == recipientName) ||
                (recipientName == "Tokyo Trip" && it.receiverName == "Tokyo Trip")
            }

            // Messages chat stack list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (recipientName == "Tokyo Trip") {
                    // Tokyo Trip historical message cards seed
                    item {
                        ChatBubble(
                            sender = "Alex",
                            text = "Hey guys, I just booked the bullet train tickets for Kyoto! 🚄",
                            time = "4:20 PM",
                            isMe = false
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            border = BorderStroke(1.dp, MinimalBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(RazorBlue.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Train, contentDescription = null, tint = RazorBlue)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Shinkansen Tickets", fontWeight = FontWeight.Bold, color = LightText, fontSize = 13.sp)
                                        Text("Paid by Alex • Split equally", color = GrayText, fontSize = 11.sp)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("$320.00", color = LightText, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                    Text("You owe $120.00", color = RazorTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        ChatBubble(
                            sender = "You",
                            text = "Thanks for handling that, Did anyone grab coffee yet? ☕️",
                            time = "4:22 PM",
                            isMe = true
                        )
                    }
                }

                // Render dynamic messages for the recipient thread
                items(threadMessages) { msg ->
                    val isMe = msg.senderName == "You"
                    val isEncrypted = msg.isEncrypted
                    val contentDisclosed = if (isEncrypted) {
                        if (encryptionEnabled) {
                            try {
                                String(android.util.Base64.decode(msg.encryptedContent, android.util.Base64.DEFAULT), Charsets.UTF_8).trim()
                            } catch (e: Exception) {
                                msg.encryptedContent
                            }
                        } else {
                            msg.encryptedContent
                        }
                    } else {
                        msg.encryptedContent
                    }

                    if (msg.isFinancialRequest) {
                        // Display secure Invoice request Card directly in thread
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            border = BorderStroke(1.dp, if (msg.paymentStatus == "PAID") RazorTeal else RazorBlue),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (msg.paymentStatus == "PAID") "PAYMENT REQUEST (PAID ✅)" else "PAYMENT REQUEST (PENDING ⏳)",
                                            fontWeight = FontWeight.Black,
                                            color = if (msg.paymentStatus == "PAID") RazorTeal else RazorBlue,
                                            fontSize = 10.sp
                                        )
                                        Text("Dinner split bill request from Alex", fontWeight = FontWeight.Bold, color = LightText, fontSize = 14.sp)
                                        Text("Amount due: $${String.format("%.2f", msg.amountRequested)}", color = GrayText, fontSize = 12.sp)
                                    }
                                    if (msg.paymentStatus == "NONE") {
                                        Button(
                                            onClick = { onPayInvoice(msg) },
                                            colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Pay Now", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                        }
                                    } else {
                                        Badge(containerColor = if (msg.paymentStatus == "PAID") RazorTeal.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f)) {
                                            Text(
                                                text = msg.paymentStatus, 
                                                color = if (msg.paymentStatus == "PAID") RazorTeal else Color.Red, 
                                                fontSize = 9.sp, 
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        ChatBubble(
                            sender = msg.senderName,
                            text = if (isEncrypted && encryptionEnabled) "🔒 $contentDisclosed" else contentDisclosed,
                            time = "Secure Log",
                            isMe = isMe
                        )
                    }
                }
            }

            // Bottom Settle up card action if owed money exists - Page 4
            if (!settleUpStatePaid) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, RazorBlue.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Settle Up", fontWeight = FontWeight.Bold, color = LightText)
                            Text("Use wallet to pay Alex directly", color = GrayText, fontSize = 11.sp)
                        }
                        
                        Button(
                            onClick = {
                                settleUpStatePaid = true
                                onSendMessage("Tokyo Trip", "✅ Settled up my part of Shinkansen Tickets ($120.00) !")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Settle Up $120.00", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Typing block input field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D081C))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) { Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = LightText) }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF261D3A))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    BasicTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        textStyle = LocalTextStyle.current.copy(color = LightText),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (inputMessage.isEmpty()) {
                        Text("Type a message...", color = GrayText, fontSize = 14.sp)
                    }
                }

                IconButton(
                    onClick = {
                        if (inputMessage.isNotBlank()) {
                            onSendMessage(recipientName, inputMessage)
                            inputMessage = ""
                        }
                    },
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .background(RazorTeal, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send message", tint = Color.Black, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun ChatConversationRow(
    title: String,
    sub: String,
    time: String,
    indicator: Color?,
    onOpen: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(46.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1F2E)),
                contentAlignment = Alignment.Center
            ) {
                Text(title.take(1).uppercase(), color = LightText, fontWeight = FontWeight.Bold)
            }
            if (indicator != null) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(indicator)
                        .border(2.dp, ObsidianDark, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, color = LightText, fontSize = 14.sp)
            Text(text = sub, color = GrayText, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(text = time, color = GrayText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ChatBubble(
    sender: String,
    text: String,
    time: String,
    isMe: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isMe) RazorTeal else Color(0xFF261D3A)
            ),
            modifier = Modifier.widthIn(max = 260.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isMe) {
                    Text(text = sender, fontWeight = FontWeight.Black, color = RazorBlue, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = text,
                    color = if (isMe) Color.Black else Color.White,
                    fontSize = 13.sp,
                    fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
        Text(text = time, color = GrayText, fontSize = 9.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

// ============================================
// SCREEN 6: VIRTUAL WALLET INTEGRATION (Page 6)
// ============================================
@Composable
fun WalletScreen(
    balance: Double,
    transactions: List<Transaction>,
    userName: String,
    userHandle: String,
    onUpdateProfile: (String, String) -> Unit,
    onLoadFunds: (Double) -> Unit,
    onPayPizzaSplit: () -> Unit,
    pizzaSplitPaid: Boolean,
    posts: List<Post>,
    subscriptions: List<Subscription>,
    creators: List<Creator>,
    onLikePost: (Post) -> Unit,
    onCancelSubscription: (String, String) -> Unit
) {
    var isBalanceVisible by remember { mutableStateOf(true) }
    var showAnalyticsPanel by remember { mutableStateOf(false) }
    var showProfileEditor by remember { mutableStateOf(false) }
    var showTopUpDialog by remember { mutableStateOf(false) }
    var showSendMoneyDialog by remember { mutableStateOf(false) }
    var showRequestMoneyDialog by remember { mutableStateOf(false) }
    var showSplitDialog by remember { mutableStateOf(false) }
    var successMessageToast by remember { mutableStateOf<String?>(null) }
    var activeProfileTab by remember { mutableStateOf("wallet") }

    var editName by remember(userName) { mutableStateOf(userName) }
    var editHandle by remember(userHandle) { mutableStateOf(userHandle) }

    if (showAnalyticsPanel) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showAnalyticsPanel = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                border = BorderStroke(1.dp, RazorBlue)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "MONETARY ANALYTICS", color = RazorBlue, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Your transaction flows are distributed 73% towards premium tips & subscription tiers, and 27% to live event tickets.",
                        color = LightText,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { showAnalyticsPanel = false },
                        colors = ButtonDefaults.buttonColors(containerColor = RazorTeal)
                    ) {
                        Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showProfileEditor) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showProfileEditor = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                border = BorderStroke(1.5.dp, RazorTeal),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Edit Profile Profile Details", color = RazorTeal, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Display Name", color = GrayText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RazorBlue,
                            unfocusedBorderColor = MinimalBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("User Handle", color = GrayText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = editHandle,
                        onValueChange = { editHandle = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RazorBlue,
                            unfocusedBorderColor = MinimalBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showProfileEditor = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color.White)
                        }
                        Button(
                            onClick = {
                                if (editName.isNotBlank() && editHandle.isNotBlank()) {
                                    onUpdateProfile(editName, editHandle)
                                    successMessageToast = "Profile Updated successfully! 🎉"
                                    showProfileEditor = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Welcomer bar with small bell and analytics icon links - Page 6
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("WELCOME BACK", color = GrayText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(userName, color = LightText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { showProfileEditor = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile Info", tint = RazorTeal, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Row {
                IconButton(onClick = { showAnalyticsPanel = true }) { Icon(imageVector = Icons.Default.Analytics, contentDescription = "Analytics Panel", tint = RazorTeal) }
                IconButton(onClick = { successMessageToast = "No new system notifications." }) { Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notification check", tint = LightText) }
            }
        }

        // Tab Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .background(Color(0xFF130E29), RoundedCornerShape(12.dp))
                .border(1.dp, RazorBlue.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Button(
                onClick = { activeProfileTab = "wallet" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeProfileTab == "wallet") RazorBlue else Color.Transparent,
                    contentColor = if (activeProfileTab == "wallet") Color.Black else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp),
                elevation = null
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = "Wallet panel",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Secure Wallet", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Button(
                onClick = { activeProfileTab = "profile" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeProfileTab == "profile") RazorTeal else Color.Transparent,
                    contentColor = if (activeProfileTab == "profile") Color.Black else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp),
                elevation = null
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Creator Profile Hub",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("My Creator Hub", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (activeProfileTab == "wallet") {
            // Numeric Balance details - Page 6 (Marked inactive/SOON)
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text("Total Balance", color = GrayText, fontSize = 12.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isBalanceVisible) "$${String.format("%,.2f", balance)}" else "$ ••••••",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = LightText.copy(alpha = 0.25f)
                        )
                        
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEA739B), RoundedCornerShape(6.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("COMING SOON", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { isBalanceVisible = !isBalanceVisible }, enabled = false) {
                        Icon(
                            imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, 
                            contentDescription = "Toggle Balance visibility", 
                            tint = GrayText.copy(alpha = 0.25f)
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.alpha(0.5f)
                ) {
                    Text("Analytics ", color = RazorTeal.copy(alpha = 0.5f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = RazorTeal.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                }
            }
        }

        // Stunning Dark High-Fidelity VISA Credit Card Canvas - Page 6
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(180.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF2C194D), Color(0xFF0D091B))
                        )
                    )
                    .padding(20.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Wireless network waves vector drawing
                    drawCircle(
                        color = Color.White.copy(alpha = 0.04f),
                        radius = 200f,
                        center = Offset(size.width, 0f)
                    )
                }

                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Payment, contentDescription = null, tint = LightText.copy(alpha = 0.6f))
                        Text("VISA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp, fontFamily = FontFamily.SansSerif)
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("● ● ● ●   ● ● ● ●   ● ● ● ●   4589", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Badge(containerColor = RazorTeal) {
                            Text("VIRTUAL", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("CARD HOLDER", color = GrayText, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text(userName.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("EXPIRES", color = GrayText, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text("09/28", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Wallet Pill Actions Send, Request, Split, Settle - Page 6
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            WalletActivityBtn(icon = Icons.Default.ArrowOutward, title = "Send", onClick = { })
            WalletActivityBtn(icon = Icons.Default.SouthEast, title = "Request", onClick = { })
            WalletActivityBtn(icon = Icons.Default.CallSplit, title = "Split", onClick = { })
            
            // Neon Green Action for funding - Page 6
            Box(contentAlignment = Alignment.TopEnd) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.alpha(0.5f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(RazorTeal.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Top up cash", tint = Color.Black.copy(alpha = 0.5f))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Top Up", color = LightText.copy(alpha = 0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Coming Soon Tag on top right
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = (-4).dp)
                        .background(Color(0xFFEA739B), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text("SOON", color = Color.Black, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Mario Pizza billing Split element card section - Page 6
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, MinimalBorder.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Pending Split: Mario's Pizza", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Total bill: $84.50 • You owe: $21.12", color = GrayText, fontSize = 12.sp)
                    }
                    Badge(containerColor = if (pizzaSplitPaid) RazorTeal else InstaPink) {
                        Text(
                            text = if (pizzaSplitPaid) "PAID" else "UNPAID", 
                            color = Color.Black, 
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Simulated lists of avatars
                    Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
                        listOf("A", "M", "Y").forEach { text ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(RazorBlue)
                                    .border(1.5.dp, ObsidianDark, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = text, color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (!pizzaSplitPaid) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Button(
                                onClick = { },
                                enabled = false,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RazorBlue.copy(alpha = 0.15f),
                                    disabledContainerColor = RazorBlue.copy(alpha = 0.15f),
                                    disabledContentColor = Color.White.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Pay Now", color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Black)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .offset(x = 2.dp, y = (-8).dp)
                                    .background(Color(0xFFEA739B), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("SOON", color = Color.Black, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Recent transactions activity log - Page 6
        Text(
            text = "Recent Activity",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LightText,
            modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (transactions.isEmpty()) {
                RecentActivityItem("Netflix Subscription", "TODAY, 9:41 AM", "-$15.00", Color(0xFFFF5252))
                RecentActivityItem("Transfer from Alex", "YESTERDAY, 4:20 PM", "+$50.00", RazorTeal)
                RecentActivityItem("Starbucks Coffee", "YESTERDAY, 8:15 AM", "-$5.40", Color(0xFFFF5252))
                RecentActivityItem("Nike Store", "MON, 12 OCT", "-$120.00", Color(0xFFFF5252))
            } else {
                // Take up to 8 of the latest database transactions
                transactions.sortedByDescending { it.timestamp }.take(8).forEach { tx ->
                    val isFundOrCredit = tx.type == "WALLET_FUND" || tx.type == "TRANSFER_IN"
                    val amountFormatted = if (isFundOrCredit) "+$${String.format("%.2f", tx.amount)}" else "-$${String.format("%.2f", tx.amount)}"
                    val amountColor = if (isFundOrCredit) RazorTeal else Color(0xFFFF5252)
                    RecentActivityItem(
                        title = tx.description,
                        time = "SECURE LEDGER - SUCCESS",
                        amt = amountFormatted,
                        amtColor = amountColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        } else {
            // Personal Creator Profile Component!
            // 1. Creator Stats for this user
            Text(
                text = "Personal Profile Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LightText,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatsCardBox(title = "$185K", sub = "LIQUID WEIGHT", modifier = Modifier.weight(1f))
                StatsCardBox(title = "486", sub = "SUBSCRIBED FANS", modifier = Modifier.weight(1f))
                StatsCardBox(title = "992", sub = "REPUTATION SCORE", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Active Subscription Tiers Offered to Fans & Memberships Purchased
            Text(
                text = "Subscriptions Offered & Purchased",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LightText,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
            )

            var fanBronzePrice by remember { mutableStateOf("4.99") }
            var fanSilverPrice by remember { mutableStateOf("14.99") }
            var fanGoldPrice by remember { mutableStateOf("49.99") }
            var isTiersUpdatedToast by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, MinimalBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Set Access Prices",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Customize access rates required by participants to decrypt your premium nodes.",
                        color = GrayText,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // Tier Row Bronze
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bronze Watcher", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = fanBronzePrice,
                            onValueChange = { fanBronzePrice = it },
                            prefix = { Text("$", color = GrayText, fontSize = 11.sp) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = RazorTeal,
                                unfocusedBorderColor = MinimalBorder
                            ),
                            modifier = Modifier.width(90.dp).height(46.dp),
                            singleLine = true
                        )
                    }

                    // Tier Row Silver
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Silver Analyst", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = fanSilverPrice,
                            onValueChange = { fanSilverPrice = it },
                            prefix = { Text("$", color = GrayText, fontSize = 11.sp) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = RazorTeal,
                                unfocusedBorderColor = MinimalBorder
                            ),
                            modifier = Modifier.width(90.dp).height(46.dp),
                            singleLine = true
                        )
                    }

                    // Tier Row Gold
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gold Partner", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = fanGoldPrice,
                            onValueChange = { fanGoldPrice = it },
                            prefix = { Text("$", color = GrayText, fontSize = 11.sp) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = RazorTeal,
                                unfocusedBorderColor = MinimalBorder
                            ),
                            modifier = Modifier.width(90.dp).height(46.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { isTiersUpdatedToast = true },
                        colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Fan Tier Rates", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }

            if (isTiersUpdatedToast) {
                androidx.compose.ui.window.Dialog(onDismissRequest = { isTiersUpdatedToast = false }) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                        border = BorderStroke(1.5.dp, RazorTeal),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = RazorTeal, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("FAN TIERS SAVED", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Your personal subscription tier access levels were updated on secure ledgers.", color = LightText, fontSize = 12.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = { isTiersUpdatedToast = false },
                                colors = ButtonDefaults.buttonColors(containerColor = RazorTeal)
                            ) {
                                Text("Acknowledge", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2b. Purchased Tiers / Active Memberships
            Text(
                text = "My Active Memberships",
                color = LightText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 6.dp, bottom = 4.dp)
            )

            if (subscriptions.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, MinimalBorder.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "You are not currently subscribed to any premium creator tiers.",
                        color = GrayText,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subscriptions.forEach { sub ->
                        val matchingCreator = creators.find { it.id == sub.creatorId }
                        val creatorNameLabel = matchingCreator?.name ?: "Premium Node Owner"
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            border = BorderStroke(1.dp, MinimalBorder.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = creatorNameLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                        Badge(containerColor = RazorBlue.copy(alpha = 0.15f), contentColor = RazorBlue) {
                                            Text(text = sub.tierName, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "$${sub.amount}/mo", color = GrayText, fontSize = 10.sp)
                                    }
                                }
                                Button(
                                    onClick = { onCancelSubscription(sub.creatorId, creatorNameLabel) },
                                    colors = ButtonDefaults.buttonColors(containerColor = InstaPink),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    elevation = null,
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. User Posted Content List
            Text(
                text = "My Released Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LightText,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
            )

            val myPosts = posts.filter { it.creatorHandle == userHandle || it.creatorId == "pixel_queen" || it.creatorName == userName }
            if (myPosts.isEmpty()) {
                Card(
                     modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, MinimalBorder.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Launch, contentDescription = null, tint = GrayText.copy(alpha = 0.4f), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Personal Releases Yet", color = GrayText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Any post you publish using the Creative Studio will show up here.", color = GrayText.copy(0.7f), fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp))
                    }
                }
            } else {
                CreatorPostsGrid(
                    posts = myPosts,
                    subscriptions = subscriptions,
                    onLikePost = onLikePost
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showTopUpDialog) {
        var topUpAmount by remember { mutableStateOf("50.00") }
        androidx.compose.ui.window.Dialog(onDismissRequest = { showTopUpDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                border = BorderStroke(1.5.dp, RazorTeal),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Top Up Node Balance", color = RazorTeal, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Select or enter custom loading amount:", color = LightText, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = topUpAmount,
                        onValueChange = { topUpAmount = it },
                        prefix = { Text("$ ") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RazorBlue,
                            unfocusedBorderColor = MinimalBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("10.00", "50.00", "100.00", "250.00").forEach { preset ->
                            Button(
                                onClick = { topUpAmount = preset },
                                colors = ButtonDefaults.buttonColors(containerColor = if (topUpAmount == preset) RazorTeal else Color.DarkGray),
                                modifier = Modifier.weight(1f),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                            ) {
                                Text("$preset", fontSize = 10.sp, color = if (topUpAmount == preset) Color.Black else Color.White, maxLines = 1)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { showTopUpDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color.White)
                        }
                        Button(
                            onClick = {
                                val amt = topUpAmount.toDoubleOrNull() ?: 100.00
                                onLoadFunds(amt)
                                successMessageToast = "Successfully Loaded $${String.format("%.2f", amt)} into Virtual Wallet! 💳"
                                showTopUpDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirm", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }

    if (showSendMoneyDialog) {
        var recipientInput by remember { mutableStateOf("") }
        var sendAmount by remember { mutableStateOf("") }
        androidx.compose.ui.window.Dialog(onDismissRequest = { showSendMoneyDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                border = BorderStroke(1.5.dp, RazorBlue),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Secure Wire Outflow", color = RazorBlue, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Recipient Handle:", color = LightText, fontSize = 12.sp)
                    OutlinedTextField(
                        value = recipientInput,
                        onValueChange = { recipientInput = it },
                        placeholder = { Text("e.g. @marcus_vibe", color = GrayText) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RazorBlue,
                            unfocusedBorderColor = MinimalBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Amount:", color = LightText, fontSize = 12.sp)
                    OutlinedTextField(
                        value = sendAmount,
                        onValueChange = { sendAmount = it },
                        prefix = { Text("$ ") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RazorBlue,
                            unfocusedBorderColor = MinimalBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { showSendMoneyDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color.White)
                        }
                        Button(
                            onClick = {
                                val amt = sendAmount.toDoubleOrNull()
                                if (recipientInput.isNotBlank() && amt != null && amt > 0.0) {
                                    if (balance >= amt) {
                                        onLoadFunds(-amt)
                                        successMessageToast = "Sent $${String.format("%.2f", amt)} successfully to $recipientInput! 🚀"
                                        showSendMoneyDialog = false
                                    } else {
                                        successMessageToast = "Insufficient Balance to wire $${String.format("%.2f", amt)}!"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Send", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showRequestMoneyDialog) {
        var requesterInput by remember { mutableStateOf("") }
        var requestAmount by remember { mutableStateOf("") }
        androidx.compose.ui.window.Dialog(onDismissRequest = { showRequestMoneyDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                border = BorderStroke(1.5.dp, SafeGold),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Request Inbound Credit Flow", color = SafeGold, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("From Account handle:", color = LightText, fontSize = 12.sp)
                    OutlinedTextField(
                        value = requesterInput,
                        onValueChange = { requesterInput = it },
                        placeholder = { Text("e.g. @marcus_vibe", color = GrayText) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RazorBlue,
                            unfocusedBorderColor = MinimalBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Amount:", color = LightText, fontSize = 12.sp)
                    OutlinedTextField(
                        value = requestAmount,
                        onValueChange = { requestAmount = it },
                        prefix = { Text("$ ") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RazorBlue,
                            unfocusedBorderColor = MinimalBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { showRequestMoneyDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color.White)
                        }
                        Button(
                            onClick = {
                                val amt = requestAmount.toDoubleOrNull()
                                if (requesterInput.isNotBlank() && amt != null && amt > 0.0) {
                                    successMessageToast = "Requested $${String.format("%.2f", amt)} from $requesterInput. Request invoice is pending!"
                                    showRequestMoneyDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Request", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showSplitDialog) {
        var splitDescription by remember { mutableStateOf("") }
        var splitAmount by remember { mutableStateOf("") }
        var numSplitters by remember { mutableStateOf("3") }
        androidx.compose.ui.window.Dialog(onDismissRequest = { showSplitDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                border = BorderStroke(1.5.dp, InstaPink),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Secure Node Splitter", color = InstaPink, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Bill Name Details:", color = LightText, fontSize = 12.sp)
                    OutlinedTextField(
                        value = splitDescription,
                        onValueChange = { splitDescription = it },
                        placeholder = { Text("e.g. Dinner with Friends", color = GrayText) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RazorBlue,
                            unfocusedBorderColor = MinimalBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Total amount:", color = LightText, fontSize = 12.sp)
                    OutlinedTextField(
                        value = splitAmount,
                        onValueChange = { splitAmount = it },
                        prefix = { Text("$ ") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RazorBlue,
                            unfocusedBorderColor = MinimalBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Divide among:", color = LightText, fontSize = 12.sp)
                    OutlinedTextField(
                        value = numSplitters,
                        onValueChange = { numSplitters = it },
                        suffix = { Text("members") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RazorBlue,
                            unfocusedBorderColor = MinimalBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { showSplitDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color.White)
                        }
                        Button(
                            onClick = {
                                val amtStr = splitAmount.toDoubleOrNull()
                                val splittersInt = numSplitters.toIntOrNull()
                                if (splitDescription.isNotBlank() && amtStr != null && splittersInt != null && splittersInt > 1) {
                                    val splitShare = amtStr / splittersInt
                                    successMessageToast = "Initiated split of $${String.format("%.2f", amtStr)}. Created pending node payouts of $${String.format("%.2f", splitShare)} each!"
                                    showSplitDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Split Bill", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (successMessageToast != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { successMessageToast = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                border = BorderStroke(1.5.dp, RazorTeal),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = RazorTeal, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(successMessageToast!!, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { successMessageToast = null },
                        colors = ButtonDefaults.buttonColors(containerColor = RazorTeal)
                    ) {
                        Text("Dismiss", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WalletActivityBtn(icon: ImageVector, title: String, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.TopEnd) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(0.5f)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF1B2236), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = LightText.copy(alpha = 0.5f))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, color = GrayText.copy(alpha = 0.5f), fontSize = 12.sp)
        }
        
        Box(
            modifier = Modifier
                .offset(x = 8.dp, y = (-4).dp)
                .background(Color(0xFFEA739B), RoundedCornerShape(4.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text("SOON", color = Color.Black, fontSize = 7.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RecentActivityItem(title: String, time: String, amt: String, amtColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF160F25))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2C1E4E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = RazorBlue, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, color = LightText, fontSize = 13.sp)
                Text(text = time, color = GrayText, fontSize = 11.sp)
            }
        }
        
        Text(text = amt, fontWeight = FontWeight.Black, color = amtColor, fontSize = 14.sp)
    }
}

// ===============================================
// SCREEN 7: INVESTOR PROFILE DETAIL (Page 7)
// ===============================================
@Composable
fun CreatorDetailScreen(
    creator: Creator,
    subscriptions: List<Subscription>,
    posts: List<Post>,
    events: List<Event>,
    onSubscribeClick: (String, Double) -> Unit,
    onCancelSubscription: () -> Unit,
    onFollowToggle: () -> Unit,
    onMessageClick: () -> Unit,
    onLikePost: (Post) -> Unit,
    onBack: () -> Unit
) {
    val activeSub = subscriptions.find { it.creatorId == creator.id }
    
    var selectedBadgeDetail by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showShareToast by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // High fidelity Investor Profile sub header - Page 7
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back back step", tint = LightText)
            }
            Text(
                text = "Investor Profile",
                fontWeight = FontWeight.Black,
                color = LightText,
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Default.MoreHoriz, contentDescription = "More", tint = LightText)
            }
        }

        // Verified photo details centering verified status badge
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(100.dp)) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(RazorBlue.copy(alpha = 0.2f))
                        .border(1.5.dp, RazorBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = creator.name.take(1).uppercase(),
                        fontWeight = FontWeight.Black,
                        color = RazorBlue,
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
                
                // Small dynamic verified checkmark badge overlay - Page 7
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(RazorTeal, CircleShape)
                        .border(2.dp, ObsidianDark, CircleShape)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = CoffeeDarkBrushColorFromTheme, modifier = Modifier.size(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = creator.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color.White)
            Text(text = "@${creator.handle}", color = RazorTeal, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = creator.description,
                textAlign = TextAlign.Center,
                color = LightText.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        // Action Buttons: Follow, Direct Message and Share Portfolio - Page 7
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
         ) {
             Button(
                 onClick = onFollowToggle,
                 colors = ButtonDefaults.buttonColors(
                     containerColor = if (creator.isFollowed) Color(0xFF1B2236) else RazorTeal
                 ),
                 shape = RoundedCornerShape(12.dp),
                 modifier = Modifier.weight(1f),
                 border = if (creator.isFollowed) BorderStroke(1.dp, RazorTeal) else null
             ) {
                 Text(
                     text = if (creator.isFollowed) "Following" else "+ Follow",
                     color = if (creator.isFollowed) RazorTeal else Color.Black,
                     fontWeight = FontWeight.Black,
                     maxLines = 1
                 )
             }

             Button(
                 onClick = onMessageClick,
                 colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C1E4E)),
                 shape = RoundedCornerShape(12.dp),
                 modifier = Modifier.weight(1f)
             ) {
                 Text("Message", color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
             }

             Button(
                 onClick = { showShareToast = true },
                 colors = ButtonDefaults.buttonColors(containerColor = RazorBlue),
                 shape = RoundedCornerShape(12.dp),
                 modifier = Modifier.weight(1f)
             ) {
                 Text("Share", color = Color.Black, fontWeight = FontWeight.Black, maxLines = 1)
             }
         }

        // Row of 3 Stats card details - Page 7
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatsCardBox(title = "$1.2M", sub = "PORTFOLIO VALUE", modifier = Modifier.weight(1f))
            StatsCardBox(title = "14.2K", sub = "FOLLOWERS", modifier = Modifier.weight(1f))
            StatsCardBox(title = "982", sub = "TRUST SCORE", modifier = Modifier.weight(1f))
        }

        // Interactive Tab Selector
        var activeProfileTab by remember { mutableStateOf("posts") }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .background(Color(0xFF130E29), RoundedCornerShape(12.dp))
                .border(1.dp, RazorBlue.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Button(
                onClick = { activeProfileTab = "posts" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeProfileTab == "posts") RazorTeal else Color.Transparent,
                    contentColor = if (activeProfileTab == "posts") Color.Black else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp),
                elevation = null
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Insights posts feed",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Releases", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Button(
                onClick = { activeProfileTab = "reputation" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeProfileTab == "reputation") RazorTeal else Color.Transparent,
                    contentColor = if (activeProfileTab == "reputation") Color.Black else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp),
                elevation = null
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Reputation and access tier pricing",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reputation & Access", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (activeProfileTab == "posts") {
            CreatorPostsGrid(
                posts = posts,
                subscriptions = subscriptions,
                onLikePost = onLikePost
            )
        } else {
            // Financial Reputation panel - Page 7
            Text(
            text = "Financial Reputation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LightText,
            modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, MinimalBorder.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Diamond Tier", fontWeight = FontWeight.Black, color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Badge(containerColor = RazorBlue.copy(alpha = 0.2f), contentColor = RazorBlue) {
                        Text("RANK #12", modifier = Modifier.padding(4.dp), fontWeight = FontWeight.Black, fontSize = 9.sp)
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("850 / 1000", color = GrayText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { 0.85f },
                    color = RazorTeal,
                    trackColor = MinimalSurfaceContainer,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = RazorTeal, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "+15 Reputation Points this week", color = RazorTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Community Badges section - Page 7
        Text(
            text = "Community Badges",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LightText,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BadgeCircularGroup(
                icon = Icons.Default.Star, 
                label = "Top Mentor", 
                color = Color(0xFFFFD54F),
                onClick = {
                    selectedBadgeDetail = "Top Mentor" to "Awarded for outstanding contribution. This investor has published over 50 deep-dive token analyses with a combined 95%+ community rating score!"
                }
            )
            BadgeCircularGroup(
                icon = Icons.Default.WaterDrop, 
                label = "Whale", 
                color = RazorBlue,
                onClick = {
                    selectedBadgeDetail = "Whale" to "Awarded to elite system participants holding and transacting over $100K worth of local nodes and subscription weights."
                }
            )
            BadgeCircularGroup(
                icon = Icons.Default.ElectricBolt, 
                label = "Early Adopter", 
                color = InstaPink,
                onClick = {
                    selectedBadgeDetail = "Early Adopter" to "Awarded to launch pioneers who joined in our pre-beta phase and contributed to initial load node testing."
                }
            )
        }

        // Dynamic Subscription portal to buy tiers via secure routing
        Text(
            text = "Portfolio Tiers",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LightText,
            modifier = Modifier.padding(start = 16.dp, top = 22.dp, bottom = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SubscriptionTierRow(
                tier = "BRONZE",
                price = creator.bronzeTierPrice,
                perks = "Lates daily stock alerts & tech watchlists.",
                active = activeSub?.tierName == "BRONZE",
                onBuy = { onSubscribeClick("BRONZE", creator.bronzeTierPrice) }
            )

            SubscriptionTierRow(
                tier = "SILVER",
                price = creator.silverTierPrice,
                perks = "Exclusive audio transcripts & deep portfolio wiring sheets.",
                active = activeSub?.tierName == "SILVER",
                onBuy = { onSubscribeClick("SILVER", creator.silverTierPrice) }
            )

            SubscriptionTierRow(
                tier = "GOLD",
                price = creator.goldTierPrice,
                perks = "Weekly 1-on-1 portfolio review on secure live rooms.",
                active = activeSub?.tierName == "GOLD",
                onBuy = { onSubscribeClick("GOLD", creator.goldTierPrice) }
            )

            if (activeSub != null) {
                Button(
                    onClick = onCancelSubscription,
                    colors = ButtonDefaults.buttonColors(containerColor = InstaPink),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Cancel Active Subscription tier", fontWeight = FontWeight.Bold)
                }
            }
        }

        } // End of activeProfileTab reputation block

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (selectedBadgeDetail != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { selectedBadgeDetail = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                border = BorderStroke(1.5.dp, RazorBlue),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(selectedBadgeDetail!!.first, fontWeight = FontWeight.Black, color = RazorBlue, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(selectedBadgeDetail!!.second, color = LightText, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { selectedBadgeDetail = null },
                        colors = ButtonDefaults.buttonColors(containerColor = RazorTeal)
                    ) {
                        Text("Awesome!", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showShareToast) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showShareToast = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                border = BorderStroke(1.5.dp, RazorTeal),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = RazorTeal, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Secure Invite Link Copied! 🎉", fontWeight = FontWeight.Black, color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("https://socialhub.com/creators/${creator.handle}", color = GrayText, fontSize = 12.sp, fontFamily = FontFamily.Monospace, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showShareToast = false },
                        colors = ButtonDefaults.buttonColors(containerColor = RazorTeal)
                    ) {
                        Text("Dismiss", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCardBox(title: String, sub: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, MinimalBorder.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontWeight = FontWeight.Black, color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = sub, color = GrayText, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun BadgeCircularGroup(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(color.copy(alpha = 0.15f), CircleShape)
                .border(1.5.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, color = LightText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SubscriptionTierRow(
    tier: String,
    price: Double,
    perks: String,
    active: Boolean,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (active) RazorTeal.copy(alpha = 0.15f) else CardBackground
        ),
        border = BorderStroke(1.dp, if (active) RazorTeal else MinimalBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = tier, fontWeight = FontWeight.Black, color = Color.White, style = MaterialTheme.typography.titleMedium)
                    if (active) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Badge(containerColor = RazorTeal) {
                            Text("ACTIVE", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
                Text(text = perks, color = GrayText, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
            }
            
            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "$${String.format("%.2f", price)}/mo", color = RazorBlue, fontWeight = FontWeight.Black, fontSize = 14.sp)
                if (!active) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(contentAlignment = Alignment.TopEnd) {
                        Button(
                            onClick = { },
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RazorTeal.copy(alpha = 0.15f),
                                disabledContainerColor = RazorTeal.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Unlock", color = GrayText, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                        
                        Box(
                            modifier = Modifier
                                .offset(x = 4.dp, y = (-6).dp)
                                .background(Color(0xFFEA739B), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("SOON", color = Color.Black, fontSize = 6.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Color Utility back compatibility token
val CoffeeDarkBrushColorFromTheme = Color(0xFF070B13)

// ============================================
// CHECKOUT OVERLAYS & LEDGER (Page 1 Tip / Sub / Drops)
// ============================================
@Composable
fun RazorpayCheckoutOverlay(
    checkoutInfo: CheckoutInfo,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.5.dp, RazorBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("checkout_dialog_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Razorpay Ledger Gateway",
                        fontWeight = FontWeight.Bold,
                        color = RazorBlue,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close checkouts", tint = GrayText)
                    }
                }

                Crossfade(targetState = step, label = "PaymentSteps") { currentStep ->
                    when (currentStep) {
                        1 -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = checkoutInfo.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = LightText)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = checkoutInfo.description, color = GrayText, style = MaterialTheme.typography.bodyMedium)
                                
                                Spacer(modifier = Modifier.height(18.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MinimalSurfaceContainer)
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("AUTHORIZED DEBIT TRANSACTION", fontSize = 10.sp, color = GrayText, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "$${String.format("%.2f", checkoutInfo.amount)}",
                                        fontWeight = FontWeight.Black,
                                        color = RazorTeal,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = { step = 2 },
                                    colors = ButtonDefaults.buttonColors(containerColor = RazorBlue),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pay with Virtual Wallet Ledger", color = Color.Black, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                        2 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 30.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = RazorBlue, strokeWidth = 5.dp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = "AUTHENTICATING VIRTUAL LEDGER CARD...", fontWeight = FontWeight.Bold, color = LightText, fontSize = 13.sp)
                                Text(text = "Rerouting securely via central bank ledger API nodes.", fontSize = 10.sp, color = GrayText, textAlign = TextAlign.Center)

                                val scope = rememberCoroutineScope()
                                LaunchedEffect(Unit) {
                                    delay(1800)
                                    step = 3
                                    checkoutInfo.action()
                                }
                            }
                        }
                        3 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(RazorTeal),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = "Success check", tint = Color.Black, modifier = Modifier.size(36.dp))
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = "PAYMENT SUCCESSFUL", fontWeight = FontWeight.Black, color = RazorTeal, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Ledger verified securely. Creator has been routed the funds directly.",
                                    fontSize = 11.sp,
                                    color = GrayText,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = onDismiss,
                                    colors = ButtonDefaults.buttonColors(containerColor = RazorBlue),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Back to Hub", fontWeight = FontWeight.Black, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SystemNotificationPopup(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, MinimalBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = RazorTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = title, fontWeight = FontWeight.Bold, color = LightText, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = message, color = LightText.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = RazorBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("OK", fontWeight = FontWeight.Black, color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun rememberShimmerBrushValue(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    val shimmerColors = listOf(
        ObsidianDark,
        CardBackground,
        ObsidianDark
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim.value, 0f),
        end = Offset(translateAnim.value + 300f, 300f)
    )
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(rememberShimmerBrushValue())
    )
}

@Composable
fun FeedScreenSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        userScrollEnabled = false
    ) {
        // Horizontal Stories Bar Shimmer
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive add button container
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(CardBackground)
                        .border(1.5.dp, RazorBlue.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = RazorBlue, modifier = Modifier.size(24.dp))
                }
                
                // Simulated active creators profiles
                repeat(4) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ShimmerBox(modifier = Modifier.size(56.dp), shape = CircleShape)
                        Spacer(modifier = Modifier.height(6.dp))
                        ShimmerBox(modifier = Modifier.width(48.dp).height(10.dp))
                    }
                }
            }
        }

        // Post Card Skeletons matching actual Feed visual architecture
        items(3) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, MinimalBorder)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header row with details
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShimmerBox(modifier = Modifier.size(42.dp), shape = CircleShape)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            ShimmerBox(modifier = Modifier.width(110.dp).height(14.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            ShimmerBox(modifier = Modifier.width(150.dp).height(10.dp))
                        }
                    }

                    // Content details caption shimmer
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                        ShimmerBox(modifier = Modifier.fillMaxWidth().height(12.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        ShimmerBox(modifier = Modifier.width(220.dp).height(12.dp))
                    }

                    // Large body illustration placeholder shimmer
                    Spacer(modifier = Modifier.height(10.dp))
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        shape = RoundedCornerShape(0.dp)
                    )
                    
                    // Interaction control row shimmer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShimmerBox(modifier = Modifier.width(62.dp).height(24.dp))
                        ShimmerBox(modifier = Modifier.width(62.dp).height(24.dp))
                        Spacer(modifier = Modifier.weight(1f))
                        ShimmerBox(modifier = Modifier.width(84.dp).height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CreatorsScreenSkeleton() {
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MinimalSurfaceContainer),
            border = BorderStroke(1.dp, MinimalBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ShimmerBox(modifier = Modifier.width(180.dp).height(20.dp))
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerBox(modifier = Modifier.fillMaxWidth().height(12.dp))
                Spacer(modifier = Modifier.height(4.dp))
                ShimmerBox(modifier = Modifier.width(260.dp).height(12.dp))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            userScrollEnabled = false
        ) {
            items(4) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, MinimalBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShimmerBox(modifier = Modifier.size(50.dp), shape = CircleShape)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            ShimmerBox(modifier = Modifier.width(120.dp).height(14.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            ShimmerBox(modifier = Modifier.width(80.dp).height(11.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            ShimmerBox(modifier = Modifier.fillMaxWidth(0.9f).height(10.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            ShimmerBox(modifier = Modifier.width(60.dp).height(18.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            ShimmerBox(modifier = Modifier.width(40.dp).height(11.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveEventsScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp)
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(3) { index ->
                    ShimmerBox(
                        modifier = Modifier
                            .width(if (index == 0) 90.dp else if (index == 1) 100.dp else 110.dp)
                            .height(32.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerBox(modifier = Modifier.width(140.dp).height(18.dp))
            ShimmerBox(modifier = Modifier.width(70.dp).height(14.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            repeat(2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    repeat(2) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            border = BorderStroke(1.dp, MinimalBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                ShimmerBox(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp),
                                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                )
                                Column(modifier = Modifier.padding(12.dp)) {
                                    ShimmerBox(modifier = Modifier.width(90.dp).height(12.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    ShimmerBox(modifier = Modifier.width(60.dp).height(10.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        ShimmerBox(modifier = Modifier.width(40.dp).height(14.dp))
                                        ShimmerBox(modifier = Modifier.width(50.dp).height(24.dp), shape = RoundedCornerShape(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreatorDetailScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, MinimalBorder)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ShimmerBox(
                    modifier = Modifier
                        .size(70.dp),
                    shape = CircleShape
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                ShimmerBox(modifier = Modifier.width(130.dp).height(16.dp))
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerBox(modifier = Modifier.width(80.dp).height(11.dp))
                
                Spacer(modifier = Modifier.height(14.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.9f).height(10.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f).height(10.dp))
                }
                
                Spacer(modifier = Modifier.height(18.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(3) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ShimmerBox(modifier = Modifier.width(45.dp).height(15.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            ShimmerBox(modifier = Modifier.width(35.dp).height(9.dp))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(18.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ShimmerBox(modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(12.dp))
            ShimmerBox(modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            ShimmerBox(modifier = Modifier.width(130.dp).height(14.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(3) {
                ShimmerBox(modifier = Modifier.width(90.dp).height(38.dp), shape = RoundedCornerShape(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PermissionsOnboardingDialog(
    onDismiss: () -> Unit,
    onPermissionsGranted: (Boolean) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val systemPermissions = if (android.os.Build.VERSION.SDK_INT >= 33) {
        arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO,
            android.Manifest.permission.READ_MEDIA_AUDIO
        )
    } else {
        arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        onPermissionsGranted(allGranted)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B21)),
            border = BorderStroke(2.dp, RazorTeal)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Stellar icon header
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(RazorTeal.copy(alpha = 0.1f), CircleShape)
                        .border(1.5.dp, RazorTeal, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Permission Security",
                        tint = RazorTeal,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "HARDWARE INTEGRATION",
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = RazorTeal,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Requesting Provisioning",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "To allow direct posting with high-fidelity camera pictures, rich storage media, and microphone sound notes, please enable the required phone permissions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayText,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.White.copy(alpha = 0.1f)))

                // Permissions List with interactive indicators
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PermissionRowItem(
                        icon = Icons.Default.Create,
                        title = "Live Camera Snapshots",
                        desc = "Used for real-time creative photo uploads."
                    )
                    PermissionRowItem(
                        icon = Icons.Default.MusicNote,
                        title = "Microphone Audio Capture",
                        desc = "Allows recording voices, comments & sound narratives."
                    )
                    PermissionRowItem(
                        icon = Icons.Default.Layers,
                        title = "Media Pickers & Storage",
                        desc = "Retrieve media files, videos, and music clips."
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("No, Sandbox Only", color = GrayText, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            launcher.launch(systemPermissions)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("Grant Permissions", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionRowItem(
    icon: ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(RazorBlue.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = RazorBlue, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, color = GrayText, fontSize = 11.sp)
        }
    }
}

// ============================================
// COMPONENT: RESPONSIVE CREATOR POSTS GRID (Page 7 Extensions)
// ============================================
@Composable
fun CreatorPostsGrid(
    posts: List<Post>,
    subscriptions: List<Subscription>,
    onLikePost: (Post) -> Unit
) {
    if (posts.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = null,
                tint = GrayText.copy(alpha = 0.5f),
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No Insights Released Yet",
                color = GrayText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "This creator hasn't published any token insights or portfolio updates.",
                color = GrayText.copy(alpha = 0.7f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )
        }
        return
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        val width = maxWidth
        val columns = if (width < 600.dp) 2 else 3
        val chunkedPosts = posts.chunked(columns)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            chunkedPosts.forEach { rowPosts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowPosts.forEach { post ->
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            GridPostCard(
                                post = post,
                                subscriptions = subscriptions,
                                onLikePost = onLikePost
                            )
                        }
                    }
                    if (rowPosts.size < columns) {
                        repeat(columns - rowPosts.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridPostCard(
    post: Post,
    subscriptions: List<Subscription>,
    onLikePost: (Post) -> Unit
) {
    val hasSubscription = subscriptions.any {
        it.creatorId == post.creatorId &&
        (it.tierName == "GOLD" || 
         (it.tierName == "SILVER" && post.requiredTier != "GOLD") || 
         (it.tierName == "BRONZE" && post.requiredTier == "BRONZE"))
    }
    val isLocked = post.isPremium && !hasSubscription && post.creatorId != "pixel_queen"

    var isLikedLocal by remember { mutableStateOf(false) }
    var likesCountLocal by remember { mutableStateOf(post.likesCount) }
    var showCommentsDialog by remember { mutableStateOf(false) }
    var commentsCount by remember { mutableStateOf(3) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, MinimalBorder.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Visual Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFF130E22)),
                contentAlignment = Alignment.Center
            ) {
                if (isLocked) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked Post content",
                            tint = InstaPink,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${post.requiredTier} Tier Required",
                            color = LightText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        when (post.contentImage) {
                            "attached_image" -> {
                                val cx = canvasWidth / 2
                                val cy = canvasHeight / 2
                                drawCircle(
                                    color = RazorBlue.copy(alpha = 0.15f),
                                    radius = 30.dp.toPx(),
                                    center = Offset(cx, cy)
                                )
                                drawCircle(
                                    color = RazorTeal,
                                    radius = 20.dp.toPx(),
                                    center = Offset(cx, cy),
                                    style = Stroke(width = 1.dp.toPx())
                                )
                                drawLine(color = RazorTeal, start = Offset(cx - 25.dp.toPx(), cy), end = Offset(cx + 25.dp.toPx(), cy), strokeWidth = 1f)
                                drawLine(color = RazorTeal, start = Offset(cx, cy - 25.dp.toPx()), end = Offset(cx, cy + 25.dp.toPx()), strokeWidth = 1f)
                            }
                            "attached_audio" -> {
                                val centerY = canvasHeight / 2
                                val barCount = 7
                                val gap = canvasWidth / (barCount + 1)
                                val heights = listOf(15, 35, 45, 20, 40, 25, 15)
                                for (i in 0 until barCount) {
                                    val barX = gap * (i + 1)
                                    val barH = heights[i % heights.size].dp.toPx()
                                    drawLine(
                                        color = RazorTeal,
                                        start = Offset(barX, centerY - barH / 2),
                                        end = Offset(barX, centerY + barH / 2),
                                        strokeWidth = 3.dp.toPx()
                                    )
                                }
                            }
                            "attached_video" -> {
                                val cx = canvasWidth / 2
                                val cy = canvasHeight / 2
                                drawCircle(
                                    color = InstaPink,
                                    radius = 16.dp.toPx(),
                                    center = Offset(cx, cy),
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                                val path = Path().apply {
                                    val side = 12.dp.toPx()
                                    val startX = cx - side / 3
                                    val startY = cy - side / 2
                                    moveTo(startX, startY)
                                    lineTo(startX + side, cy)
                                    lineTo(startX, startY + side)
                                    close()
                                }
                                drawPath(path = path, color = InstaPink)
                            }
                            "latte_art" -> {
                                val cupCenter = Offset(canvasWidth / 2, canvasHeight / 2)
                                val cupRadius = 30.dp.toPx()
                                drawCircle(
                                    color = Color(0xFFECEFF1).copy(0.8f),
                                    radius = cupRadius,
                                    center = cupCenter,
                                    style = Stroke(width = 3.dp.toPx())
                                )
                                drawCircle(
                                    color = Color(0xFF3E2723),
                                    radius = cupRadius - 2.dp.toPx(),
                                    center = cupCenter
                                )
                            }
                            "music_studio" -> {
                                val pathPoints = listOf(
                                    Offset(0f, canvasHeight * 0.6f),
                                    Offset(canvasWidth * 0.3f, canvasHeight * 0.3f),
                                    Offset(canvasWidth * 0.6f, canvasHeight * 0.7f),
                                    Offset(canvasWidth, canvasHeight * 0.4f)
                                )
                                for (i in 0 until pathPoints.size - 1) {
                                    drawLine(
                                        color = RazorBlue,
                                        start = pathPoints[i],
                                        end = pathPoints[i+1],
                                        strokeWidth = 3f
                                    )
                                }
                            }
                            else -> {
                                drawCircle(
                                    color = RazorBlue.copy(alpha = 0.12f),
                                    radius = 40.dp.toPx(),
                                    center = Offset(canvasWidth / 2, canvasHeight / 2)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Caption Section
            Text(
                text = post.caption,
                color = Color.White,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Interactions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Click Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        isLikedLocal = !isLikedLocal
                        if (isLikedLocal) {
                            likesCountLocal += 1
                        } else {
                            likesCountLocal -= 1
                        }
                        onLikePost(post)
                    }
                ) {
                    Icon(
                        imageVector = if (isLikedLocal) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like release",
                        tint = if (isLikedLocal) Color.Red else Color.White.copy(0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$likesCountLocal",
                        color = Color.White.copy(0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Comment Click Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showCommentsDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Discuss release",
                        tint = Color.White.copy(0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$commentsCount",
                        color = Color.White.copy(0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showCommentsDialog) {
        val commentsList = remember {
            mutableStateListOf<String>().apply {
                addAll(listOf(
                    "This insights release is absolute gold! ⚡",
                    "Incredible detail. Love the cyberpunk theme.",
                    "Can't wait for your next portfolio update!"
                ))
            }
        }
        CommentDialog(
            post = post,
            comments = commentsList,
            onAddComment = { text ->
                commentsList.add(text)
                commentsCount = commentsList.size
            },
            onDismiss = { showCommentsDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentDialog(
    post: Post,
    comments: List<String>,
    onAddComment: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newCommentText by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 450.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B21)),
            border = BorderStroke(1.5.dp, RazorBlue)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DECRYPTED DISCUSSION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = RazorTeal,
                        letterSpacing = 1.5.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(0.6f), modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Post Title/Caption
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(RazorBlue.copy(0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(post.creatorName.take(1).uppercase(), color = RazorBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(post.creatorName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            post.caption,
                            color = GrayText,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Comments
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (comments.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No commentary yet. Be the first!", color = GrayText, fontSize = 12.sp)
                        }
                    } else {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            comments.forEachIndexed { idx, comment ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    val userInitial = when(idx % 3) {
                                        0 -> "J"
                                        1 -> "M"
                                        else -> "A"
                                    }
                                    val bubbleBrush = when(idx % 3) {
                                        0 -> Brush.radialGradient(listOf(RazorBlue, ObsidianDark))
                                        1 -> Brush.radialGradient(listOf(RazorTeal, ObsidianDark))
                                        else -> Brush.radialGradient(listOf(InstaPink, ObsidianDark))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(bubbleBrush),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(userInitial, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(
                                        modifier = Modifier
                                            .background(Color(0xFF130E29), RoundedCornerShape(12.dp))
                                            .padding(10.dp)
                                            .weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = when(idx % 3) {
                                                    0 -> "Joe_Alpha"
                                                    1 -> "Mary_Nodes"
                                                    else -> "Anon_Cyber"
                                                },
                                                color = RazorTeal,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${idx + 1}m ago",
                                                color = GrayText,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(text = comment, color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF130E29), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        decorationBox = { innerTextField ->
                            if (newCommentText.isEmpty()) {
                                Text("Add a node to discussion...", color = GrayText, fontSize = 13.sp)
                            }
                            innerTextField()
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                onAddComment(newCommentText)
                                newCommentText = ""
                            }
                        },
                        modifier = Modifier.size(32.dp),
                        enabled = newCommentText.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Post commentary",
                            tint = if (newCommentText.isNotBlank()) RazorTeal else GrayText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
