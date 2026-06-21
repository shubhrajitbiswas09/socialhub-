package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.UUID

sealed class Screen {
    object Feed : Screen()
    object Creators : Screen()
    data class CreatorDetail(val creatorId: String) : Screen()
    object Wallet : Screen()
    object LiveEvents : Screen()
    data class Chat(val initialRecipient: String? = null) : Screen()
}

class SocialHubViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = SocialHubRepository(db.dao(), application)

    // Streams from Database
    val creators = repository.creators
    val posts = repository.posts
    val subscriptions = repository.subscriptions
    val transactions = repository.transactions
    val chatMessages = repository.chatMessages
    val events = repository.events

    // Expose only posts from followed creators, sorted by recency
    val followedPosts: Flow<List<Post>> = combine(repository.posts, repository.creators) { postsList, creatorsList ->
        val followedCreatorIds = creatorsList.filter { it.isFollowed }.map { it.id }.toSet()
        postsList.filter { it.creatorId in followedCreatorIds }
            .sortedByDescending { it.timestamp }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Local UI State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Feed)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _isDataFetching = MutableStateFlow(false)
    val isDataFetching: StateFlow<Boolean> = _isDataFetching.asStateFlow()

    private val _walletBalance = MutableStateFlow(500.00)
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    private val _userProfileName = MutableStateFlow("Alex Morgan")
    val userProfileName: StateFlow<String> = _userProfileName.asStateFlow()

    private val _userProfileHandle = MutableStateFlow("alex_morgan")
    val userProfileHandle: StateFlow<String> = _userProfileHandle.asStateFlow()

    fun updateProfile(name: String, handle: String) {
        _userProfileName.value = name
        _userProfileHandle.value = handle
        showNotification("Profile Updated", "Welcome back, $name!")
    }

    // Encrypted Chat Visual Toggle
    private val _chatEncryptionEnabled = MutableStateFlow(true)
    val chatEncryptionEnabled: StateFlow<Boolean> = _chatEncryptionEnabled.asStateFlow()

    // Temporary storage for active operations (e.g. active payment request or checkout)
    private val _activeCheckoutInfo = MutableStateFlow<CheckoutInfo?>(null)
    val activeCheckoutInfo: StateFlow<CheckoutInfo?> = _activeCheckoutInfo.asStateFlow()

    private val _activeNotification = MutableStateFlow<NotificationMessage?>(null)
    val activeNotification: StateFlow<NotificationMessage?> = _activeNotification.asStateFlow()

    fun triggerFirestoreSync() {
        viewModelScope.launch {
            try {
                val creatorsList = creators.first()
                val followedCreatorIds = creatorsList.filter { it.isFollowed }.map { it.id }.toSet()
                repository.fetchFollowedPostsFromFirestore(followedCreatorIds)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    init {
        viewModelScope.launch {
            _isDataFetching.value = true
            // Seed base data if empty
            creators.first().let { list ->
                if (list.isEmpty()) {
                    repository.seedInitialData()
                }
            }
            try {
                triggerFirestoreSync()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            delay(1200) // Aesthetic startup skeleton delay
            _isDataFetching.value = false
        }
    }

    fun navigateTo(screen: Screen) {
        val prevScreen = _currentScreen.value
        _currentScreen.value = screen
        // Trigger simulated premium network fetching whenever entering a content-heavy view
        if (screen != prevScreen && (screen is Screen.Feed || screen is Screen.Creators || screen is Screen.LiveEvents || screen is Screen.CreatorDetail)) {
            viewModelScope.launch {
                _isDataFetching.value = true
                delay(850) // Microsecond delay for pristine skeleton preview
                _isDataFetching.value = false
            }
        }
    }

    fun triggerRefresh() {
        viewModelScope.launch {
            _isDataFetching.value = true
            try {
                val creatorsList = creators.first()
                val followedCreatorIds = creatorsList.filter { it.isFollowed }.map { it.id }.toSet()
                repository.fetchFollowedPostsFromFirestore(followedCreatorIds)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            delay(1000) // Re-fetch from secure API node
            _isDataFetching.value = false
            showNotification("Refreshed", "Content cache successfully synchronized! 🌐")
        }
    }

    fun toggleChatEncryption() {
        _chatEncryptionEnabled.value = !_chatEncryptionEnabled.value
    }

    // Add Balance (Simulate Razorpay Top-up)
    fun addWalletFunds(amount: Double) {
        viewModelScope.launch {
            _walletBalance.value += amount
            val tx = Transaction(
                type = "WALLET_FUND",
                description = "Loaded funds via Razorpay Secure Gateway",
                amount = amount,
                currency = "USD",
                recipientHandle = "user_wallet",
                status = "SUCCESS",
                paymentId = "pay_fund_${UUID.randomUUID().toString().take(10)}"
            )
            repository.insertTransaction(tx)
            showNotification("Success", "Deposited $${String.format("%.2f", amount)} successfully via Razorpay!")
        }
    }

    // Like a post
    fun likePost(post: Post) {
        viewModelScope.launch {
            repository.updatePost(post.copy(likesCount = post.likesCount + 1))
        }
    }

    // Toggle follow/unfollow status for a creator
    fun toggleFollow(creator: Creator) {
        viewModelScope.launch {
            val updatedCreator = creator.copy(
                isFollowed = !creator.isFollowed,
                followersCount = if (creator.isFollowed) {
                    (creator.followersCount - 1).coerceAtLeast(0)
                } else {
                    creator.followersCount + 1
                }
            )
            repository.updateCreator(updatedCreator)
            val actionWord = if (updatedCreator.isFollowed) "followed" else "unfollowed"
            showNotification("Success", "You have successfully $actionWord @${creator.handle}!")
            try {
                triggerFirestoreSync()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Send a tip to a creator post (Razorpay interactive popover)
    fun triggerPostTip(post: Post, amount: Double) {
        val checkout = CheckoutInfo(
            title = "Tip Creator @${post.creatorHandle}",
            description = "Tipping secure token for: ${post.caption.take(20)}...",
            amount = amount,
            creatorId = post.creatorId,
            creatorHandle = post.creatorHandle,
            action = {
                executePostTip(post, amount)
            }
        )
        _activeCheckoutInfo.value = checkout
    }

    private fun executePostTip(post: Post, amount: Double) {
        if (_walletBalance.value < amount) {
            showNotification("Insufficient Funds", "Please load funds using Razorpay in your wallet first!")
            return
        }
        viewModelScope.launch {
            _walletBalance.value -= amount
            // Update post tips
            repository.updatePost(post.copy(tipsTotal = post.tipsTotal + amount))
            // Record payment transaction
            val tx = Transaction(
                type = "TIP",
                description = "Tip for post ID #${post.id}",
                amount = amount,
                currency = "USD",
                recipientHandle = post.creatorHandle,
                status = "SUCCESS",
                paymentId = "pay_tip_${UUID.randomUUID().toString().take(10)}"
            )
            repository.insertTransaction(tx)
            showNotification("Tip Sent", "Processed $${String.format("%.2f", amount)} securely with Razorpay!")
        }
    }

    // Buy Creator Subscription (Silver, Bronze, Gold with instant feed locks release!)
    fun triggerSubscriptionBuy(creator: Creator, tier: String, price: Double) {
        val checkout = CheckoutInfo(
            title = "Subscribe: ${tier.uppercase()} Tier",
            description = "Unlock premium, high-tier post lockouts for @${creator.handle}",
            amount = price,
            creatorId = creator.id,
            creatorHandle = creator.handle,
            action = {
                executeSubscriptionBuy(creator, tier, price)
            }
        )
        _activeCheckoutInfo.value = checkout
    }

    private fun executeSubscriptionBuy(creator: Creator, tier: String, price: Double) {
        if (_walletBalance.value < price) {
            showNotification("Payment Failed", "Wallet balance insufficient. Please deposit funds.")
            return
        }
        viewModelScope.launch {
            _walletBalance.value -= price
            // Save subscription to Room database
            repository.insertSubscription(
                Subscription(
                    creatorId = creator.id,
                    creatorName = creator.name,
                    creatorHandle = creator.handle,
                    tierName = tier.uppercase(),
                    amount = price
                )
            )
            // Add Transaction history
            val tx = Transaction(
                type = "SUBSCRIPTION",
                description = "Subscribed to @${creator.handle} [${tier.uppercase()}]",
                amount = price,
                currency = creator.currency,
                recipientHandle = creator.handle,
                status = "SUCCESS",
                paymentId = "pay_sub_${UUID.randomUUID().toString().take(10)}"
            )
            repository.insertTransaction(tx)
            showNotification("Subscription Active!", "Welcome to @${creator.handle}'s ${tier.uppercase()} tier!")
        }
    }

    // Cancel dynamic subscription
    fun cancelSubscription(creatorId: String, handle: String) {
        viewModelScope.launch {
            repository.removeSubscription(creatorId)
            showNotification("Subscription Ended", "Cancelled tier for @$handle successfully.")
        }
    }

    // Buy Live Ticketing Event
    fun triggerTicketBuy(event: Event) {
        if (event.ticketsBought >= event.originalAvailable) {
            showNotification("Event Sold Out", "All physical and livestream credentials have been allocated.")
            return
        }
        val checkout = CheckoutInfo(
            title = "Buy Event Ticket",
            description = "Entry ticket: ${event.title}",
            amount = event.ticketPrice,
            creatorId = event.creatorId,
            creatorHandle = event.creatorHandle,
            action = {
                executeTicketBuy(event)
            }
        )
        _activeCheckoutInfo.value = checkout
    }

    private fun executeTicketBuy(event: Event) {
        if (_walletBalance.value < event.ticketPrice) {
            showNotification("Insufficient Wallet", "Deposit funds via Razorpay first.")
            return
        }
        viewModelScope.launch {
            _walletBalance.value -= event.ticketPrice
            // Update sold ticket count in Room Database
            repository.buyTicket(event.id, event)
            // Insert premium transaction slip
            val tx = Transaction(
                type = "TICKET_BUY",
                description = "Bought Ticket: ${event.title}",
                amount = event.ticketPrice,
                currency = event.currency,
                recipientHandle = event.creatorHandle,
                status = "SUCCESS",
                paymentId = "pay_tkt_${UUID.randomUUID().toString().take(10)}"
            )
            repository.insertTransaction(tx)
            showNotification("Ticket Purchased 🎟️", "Your QR ticket code is now securely loaded!")
        }
    }

    // Send encrypted/plain chat message
    fun sendChatMessage(receiverHandle: String, rawContent: String) {
        if (rawContent.isBlank()) return
        viewModelScope.launch {
            // Apply encrypted conversion if requested
            val finalContent = if (_chatEncryptionEnabled.value) {
                // simple base64 cipher representing standard client-side chat encryption
                android.util.Base64.encodeToString(rawContent.toByteArray(), android.util.Base64.DEFAULT).trim()
            } else {
                rawContent
            }
            val msg = ChatMessage(
                senderName = "You",
                receiverName = receiverHandle,
                encryptedContent = finalContent,
                isEncrypted = _chatEncryptionEnabled.value,
                timestamp = System.currentTimeMillis()
            )
            repository.sendChatMessage(msg)
        }
    }

    // Pay requested invoice in encrypted chat
    fun payChatInvoice(chat: ChatMessage) {
        if (chat.amountRequested <= 0) return
        if (_walletBalance.value < chat.amountRequested) {
            showNotification("Payment Declined", "Insufficient funds. Please fund your wallet.")
            return
        }
        viewModelScope.launch {
            _walletBalance.value -= chat.amountRequested
            // Create Transaction record
            val tx = Transaction(
                type = "CHAT_INVOICE",
                description = "Paid Invoice Request in Chat to @${chat.senderName}",
                amount = chat.amountRequested,
                currency = "USD",
                recipientHandle = chat.senderName,
                status = "SUCCESS",
                paymentId = "pay_inv_${UUID.randomUUID().toString().take(10)}"
            )
            repository.insertTransaction(tx)
            // Update chat status in Database
            repository.updateChatMessage(chat.copy(paymentStatus = "PAID"))
            showNotification("Invoice Paid ✅", "Funds routed directly to ${chat.senderName}!")
        }
    }

    // Decline dynamic chat invoice
    fun declineChatInvoice(chat: ChatMessage) {
        viewModelScope.launch {
            repository.updateChatMessage(chat.copy(paymentStatus = "DECLINED"))
            showNotification("Invoice Declined", "Invoice request of $${String.format("%.2f", chat.amountRequested)} was rejected.")
        }
    }

    // Publish dynamic post from Creative Studio or New Post custom dialog
    fun publishPost(caption: String, creator: Creator? = null, attachedMediaType: String? = null) {
        if (caption.isBlank()) return
        viewModelScope.launch {
            val cId = creator?.id ?: "pixel_queen"
            val cName = creator?.name ?: _userProfileName.value
            val cHandle = creator?.handle ?: _userProfileHandle.value
            val post = Post(
                creatorId = cId,
                creatorName = cName,
                creatorHandle = cHandle,
                creatorAvatar = if (creator != null) creator.id else "",
                caption = caption,
                contentImage = attachedMediaType ?: listOf("vector_creative", "gradient_neon", "cyberpunk_city", "neon_synthwave").random(),
                isPremium = false,
                likesCount = 0,
                tipsTotal = 0.0,
                timestamp = System.currentTimeMillis()
            )
            repository.insertPost(post)
            showNotification("Post Published 🎉", "New post by @$cHandle has been uploaded successfully!")
            try {
                triggerFirestoreSync()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun dismissCheckout() {
        _activeCheckoutInfo.value = null
    }

    private fun showNotification(title: String, message: String) {
        _activeNotification.value = NotificationMessage(title, message)
    }

    fun clearNotification() {
        _activeNotification.value = null
    }
}

data class CheckoutInfo(
    val title: String,
    val description: String,
    val amount: Double,
    val creatorId: String,
    val creatorHandle: String,
    val action: () -> Unit
)

data class NotificationMessage(
    val title: String,
    val message: String
)
