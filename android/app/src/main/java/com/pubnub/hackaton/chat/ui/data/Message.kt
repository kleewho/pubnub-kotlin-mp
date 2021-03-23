package com.pubnub.hackaton.chat.ui.data

import com.github.kleewho.pubnub.PublishedMessage
import com.pubnub.hackaton.chat.network.data.MessageData
import com.pubnub.hackaton.chat.ui.Settings

data class Message (
    val userId: String,
    val content: String,
    val timestamp: Long,
    val platform: String,
    val isOwn: Boolean,
)
//
//fun Message.toNetwork() = MessageData(
//    userId, content, timestamp, platform
//)
//
//fun MessageData.toUi() = Message(userId, content, timestamp, platform, userId == Settings.userId)

fun PublishedMessage.toUi() = Message(publisher, payload.content, timetoken/10_000L, platform?:"unknown", publisher == Settings.userId)