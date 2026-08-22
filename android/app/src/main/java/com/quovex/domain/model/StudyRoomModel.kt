package com.quovex.domain.model

data class StudyRoomModel(
    val id: String = "",
    val name: String = "",
    val subject: String = "Physics",
    val activeMembers: Int = 1,
    val targetExam: String = "JEE 2027",
    val avatars: List<Int> = listOf(1, 2, 3),
    val createdBy: String = "system"
)
