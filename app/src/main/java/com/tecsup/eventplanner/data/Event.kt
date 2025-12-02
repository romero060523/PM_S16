package com.tecsup.eventplanner.data

import com.google.firebase.firestore.DocumentId

data class Event(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val description: String = "",
    val userId: String = ""
)

