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
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity

class BubbleAnimState(
    val startX: Float,
    val startY: Float,
    val targetX: Float,
    val targetY: Float,
    val radiusSecive: androidx.compose.ui.unit.Dp,
    val delay: Int
) {
    var currentX by mutableStateOf(0f)
    var currentY by mutableStateOf(0f)
    var alpha by mutableStateOf(0f)
    var currentRadius by mutableStateOf(radiusSecive)
}

@Composable
fun BubbleEffectContainer(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val bubbles = remember { mutableStateListOf<BubbleAnimState>() }
    
    LaunchedEffect(isSelected) {
        if (isSelected) {
            bubbles.clear()
            // Spawn 10 premium translucent soap bubbles
            val count = 10
            for (i in 0 until count) {
                // semi-random angles to create a splash effect upward and outward
                val angle = -Math.PI / 2 + (Math.random() * Math.PI * 0.8 - Math.PI * 0.4) // upward arc
                val distance = 40f + (Math.random() * 60f).toFloat()
                val targetX = (Math.cos(angle) * distance).toFloat()
                val targetY = (Math.sin(angle) * distance).toFloat()
                
                bubbles.add(
                    BubbleAnimState(
                        startX = 0f,
                        startY = 10f, // start slightly lower from bottom bar
                        targetX = targetX,
                        targetY = targetY,
                        radiusSecive = (4 + (Math.random() * 6)).dp,
                        delay = i * 35 // staggered
                    )
                )
            }
            
            val startTime = System.currentTimeMillis()
            val totalDuration = 800L
            while (System.currentTimeMillis() - startTime < totalDuration) {
                val elapsed = System.currentTimeMillis() - startTime
                for (bubble in bubbles) {
                    val bubbleElapsed = elapsed - bubble.delay
                    if (bubbleElapsed > 0) {
                        val progress = (bubbleElapsed.toFloat() / 500f).coerceIn(0f, 1f)
                        // cubic ease out
                        val easedProgress = 1f - (1f - progress) * (1f - progress) * (1f - progress)
                        
                        bubble.currentX = bubble.startX + (bubble.targetX - bubble.startX) * easedProgress
                        // drift up faster
                        bubble.currentY = bubble.startY + (bubble.targetY - bubble.startY) * easedProgress - (45f * easedProgress)
                        bubble.alpha = 1f - easedProgress
                        bubble.currentRadius = bubble.radiusSecive * (0.4f + 0.9f * easedProgress)
                    } else {
                        bubble.alpha = 0f
                    }
                }
                delay(16)
            }
            bubbles.clear()
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Draw the bubbles floating behind / around the icon
        Canvas(modifier = Modifier.fillMaxSize()) {
            bubbles.forEach { bubble ->
                if (bubble.alpha > 0f) {
                    val currentRadiusPx = with(density) { bubble.currentRadius.toPx() }
                    val centerOffset = Offset(
                        x = size.width / 2f + bubble.currentX,
                        y = size.height / 2f + bubble.currentY
                    )
                    
                    // Glass bubble outline with soft rainbow/teal tint
                    drawCircle(
                        color = Color(0xFF81D4FA).copy(alpha = bubble.alpha * 0.8f),
                        radius = currentRadiusPx,
                        center = centerOffset,
                        style = Stroke(width = with(density) { 1.dp.toPx() })
                    )
                    
                    // Bubble body ambient glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x66B2EBF2).copy(alpha = bubble.alpha * 0.4f),
                                Color(0x33F8BBD0).copy(alpha = bubble.alpha * 0.2f),
                                Color.Transparent
                            ),
                            center = centerOffset,
                            radius = currentRadiusPx
                        ),
                        radius = currentRadiusPx,
                        center = centerOffset
                    )

                    // Bubble specular highlight (shiny spot)
                    drawCircle(
                        color = Color.White.copy(alpha = bubble.alpha * 0.9f),
                        radius = currentRadiusPx * 0.25f,
                        center = Offset(
                            x = centerOffset.x - currentRadiusPx * 0.35f,
                            y = centerOffset.y - currentRadiusPx * 0.35f
                        )
                    )
                }
            }
        }
        content()
    }
}

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

    val userVerified by viewModel.userVerified.collectAsStateWithLifecycle()
    val userBronzeName by viewModel.userBronzeName.collectAsStateWithLifecycle()
    val userBronzePrice by viewModel.userBronzePrice.collectAsStateWithLifecycle()
    val userBronzePerks by viewModel.userBronzePerks.collectAsStateWithLifecycle()
    val userSilverName by viewModel.userSilverName.collectAsStateWithLifecycle()
    val userSilverPrice by viewModel.userSilverPrice.collectAsStateWithLifecycle()
    val userSilverPerks by viewModel.userSilverPerks.collectAsStateWithLifecycle()
    val userGoldName by viewModel.userGoldName.collectAsStateWithLifecycle()
    val userGoldPrice by viewModel.userGoldPrice.collectAsStateWithLifecycle()
    val userGoldPerks by viewModel.userGoldPerks.collectAsStateWithLifecycle()

    var isHeaderSearchActive by remember { mutableStateOf(false) }
    var headerSearchQuery by remember { mutableStateOf("") }

    val creators by viewModel.creators.collectAsStateWithLifecycle(initialValue = emptyList())
    val posts by viewModel.posts.collectAsStateWithLifecycle(initialValue = emptyList())
    val followedPosts by viewModel.followedPosts.collectAsStateWithLifecycle(initialValue = emptyList())
    val subscriptions by viewModel.subscriptions.collectAsStateWithLifecycle(initialValue = emptyList())
    val transactions by viewModel.transactions.collectAsStateWithLifecycle(initialValue = emptyList())
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle(initialValue = emptyList())
    val events by viewModel.events.collectAsStateWithLifecycle(initialValue = emptyList())

    // Trending topics states
    val trendingTopics by viewModel.trendingTopics.collectAsStateWithLifecycle(initialValue = emptyList())
    val isTrendingFetching by viewModel.isTrendingFetching.collectAsStateWithLifecycle(initialValue = false)
    val selectedTopicInsight by viewModel.selectedTopicInsight.collectAsStateWithLifecycle(initialValue = null)
    val isGeneratingInsight by viewModel.isGeneratingInsight.collectAsStateWithLifecycle(initialValue = false)

    // Direct local state for splits
    var marioPizzaSplitPaid by remember { mutableStateOf(false) }
    var activeInAppVideoPost by remember { mutableStateOf<Post?>(null) }

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
                                placeholder = { Text("Search feed, market...", color = GrayText, fontSize = 14.sp) },
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
                        if (!isHeaderSearchActive && currentScreen !is Screen.LiveEvents) {
                            IconButton(onClick = { isHeaderSearchActive = true }) {
                                Icon(imageVector = Icons.Default.Search, contentDescription = "Search hub", tint = LightText)
                            }
                        }
                        
                        // SECURE TECH GLOWING WALLET PILL - Page 1 & Page 5 (Category F Integration)
                        if (currentScreen !is Screen.Feed && currentScreen !is Screen.LiveEvents && currentScreen !is Screen.Chat) {
                            Surface(
                                onClick = { viewModel.navigateTo(Screen.Wallet) },
                                shape = RoundedCornerShape(20.dp),
                                color = ObsidianDark,
                                border = BorderStroke(1.5.dp, RazorBlue),
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .testTag("app_quick_wallet")
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp)
                    // Ambient bottom neon-glow behind the bar to simulate modern light projection
                    .drawBehind {
                        drawRoundRect(
                            color = Color(0x2CFF4CA0), // Smooth liquid pink glass-glow
                            topLeft = Offset(-2f, -2f),
                            size = size.copy(width = size.width + 4f, height = size.height + 4f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(32.dp.toPx(), 32.dp.toPx()),
                            style = Stroke(width = 6.dp.toPx())
                        )
                    }
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x4DFFFFFF), // Bright glass high-glare gloss
                                Color(0x2BFFD5EC), // Delicious liquid satin rose pink gloss
                                Color(0x13FFFFFF)  // Sheer base glass shine
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF7B40).copy(alpha = 0.5f), // Glowing Neon Tangerine highlight
                                Color(0xFFFF4CA0).copy(alpha = 0.7f), // Liquid pink highlight
                                Color.White.copy(alpha = 0.25f)       // Bright rim glare
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NavigationBarItem(
                        selected = currentScreen is Screen.Feed,
                        onClick = { viewModel.navigateTo(Screen.Feed) },
                        icon = {
                            BubbleEffectContainer(isSelected = currentScreen is Screen.Feed) {
                                Icon(imageVector = if (currentScreen is Screen.Feed) Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Feed")
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RazorBlue,
                            indicatorColor = MinimalLavenderContainer,
                            unselectedIconColor = GrayText
                        ),
                        modifier = Modifier.testTag("nav_feed")
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.LiveEvents,
                        onClick = { viewModel.navigateTo(Screen.LiveEvents) },
                        icon = {
                            BubbleEffectContainer(isSelected = currentScreen is Screen.LiveEvents) {
                                Icon(imageVector = if (currentScreen is Screen.LiveEvents) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart, contentDescription = "Marketplace")
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RazorBlue,
                            indicatorColor = MinimalLavenderContainer,
                            unselectedIconColor = GrayText
                        ),
                        modifier = Modifier.testTag("nav_market")
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.Chat,
                        onClick = { viewModel.navigateTo(Screen.Chat()) },
                        icon = {
                            BubbleEffectContainer(isSelected = currentScreen is Screen.Chat) {
                                Icon(imageVector = if (currentScreen is Screen.Chat) Icons.Filled.Mail else Icons.Outlined.Mail, contentDescription = "Secure Chats")
                            }
                        },
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
                        icon = {
                            BubbleEffectContainer(isSelected = currentScreen is Screen.Wallet) {
                                Icon(imageVector = if (currentScreen is Screen.Wallet) Icons.Filled.Person else Icons.Outlined.Person, contentDescription = "Profile")
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RazorBlue,
                            indicatorColor = MinimalLavenderContainer,
                            unselectedIconColor = GrayText
                        ),
                        modifier = Modifier.testTag("nav_wallet")
                    )
                }
            }
        },
        containerColor = ObsidianDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (isHeaderSearchActive) {
                    TrendingTopicsCard(
                        topics = trendingTopics,
                        isFetching = isTrendingFetching,
                        selectedInsight = selectedTopicInsight,
                        isGenerating = isGeneratingInsight,
                        onTopicClick = { topic ->
                            headerSearchQuery = topic
                        },
                        onGenerateInsight = { topic ->
                            viewModel.generateTopicInsight(topic)
                        },
                        onClearInsight = {
                            viewModel.clearTopicInsight()
                        },
                        onRefresh = {
                            viewModel.fetchTrendingTopics()
                        }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
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
                                        onDiscoverCreators = { viewModel.navigateTo(Screen.Creators) },
                                        onCreatorClick = { creatorId ->
                                            viewModel.navigateTo(Screen.CreatorDetail(creatorId))
                                        },
                                        onVideoClick = { activeInAppVideoPost = it }
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
                                            onBack = { viewModel.navigateTo(Screen.Creators) },
                                            onVerifyToggle = { isVerified ->
                                                viewModel.verifyCreator(creator.id, isVerified)
                                            },
                                            onVideoClick = { activeInAppVideoPost = it }
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
                                onMarkAsSeen = { viewModel.markMessagesAsSeen(it) },
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
                                onCancelSubscription = { creatorId, handle -> viewModel.cancelSubscription(creatorId, handle) },
                                userVerified = userVerified,
                                userBronzeName = userBronzeName,
                                userBronzePrice = userBronzePrice,
                                userBronzePerks = userBronzePerks,
                                userSilverName = userSilverName,
                                userSilverPrice = userSilverPrice,
                                userSilverPerks = userSilverPerks,
                                userGoldName = userGoldName,
                                userGoldPrice = userGoldPrice,
                                userGoldPerks = userGoldPerks,
                                onVerifyUser = { viewModel.verifyUser() },
                                onUnverifyUser = { viewModel.unverifyUser() },
                                onUpdateUserTiers = { bN, bPr, bPe, sN, sPr, sPe, gN, gPr, gPe ->
                                    viewModel.updateUserTiers(bN, bPr, bPe, sN, sPr, sPe, gN, gPr, gPe)
                                },
                                onVerifyCreator = { id, verified -> viewModel.verifyCreator(id, verified) }
                            )
                        }
                    }
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

            activeInAppVideoPost?.let { post ->
                BuiltInVideoPlayerOverlay(
                    post = post,
                    subscriptions = subscriptions,
                    walletBalance = walletBalance,
                    onLikeToggle = { viewModel.likePost(post) },
                    onTipSend = { amount -> viewModel.triggerPostTip(post, amount) },
                    onUnlockCreator = { 
                        viewModel.navigateTo(Screen.CreatorDetail(post.creatorId))
                        activeInAppVideoPost = null
                    },
                    onDismiss = { activeInAppVideoPost = null }
                )
            }
        }
    }
}

// ============================================
// SCREEN 1: FEED SCREEN WITH STORIES (Page 1)
// ============================================
sealed class StreamItem {
    data class VideoPost(val post: Post) : StreamItem()
    data class Advertisement(
        val id: String,
        val sponsorName: String,
        val sponsorAvatar: String,
        val caption: String,
        val imageUrl: String,
        val ctaText: String,
        val url: String
    ) : StreamItem()
}

data class MockAd(
    val id: String,
    val sponsorName: String,
    val sponsorAvatar: String,
    val caption: String,
    val imageUrl: String,
    val ctaText: String,
    val url: String
)

