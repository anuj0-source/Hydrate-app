package com.example.hydrate.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_messages")
data class CustomMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val icon: String = "💕",
    val isFavorite: Boolean = false
)
