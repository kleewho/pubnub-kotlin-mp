package com.pubnub.hackaton.chat.network.utils

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

inline fun <reified T> T?.toJson() =
    Json.encodeToString( this)

inline fun <reified T> String.fromJson(): T =
    Json.decodeFromString(this)