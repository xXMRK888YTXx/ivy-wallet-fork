package com.ivy.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parsed_notifications")
data class ParsedNotificationEntity(
    @PrimaryKey
    val id: String,
    val packageName: String,
    val title: String?,
    val text: String?,
    val amount: Double,
    val currency: String?,
    val timestamp: Long,
    val isUsed: Boolean = false,
    val alternativeAmounts: String? = null
)
