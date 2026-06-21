package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "creators")
data class Creator(
    @PrimaryKey val id: String,
    val name: String,
    val handle: String,
    val avatarUrl: String,
    val description: String,
    val followersCount: Int,
    val bronzeTierPrice: Double,
    val silverTierPrice: Double,
    val goldTierPrice: Double,
    val isVerified: Boolean = true,
    val currency: String = "USD",
    val isFollowed: Boolean = false
)
