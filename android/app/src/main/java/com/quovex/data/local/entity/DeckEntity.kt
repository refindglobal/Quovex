package com.quovex.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val subject: String,
    val createdAt: Long = System.currentTimeMillis(),
    val totalCards: Int = 0,
    val xpValue: Int = 100 // Default XP reward for completing the deck
)