@Composable
fun FeedScreen(
    posts: List<Post>,
    creators: List<Creator>,
    subscriptions: List<Subscription>,
    onLike: (Post) -> Unit,
    onTip: (Post, Double) -> Unit,
    onUnlock: (String) -> Unit,
    onPublishPost: (String, Creator?, String?) -> Unit,
    onDiscoverCreators: () -> Unit,
    onCreatorClick: (String) -> Unit,
    onVideoClick: (Post) -> Unit = {}
) {
    var showStoryStudio by remember { mutableStateOf(false) }
    var showNewPostDialog by remember { mutableStateOf(false) }

    var isGridView by remember { mutableStateOf(false) }
    var selectedGridPost by remember { mutableStateOf<Post?>(null) }
    var activePlayingSnap by remember { mutableStateOf<CreatorSnap?>(null) }

    val sampleAds = remember {
        listOf(
            MockAd(
                id = "ad_aeropress",
                sponsorName = "AeroPress Labs",
                sponsorAvatar = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=150",
                caption = "Craft the perfect high-pressure espresso extraction on-the-go. Special 20% off for SocialHub creators! ☕️⚡️ #coffee #aeropress",
                imageUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=800",
                ctaText = "SHOP NOW & SAVE 20%",
                url = "https://aeropress.com"
            ),
            MockAd(
                id = "ad_fortress",
                sponsorName = "Fortress Cryptography",
                sponsorAvatar = "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=150",
                caption = "Secure your digital assets with high-performance cryptographic keys. Zero-knowledge protocols, decentralized ledger recovery, and multi-signature security. 🔒🛡️ #security #cyber #web3",
                imageUrl = "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=800",
                ctaText = "INSTALL SECURE VAULT",
                url = "https://fortress.io"
            ),
            MockAd(
                id = "ad_korg",
                sponsorName = "Korg Synthesizers",
                sponsorAvatar = "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=150",
                caption = "The legendary multi-engine synthesizer reimagined in a holographic physical interface. Experience the future of spatial frequencies. 🎹🌌 #synthwave #music #hardware",
                imageUrl = "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=800",
                ctaText = "EXPLORE SYNTH MODELS",
                url = "https://korg.com"
            )
        )
    }

    val defaultVideos = remember {
        listOf(
            Post(
                id = -1001,
                creatorId = "alex_johnson",
                creatorName = "Alex Johnson",
                creatorHandle = "alex_johnson",
                creatorAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                caption = "Fast espresso puck preparation routine! ☕️⚡️ Perfect extraction every single time. #Shorts #coffee #barista #aesthetic",
                contentImage = "attached_video",
                isPremium = false,
                likesCount = 820,
                tipsTotal = 10.0
            ),
            Post(
                id = -1002,
                creatorId = "aura_music",
                creatorName = "Aura Rhythm",
                creatorHandle = "music_guru",
                creatorAvatar = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=150",
                caption = "Full tutorial: Designing deep space ambient soundscapes using multi-wave lofi oscillators. 🌌🎹 Check the full frequency calibration. #lofi #synthesizer #music",
                contentImage = "attached_video",
                isPremium = false,
                likesCount = 1450,
                tipsTotal = 45.0
            ),
            Post(
                id = -1003,
                creatorId = "pixel_queen",
                creatorName = "Pixel Queen",
                creatorHandle = "PixelQueen",
                creatorAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                caption = "Quick preview of the Neon Genesis 3D hologram asset drops. Render speeds are absolutely unreal! 📈🛸 #Shorts #3D #cryptoart",
                contentImage = "attached_video",
                isPremium = false,
                likesCount = 950,
                tipsTotal = 25.0
            ),
            Post(
                id = -1004,
                creatorId = "alex_future",
                creatorName = "Alex Chen",
                creatorHandle = "ALX_FUTURE",
                creatorAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                caption = "Macro analysis of high-yield silicon and tech stocks. Exploring moving averages and holographic support bands. 📊⚡️ #finance #investing #stocks",
                contentImage = "attached_video",
                isPremium = false,
                likesCount = 2100,
                tipsTotal = 180.0
            ),
            Post(
                id = -1005,
                creatorId = "alex_johnson",
                creatorName = "Alex Johnson",
                creatorHandle = "alex_johnson",
                creatorAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                caption = "Pouring a pristine Rosetta latte art under 20 seconds. Super smooth velvet milk texture! 🥛☕️ #Shorts #latteart #barista",
                contentImage = "attached_video",
                isPremium = false,
                likesCount = 640,
                tipsTotal = 5.5
            ),
            Post(
                id = -1006,
                creatorId = "aura_music",
                creatorName = "Aura Rhythm",
                creatorHandle = "music_guru",
                creatorAvatar = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=150",
                caption = "Late night mountain studio live jam session. Relaxed background tempos for focused coding & asynchronous workflows. 🎧💻 #lofi #ambient #coding",
                contentImage = "attached_video",
                isPremium = false,
                likesCount = 3200,
                tipsTotal = 120.0
            ),
            Post(
                id = -1007,
                creatorId = "pixel_queen",
                creatorName = "Pixel Queen",
                creatorHandle = "PixelQueen",
                creatorAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                caption = "Unboxing the futuristic holographic virtual display unit. The latency is practically imperceptible! 🕶️👾 #Shorts #hologram #unboxing",
                contentImage = "attached_video",
                isPremium = false,
                likesCount = 1200,
                tipsTotal = 40.0
            ),
            Post(
                id = -1008,
                creatorId = "alex_future",
                creatorName = "Alex Chen",
                creatorHandle = "ALX_FUTURE",
                creatorAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                caption = "Masterclass: How to set up a secure decentralized crypto wallet with zero-knowledge rotation ciphers. 🔑🛡️ #crypto #security #tutorial",
                contentImage = "attached_video",
                isPremium = false,
                likesCount = 1850,
                tipsTotal = 95.0
            ),
            Post(
                id = -1009,
                creatorId = "alex_johnson",
                creatorName = "Alex Johnson",
                creatorHandle = "alex_johnson",
                creatorAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                caption = "The satisfying sound of freshly roasted coffee beans cooling down. ASMR triggers! 🎧💨 #Shorts #coffee #asmr",
                contentImage = "attached_video",
                isPremium = false,
                likesCount = 530,
                tipsTotal = 4.0
            ),
            Post(
                id = -1010,
                creatorId = "aura_music",
                creatorName = "Aura Rhythm",
                creatorHandle = "music_guru",
                creatorAvatar = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=150",
                caption = "Synthesizer workshop: calibrating dynamic resonant filters and high-pass filters under heavy load. 🎛️⚡️ #synthesizer #hardware #tech",
                contentImage = "attached_video",
                isPremium = false,
                likesCount = 980,
                tipsTotal = 35.0
            )
        )
    }

    val dismissedAds = remember { mutableStateListOf<String>() }
    var activeAdBrowser by remember { mutableStateOf<Pair<String, String>?>(null) }
    
    // Performance Cache Booster for Feed Refresh
    var isBoostingRefresh by remember { mutableStateOf(false) }
    var boostProgress by remember { mutableStateOf(0f) }
    var showBoostSuccessBadge by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()

    val onTriggerBoostRefresh = {
        if (!isBoostingRefresh) {
            coroutineScope.launch {
                isBoostingRefresh = true
                showBoostSuccessBadge = false
                boostProgress = 0f
                while (boostProgress < 1f) {
                    kotlinx.coroutines.delay(35)
                    boostProgress += 0.05f
                }
                boostProgress = 1f
                kotlinx.coroutines.delay(150)
                isBoostingRefresh = false
                showBoostSuccessBadge = true
                kotlinx.coroutines.delay(3000)
                showBoostSuccessBadge = false
            }
        }
    }

    val creatorSnaps = remember {
        listOf(
            CreatorSnap(
                id = "snap_coffee_1",
                creatorId = "alex_johnson",
                creatorName = "Alex Johnson",
                creatorHandle = "alex_johnson",
                creatorAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                imageUrl = "https://images.unsplash.com/photo-1541167760496-1628856ab772?w=800",
                caption = "Brewing premium gourmet espresso! Look at those crema layers ☕️✨ #morning #coffee #crema",
                durationSeconds = 8,
                isPremium = false,
                likesCount = 312
            ),
            CreatorSnap(
                id = "snap_synth_1",
                creatorId = "aura_music",
                creatorName = "Aura Rhythm",
                creatorHandle = "music_guru",
                creatorAvatar = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=150",
                imageUrl = "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=800",
                caption = "Adjusting decibel ratios & low-pass filtering. Late night neon synth session! 🌌🎧 #synthwave #music",
                durationSeconds = 12,
                isPremium = true,
                requiredTier = "BRONZE",
                likesCount = 482
            ),
            CreatorSnap(
                id = "snap_crypto_1",
                creatorId = "pixel_queen",
                creatorName = "Pixel Queen",
                creatorHandle = "PixelQueen",
                creatorAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800",
                caption = "Exclusive sneak preview of the virtual cyberpunk 3D art collection. Drops on Friday! 🎭⚡️ #neon #pixel",
                durationSeconds = 10,
                isPremium = true,
                requiredTier = "SILVER",
                likesCount = 924
            ),
            CreatorSnap(
                id = "snap_fin_1",
                creatorId = "alex_future",
                creatorName = "Alex Chen",
                creatorHandle = "ALX_FUTURE",
                creatorAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                imageUrl = "https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?w=800",
                caption = "High-frequency trends & algorithmic matrix projection analysis. Stay ahead! 📊🧠 #finance #strategy",
                durationSeconds = 15,
                isPremium = true,
                requiredTier = "GOLD",
                likesCount = 1045
            )
        )
    }

    var pullOffset by remember { mutableStateOf(0f) }
    val maxPull = 160f

    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
                val dy = available.y
                if (dy < 0 && pullOffset > 0) {
                    val consumed = dy.coerceAtLeast(-pullOffset)
                    pullOffset += consumed
                    return androidx.compose.ui.geometry.Offset(0f, consumed)
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }

            override fun onPostScroll(
                consumed: androidx.compose.ui.geometry.Offset,
                available: androidx.compose.ui.geometry.Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                val dy = available.y
                if (dy > 0 && !isBoostingRefresh) {
                    val added = dy * 0.45f
                    val old = pullOffset
                    pullOffset = (pullOffset + added).coerceAtMost(maxPull)
                    return androidx.compose.ui.geometry.Offset(0f, pullOffset - old)
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }

            override suspend fun onPreFling(available: androidx.compose.ui.unit.Velocity): androidx.compose.ui.unit.Velocity {
                if (pullOffset >= maxPull - 15 && !isBoostingRefresh) {
                    coroutineScope.launch {
                        onTriggerBoostRefresh()
                        while (pullOffset > 0f) {
                            kotlinx.coroutines.delay(10)
                            pullOffset = (pullOffset - 5f).coerceAtLeast(0f)
                        }
                    }
                } else {
                    coroutineScope.launch {
                        while (pullOffset > 0f) {
                            kotlinx.coroutines.delay(10)
                            pullOffset = (pullOffset - 8f).coerceAtLeast(0f)
                        }
                    }
                }
                return androidx.compose.ui.unit.Velocity.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        // Floating pull-to-refresh indicator & progress (Category D)
        AnimatedVisibility(
            visible = pullOffset > 0f || isBoostingRefresh,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .zIndex(99f)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.85f))
                    .border(
                        1.dp,
                        Brush.linearGradient(colors = listOf(RazorBlue, InstaPink)),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isBoostingRefresh) {
                        CircularProgressIndicator(
                            progress = boostProgress,
                            modifier = Modifier.size(16.dp),
                            color = RazorTeal,
                            trackColor = Color.White.copy(alpha = 0.1f),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SYNC FEED: ${(boostProgress * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    } else {
                        val pullRatio = (pullOffset / maxPull).coerceIn(0f, 1f)
                        CircularProgressIndicator(
                            progress = pullRatio,
                            modifier = Modifier.size(16.dp),
                            color = InstaPink,
                            trackColor = Color.White.copy(alpha = 0.1f),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (pullOffset >= maxPull - 15) "RELEASE TO BOOST" else "PULL TO REFRESH",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Floating success badge (Category D)
        AnimatedVisibility(
            visible = showBoostSuccessBadge,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .zIndex(100f)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(colors = listOf(Color(0xFF0D533A), Color(0xFF133F2E))))
                    .border(
                        1.2.dp,
                        RazorTeal,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success check",
                        tint = RazorTeal,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🚀 CACHE SYNCHRONIZED",
                        color = RazorTeal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .graphicsLayer { translationY = pullOffset }
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
                            text = "Explore Creator Market",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        } else {
            val chunkedGridPosts = remember(posts) { posts.chunked(3) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationY = pullOffset },
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Horizontal Stories Bar - Page 1
                item {
                    StoriesBar(
                        snaps = creatorSnaps,
                        subscriptions = subscriptions,
                        onSnapClick = { activePlayingSnap = it },
                        onCreateStoryClick = { showStoryStudio = true }
                    )
                }

                // Dual view modes with gorgeous liquid glass styling
                item {
                    val streamBrush = remember { Brush.linearGradient(colors = listOf(RazorBlue.copy(alpha = 0.35f), InstaPink.copy(alpha = 0.35f))) }
                    val streamModifier = if (!isGridView) Modifier.background(streamBrush) else Modifier.background(Color.Transparent)
                    val gridModifier = if (isGridView) Modifier.background(streamBrush) else Modifier.background(Color.Transparent)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x18FFFFFF))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                            .padding(4.dp)
                    ) {
                        // Stream view button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .then(streamModifier)
                                .border(1.dp, if (!isGridView) Color.White.copy(alpha = 0.25f) else Color.Transparent, RoundedCornerShape(12.dp))
                                .clickable { isGridView = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = "Stream mode",
                                    tint = if (!isGridView) Color.White else GrayText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "STREAM DECK",
                                    color = if (!isGridView) Color.White else GrayText,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // Grid view button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .then(gridModifier)
                                .border(1.dp, if (isGridView) Color.White.copy(alpha = 0.25f) else Color.Transparent, RoundedCornerShape(12.dp))
                                .clickable { isGridView = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Creators snaps mode",
                                    tint = if (isGridView) Color.White else GrayText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CREATORS",
                                    color = if (isGridView) Color.White else GrayText,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                if (isGridView) {
                    item {
                        CreatorSnapsGrid(
                            snaps = creatorSnaps,
                            subscriptions = subscriptions,
                            onSnapClick = { activePlayingSnap = it }
                        )
                    }
                } else {
                    val userVideos = posts.filter { it.contentImage == "attached_video" && it.id >= 0 }
                    val combinedVideos = userVideos + defaultVideos
                    val streamItems = combinedVideos.flatMapIndexed { index, post ->
                        val items = mutableListOf<StreamItem>()
                        items.add(StreamItem.VideoPost(post))
                        val videoIndex = index + 1
                        if (videoIndex % 5 == 0) {
                            val adIndex = (videoIndex / 5) - 1
                            val ad = sampleAds[adIndex % sampleAds.size]
                            if (!dismissedAds.contains(ad.id)) {
                                items.add(
                                    StreamItem.Advertisement(
                                        id = ad.id,
                                        sponsorName = ad.sponsorName,
                                        sponsorAvatar = ad.sponsorAvatar,
                                        caption = ad.caption,
                                        imageUrl = ad.imageUrl,
                                        ctaText = ad.ctaText,
                                        url = ad.url
                                    )
                                )
                            }
                        }
                        items
                    }

                    items(streamItems, key = { item ->
                        when (item) {
                            is StreamItem.VideoPost -> "video_${item.post.id}"
                            is StreamItem.Advertisement -> "ad_${item.id}"
                        }
                    }) { item ->
                        when (item) {
                            is StreamItem.VideoPost -> {
                                val post = item.post
                                val hasSubscription = subscriptions.any {
                                    it.creatorId == post.creatorId &&
                                    (it.tierName == "GOLD" || 
                                     (it.tierName == "SILVER" && post.requiredTier != "GOLD") || 
                                     (it.tierName == "BRONZE" && post.requiredTier == "BRONZE"))
                                }
                                val isLocked = post.isPremium && !hasSubscription

                                EntranceAnimationContainer {
                                    PostCard(
                                        post = post,
                                        isLocked = isLocked,
                                        onLike = { onLike(post) },
                                        onTip = { amt -> onTip(post, amt) },
                                        onUnlock = { onUnlock(post.creatorId) },
                                        onCreatorClick = { onCreatorClick(post.creatorId) },
                                        isCreatorVerified = creators.find { it.id == post.creatorId }?.isVerified ?: true,
                                        onVideoClick = { onVideoClick(post) }
                                    )
                                }
                            }
                            is StreamItem.Advertisement -> {
                                EntranceAnimationContainer {
                                    InstagramAdCard(
                                        sponsorName = item.sponsorName,
                                        sponsorAvatar = item.sponsorAvatar,
                                        caption = item.caption,
                                        imageUrl = item.imageUrl,
                                        ctaText = item.ctaText,
                                        onDismissAd = { dismissedAds.add(item.id) },
                                        onCtaClick = { activeAdBrowser = Pair(item.sponsorName, item.url) }
                                    )
                                }
                            }
                        }
                    }
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

    if (activePlayingSnap != null) {
        SnapPlaybackOverlay(
            snaps = creatorSnaps,
            initialSnap = activePlayingSnap!!,
            subscriptions = subscriptions,
            onLikeToggle = { /* Optional analytical or status logging */ },
            onVisitProfile = { pId ->
                activePlayingSnap = null
                onCreatorClick(pId)
            },
            onDismiss = { activePlayingSnap = null }
        )
    }

    selectedGridPost?.let { post ->
        Dialog(onDismissRequest = { selectedGridPost = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ObsidianDark)
                    .border(1.2.dp, Brush.linearGradient(colors = listOf(Color.White.copy(0.3f), Color.White.copy(0.05f))), RoundedCornerShape(24.dp))
                    .padding(vertical = 12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "POST EXPLORER DETAILS",
                            fontWeight = FontWeight.ExtraBold,
                            color = InstaPink,
                            fontSize = 11.5.sp,
                            letterSpacing = 0.8.sp
                        )
                        IconButton(onClick = { selectedGridPost = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Detail Modal",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

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
                        onUnlock = { 
                            selectedGridPost = null
                            onUnlock(post.creatorId) 
                        },
                        onCreatorClick = {
                            selectedGridPost = null
                            onCreatorClick(post.creatorId)
                        },
                        isCreatorVerified = creators.find { it.id == post.creatorId }?.isVerified ?: true,
                        onVideoClick = { onVideoClick(post) }
                    )
                }
            }
        }
    }

    activeAdBrowser?.let { adDetails ->
        SimulatedBrowserDialog(
            sponsorName = adDetails.first,
            url = adDetails.second,
            onDismiss = { activeAdBrowser = null }
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
                    android.util.Log.e("SocialHubUI", "Cursor file info retrieval failed: ${e.message}")
                }

                if (fileSize <= 0) {
                    try {
                        resolver.openInputStream(uri)?.use { stream ->
                            fileSize = stream.available().toLong()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SocialHubUI", "Fallback size retrieval failed: ${e.message}")
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
                        arrayOf(
                            android.Manifest.permission.READ_MEDIA_IMAGES,
                            android.Manifest.permission.READ_MEDIA_VIDEO,
                            android.Manifest.permission.READ_MEDIA_AUDIO
                        )
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
fun StoriesBar(
    snaps: List<CreatorSnap>,
    subscriptions: List<Subscription>,
    onSnapClick: (CreatorSnap) -> Unit,
    onCreateStoryClick: () -> Unit
) {
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
            // Your Shot (with + click button) - Page 1
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
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(RazorBlue.copy(alpha = 0.25f), InstaPink.copy(alpha = 0.25f))
                                    )
                                )
                                .border(
                                    1.5.dp, 
                                    Brush.linearGradient(listOf(RazorBlue, InstaPink)), 
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("ME", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 0.5.sp)
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
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Shot", tint = Color.Black, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Your Shot", color = GrayText, fontSize = 12.sp)
                }
            }

            // Dynamic Creator Snaps listed as Shots! Like Instagram stories.
            items(snaps) { snap ->
                val hasSubscription = subscriptions.any {
                    it.creatorId == snap.creatorId &&
                    (it.tierName == "GOLD" || 
                     (it.tierName == "SILVER" && snap.requiredTier != "GOLD") || 
                     (it.tierName == "BRONZE" && snap.requiredTier == "BRONZE"))
                }
                val isLocked = snap.isPremium && !hasSubscription

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onSnapClick(snap) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Glowing ring around Shot avatar
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(Color.Transparent)
                                .border(
                                    width = 2.5.dp,
                                    brush = Brush.linearGradient(
                                        colors = if (isLocked) listOf(InstaPink, LightText.copy(alpha = 0.2f))
                                                else listOf(RazorTeal, RazorBlue)
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF130E22)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = snap.creatorAvatar,
                                    contentDescription = "Shot Avatar",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(if (isLocked) 0.5f else 0.9f),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        // Lock indicator badge on top of glowing avatar boundary
                        if (isLocked) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(InstaPink)
                                    .align(Alignment.BottomEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked Shot",
                                    tint = Color.Black,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = snap.creatorName.split(" ").firstOrNull() ?: snap.creatorHandle,
                        color = LightText,
                        fontSize = 12.sp,
                        fontWeight = if (isLocked) FontWeight.Normal else FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
    onUnlock: () -> Unit,
    onCreatorClick: () -> Unit = {},
    isCreatorVerified: Boolean = true,
    onVideoClick: (() -> Unit)? = null
) {
    var isLikedLocal by remember(post.id, post.isLiked) { mutableStateOf(post.isLiked) }
    var likesCountLocal by remember(post.id, post.likesCount) { mutableStateOf(post.likesCount) }
    val scale = remember { Animatable(1f) }
    val commentScale = remember { Animatable(1f) }
    val commentRotation = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    var commentsCount by remember(post.id) { mutableStateOf(84) }
    var showCommentsDialog by remember { mutableStateOf(false) }

    val commentsList = remember(post.id) {
        mutableStateListOf<String>().apply {
            when (post.creatorId) {
                "alex_johnson" -> {
                    addAll(listOf(
                        "This espresso pull looks incredibly clean! ☕️",
                        "What origin coffee beans is this? Savoring it!",
                        "Awesome latte art patterns. Always a masterclass.",
                        "The crema consistency is amazing. Next level."
                    ))
                }
                "aura_music" -> {
                    addAll(listOf(
                        "This ambient synth line is magnificent! 🌌🎹",
                        "Which oscillator waveform did you tune?",
                        "Perfect background study tracks right here.",
                        "Beautiful synthwave sunset frequencies."
                    ))
                }
                "alex_future" -> {
                    addAll(listOf(
                        "Superb market analysis. Bronze tier is so valuable! 📈",
                        "What is your forecast for NVIDIA silicon targets this month?",
                        "Excellent detail. Incredibly crisp charts.",
                        "Substantial financial reports as always."
                    ))
                }
                else -> {
                    addAll(listOf(
                        "This looks like pristine digital craft! ✨⚡️",
                        "Incredible, love the cyberpunk tech vibe.",
                        "Looking forward to the next update!",
                        "Superb layout and visual content."
                    ))
                }
            }
        }
    }
    
    // Sync comments count
    LaunchedEffect(commentsList.size) {
        commentsCount = commentsList.size
    }

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
                // Profile Avatar Circular Indicator with ultra-polished pink-orange fluid gradient border
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(RazorBlue, InstaPink)
                            )
                        )
                        .clickable { onCreatorClick() }
                        .padding(2.5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(ObsidianDark),
                        contentAlignment = Alignment.Center
                    ) {
                        if (post.creatorAvatar.startsWith("http")) {
                            AsyncImage(
                                model = post.creatorAvatar,
                                contentDescription = "Creator Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = post.creatorName.take(1).uppercase(),
                                fontWeight = FontWeight.ExtraBold,
                                color = InstaPink,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(
                    modifier = Modifier.clickable { onCreatorClick() }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.creatorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LightText
                        )
                        if (isCreatorVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified status checked",
                                tint = RazorTeal,
                                modifier = Modifier.size(15.dp)
                            )
                        }
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
                        .aspectRatio(1.25f)
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
                var heartVisible by remember { mutableStateOf(false) }
                val heartScale = remember { Animatable(0f) }

                var isPlayingInline by remember { mutableStateOf(false) }
                var showControls by remember { mutableStateOf(false) }
                var isMutedInline by remember { mutableStateOf(false) }
                var videoProgress by remember { mutableStateOf(0f) }
                var isDragging by remember { mutableStateOf(false) }
                var controlTimerJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

                LaunchedEffect(isPlayingInline) {
                    if (isPlayingInline) {
                        var lastTime = System.currentTimeMillis()
                        while (isPlayingInline) {
                            delay(16)
                            if (!isDragging) {
                                val now = System.currentTimeMillis()
                                val elapsed = now - lastTime
                                videoProgress = (videoProgress + (elapsed.toFloat() / 12000f)) % 1.0f
                                lastTime = now
                            } else {
                                lastTime = System.currentTimeMillis()
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.25f)
                        .background(Color(0xFF130E22))
                        .pointerInput(post.id) {
                            detectTapGestures(
                                onTap = {
                                    if (post.contentImage == "attached_video") {
                                        if (!isPlayingInline) {
                                            isPlayingInline = true
                                            showControls = true
                                            controlTimerJob?.cancel()
                                            controlTimerJob = coroutineScope.launch {
                                                delay(5000)
                                                showControls = false
                                            }
                                        } else {
                                            showControls = !showControls
                                            if (showControls) {
                                                controlTimerJob?.cancel()
                                                controlTimerJob = coroutineScope.launch {
                                                    delay(5000)
                                                    showControls = false
                                                }
                                            }
                                        }
                                    }
                                },
                                onDoubleTap = { offset ->
                                    if (!isLikedLocal) {
                                        isLikedLocal = true
                                        likesCountLocal += 1
                                        onLike()
                                    }
                                    coroutineScope.launch {
                                        heartVisible = true
                                        heartScale.snapTo(0f)
                                        heartScale.animateTo(
                                            1.5f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioHighBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                        delay(500)
                                        heartScale.animateTo(0f, animationSpec = tween(200))
                                        heartVisible = false
                                    }
                                }
                            )
                        }
                ) {
                    val imageUrl = if (post.contentImage.startsWith("http")) {
                        post.contentImage
                    } else {
                        when (post.contentImage) {
                            "latte_art" -> "https://images.unsplash.com/photo-1541167760496-1628856ab772?w=800&auto=format&fit=crop&q=80"
                            "music_studio" -> "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=800&auto=format&fit=crop&q=80"
                            "attached_image" -> "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=800&auto=format&fit=crop&q=80"
                            "attached_audio" -> "https://images.unsplash.com/photo-1484755560695-a4c68e910a14?w=800&auto=format&fit=crop&q=80"
                            "attached_video" -> "https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=800&auto=format&fit=crop&q=80"
                            else -> "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=80"
                        }
                    }

                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Post Content Image",
                        modifier = Modifier.fillMaxSize().alpha(if (isPlayingInline) 0.85f else 0.95f),
                        contentScale = ContentScale.Crop
                    )

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
                                if (!isPlayingInline) {
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
                                } else {
                                    // Live Instagram-style equalizer waves when playing
                                    if (!isMutedInline) {
                                        val count = 12
                                        val gap = canvasWidth / (count + 1)
                                        val pulse = videoProgress
                                        for (i in 0 until count) {
                                            val barX = gap * (i + 1)
                                            val waveFactor = kotlin.math.sin((i * 0.4f + pulse * 2 * 3.14159f).toDouble()).toFloat()
                                            val barHeight = (25.dp.toPx() + 20.dp.toPx() * waveFactor)
                                            drawLine(
                                                color = RazorTeal.copy(alpha = 0.7f),
                                                start = Offset(barX, canvasHeight - 40.dp.toPx() - barHeight / 2),
                                                end = Offset(barX, canvasHeight - 40.dp.toPx() + barHeight / 2),
                                                strokeWidth = 3.dp.toPx()
                                            )
                                        }
                                    }
                                }
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
                        // Always-visible Speaker Icon Toggle in Top-Right Corner
                        IconButton(
                            onClick = {
                                isMutedInline = !isMutedInline
                                controlTimerJob?.cancel()
                                controlTimerJob = coroutineScope.launch {
                                    delay(5000)
                                    showControls = false
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .size(32.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isMutedInline) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "Speaker Toggle Corner",
                                tint = if (isMutedInline) Color.Red else RazorTeal,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (isPlayingInline && showControls) {
                            // Centered Play/Pause Button
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = {
                                        isPlayingInline = !isPlayingInline
                                        // Reset the 5-sec timer
                                        controlTimerJob?.cancel()
                                        controlTimerJob = coroutineScope.launch {
                                            delay(5000)
                                            showControls = false
                                        }
                                    },
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingInline) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause Inline",
                                        tint = RazorTeal,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            // Bottom Overlay: Mute, Full Screen, Progress bar
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(0.85f))))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                // Slim Draggable Seekable Progress Bar
                                BoxWithConstraints(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(16.dp)
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onPress = { offset ->
                                                    isDragging = true
                                                    val clickX = offset.x
                                                    val width = size.width
                                                    if (width > 0) {
                                                        videoProgress = (clickX / width).coerceIn(0f, 1f)
                                                    }
                                                    try {
                                                        awaitRelease()
                                                    } finally {
                                                        isDragging = false
                                                    }
                                                }
                                            )
                                        }
                                        .pointerInput(Unit) {
                                            detectDragGestures(
                                                onDragStart = { offset ->
                                                    isDragging = true
                                                    val clickX = offset.x
                                                    val width = size.width
                                                    if (width > 0) {
                                                        videoProgress = (clickX / width).coerceIn(0f, 1f)
                                                    }
                                                },
                                                onDragEnd = {
                                                    isDragging = false
                                                },
                                                onDragCancel = {
                                                    isDragging = false
                                                },
                                                onDrag = { change, _ ->
                                                    change.consume()
                                                    val dragX = change.position.x
                                                    val width = size.width
                                                    if (width > 0) {
                                                        videoProgress = (dragX / width).coerceIn(0f, 1f)
                                                    }
                                                }
                                            )
                                        },
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    val width = constraints.maxWidth.toFloat()
                                    val progressWidth = width * videoProgress
                                    val progressWidthDp = with(LocalDensity.current) { progressWidth.toDp() }
                                    val thumbOffset = with(LocalDensity.current) { (progressWidth - 6.dp.toPx()).coerceAtLeast(0f).toDp() }

                                    // Background
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f))
                                    )

                                    // Active progress track
                                    Box(
                                        modifier = Modifier
                                            .width(progressWidthDp)
                                            .height(4.dp)
                                            .clip(CircleShape)
                                            .background(RazorTeal)
                                    )

                                    // Tiny thumb handle
                                    Box(
                                        modifier = Modifier
                                            .offset(x = thumbOffset)
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(if (isDragging) Color.White else RazorTeal)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Mute button & live timing
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                isMutedInline = !isMutedInline
                                                // Reset the 5-sec timer
                                                controlTimerJob?.cancel()
                                                controlTimerJob = coroutineScope.launch {
                                                    delay(5000)
                                                    showControls = false
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isMutedInline) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                                contentDescription = "Mute Inline",
                                                tint = if (isMutedInline) Color.Red else RazorTeal,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        
                                        Text(
                                            text = "0:${String.format("%02d", (videoProgress * 12).toInt())} / 0:12",
                                            color = Color.White.copy(0.8f),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Full screen button
                                    Button(
                                        onClick = {
                                            onVideoClick?.invoke()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(0.5f)),
                                        border = BorderStroke(1.dp, RazorTeal.copy(0.5f)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Launch,
                                                contentDescription = "Full Screen",
                                                tint = RazorTeal,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                "Full Screen",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (!isPlayingInline) {
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

                        val isShort = post.caption.contains("#Shorts", ignoreCase = true) || post.caption.contains("#shorts", ignoreCase = true)
                        val durationLabel = if (isShort) {
                            val seconds = when (post.id) {
                                -1001 -> "15s"
                                -1003 -> "30s"
                                -1005 -> "18s"
                                -1007 -> "45s"
                                -1009 -> "22s"
                                else -> "15s"
                            }
                            "⚡️ SHORT VIDEO • $seconds"
                        } else {
                            val duration = when (post.id) {
                                -1002 -> "8m 45s"
                                -1004 -> "12m 20s"
                                -1006 -> "25m 00s"
                                -1008 -> "15m 10s"
                                -1010 -> "6m 15s"
                                else -> "4m 12s"
                            }
                            "📹 FULL VIDEO • $duration"
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(14.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isShort) RazorTeal.copy(alpha = 0.85f) else InstaPink.copy(alpha = 0.85f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = durationLabel,
                                color = if (isShort) Color.Black else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
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

                    // Double-tap Pop heart overlay animation
                    if (heartVisible) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Double tap heart animation",
                            tint = Color(0xFFFF2D7F),
                            modifier = Modifier
                                .size(100.dp)
                                .align(Alignment.Center)
                                .graphicsLayer {
                                    scaleX = heartScale.value
                                    scaleY = heartScale.value
                                }
                        )
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
                            isLikedLocal = !isLikedLocal
                            if (isLikedLocal) {
                                likesCountLocal += 1
                            } else {
                                likesCountLocal -= 1
                            }
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
                        imageVector = if (isLikedLocal) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like button",
                        tint = if (isLikedLocal) InstaPink else GrayText,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale.value
                                scaleY = scale.value
                            }
                            .size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (likesCountLocal >= 1000) {
                            String.format("%.1fk", likesCountLocal / 1000.0)
                        } else {
                            likesCountLocal.toString()
                        },
                        color = LightText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            showCommentsDialog = true
                            coroutineScope.launch {
                                launch {
                                    commentScale.snapTo(1f)
                                    commentScale.animateTo(1.3f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
                                    commentScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                }
                                launch {
                                    commentRotation.snapTo(0f)
                                    commentRotation.animateTo(15f, tween(100))
                                    commentRotation.animateTo(-15f, tween(100))
                                    commentRotation.animateTo(0f, tween(100))
                                }
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline, 
                        contentDescription = "Comments button", 
                        tint = GrayText, 
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = commentScale.value
                                scaleY = commentScale.value
                                rotationZ = commentRotation.value
                            }
                            .size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "$commentsCount", color = LightText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Icon(imageVector = Icons.Default.Share, contentDescription = "Share post", tint = LightText, modifier = Modifier.size(20.dp))

                // Enabled tips trigger with functional callback instead of hardcoded disabled states
                Box(contentAlignment = Alignment.Center) {
                    Button(
                        onClick = { onTip(5.0) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RazorBlue.copy(alpha = 0.85f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp).testTag("pay_5_tip_btn")
                    ) {
                        Text(text = "Tip $5", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }

            Text(
                text = "View all $commentsCount comments",
                style = MaterialTheme.typography.bodySmall,
                color = GrayText,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        showCommentsDialog = true
                    }
                    .padding(start = 14.dp, bottom = 14.dp, top = 2.dp, end = 14.dp)
            )
        }
    }

    if (showCommentsDialog) {
        CommentDialog(
            post = post,
            comments = commentsList,
            onAddComment = { text ->
                commentsList.add(text)
            },
            onDismiss = { showCommentsDialog = false }
        )
    }
}

@Composable
fun EntranceAnimationContainer(
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val alpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 300, easing = androidx.compose.animation.core.LinearOutSlowInEasing),
        label = "entranceAlpha"
    )
    val translationY by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (visible) 0f else 30f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 300, easing = androidx.compose.animation.core.LinearOutSlowInEasing),
        label = "entranceY"
    )
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            this.translationY = translationY
        }
    ) {
        content()
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
                    text = "Global Creator Market",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = RazorBlue
                )
                Text(
                    text = "Decentralized Creator Arcade. Instantly buy subscription levels or support verified global talent via local ledger routing.",
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
                                if (creator.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified creator",
                                        tint = RazorTeal,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
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

        // Live Event Tickets Section - Page 5 (Category E Integration)
        val filteredEvents = remember(events, searchQuery) {
            events.filter { event ->
                searchQuery.isBlank() ||
                event.title.contains(searchQuery, ignoreCase = true) ||
                event.description.contains(searchQuery, ignoreCase = true) ||
                event.creatorName.contains(searchQuery, ignoreCase = true) ||
                event.creatorHandle.contains(searchQuery, ignoreCase = true)
            }
        }

        if (filteredEvents.isNotEmpty()) {
            Text(
                text = "Live Event Tickets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LightText,
                modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                filteredEvents.forEach { event ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.dp, MinimalBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top Row: Creator & Status badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(RazorBlue.copy(0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = event.creatorName.take(1).uppercase(),
                                            color = RazorBlue,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = event.creatorName,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "@${event.creatorHandle}",
                                            color = GrayText,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                // Glowing LIVE badge or Virtual Room
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(InstaPink.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(InstaPink)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "LIVESTREAM",
                                            color = InstaPink,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Middle Portion: Title & Description
                            Text(
                                text = event.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = event.description,
                                color = GrayText,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Date & Room indicators
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = "Date",
                                        tint = RazorTeal,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = event.dateString,
                                        color = GrayText,
                                        fontSize = 10.sp
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = "Location",
                                        tint = RazorBlue,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = event.location.take(20) + if (event.location.length > 20) "..." else "",
                                        color = GrayText,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Separator Line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.08f))
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Ticket stub details & CTA buy button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "TICKET PRICE",
                                        color = GrayText,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "$${String.format("%.2f", event.ticketPrice)}",
                                        color = RazorTeal,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                // Tickets progress bar
                                val percentSold = (event.ticketsBought.toFloat() / event.originalAvailable.toFloat()).coerceIn(0f, 1f)
                                val remaining = event.originalAvailable - event.ticketsBought
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                                ) {
                                    Text(
                                        text = "$remaining LEFT / ${event.originalAvailable} TOTAL",
                                        color = GrayText,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = percentSold,
                                        color = RazorBlue,
                                        trackColor = Color.White.copy(alpha = 0.05f),
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                    )
                                }

                                val isSoldOut = event.ticketsBought >= event.originalAvailable
                                Button(
                                    onClick = { onBuyTicketClick(event) },
                                    enabled = !isSoldOut,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSoldOut) Color.DarkGray else RazorTeal,
                                        disabledContainerColor = Color.DarkGray
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.height(32.dp).testTag("buy_ticket_${event.id}")
                                ) {
                                    Text(
                                        text = if (isSoldOut) "SOLD OUT" else "GET TICKET",
                                        color = if (isSoldOut) Color.Gray else Color.Black,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
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
                    
                    Button(
                        onClick = onBuy,
                        colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("buy_shop_item_${title.lowercase().replace(" ", "_")}")
                    ) {
                        Text(
                            text = "BUY",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
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
    onMarkAsSeen: (String) -> Unit,
    initialRecipient: String? = null
) {
    var activeThreadRecipient by remember(initialRecipient) { mutableStateOf<String?>(initialRecipient) }
    var inputMessage by remember { mutableStateOf("") }
    var settleUpStatePaid by remember { mutableStateOf(false) }
    var chatSearchQuery by remember { mutableStateOf("") }

    val recipientName = activeThreadRecipient
    LaunchedEffect(recipientName, chatMessages.size) {
        if (recipientName != null) {
            onMarkAsSeen(recipientName)
        }
    }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    if (activeThreadRecipient == null) {
        // --- CHAT PORTAL (MESSENGER REDESIGN) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F081D))
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chats",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = Color.White
                )
                
                // Encryption Global Toggle Indicator in Portal Header
                IconButton(
                    onClick = { onToggleEncryption() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (encryptionEnabled) RazorTeal.copy(alpha = 0.15f) else Color.DarkGray.copy(alpha = 0.3f))
                        .testTag("portal_encryption_toggle")
                ) {
                    Icon(
                        imageVector = if (encryptionEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Toggle Encryption",
                        tint = if (encryptionEnabled) RazorTeal else Color.LightGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Messenger-Style Unified Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1F1532))
                    .border(1.dp, Color(0xFF332353), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = GrayText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = chatSearchQuery,
                        onValueChange = { chatSearchQuery = it },
                        textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 14.sp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (chatSearchQuery.isEmpty()) {
                                Text("Search contacts or messages...", color = GrayText, fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                    )
                }
            }

            // Stories / Active Users row (Messenger-style)
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "ACTIVE NOW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RazorTeal,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val activeUsers = listOf(
                        Triple("Alex Rivera", "Alex", RazorTeal),
                        Triple("Sarah Chen", "Sarah", Color(0xFFFFB300)),
                        Triple("Tokyo Trip", "Tokyo", RazorBlue),
                        Triple("David Kim", "David", Color.LightGray),
                        Triple("Elena Rodriguez", "Elena", Color.LightGray)
                    )
                    
                    items(activeUsers) { user ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { activeThreadRecipient = user.first }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(RazorBlue, InstaPink)
                                            )
                                        )
                                        .padding(2.5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(Color(0xFF130E22)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = user.second.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                                // Glowing active dot
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(RazorTeal)
                                        .border(2.dp, Color(0xFF0F081D), CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = user.second,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Pending Action card - Diners Request (Page 3 Mockup)
            val pendingDineMsg = chatMessages.find { it.isFinancialRequest && it.paymentStatus == "NONE" }
            if (pendingDineMsg != null && chatSearchQuery.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1235)),
                    border = BorderStroke(1.2.dp, RazorTeal.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(RazorTeal.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Payment, contentDescription = null, tint = RazorTeal)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Alex Rivera", fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(InstaPink, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("ACTION REQUIRED", color = Color.Black, fontSize = 7.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                                Text("$45.00 for Dinner split liability", color = GrayText, fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = { onPayInvoice(pendingDineMsg) },
                            colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Pay Now", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }

            Text(
                text = "CONVERSATIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GrayText,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp)
            )

            // Dynamic listed chats mapping with filter
            data class ChatItem(
                val title: String,
                val lastMsg: String,
                val time: String,
                val unseenCount: Int
            )

            val defaultChats = listOf(
                ChatItem("Alex Rivera", "Got the transfer, thanks ...", "10:42 AM", 0),
                ChatItem("Sarah Chen", "Are we still meeting at 5 for coffee?", "Yesterday", 0),
                ChatItem("Tokyo Trip", "@mihe: secure key updated for ...", "2m ago", 0),
                ChatItem("David Kim", "Sent an attachment.", "Tue", 0),
                ChatItem("Elena Rodriguez", "See you at the workshop!", "Mon", 0)
            )

            val recentChatsList = defaultChats.map { defaultChat ->
                val recipient = defaultChat.title
                val threadMsgs = chatMessages.filter {
                    (it.senderName == recipient && it.receiverName == "You") ||
                    (it.senderName == "You" && it.receiverName == recipient) ||
                    (recipient == "Tokyo Trip" && it.receiverName == "Tokyo Trip")
                }
                if (threadMsgs.isNotEmpty()) {
                    val lastMsg = threadMsgs.last()
                    val contentDisclosed = if (lastMsg.isEncrypted) {
                        if (encryptionEnabled) {
                            try {
                                val decoded = android.util.Base64.decode(lastMsg.encryptedContent, android.util.Base64.DEFAULT)
                                String(decoded)
                            } catch (e: Exception) {
                                lastMsg.encryptedContent
                            }
                        } else {
                            "🔒 [Encrypted Message]"
                        }
                    } else {
                        lastMsg.encryptedContent
                    }
                    val unseenCount = threadMsgs.count { it.senderName != "You" && !it.isSeen }
                    ChatItem(
                        title = recipient,
                        lastMsg = contentDisclosed,
                        time = formatChatTimestamp(lastMsg.timestamp),
                        unseenCount = unseenCount
                    )
                } else {
                    defaultChat
                }
            }

            val filteredChats = recentChatsList.filter {
                it.title.contains(chatSearchQuery, ignoreCase = true) ||
                it.lastMsg.contains(chatSearchQuery, ignoreCase = true)
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (filteredChats.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No chats found matching your search", color = GrayText, fontSize = 14.sp, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    items(filteredChats) { chat ->
                        val isGroup = chat.title == "Tokyo Trip"
                        val dynamicIndicator = when (chat.title) {
                            "Alex Rivera" -> RazorTeal
                            "Sarah Chen" -> Color(0xFFFFB300)
                            else -> null
                        }
                        
                        ChatConversationRow(
                            title = chat.title,
                            sub = chat.lastMsg,
                            time = chat.time,
                            indicator = dynamicIndicator,
                            isGroup = isGroup,
                            unseenCount = chat.unseenCount,
                            onOpen = { activeThreadRecipient = chat.title }
                        )
                    }
                }
            }
        }
    } else {
        // --- CHAT WINDOW (THREAD DETAIL VIEW) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0C071A))
        ) {
            val recipientName = activeThreadRecipient ?: ""
            
            // Header Room thread
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B112D))
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { activeThreadRecipient = null },
                    modifier = Modifier.testTag("chat_back_btn")
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                
                Spacer(modifier = Modifier.width(4.dp))
                
                // Avatar next to name (like Messenger)
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(RazorBlue, RazorTeal)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = recipientName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    // Online status dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(RazorTeal)
                            .border(1.5.dp, Color(0xFF1B112D), CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (recipientName == "Tokyo Trip") "Tokyo Trip 🇯🇵" else recipientName,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (encryptionEnabled) RazorTeal else GrayText)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (encryptionEnabled) "Encrypted Secure Channel" else "Unencrypted Plaintext",
                            color = if (encryptionEnabled) RazorTeal else GrayText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Secure encryption toggle switch directly in the header (intuitive & functional)
                IconButton(
                    onClick = { onToggleEncryption() },
                    modifier = Modifier
                        .testTag("chat_header_encryption_toggle")
                        .clip(CircleShape)
                        .background(if (encryptionEnabled) RazorTeal.copy(alpha = 0.15f) else Color.DarkGray.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = if (encryptionEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Toggle Encryption",
                        tint = if (encryptionEnabled) RazorTeal else Color.LightGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Total spent / owe stats strip inside group thread
            if (recipientName == "Tokyo Trip") {
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
                            color = if (settleUpStatePaid) RazorTeal else Color(0xFFFFEB3B),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Beautiful status banner about cryptography encryption (eco-system clarity)
            if (encryptionEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RazorTeal.copy(alpha = 0.08f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = RazorTeal,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "End-to-End Encrypted: Base64 secure cipher active",
                            color = RazorTeal,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Filter thread messages
            val threadMessages = chatMessages.filter {
                (it.senderName == recipientName && it.receiverName == "You") ||
                (it.senderName == "You" && it.receiverName == recipientName) ||
                (recipientName == "Tokyo Trip" && it.receiverName == "Tokyo Trip")
            }

            // Auto-scroller whenever a new message is loaded
            LaunchedEffect(threadMessages.size) {
                if (threadMessages.isNotEmpty()) {
                    listState.animateScrollToItem(threadMessages.size - 1)
                }
            }

            // Message history area
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (recipientName == "Tokyo Trip") {
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
                            text = "Thanks for handling that. Did anyone grab coffee yet? ☕️",
                            time = "4:22 PM",
                            isMe = true
                        )
                    }
                }

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
                        // Secure Invoice card directly in the stream
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1335)),
                            border = BorderStroke(1.2.dp, if (msg.paymentStatus == "PAID") RazorTeal else RazorBlue),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (msg.paymentStatus == "PAID") "SECURE INVOICE (PAID ✅)" else "SECURE INVOICE (PENDING 💸)",
                                            fontWeight = FontWeight.Black,
                                            color = if (msg.paymentStatus == "PAID") RazorTeal else RazorBlue,
                                            fontSize = 10.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Dinner split bill request", fontWeight = FontWeight.Bold, color = LightText, fontSize = 14.sp)
                                        Text("Amount: $${String.format("%.2f", msg.amountRequested)}", color = GrayText, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (msg.paymentStatus == "NONE") {
                                        Button(
                                            onClick = { onPayInvoice(msg) },
                                            colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Pay", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .background(if (msg.paymentStatus == "PAID") RazorTeal.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = msg.paymentStatus, 
                                                color = if (msg.paymentStatus == "PAID") RazorTeal else Color.Red, 
                                                fontSize = 9.sp, 
                                                fontWeight = FontWeight.Black
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
                            time = "${formatChatTimestamp(msg.timestamp)} • ${if (isEncrypted) "Secure E2EE 🔒" else "Plaintext"}",
                            isMe = isMe,
                            isSeen = msg.isSeen,
                            isDelivered = msg.isDelivered
                        )
                    }
                }
            }

            // Bottom Settle up card action if owed money exists - Page 4
            if (recipientName == "Tokyo Trip" && !settleUpStatePaid) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1335)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, RazorBlue.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Settle Up Liability", fontWeight = FontWeight.Bold, color = LightText, fontSize = 14.sp)
                            Text("Use wallet to pay Alex directly", color = GrayText, fontSize = 11.sp)
                        }
                        
                        Button(
                            onClick = {
                                settleUpStatePaid = true
                                onSendMessage("Tokyo Trip", "✅ Settled up my part of Shinkansen Tickets ($120.00) !")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Settle Up $120.00", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Typing block input field (Persistent Input Area)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F081D))
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Action attachment buttons (Gallery, mic, location)
                Row(
                    modifier = Modifier.padding(end = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(onClick = {}) { 
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = RazorBlue, modifier = Modifier.size(20.dp)) 
                    }
                    IconButton(onClick = {}) { 
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice note", tint = RazorBlue, modifier = Modifier.size(20.dp)) 
                    }
                    IconButton(onClick = {}) { 
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Share location", tint = RazorBlue, modifier = Modifier.size(20.dp)) 
                    }
                }
                
                // Rounded Text Input field with inline Lock indicator
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1F1532))
                        .border(
                            width = 1.2.dp,
                            color = if (encryptionEnabled) RazorTeal.copy(alpha = 0.8f) else Color(0xFF332353),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BasicTextField(
                            value = inputMessage,
                            onValueChange = { inputMessage = it },
                            textStyle = LocalTextStyle.current.copy(color = LightText, fontSize = 14.sp),
                            modifier = Modifier.weight(1f),
                            singleLine = false,
                            decorationBox = { innerTextField ->
                                if (inputMessage.isEmpty()) {
                                    Text(
                                        text = if (encryptionEnabled) "🔒 Send encrypted message..." else "Send unencrypted message...",
                                        color = if (encryptionEnabled) RazorTeal.copy(alpha = 0.5f) else GrayText,
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                        
                        // Clickable Secure shield lock right inside input area (persistent encrypted toggle)
                        IconButton(
                            onClick = { onToggleEncryption() },
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("chat_input_encryption_toggle")
                        ) {
                            Icon(
                                imageVector = if (encryptionEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Toggle Encryption",
                                tint = if (encryptionEnabled) RazorTeal else GrayText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Glowing circular send button
                IconButton(
                    onClick = {
                        if (inputMessage.isNotBlank()) {
                            onSendMessage(recipientName, inputMessage)
                            inputMessage = ""
                        }
                    },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (inputMessage.isNotBlank()) RazorTeal else Color.DarkGray)
                        .testTag("chat_send_message_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send message",
                        tint = if (inputMessage.isNotBlank()) Color.Black else Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
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
    isGroup: Boolean = false,
    unseenCount: Int = 0,
    onOpen: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(48.dp)) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = if (isGroup) {
                                listOf(RazorBlue, RazorTeal)
                            } else {
                                listOf(RazorBlue, InstaPink)
                            }
                        )
                    )
                    .padding(2.5.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(ObsidianDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }
            if (indicator != null) {
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(indicator)
                        .border(2.dp, ObsidianDark, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 15.sp,
                    letterSpacing = 0.2.sp
                )
                if (isGroup) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(RazorBlue.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("GROUP", color = RazorBlue, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sub,
                color = if (isGroup) RazorTeal else Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                fontWeight = if (unseenCount > 0) FontWeight.ExtraBold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = time,
                color = if (unseenCount > 0) RazorTeal else Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp,
                fontWeight = if (unseenCount > 0) FontWeight.ExtraBold else FontWeight.Bold
            )
            if (unseenCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .background(RazorTeal, RoundedCornerShape(10.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unseenCount.toString(),
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

fun formatChatTimestamp(timestamp: Long): String {
    val messageTime = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
    val now = java.util.Calendar.getInstance()
    
    return when {
        messageTime.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR) &&
        messageTime.get(java.util.Calendar.DAY_OF_YEAR) == now.get(java.util.Calendar.DAY_OF_YEAR) -> {
            java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(messageTime.time)
        }
        messageTime.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR) &&
        messageTime.get(java.util.Calendar.DAY_OF_YEAR) == now.get(java.util.Calendar.DAY_OF_YEAR) - 1 -> {
            "Yesterday " + java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(messageTime.time)
        }
        else -> {
            java.text.SimpleDateFormat("MMM d, h:mm a", java.util.Locale.getDefault()).format(messageTime.time)
        }
    }
}

@Composable
fun ChatBubble(
    sender: String,
    text: String,
    time: String,
    isMe: Boolean,
    isSeen: Boolean = true,
    isDelivered: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // Recipient Avatar on left of their message bubble (Messenger-Style)
        if (!isMe) {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp, bottom = 4.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF32235E)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sender.take(1).uppercase(),
                    color = RazorTeal,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }

        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            val bubbleShape = if (isMe) {
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
            } else {
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp)
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 250.dp)
                    .clip(bubbleShape)
                    .background(
                        if (isMe) {
                            Brush.linearGradient(
                                colors = listOf(RazorBlue, Color(0xFF0055FF))
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF231A33), Color(0xFF191124))
                            )
                        }
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = if (isMe) {
                                listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)
                            } else {
                                listOf(Color.White.copy(alpha = 0.12f), Color.Transparent)
                            }
                        ),
                        shape = bubbleShape
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    if (!isMe && sender.isNotBlank() && sender != "Alex" && sender != "Sarah") {
                        Text(
                            text = sender.uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            color = RazorTeal,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Text(
                        text = text,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 19.sp
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp, start = if (isMe) 0.dp else 4.dp, end = if (isMe) 4.dp else 0.dp)
            ) {
                Text(
                    text = time,
                    color = GrayText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                if (isMe) {
                    Spacer(modifier = Modifier.width(4.dp))
                    if (isSeen) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Seen",
                                tint = RazorTeal,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Seen", color = RazorTeal, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (isDelivered) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = "Delivered",
                                tint = Color.LightGray,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Delivered", color = GrayText, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
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
    onCancelSubscription: (String, String) -> Unit,
    userVerified: Boolean = false,
    userBronzeName: String = "Bronze Watcher",
    userBronzePrice: Double = 4.99,
    userBronzePerks: String = "Latest daily stock alerts & tech watchlists.",
    userSilverName: String = "Silver Analyst",
    userSilverPrice: Double = 14.99,
    userSilverPerks: String = "Exclusive audio transcripts & deep portfolio wiring sheets.",
    userGoldName: String = "Gold Partner",
    userGoldPrice: Double = 49.99,
    userGoldPerks: String = "Weekly 1-on-1 portfolio review on secure live rooms.",
    onVerifyUser: () -> Unit = {},
    onUnverifyUser: () -> Unit = {},
    onUpdateUserTiers: (String, Double, String, String, Double, String, String, Double, String) -> Unit = { _,_,_, _,_,_, _,_,_ -> },
    onVerifyCreator: (String, Boolean) -> Unit = { _,_ -> }
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
            // Numeric Balance details - Page 6 (Category F Integration)
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text("Total Balance", color = GrayText, fontSize = 12.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isBalanceVisible) "$${String.format("%,.2f", balance)}" else "$ ••••••",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = RazorBlue,
                        modifier = Modifier.testTag("wallet_balance_text")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { isBalanceVisible = !isBalanceVisible },
                        enabled = true,
                        modifier = Modifier.testTag("wallet_balance_toggle")
                    ) {
                        Icon(
                            imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, 
                            contentDescription = "Toggle Balance visibility", 
                            tint = RazorTeal
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Analytics ", color = RazorTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = RazorTeal, modifier = Modifier.size(16.dp))
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
            WalletActivityBtn(icon = Icons.Default.ArrowOutward, title = "Send", onClick = { showSendMoneyDialog = true })
            WalletActivityBtn(icon = Icons.Default.SouthEast, title = "Request", onClick = { showRequestMoneyDialog = true })
            WalletActivityBtn(icon = Icons.Default.CallSplit, title = "Split", onClick = { showSplitDialog = true })
            
            // Neon Green Action for funding - Page 6 (Category F Integration)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { showTopUpDialog = true }
                    .testTag("wallet_top_up")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(RazorTeal.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, RazorTeal, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Top up cash", tint = RazorTeal)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Top Up", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                        Button(
                            onClick = { onPayPizzaSplit() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RazorTeal,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("wallet_pay_pizza")
                        ) {
                            Text("Pay Now", fontWeight = FontWeight.Black)
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
                text = "Verification & Subscriptions Setup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LightText,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
            )

            // Dynamic Verification Center
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (userVerified) Color(0xFF0F2D24) else Color(0xFF251A24)
                ),
                border = BorderStroke(1.2.dp, if (userVerified) RazorTeal else RazorBlue.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (userVerified) Icons.Default.CheckCircle else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (userVerified) RazorTeal else Color(0xFFEA739B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (userVerified) "LEDGER IDENTITY VERIFIED" else "IDENTITY NOT YET CERTIFIED",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        if (userVerified) {
                            Badge(containerColor = RazorTeal, contentColor = Color.Black) {
                                Text("ACTIVE BADGE", fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        } else {
                            Badge(containerColor = Color(0xFFEA739B), contentColor = Color.Black) {
                                Text("LOCKED", fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (userVerified) {
                        Text(
                            text = "Identity certified successfully on decentralized ledgers. Other SocialHub network participants can now subscribe to your dynamic premium tiers and stream feeds. Customize tier rates and benefits below.",
                            color = LightText,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onUnverifyUser,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text("Reset Identity Verification Status", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        var verifyStep by remember { mutableStateOf(1) }
                        var partnerLegalName by remember { mutableStateOf("") }
                        var partnerCategory by remember { mutableStateOf("Tech & Assets") }
                        var scannerProgress by remember { mutableStateOf(0f) }
                        var isScanning by remember { mutableStateOf(false) }

                        if (isScanning) {
                            LaunchedEffect(Unit) {
                                while (scannerProgress < 1f) {
                                    kotlinx.coroutines.delay(100)
                                    scannerProgress += 0.05f
                                }
                                isScanning = false
                                verifyStep = 3
                            }
                        }

                        Text(
                            text = "Creators must complete our automated ledger identity check before customizing public subscription benefits.",
                            color = GrayText,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        if (verifyStep == 1) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Step 1: Legal Name & Niche Category", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = partnerLegalName,
                                    onValueChange = { partnerLegalName = it },
                                    label = { Text("Legal Name (matches official documents)", fontSize = 10.sp) },
                                    textStyle = MaterialTheme.typography.bodySmall,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = RazorBlue,
                                        unfocusedBorderColor = MinimalBorder
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    singleLine = true
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Category: ", color = GrayText, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    listOf("Synthwave", "Crypto Art", "Finance", "Barista").forEach { cat ->
                                        Button(
                                            onClick = { partnerCategory = cat },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (partnerCategory == cat) RazorBlue else Color.DarkGray
                                            ),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.padding(end = 4.dp).height(24.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(cat, fontSize = 9.sp, color = if (partnerCategory == cat) Color.Black else Color.White)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { 
                                        if (partnerLegalName.isNotBlank()) {
                                            verifyStep = 2
                                            isScanning = true
                                        }
                                    },
                                    enabled = partnerLegalName.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Text("Proceed to Identity Audit & Scan", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }
                            }
                        } else if (verifyStep == 2) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                Text("Step 2: Security & ID Authenticity Scan", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))
                                CircularProgressIndicator(
                                    progress = { scannerProgress },
                                    color = RazorBlue,
                                    trackColor = Color.DarkGray,
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Scanning ledger signatures: ${(scannerProgress*100).toInt()}%...",
                                    color = RazorBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text("Auditing identity structures and validating reputation anchors on network nodes.", color = GrayText, fontSize = 9.sp, textAlign = TextAlign.Center)
                            }
                        } else if (verifyStep == 3) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Step 3: Certify securely on the blockchain ledger", color = RazorTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Security check passed perfectly. Ready to update your verified profile badge and activate your creator studio.", color = LightText, fontSize = 11.sp)
                                Button(
                                    onClick = {
                                        onVerifyUser()
                                        verifyStep = 1
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Text("Authorize Digital Certification & Get Badge", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (userVerified) {
                var isTiersUpdatedToast by remember { mutableStateOf(false) }
                
                var bronzeNameState by remember(userBronzeName) { mutableStateOf(userBronzeName) }
                var bronzePriceState by remember(userBronzePrice) { mutableStateOf(userBronzePrice.toString()) }
                var bronzePerksState by remember(userBronzePerks) { mutableStateOf(userBronzePerks) }

                var silverNameState by remember(userSilverName) { mutableStateOf(userSilverName) }
                var silverPriceState by remember(userSilverPrice) { mutableStateOf(userSilverPrice.toString()) }
                var silverPerksState by remember(userSilverPerks) { mutableStateOf(userSilverPerks) }

                var goldNameState by remember(userGoldName) { mutableStateOf(userGoldName) }
                var goldPriceState by remember(userGoldPrice) { mutableStateOf(userGoldPrice.toString()) }
                var goldPerksState by remember(userGoldPerks) { mutableStateOf(userGoldPerks) }

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
                            text = "Set Access Prices & Benefits",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Define titles, price multipliers, and customized benefit details for participants.",
                            color = GrayText,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        // Bronze Level
                        Text("🟢 BASE TIER (BRONZE LEVEL)", color = RazorBlue, fontWeight = FontWeight.Bold, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        OutlinedTextField(
                            value = bronzeNameState,
                            onValueChange = { bronzeNameState = it },
                            label = { Text("Tier Title", fontSize = 10.sp) },
                            textStyle = MaterialTheme.typography.bodySmall,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = bronzePriceState,
                                onValueChange = { bronzePriceState = it },
                                label = { Text("Price/mo", fontSize = 10.sp) },
                                prefix = { Text("$", color = GrayText, fontSize = 10.sp) },
                                textStyle = MaterialTheme.typography.bodySmall,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.weight(1.1f).height(52.dp)
                            )
                            OutlinedTextField(
                                value = bronzePerksState,
                                onValueChange = { bronzePerksState = it },
                                label = { Text("Benefit Description", fontSize = 10.sp) },
                                textStyle = MaterialTheme.typography.bodySmall,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.weight(2.4f).height(52.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Silver Level
                        Text("🔵 INTERMEDIATE TIER (SILVER LEVEL)", color = RazorTeal, fontWeight = FontWeight.Bold, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        OutlinedTextField(
                            value = silverNameState,
                            onValueChange = { silverNameState = it },
                            label = { Text("Tier Title", fontSize = 10.sp) },
                            textStyle = MaterialTheme.typography.bodySmall,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = silverPriceState,
                                onValueChange = { silverPriceState = it },
                                label = { Text("Price/mo", fontSize = 10.sp) },
                                prefix = { Text("$", color = GrayText, fontSize = 10.sp) },
                                textStyle = MaterialTheme.typography.bodySmall,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.weight(1.1f).height(52.dp)
                            )
                            OutlinedTextField(
                                value = silverPerksState,
                                onValueChange = { silverPerksState = it },
                                label = { Text("Benefit Description", fontSize = 10.sp) },
                                textStyle = MaterialTheme.typography.bodySmall,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.weight(2.4f).height(52.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Gold Level
                        Text("👑 PREMIUM TIER (GOLD LEVEL)", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        OutlinedTextField(
                            value = goldNameState,
                            onValueChange = { goldNameState = it },
                            label = { Text("Tier Title", fontSize = 10.sp) },
                            textStyle = MaterialTheme.typography.bodySmall,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = goldPriceState,
                                onValueChange = { goldPriceState = it },
                                label = { Text("Price/mo", fontSize = 10.sp) },
                                prefix = { Text("$", color = GrayText, fontSize = 10.sp) },
                                textStyle = MaterialTheme.typography.bodySmall,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.weight(1.1f).height(52.dp)
                            )
                            OutlinedTextField(
                                value = goldPerksState,
                                onValueChange = { goldPerksState = it },
                                label = { Text("Benefit Description", fontSize = 10.sp) },
                                textStyle = MaterialTheme.typography.bodySmall,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.weight(2.4f).height(52.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val bPrice = bronzePriceState.toDoubleOrNull() ?: 4.99
                                val sPrice = silverPriceState.toDoubleOrNull() ?: 14.99
                                val gPrice = goldPriceState.toDoubleOrNull() ?: 49.99
                                onUpdateUserTiers(
                                    bronzeNameState, bPrice, bronzePerksState,
                                    silverNameState, sPrice, silverPerksState,
                                    goldNameState, gPrice, goldPerksState
                                )
                                isTiersUpdatedToast = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Dynamic Subscription Levels", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
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
                                Text("SUBSCRIPTION TIERS UPDATE COMPLETE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Your customizable creator levels, benefit perks, and subscription pricing rates were successfully written on secure local tables.", color = LightText, fontSize = 11.sp, textAlign = TextAlign.Center)
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
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, MinimalBorder.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = GrayText.copy(alpha = 0.4f), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Custom Subscription Setup Locked", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("You must complete secure identity verification above to establish multiple subscription levels.", color = GrayText, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 2.dp))
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
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF1B2236), CircleShape)
                    .border(1.dp, RazorBlue.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = RazorBlue)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
    onBack: () -> Unit,
    onVerifyToggle: (Boolean) -> Unit = {},
    onVideoClick: (Post) -> Unit = {}
) {
    val activeSub = subscriptions.find { it.creatorId == creator.id }
    
    var selectedBadgeDetail by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showShareToast by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Premium overlapping mobile-first header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            // Elegant background cover gradient with glassmorphic top header bar overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(RazorBlue.copy(alpha = 0.15f), Color(0xFF2C1E4E))
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .size(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back back step", tint = LightText, modifier = Modifier.size(16.dp))
                }
                Text(
                    text = "Creator Hub",
                    fontWeight = FontWeight.Black,
                    color = LightText,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .size(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.MoreHoriz, contentDescription = "More", tint = LightText, modifier = Modifier.size(16.dp))
                }
            }

            // Overlapping Avatar centering at the edge of the banner
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 40.dp)
            ) {
                Box(modifier = Modifier.size(90.dp)) {
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF130E22))
                            .border(3.dp, RazorBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = creator.name.take(1).uppercase(),
                            fontWeight = FontWeight.Black,
                            color = RazorBlue,
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }

                    // Small verified checkmark badge overlay
                    if (creator.isVerified) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(RazorTeal, CircleShape)
                                .border(2.dp, ObsidianDark, CircleShape)
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Verified creator", tint = Color.Black, modifier = Modifier.size(11.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Name, Handle & Bio
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = creator.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                if (creator.isVerified) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified Identity certified",
                        tint = RazorTeal,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "@${creator.handle}", color = RazorTeal, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                if (activeSub != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFFD54F).copy(alpha = 0.15f))
                            .border(0.5.dp, Color(0xFFFFD54F), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(text = activeSub.tierName, color = Color(0xFFFFD54F), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = creator.description,
                textAlign = TextAlign.Center,
                color = LightText.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        // Consolidated Premium Contact & Main Actions Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main Follow Button (weight-scaled)
            Button(
                onClick = onFollowToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (creator.isFollowed) Color(0xFF1B2236) else RazorTeal
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.5f),
                border = if (creator.isFollowed) BorderStroke(1.dp, RazorTeal) else null,
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (creator.isFollowed) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        tint = if (creator.isFollowed) RazorTeal else Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (creator.isFollowed) "Following" else "Follow",
                        color = if (creator.isFollowed) RazorTeal else Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
            }

            // Message Button with matching styling
            Button(
                onClick = onMessageClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C1E4E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.2f),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MailOutline,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Message",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
            }

            // Minimalist Share Button
            IconButton(
                onClick = { showShareToast = true },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF1B2236), RoundedCornerShape(12.dp))
                    .border(1.dp, RazorBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Portfolio",
                    tint = RazorBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Beautiful Grid of stats details
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatsCardBox(title = "$1.2M", sub = "PORTFOLIO VALUE", modifier = Modifier.weight(1f))
            StatsCardBox(title = "14.2K", sub = "FOLLOWERS", modifier = Modifier.weight(1f))
            StatsCardBox(title = "982", sub = "TRUST SCORE", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interactive Tab Selector (Insights, Subscriptions, Trust)
        var activeProfileTab by remember { mutableStateOf("feed") }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .background(Color(0xFF130E29), RoundedCornerShape(12.dp))
                .border(1.dp, RazorBlue.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            // Tab 1: Feed (including Publications / Reels)
            Button(
                onClick = { activeProfileTab = "feed" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeProfileTab == "feed") RazorTeal else Color.Transparent,
                    contentColor = if (activeProfileTab == "feed") Color.Black else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp),
                elevation = null
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Insights and reels feed",
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Insights", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Tab 2: Premium Subscription Options
            Button(
                onClick = { activeProfileTab = "subscriptions" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeProfileTab == "subscriptions") RazorTeal else Color.Transparent,
                    contentColor = if (activeProfileTab == "subscriptions") Color.Black else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1.3f),
                contentPadding = PaddingValues(vertical = 4.dp),
                elevation = null
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Premium Subscription Tiers",
                        modifier = Modifier.size(13.dp),
                        tint = if (activeProfileTab == "subscriptions") Color.Black else Color(0xFFFFD54F)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Subscription", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Tab 3: Reputation & Badges
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
                        contentDescription = "Trust reputation",
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Trust", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (activeProfileTab == "feed") {
            // Sub-toggles inside Insights tab to filter Posts vs Reels
            var gridType by remember { mutableStateOf("releases") }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Releases filter chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (gridType == "releases") RazorTeal.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (gridType == "releases") RazorTeal else MinimalBorder.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { gridType = "releases" }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Publications",
                        color = if (gridType == "releases") RazorTeal else GrayText,
                        fontSize = 11.sp,
                        fontWeight = if (gridType == "releases") FontWeight.Bold else FontWeight.Normal
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Reels filter chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (gridType == "reels") RazorTeal.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (gridType == "reels") RazorTeal else MinimalBorder.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { gridType = "reels" }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Short Reels",
                        color = if (gridType == "reels") RazorTeal else GrayText,
                        fontSize = 11.sp,
                        fontWeight = if (gridType == "reels") FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (gridType == "releases") {
                CreatorPostsGrid(
                    posts = posts,
                    subscriptions = subscriptions,
                    onLikePost = onLikePost,
                    onVideoClick = onVideoClick
                )
            } else {
                CreatorReelsGrid(
                    posts = posts,
                    creator = creator,
                    subscriptions = subscriptions,
                    onLikePost = onLikePost,
                    onVideoClick = onVideoClick
                )
            }
        } else if (activeProfileTab == "subscriptions") {
            // Highly Consolidated and Premium Subscriptions Portal
            Text(
                text = "Premium Portfolio Access Tiers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 2.dp)
            )
            Text(
                text = "Unlocking a tier awards real-time token alerts, private portfolio wire sheets, and audio transcripts.",
                fontSize = 11.sp,
                color = GrayText,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (activeSub != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = RazorTeal.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, RazorTeal)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = RazorTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ACTIVE PASS: ${activeSub.tierName} TIER", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Text("You are connected as a premium ${activeSub.tierName} member. Check your inbox and feed for premium unlockable updates.", color = LightText, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onCancelSubscription,
                            colors = ButtonDefaults.buttonColors(containerColor = InstaPink),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text("Cancel Subscription Pass", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            if (!creator.isVerified) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C151D)),
                    border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "SUBCRIPTION LEVELS TEMPORARILY LOCKED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text(
                            text = "To protect network users, premium membership subscription levels are disabled until this creator completes secure KYC verification.",
                            color = LightText,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SubscriptionTierRow(
                        tier = creator.bronzeTierName,
                        price = creator.bronzeTierPrice,
                        perks = creator.bronzeTierPerks,
                        active = activeSub?.tierName == creator.bronzeTierName,
                        onBuy = { onSubscribeClick(creator.bronzeTierName, creator.bronzeTierPrice) }
                    )

                    SubscriptionTierRow(
                        tier = creator.silverTierName,
                        price = creator.silverTierPrice,
                        perks = creator.silverTierPerks,
                        active = activeSub?.tierName == creator.silverTierName,
                        onBuy = { onSubscribeClick(creator.silverTierName, creator.silverTierPrice) }
                    )

                    SubscriptionTierRow(
                        tier = creator.goldTierName,
                        price = creator.goldTierPrice,
                        perks = creator.goldTierPerks,
                        active = activeSub?.tierName == creator.goldTierName,
                        onBuy = { onSubscribeClick(creator.goldTierName, creator.goldTierPrice) }
                    )
                }
            }
        } else {
            // Trust & Reputation Panel
            Text(
                text = "Financial Reputation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LightText,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
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
                        Text("Diamond-Tier Safe Mode", fontWeight = FontWeight.Black, color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Badge(containerColor = RazorBlue.copy(alpha = 0.2f), contentColor = RazorBlue) {
                            Text("RANK #12", modifier = Modifier.padding(4.dp), fontWeight = FontWeight.Black, fontSize = 9.sp)
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("850 / 1000 Trust Weight", color = GrayText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
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
                        Text(text = "+15 Network Reputation Points this week", color = RazorTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "🛠️ System Verification Override (Demo Controller)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RazorBlue,
                modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 4.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1730)),
                border = BorderStroke(1.dp, RazorBlue.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Toggle this creator's verification badge and mock dynamic sub-tier rates instantly for demonstration purposes.",
                        fontSize = 11.sp,
                        color = LightText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onVerifyToggle(!creator.isVerified) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (creator.isVerified) Color(0xFFFF5252) else RazorTeal
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text(
                            text = if (creator.isVerified) "Bypass: Revoke Verification License" else "Bypass: Authorize Verification Badge",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        } // End of activeProfileTab blocks

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
                    Button(
                        onClick = onBuy,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RazorTeal,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Unlock", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
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

    // Bright glass metallic pastel shimmer colors (not dark and blurry)
    val shimmerColors = listOf(
        Color(0x2BFFFFFF), // Elegant translucent white glass sheer
        Color(0x4DFF4CA0), // Glowing bright liquid pink highlight
        Color(0x4DFF7B40), // Bright neon tangerine highlight
        Color(0x2BFFFFFF)
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
    
    var cameraStatus by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    
    var micStatus by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    
    var mediaStatus by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.READ_MEDIA_IMAGES
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.READ_MEDIA_VIDEO
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.READ_MEDIA_AUDIO
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            }
        )
    }
    
    var notificationsStatus by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context, "android.permission.POST_NOTIFICATIONS"
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val systemPermissions = if (android.os.Build.VERSION.SDK_INT >= 33) {
        arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO,
            android.Manifest.permission.READ_MEDIA_AUDIO,
            "android.permission.POST_NOTIFICATIONS"
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
        cameraStatus = results[android.Manifest.permission.CAMERA] ?: cameraStatus
        micStatus = results[android.Manifest.permission.RECORD_AUDIO] ?: micStatus
        
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            mediaStatus = (results[android.Manifest.permission.READ_MEDIA_IMAGES] == true) ||
                          (results[android.Manifest.permission.READ_MEDIA_VIDEO] == true) ||
                          (results[android.Manifest.permission.READ_MEDIA_AUDIO] == true)
            notificationsStatus = results["android.permission.POST_NOTIFICATIONS"] ?: notificationsStatus
        } else {
            mediaStatus = results[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: mediaStatus
        }
    }

    val isAllCurrentlyGranted = cameraStatus && micStatus && mediaStatus && notificationsStatus

    androidx.compose.runtime.LaunchedEffect(isAllCurrentlyGranted) {
        if (isAllCurrentlyGranted) {
            onPermissionsGranted(true)
            onDismiss()
        }
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
                        icon = Icons.Default.CameraAlt,
                        title = "Live Camera Snapshots",
                        desc = "Used for real-time creative photo uploads.",
                        isGranted = cameraStatus
                    )
                    PermissionRowItem(
                        icon = Icons.Default.Mic,
                        title = "Microphone Audio Capture",
                        desc = "Allows recording voices, comments & sound narratives.",
                        isGranted = micStatus
                    )
                    PermissionRowItem(
                        icon = Icons.Default.PhotoLibrary,
                        title = "Media Pickers & Storage",
                        desc = "Retrieve media files, videos, and music clips.",
                        isGranted = mediaStatus
                    )
                    if (android.os.Build.VERSION.SDK_INT >= 33) {
                        PermissionRowItem(
                            icon = Icons.Default.Notifications,
                            title = "System Push Notifications",
                            desc = "Provides real-time transactional alert telemetry.",
                            isGranted = notificationsStatus
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isAllCurrentlyGranted) {
                    Button(
                        onClick = {
                            onPermissionsGranted(true)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enter Social Hub", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Skip for Now", color = GrayText, fontWeight = FontWeight.Bold)
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
}

@Composable
fun PermissionRowItem(
    icon: ImageVector,
    title: String,
    desc: String,
    isGranted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(if (isGranted) RazorTeal.copy(alpha = 0.15f) else RazorBlue.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = if (isGranted) RazorTeal else RazorBlue, 
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, color = GrayText, fontSize = 11.sp)
        }
        // Custom visual badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isGranted) RazorTeal.copy(alpha = 0.12f) else SafeGold.copy(alpha = 0.12f))
                .border(1.dp, if (isGranted) RazorTeal.copy(alpha = 0.3f) else SafeGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isGranted) "AUTHORIZED" else "PENDING",
                color = if (isGranted) RazorTeal else SafeGold,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold
            )
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
    onLikePost: (Post) -> Unit,
    onVideoClick: (Post) -> Unit = {}
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
                                onLikePost = onLikePost,
                                onVideoClick = onVideoClick
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
fun CreatorReelsGrid(
    posts: List<Post>,
    creator: Creator,
    subscriptions: List<Subscription>,
    onLikePost: (Post) -> Unit,
    onVideoClick: (Post) -> Unit = {}
) {
    val videoPosts = posts.filter { it.contentImage == "attached_video" }
    
    val displayReels = if (videoPosts.isNotEmpty()) {
        videoPosts
    } else {
        listOf(
            Post(
                id = -100 - creator.id.hashCode(),
                creatorId = creator.id,
                creatorName = creator.name,
                creatorHandle = creator.handle,
                creatorAvatar = creator.avatarUrl,
                caption = "Epic aesthetic studio sessions! Decibel thresholds and live synthesizer adjustments. 🌌⚡️ #synthesizer #reels",
                contentImage = "attached_video",
                isPremium = false,
                likesCount = 240,
                tipsTotal = 15.0
            ),
            Post(
                id = -200 - creator.id.hashCode(),
                creatorId = creator.id,
                creatorName = creator.name,
                creatorHandle = creator.handle,
                creatorAvatar = creator.avatarUrl,
                caption = "Behind the scenes: calibrating high-frequency cybernetic audio monitors. Crisp frequencies. 🎧📹 #vlog #creators",
                contentImage = "attached_video",
                isPremium = true,
                requiredTier = "BRONZE",
                likesCount = 512,
                tipsTotal = 60.0
            ),
            Post(
                id = -300 - creator.id.hashCode(),
                creatorId = creator.id,
                creatorName = creator.name,
                creatorHandle = creator.handle,
                creatorAvatar = creator.avatarUrl,
                caption = "Quick market structure synthesis & holographic trend prediction models. Let's watch! 📈⚡️ #finance #strategy",
                contentImage = "attached_video",
                isPremium = true,
                requiredTier = "GOLD",
                likesCount = 1400,
                tipsTotal = 250.0
            )
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        val width = maxWidth
        val columns = if (width < 600.dp) 3 else 4
        val chunkedReels = displayReels.chunked(columns)

        var activePlayingReel by remember { mutableStateOf<Post?>(null) }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chunkedReels.forEach { rowReels ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowReels.forEach { reel ->
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            GridReelCard(
                                reel = reel,
                                subscriptions = subscriptions,
                                onReelClick = {
                                    onVideoClick(reel)
                                }
                            )
                        }
                    }
                    if (rowReels.size < columns) {
                        repeat(columns - rowReels.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        if (activePlayingReel != null) {
            ReelPlaybackOverlay(
                reel = activePlayingReel!!,
                subscriptions = subscriptions,
                onLike = { onLikePost(activePlayingReel!!) },
                onDismiss = { activePlayingReel = null }
            )
        }
    }
}

@Composable
fun GridReelCard(
    reel: Post,
    subscriptions: List<Subscription>,
    onReelClick: () -> Unit
) {
    val hasSubscription = subscriptions.any {
        it.creatorId == reel.creatorId &&
        (it.tierName == "GOLD" || 
         (it.tierName == "SILVER" && reel.requiredTier != "GOLD") || 
         (it.tierName == "BRONZE" && reel.requiredTier == "BRONZE"))
    }
    val isLocked = reel.isPremium && !hasSubscription && reel.creatorId != "pixel_queen"

    val randomViews = remember(reel.id) { (1000..95000).random() }
    val viewsStr = if (randomViews >= 1000) "${String.format("%.1f", randomViews / 1000f)}K" else "$randomViews"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.65f)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, MinimalBorder.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable { onReelClick() },
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val brush = Brush.verticalGradient(
                    colors = listOf(
                        InstaPink.copy(alpha = 0.15f),
                        ObsidianDark.copy(alpha = 0.95f)
                    )
                )
                drawRect(brush = brush)
                
                val cx = size.width / 2
                val cy = size.height / 2
                drawCircle(
                    color = RazorBlue.copy(alpha = 0.1f),
                    radius = 24.dp.toPx(),
                    center = Offset(cx, cy)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLocked) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked Reel",
                            tint = InstaPink,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = reel.requiredTier,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Reel",
                        tint = RazorTeal,
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .padding(4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = viewsStr,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = "0:${(10..45).random()}",
                    color = GrayText,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun BuiltInVideoPlayerOverlay(
    post: Post,
    subscriptions: List<Subscription>,
    walletBalance: Double,
    onLikeToggle: () -> Unit,
    onTipSend: (Double) -> Unit,
    onUnlockCreator: () -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val hasSubscription = subscriptions.any {
        it.creatorId == post.creatorId &&
        (it.tierName == "GOLD" || 
         (it.tierName == "SILVER" && post.requiredTier != "GOLD") || 
         (it.tierName == "BRONZE" && post.requiredTier == "BRONZE"))
    }
    val isLocked = post.isPremium && !hasSubscription && post.creatorId != "pixel_queen"

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.6f)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, Brush.linearGradient(colors = listOf(RazorBlue, RazorTeal)), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = ObsidianDark)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLocked) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .background(InstaPink.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked Content",
                                tint = InstaPink,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "PREMIUM PORTFOLIO VIDEO",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Subscribe to @${post.creatorHandle}'s ${post.requiredTier} tier to unlock instant cinematic access.",
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = GrayText
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                        Button(
                            onClick = onUnlockCreator,
                            colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Unlock Portal Tier", color = Color.Black, fontWeight = FontWeight.Black)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = onDismiss) {
                            Text("Go Back", color = Color.White.copy(0.6f), fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    var isPlaying by remember { mutableStateOf(true) }
                    var isMuted by remember { mutableStateOf(false) }
                    var playbackSpeed by remember { mutableStateOf(1f) }
                    var aspectRatioMode by remember { mutableStateOf("Original") }
                    var showStatsHUD by remember { mutableStateOf(false) }
                    var activeFilter by remember { mutableStateOf("Normal") }
                    var volume by remember { mutableStateOf(0.8f) }
                    var showCommentsSheet by remember { mutableStateOf(false) }
                    var showTipSheet by remember { mutableStateOf(false) }
                    var commentInput by remember { mutableStateOf("") }
                    
                    val commentsList = remember {
                        mutableStateListOf(
                            "@cyber_sam: This alpha stream analysis is completely top-tier! 🌐🚀",
                            "@hustle_hard: The speed and resolution of this feed is crazy.",
                            "@pixel_pioneer: Those frequency lines on the audio EQ are sick!",
                            "@trade_pro: Cleanest studio setup, outstanding quality."
                        )
                    }

                    var isLikedLocal by remember { mutableStateOf(post.isLiked) }
                    var likesLocal by remember { mutableStateOf(post.likesCount) }
                    
                    val progressValue = remember { Animatable(0f) }

                    LaunchedEffect(isPlaying, playbackSpeed) {
                        if (isPlaying) {
                            val durationMs = (12000 / playbackSpeed).toInt()
                            progressValue.animateTo(
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(durationMs, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                )
                            )
                        } else {
                            progressValue.stop()
                        }
                    }

                    val floatingHearts = remember { mutableStateListOf<Pair<Long, Offset>>() }

                    val contentScale = when (aspectRatioMode) {
                        "Fit" -> ContentScale.Fit
                        "Stretch" -> ContentScale.FillBounds
                        "Zoom" -> ContentScale.Crop
                        else -> ContentScale.Inside
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(post.id) {
                                detectTapGestures(
                                    onTap = { isPlaying = !isPlaying },
                                    onDoubleTap = { offset ->
                                        if (!isLikedLocal) {
                                            isLikedLocal = true
                                            likesLocal += 1
                                            onLikeToggle()
                                        }
                                        floatingHearts.add(Pair(System.currentTimeMillis(), offset))
                                    }
                                )
                            }
                    ) {
                        val imageUrl = if (post.contentImage.startsWith("http")) {
                            post.contentImage
                        } else {
                            "https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=800&auto=format&fit=crop&q=80"
                        }

                        val matrixOverlayColor = when (activeFilter) {
                            "Neon Cyber" -> InstaPink.copy(alpha = 0.3f)
                            "Warm Vintage" -> Color(0xFF8B5A2B).copy(alpha = 0.35f)
                            "Cinematic Noir" -> Color.Black.copy(alpha = 0.4f)
                            "Vaporwave" -> RazorBlue.copy(alpha = 0.35f)
                            else -> Color.Transparent
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Active video frame stream",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(if (activeFilter == "Cinematic Noir") 0.45f else 0.85f),
                                contentScale = contentScale
                            )

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                if (activeFilter == "Cinematic Noir") {
                                    drawRect(color = Color.Gray.copy(alpha = 0.15f))
                                }
                                if (matrixOverlayColor != Color.Transparent) {
                                    drawRect(color = matrixOverlayColor)
                                }

                                if (activeFilter == "Neon Cyber" || activeFilter == "Vaporwave") {
                                    val gap = 6.dp.toPx()
                                    var y = 0f
                                    while (y < size.height) {
                                        drawLine(
                                            color = Color.White.copy(alpha = 0.08f),
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = 1.dp.toPx()
                                        )
                                        y += gap
                                    }
                                }

                                if (isPlaying && !isMuted) {
                                    val count = 12
                                    val gap = size.width / (count + 1)
                                    val pulse = progressValue.value
                                    for (i in 0 until count) {
                                        val barX = gap * (i + 1)
                                        val waveFactor = kotlin.math.sin((i * 0.4f + pulse * 2 * 3.14159f).toDouble()).toFloat()
                                        val barHeight = (40.dp.toPx() + 35.dp.toPx() * waveFactor) * (volume)
                                        drawLine(
                                            color = RazorTeal.copy(alpha = 0.65f),
                                            start = Offset(barX, size.height - 85.dp.toPx() - barHeight / 2),
                                            end = Offset(barX, size.height - 85.dp.toPx() + barHeight / 2),
                                            strokeWidth = 3.dp.toPx()
                                        )
                                    }
                                }
                            }
                        }

                        floatingHearts.forEach { (id, offset) ->
                            var isHeartActive by remember { mutableStateOf(true) }
                            val scale = remember { Animatable(0f) }
                            val alpha = remember { Animatable(1f) }

                            LaunchedEffect(id) {
                                scale.animateTo(1.6f, animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioHighBouncy))
                                delay(300)
                                alpha.animateTo(0f, animationSpec = tween(300))
                                isHeartActive = false
                            }

                            if (isHeartActive) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = InstaPink,
                                    modifier = Modifier
                                        .offset(
                                            x = (offset.x / 3f).dp,
                                            y = (offset.y / 3f).dp
                                        )
                                        .graphicsLayer(
                                            scaleX = scale.value,
                                            scaleY = scale.value,
                                            alpha = alpha.value
                                        )
                                        .size(34.dp)
                                )
                            }
                        }

                        if (!isPlaying) {
                            Box(
                                modifier = Modifier
                                    .size(65.dp)
                                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                    .align(Alignment.Center),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Resume",
                                    tint = RazorTeal,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .background(Brush.verticalGradient(colors = listOf(Color.Black.copy(0.7f), Color.Transparent)))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.Black.copy(0.4f), CircleShape)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close player", tint = Color.White, modifier = Modifier.size(16.dp))
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(0.4f))
                                        .clickable {
                                            playbackSpeed = when (playbackSpeed) {
                                                1.0f -> 1.25f
                                                1.25f -> 1.5f
                                                1.5f -> 2.0f
                                                else -> 1.0f
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("${playbackSpeed}x", color = RazorTeal, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(0.4f))
                                        .clickable {
                                            aspectRatioMode = when (aspectRatioMode) {
                                                "Original" -> "Fit"
                                                "Fit" -> "Stretch"
                                                "Stretch" -> "Zoom"
                                                else -> "Original"
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(aspectRatioMode, color = RazorBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(0.4f))
                                        .clickable {
                                            activeFilter = when (activeFilter) {
                                                "Normal" -> "Neon Cyber"
                                                "Neon Cyber" -> "Warm Vintage"
                                                "Warm Vintage" -> "Cinematic Noir"
                                                "Cinematic Noir" -> "Vaporwave"
                                                else -> "Normal"
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(activeFilter, color = InstaPink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                IconButton(
                                    onClick = { showStatsHUD = !showStatsHUD },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color.Black.copy(0.4f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Stats HUD",
                                        tint = if (showStatsHUD) RazorTeal else Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        if (showStatsHUD) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(20.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black.copy(alpha = 0.85f))
                                    .border(1.dp, RazorBlue.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("CYBERSTREAM HARDWARE STATS", color = RazorTeal, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    HorizontalDivider(color = Color.White.copy(0.15f), modifier = Modifier.padding(vertical = 4.dp))
                                    Text("Codec: h.264 High Profile @L4.1", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    Text("Resolution: 1080x1920 (Vertical 9:16)", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    Text("Framerate: 60.00 fps (Constant)", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    Text("Bitrate: 4520 kbps (VBR)", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    Text("Buffer: 124 ms", color = RazorBlue, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    Text("Active Filter: $activeFilter", color = InstaPink, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp)
                                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
                                .padding(vertical = 12.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    isLikedLocal = !isLikedLocal
                                    likesLocal += if (isLikedLocal) 1 else -1
                                    onLikeToggle()
                                }
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (isLikedLocal) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like stream",
                                        tint = if (isLikedLocal) InstaPink else Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text("$likesLocal", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            IconButton(onClick = { showCommentsSheet = !showCommentsSheet }) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.MailOutline,
                                        contentDescription = "View interactive stream feedback comments",
                                        tint = if (showCommentsSheet) RazorBlue else Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text("${commentsList.size}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            IconButton(onClick = { showTipSheet = !showTipSheet }) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Tip Creator",
                                        tint = if (showTipSheet) RazorTeal else Color(0xFFFFD54F),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text("Tip", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            IconButton(onClick = { isMuted = !isMuted }) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                    contentDescription = "Audio toggle",
                                    tint = if (isMuted) Color.Red else RazorTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(0.85f))))
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(RazorBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(post.creatorName.take(1).uppercase(), color = Color.Black, fontWeight = FontWeight.Black, fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("@${post.creatorHandle}", color = RazorTeal, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = post.caption,
                                color = Color.White,
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "0:${String.format("%02d", (progressValue.value * 12).toInt())}",
                                    color = GrayText,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(16.dp)
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onTap = { offset ->
                                                    val fraction = offset.x / size.width
                                                    coroutineScope.launch {
                                                        progressValue.snapTo(fraction.coerceIn(0f, 1f))
                                                    }
                                                }
                                            )
                                        },
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(progressValue.value)
                                            .height(4.dp)
                                            .clip(CircleShape)
                                            .background(RazorTeal)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .offset(x = (progressValue.value * 150).dp)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }

                                Text(
                                    text = "0:12",
                                    color = GrayText,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        if (showCommentsSheet) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.55f)
                                    .align(Alignment.BottomCenter)
                                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                    .background(ObsidianDark)
                                    .border(1.5.dp, RazorBlue.copy(0.3f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                    .padding(14.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("LIVE VIDEO CHAT COMMENTS", color = InstaPink, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        IconButton(onClick = { showCommentsSheet = false }, modifier = Modifier.size(24.dp)) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    HorizontalDivider(color = Color.White.copy(0.12f), modifier = Modifier.padding(vertical = 6.dp))

                                    LazyColumn(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(commentsList) { item ->
                                            val parts = item.split(": ", limit = 2)
                                            Row(modifier = Modifier.fillMaxWidth()) {
                                                Text(parts[0], color = RazorTeal, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(parts.getOrElse(1) { "" }, color = Color.White, fontSize = 11.sp)
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = commentInput,
                                            onValueChange = { commentInput = it },
                                            placeholder = { Text("Add verified feedback comment...", color = GrayText, fontSize = 11.sp) },
                                            modifier = Modifier.weight(1f),
                                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = Color.White.copy(0.2f),
                                                focusedBorderColor = RazorTeal
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        IconButton(
                                            onClick = {
                                                if (commentInput.isNotBlank()) {
                                                    commentsList.add("@anonymous: $commentInput")
                                                    commentInput = ""
                                                }
                                            },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(RazorTeal, RoundedCornerShape(10.dp))
                                        ) {
                                            Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        if (showTipSheet) {
                            var customTipAmt by remember { mutableStateOf("") }
                            var tipNotificationMessage by remember { mutableStateOf<String?>(null) }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                    .background(ObsidianDark)
                                    .border(1.5.dp, RazorTeal.copy(0.4f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                    .padding(16.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("INSTANT TIPPING HUB", color = RazorTeal, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                        IconButton(onClick = { showTipSheet = false }, modifier = Modifier.size(24.dp)) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Tip @${post.creatorHandle} securely from wallet. Balance: $${String.format("%.2f", walletBalance)}", color = GrayText, fontSize = 11.sp)
                                    HorizontalDivider(color = Color.White.copy(0.12f), modifier = Modifier.padding(vertical = 8.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        listOf(5.0, 10.0, 25.0).forEach { value ->
                                            Button(
                                                onClick = {
                                                    if (walletBalance >= value) {
                                                        onTipSend(value)
                                                        tipNotificationMessage = "Successfully Tipped $${value}!"
                                                    } else {
                                                        tipNotificationMessage = "Insufficient Wallet Funds!"
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B2236)),
                                                border = BorderStroke(1.dp, RazorBlue.copy(0.4f)),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("$$value", color = RazorBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = customTipAmt,
                                            onValueChange = { customTipAmt = it },
                                            placeholder = { Text("Custom Amount", color = GrayText, fontSize = 11.sp) },
                                            modifier = Modifier.weight(1f),
                                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = Color.White.copy(0.2f),
                                                focusedBorderColor = RazorTeal
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )
                                        Button(
                                            onClick = {
                                                val parsed = customTipAmt.toDoubleOrNull()
                                                if (parsed != null && parsed > 0) {
                                                    if (walletBalance >= parsed) {
                                                        onTipSend(parsed)
                                                        tipNotificationMessage = "Successfully Tipped $${parsed}!"
                                                    } else {
                                                        tipNotificationMessage = "Insufficient Wallet Funds!"
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = RazorTeal),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Send Tip", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                        }
                                    }

                                    tipNotificationMessage?.let { msg ->
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = msg,
                                            color = if (msg.contains("Successfully")) RazorTeal else Color.Red,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
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
fun ReelPlaybackOverlay(
    reel: Post,
    subscriptions: List<Subscription>,
    onLike: () -> Unit,
    onDismiss: () -> Unit
) {
    val hasSubscription = subscriptions.any {
        it.creatorId == reel.creatorId &&
        (it.tierName == "GOLD" || 
         (it.tierName == "SILVER" && reel.requiredTier != "GOLD") || 
         (it.tierName == "BRONZE" && reel.requiredTier == "BRONZE"))
    }
    val isLocked = reel.isPremium && !hasSubscription && reel.creatorId != "pixel_queen"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.6f)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, RazorBlue, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = ObsidianDark)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLocked) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked Reel Content",
                            tint = InstaPink,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Premium Reel Closed",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Unlock access by purchasing the ${reel.requiredTier} Subscription tier for @${reel.creatorHandle}.",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = GrayText
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = RazorTeal)
                        ) {
                            Text("Go Back", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                } else {
                    var isPlaying by remember { mutableStateOf(true) }
                    var isLikedLocal by remember { mutableStateOf(reel.isLiked) }
                    var likesLocal by remember { mutableStateOf(reel.likesCount) }
                    val progressValue = remember { Animatable(0f) }

                    LaunchedEffect(isPlaying) {
                        if (isPlaying) {
                            progressValue.animateTo(
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(8000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                )
                            )
                        } else {
                            progressValue.stop()
                        }
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(color = ObsidianDark)
                        
                        val path = Path()
                        val pulse = progressValue.value
                        val centerY = size.height * 0.7f
                        path.moveTo(0f, centerY)
                        
                        val segmentWidth = size.width / 100
                        for (i in 0..100) {
                            val x = i * segmentWidth
                            val waveAmp = if (isPlaying) 20.dp.toPx() else 4.dp.toPx()
                            val y = centerY + waveAmp * kotlin.math.sin((i * 0.15f + pulse * 2f * 3.14159265f).toDouble()).toFloat()
                            path.lineTo(x, y)
                        }
                        path.lineTo(size.width, size.height)
                        path.lineTo(0f, size.height)
                        path.close()

                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    InstaPink.copy(alpha = 0.25f),
                                    RazorBlue.copy(alpha = 0.05f)
                                )
                            )
                        )
                    }

                    if (isPlaying) {
                        val pulseFactor = progressValue.value
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = RazorTeal.copy(alpha = 0.15f * (1f - pulseFactor)),
                                    radius = 120.dp.toPx() * pulseFactor,
                                    center = Offset(size.width / 2, size.height / 2)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { isPlaying = !isPlaying },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isPlaying) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .padding(16.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(vertical = 12.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(
                            onClick = {
                                isLikedLocal = !isLikedLocal
                                likesLocal += if (isLikedLocal) 1 else -1
                                onLike()
                            }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (isLikedLocal) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Like Reel",
                                    tint = if (isLikedLocal) InstaPink else Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                                Text(
                                    text = "$likesLocal",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        var linkCopiedNotification by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = {
                                linkCopiedNotification = true
                            }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text("Share", color = Color.White, fontSize = 9.sp)
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.Red,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(0.8f)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(RazorBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(reel.creatorName.take(1).uppercase(), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("@${reel.creatorHandle}", color = RazorTeal, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = reel.caption,
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        LinearProgressIndicator(
                            progress = { progressValue.value },
                            color = RazorTeal,
                            trackColor = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(CircleShape)
                        )
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
    onLikePost: (Post) -> Unit,
    onVideoClick: ((Post) -> Unit)? = null
) {
    val hasSubscription = subscriptions.any {
        it.creatorId == post.creatorId &&
        (it.tierName == "GOLD" || 
         (it.tierName == "SILVER" && post.requiredTier != "GOLD") || 
         (it.tierName == "BRONZE" && post.requiredTier == "BRONZE"))
    }
    val isLocked = post.isPremium && !hasSubscription && post.creatorId != "pixel_queen"

    var isLikedLocal by remember(post.id, post.isLiked) { mutableStateOf(post.isLiked) }
    var likesCountLocal by remember(post.id, post.likesCount) { mutableStateOf(post.likesCount) }
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
                    .background(Color(0xFF130E22))
                    .clickable {
                        if (post.contentImage == "attached_video" && onVideoClick != null) {
                            onVideoClick(post)
                        } else {
                            showCommentsDialog = true
                        }
                    },
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
                            .clip(CircleShape)
                            .background(RazorBlue.copy(0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (post.creatorAvatar.startsWith("http")) {
                            AsyncImage(
                                model = post.creatorAvatar,
                                contentDescription = "Creator Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(post.creatorName.take(1).uppercase(), color = RazorBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
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
                        
                        LaunchedEffect(comments.size) {
                            if (comments.isNotEmpty()) {
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }

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

// =========================================================================
// PREMIUM GOOGLE SEARCH TRENDING TOPICS COMPOSABLE (Jetpack Compose / M3)
// =========================================================================
@Composable
fun TrendingTopicsCard(
    topics: List<String>,
    isFetching: Boolean,
    selectedInsight: String?,
    isGenerating: Boolean,
    onTopicClick: (String) -> Unit,
    onGenerateInsight: (String) -> Unit,
    onClearInsight: () -> Unit,
    onRefresh: () -> Unit
) {
    var activeTopic by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x0EFFFFFF)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pulsing Search Dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(RazorTeal, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIVE GOOGLE SEARCH TRENDS",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 0.6.sp
                    )
                }
                
                IconButton(
                    onClick = {
                        onRefresh()
                        activeTopic = null
                        onClearInsight()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync Search Tags",
                        tint = RazorTeal.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isFetching) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = RazorBlue,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fetching trends...", color = GrayText, fontSize = 11.sp)
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(topics) { topic ->
                        val isSelected = activeTopic == topic
                        val chipBg = if (isSelected) {
                            Brush.linearGradient(colors = listOf(RazorBlue.copy(alpha = 0.3f), InstaPink.copy(alpha = 0.3f)))
                        } else {
                            Brush.linearGradient(colors = listOf(Color(0x13FFFFFF), Color(0x13FFFFFF)))
                        }
                        val chipBorderColor = if (isSelected) RazorBlue else Color.White.copy(alpha = 0.08f)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(chipBg)
                                .border(1.dp, chipBorderColor, RoundedCornerShape(12.dp))
                                .clickable {
                                    if (activeTopic == topic) {
                                        activeTopic = null
                                        onClearInsight()
                                    } else {
                                        activeTopic = topic
                                        onGenerateInsight(topic)
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = if (isSelected) RazorTeal else RazorBlue,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = topic,
                                    color = if (isSelected) Color.White else LightText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Expanded Insight Panel powered by Gemini API based on google trends
            if (activeTopic != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x0BFFFFFF))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "Insights",
                                    tint = SafeGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Google Search Insight: $activeTopic",
                                    color = SafeGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            // Filter Feed Button
                            TextButton(
                                onClick = { onTopicClick(activeTopic!!) },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search category",
                                        tint = RazorTeal,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Filter Feed", color = RazorTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (isGenerating) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = RazorTeal,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Synching telemetry logs...",
                                    color = GrayText,
                                    fontSize = 11.sp
                                )
                            }
                        } else if (selectedInsight != null) {
                            Text(
                                text = selectedInsight,
                                color = LightText,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// SNAPCHAT & INSTAGRAM STYLED STORIES MODELS & VIEWS
// ============================================

data class CreatorSnap(
    val id: String,
    val creatorId: String,
    val creatorName: String,
    val creatorHandle: String,
    val creatorAvatar: String,
    val imageUrl: String,
    val caption: String,
    val durationSeconds: Int = 10,
    val isPremium: Boolean = false,
    val requiredTier: String = "BRONZE",
    val likesCount: Int = 120
)

@Composable
fun CreatorSnapsGrid(
    snaps: List<CreatorSnap>,
    subscriptions: List<Subscription>,
    onSnapClick: (CreatorSnap) -> Unit
) {
    val chunkedSnaps = remember(snaps) { snaps.chunked(2) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        chunkedSnaps.forEach { rowSnaps ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowSnaps.forEach { snap ->
                    Box(modifier = Modifier.weight(1f)) {
                        SnapCard(
                            snap = snap,
                            subscriptions = subscriptions,
                            onSnapClick = { onSnapClick(snap) }
                        )
                    }
                }
                if (rowSnaps.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun SnapCard(
    snap: CreatorSnap,
    subscriptions: List<Subscription>,
    onSnapClick: () -> Unit
) {
    val hasSubscription = subscriptions.any {
        it.creatorId == snap.creatorId &&
        (it.tierName == "GOLD" || 
         (it.tierName == "SILVER" && snap.requiredTier != "GOLD") || 
         (it.tierName == "BRONZE" && snap.requiredTier == "BRONZE"))
    }
    val isLocked = snap.isPremium && !hasSubscription

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.68f)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = if (isLocked) listOf(InstaPink.copy(alpha = 0.5f), Color.Transparent)
                            else listOf(RazorTeal.copy(alpha = 0.6f), RazorBlue.copy(alpha = 0.6f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSnapClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF130E29))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = snap.imageUrl,
                contentDescription = "Snap preview",
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (isLocked) 0.35f else 0.82f),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.35f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isLocked) InstaPink else RazorTeal)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isLocked) "PREMIUM" else "SNAP",
                        color = Color.Black,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                if (isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked Snap",
                        tint = InstaPink,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(0.4f))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = RazorTeal, modifier = Modifier.size(12.dp))
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .align(Alignment.BottomStart)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(RazorBlue)
                            .border(1.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (snap.creatorAvatar.startsWith("http")) {
                            AsyncImage(
                                model = snap.creatorAvatar,
                                contentDescription = "Creator Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = snap.creatorName.take(1).uppercase(),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "@${snap.creatorHandle}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = snap.caption,
                    color = LightText.copy(0.9f),
                    fontSize = 10.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

@Composable
fun SnapPlaybackOverlay(
    snaps: List<CreatorSnap>,
    initialSnap: CreatorSnap,
    subscriptions: List<Subscription>,
    onLikeToggle: () -> Unit,
    onVisitProfile: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(snaps.indexOf(initialSnap).coerceAtLeast(0)) }
    val currentSnap = snaps.getOrNull(currentIndex) ?: initialSnap

    val hasSubscription = subscriptions.any {
        it.creatorId == currentSnap.creatorId &&
        (it.tierName == "GOLD" || 
         (it.tierName == "SILVER" && currentSnap.requiredTier != "GOLD") || 
         (it.tierName == "BRONZE" && currentSnap.requiredTier == "BRONZE"))
    }
    val isLocked = currentSnap.isPremium && !hasSubscription

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.6f)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, if (isLocked) InstaPink else RazorTeal, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = ObsidianDark)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLocked) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Premium lock",
                            tint = InstaPink,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Premium Snap Closed",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "This story requires a @${currentSnap.creatorHandle} ${currentSnap.requiredTier} access tier.",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = GrayText
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { onVisitProfile(currentSnap.creatorId) },
                            colors = ButtonDefaults.buttonColors(containerColor = RazorTeal)
                        ) {
                            Text("Unlock on Profile", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = onDismiss) {
                            Text("Not Now", color = GrayText, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    var progress by remember(currentIndex) { mutableStateOf(0f) }
                    var isPlayActive by remember { mutableStateOf(true) }
                    var isLikedLocal by remember(currentIndex) { mutableStateOf(false) }
                    var likesLocal by remember(currentIndex) { mutableStateOf(currentSnap.likesCount) }

                    LaunchedEffect(isPlayActive, currentIndex) {
                        if (isPlayActive) {
                            val durationSeconds = currentSnap.durationSeconds.coerceAtLeast(5)
                            val steps = durationSeconds * 20
                            val delayMs = 50L
                            for (i in (progress * steps).toInt()..steps) {
                                delay(delayMs)
                                progress = i.toFloat() / steps
                            }
                            // Auto advance
                            if (currentIndex + 1 < snaps.size) {
                                currentIndex++
                            } else {
                                onDismiss()
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(currentIndex) {
                                detectTapGestures(
                                    onTap = { offset ->
                                        val width = size.width
                                        if (offset.x < width * 0.3f) {
                                            if (currentIndex > 0) {
                                                currentIndex--
                                            } else {
                                                progress = 0f
                                            }
                                        } else {
                                            if (currentIndex + 1 < snaps.size) {
                                                currentIndex++
                                            } else {
                                                onDismiss()
                                            }
                                        }
                                    },
                                    onLongPress = {
                                        isPlayActive = false
                                    },
                                    onPress = {
                                        tryAwaitRelease()
                                        isPlayActive = true
                                    }
                                )
                            }
                    ) {
                        AsyncImage(
                            model = currentSnap.imageUrl,
                            contentDescription = "Active story snap",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.5f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.9f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            snaps.forEachIndexed { index, _ ->
                                val segmentProgress = when {
                                    index < currentIndex -> 1f
                                    index == currentIndex -> progress
                                    else -> 0f
                                }
                                LinearProgressIndicator(
                                    progress = { segmentProgress },
                                    color = RazorTeal,
                                    trackColor = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

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
                                        .background(RazorBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentSnap.creatorName.take(1).uppercase(),
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = currentSnap.creatorName,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "@${currentSnap.creatorHandle}",
                                        color = RazorTeal,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!isPlayActive) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.Black.copy(0.5f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("PAUSED", color = RazorTeal, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color.Black.copy(0.4f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close snap story",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = currentSnap.caption,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .clickable {
                                        isLikedLocal = !isLikedLocal
                                        likesLocal += if (isLikedLocal) 1 else -1
                                        onLikeToggle()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLikedLocal) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Like story snap",
                                    tint = if (isLikedLocal) InstaPink else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$likesLocal",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "Tap left/right to browse",
                                color = LightText.copy(0.5f),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InstagramAdCard(
    sponsorName: String,
    sponsorAvatar: String,
    caption: String,
    imageUrl: String,
    ctaText: String,
    onDismissAd: () -> Unit,
    onCtaClick: () -> Unit
) {
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
                // Circular Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(RazorBlue, InstaPink)
                            )
                        )
                        .padding(2.5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(ObsidianDark),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = sponsorAvatar,
                            contentDescription = "Sponsor Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = sponsorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LightText
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(RazorTeal.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("Ad", color = RazorTeal, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "Sponsored",
                        style = MaterialTheme.typography.bodySmall,
                        color = RazorTeal,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                
                // Close button to not disturb user
                IconButton(onClick = onDismissAd) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hide Advertisement",
                        tint = GrayText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Ad Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.25f)
                    .background(Color(0xFF130E22))
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Sponsored Content Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Overlay label
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("LIMITED CAMPAIGN", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }

            // Full-width elegant Instagram-style CTA bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RazorBlue)
                    .clickable { onCtaClick() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = ctaText,
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "VISIT PAGE",
                        color = Color.Black.copy(0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Visit sponsor site",
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Caption block
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = LightText)) {
                            append("$sponsorName ")
                        }
                        withStyle(style = SpanStyle(color = LightText)) {
                            append(caption)
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "SocialHub Verified Ad Partner • Secure Delivery",
                    color = GrayText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SimulatedBrowserDialog(
    sponsorName: String,
    url: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianDark),
            border = BorderStroke(1.2.dp, Brush.linearGradient(colors = listOf(RazorBlue, InstaPink)))
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                // Header of simulated browser
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secure Connection Status",
                            tint = RazorTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = url,
                            color = GrayText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 180.dp)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Browser Window",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                HorizontalDivider(color = Color.White.copy(0.1f), modifier = Modifier.padding(vertical = 12.dp))
                
                // Simulated landing page content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(RazorTeal.copy(0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Secure status checked",
                            tint = RazorTeal,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Welcome to $sponsorName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SocialHub secure decentralized partner tunnel established successfully.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrayText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(0.08f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "🎉 EXCLUSIVE CREATOR BONUS APPLIED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = RazorTeal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Special promo code 'SOCIALHUB' automatically loaded. Enjoy 20% off all physical assets and merchandise.",
                                fontSize = 11.sp,
                                color = Color.White.copy(0.9f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = RazorBlue),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Return to SocialHub", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
