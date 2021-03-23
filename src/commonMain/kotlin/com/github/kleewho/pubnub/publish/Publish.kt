package com.github.kleewho.pubnub.publish

import com.github.kleewho.pubnub.PNChannel
import com.github.kleewho.pubnub.PublishKey
import com.github.kleewho.pubnub.SubscribeKey
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class PublishResult(val timetoken: Long)

internal suspend fun publishCall(
    httpClient: HttpClient,
    baseUrl: String,
    pubKey: PublishKey,
    subKey: SubscribeKey,
    channel: PNChannel,
    body: Any,
    options: Map<String, String>
): PublishResult {
    val result: JsonElement = httpClient.post() {
        url("$baseUrl/publish/${pubKey.key}/${subKey.key}/0/${channel.name}/0")
        this.body = body
        contentType(ContentType.Application.Json)
        accept(ContentType.Application.Json)
        options.entries.fold(this.url.parameters) { b, (k, v) -> b.append(k, v); b }
    }
    return when (result) {

        is JsonArray -> {
            PublishResult((result[2] as JsonPrimitive).content.toLong())
        }
        else -> TODO()
    }

}