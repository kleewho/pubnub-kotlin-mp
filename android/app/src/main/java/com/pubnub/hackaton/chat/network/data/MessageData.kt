package com.pubnub.hackaton.chat.network.data

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class MessageData(
    val userId: String,
    val content: String,
    val timestamp: Long,
    val platform: String,
)