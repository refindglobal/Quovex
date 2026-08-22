package com.quovex.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("deckId"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["deckId"]),
        Index(value = ["nextReviewDate"])
    ]
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val deckId: Int,
    val frontContent: String,
    val backContent: String,
    
    // SuperMemo-2 Algorithm specific fields
    val easeFactor: Float = 2.5f,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,
    val nextReviewDate: Long = System.currentTimeMillis(),

    // Extended fields for Learning Transformation System
    val tags: String? = null,
    val formulaLatex: String? = null,
    val isRemedial: Boolean = false,
    val difficulty: Int = 3
)
