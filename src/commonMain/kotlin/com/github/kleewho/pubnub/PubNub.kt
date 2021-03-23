package com.github.kleewho.pubnub

import com.benasher44.uuid.Uuid
import com.github.kleewho.pubnub.publish.PublishResult
import com.github.kleewho.pubnub.publish.publishCall
import com.github.kleewho.pubnub.subscribe.SubscriberManager
import com.github.kleewho.pubnub.subscribe.handleCommand
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.http.ContentType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.serialization.Serializable

class PubNub(private val configuration: Configuration) {
    private val httpClient = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                isLenient = false
                ignoreUnknownKeys = true
                allowSpecialFloatingPointValues = true
                useArrayPolymorphism = false
            })
            accept(ContentType.Application.Json, ContentType.Text.JavaScript)

        }

        if (configuration.logVerbosity == PNLogVerbosity.BODY) {
            install(Logging) {
                this.level = LogLevel.BODY
            }
        }
    }

    private val baseUrl = "https://ps.pndsn.com"

    private val subscriberManager = SubscriberManager(
        httpClient = httpClient,
        configuration = configuration,
        baseUrl = baseUrl
    ).also { it.subscriptionLoop() }

    fun publish(
        channel: PNChannel,
        message: Message
    ): ReceiveChannel<PublishResult> {
        val comChannel = Channel<PublishResult>()
        GlobalScope.launch { //todo change the scope here
            val result = publishCall(
                httpClient = httpClient,
                baseUrl = baseUrl,
                subKey = configuration.subscribeKey,
                pubKey = configuration.publishKey,
                channel = channel,
                body = MessageData(content = message.payload, platform = platform(), userId = platform()),
                options = mapOf()
            )

            comChannel.send(result)
            comChannel.close()

        }
        return comChannel
    }

    fun subscribe(channel: PNChannel): Subscription {
        return subscriberManager.subscribe(channel)
    }

    fun close() {
        httpClient.close()
    }
}

expect fun platform(): String

inline class SubscribeKey(val key: String)
inline class PublishKey(val key: String)

enum class PNLogVerbosity {
    NONE,

    BODY,
}


data class Configuration(
    val subscribeKey: SubscribeKey,
    val publishKey: PublishKey,
    val host: String = "ps.psdn.com",
    val logVerbosity: PNLogVerbosity = PNLogVerbosity.NONE
)

interface WithUnsubscribe {
    fun unsubscribe()
}

class Subscription(
    val id: Uuid,
    val channel: PNChannel,
    val data: Channel<PublishedMessage>,

    private val withUnsubscribe: WithUnsubscribe
) : WithUnsubscribe by withUnsubscribe {
    fun forEach(handler: (PublishedMessage) -> Unit) {
        GlobalScope.launch {
            while (true) {
                select<Unit> {
                    data.onReceive { d ->
                        handler(d)
                    }
                }
            }
        }
    }
}

inline class PNChannel(val name: String)

@Serializable
data class MessageData(
    val userId: String,
    val content: String,
    val platform: String,
)

data class Message(
    val payload: String
)

data class Payload(val content: String)

data class PublishedMessage(
    val payload: Payload,
    val timetoken: Long,
    val publisher: String,
    val platform: String?
)


