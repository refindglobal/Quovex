package com.quovex.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val examRelevance: String = "",
    val masteryLevel: Int = 1,
    val totalMaterials: Int = 0
)
