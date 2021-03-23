package com.github.kleewho.pubnub.subscribe

import com.benasher44.uuid.uuid4
import com.github.kleewho.pubnub.Configuration
import com.github.kleewho.pubnub.Message
import com.github.kleewho.pubnub.MessageData
import com.github.kleewho.pubnub.PNChannel
import com.github.kleewho.pubnub.Payload
import com.github.kleewho.pubnub.PublishedMessage
import com.github.kleewho.pubnub.SubscribeKey
import com.github.kleewho.pubnub.Subscription
import com.github.kleewho.pubnub.WithUnsubscribe
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.coroutines.coroutineContext


@Serializable
internal data class SubscribeEnvelope(
    @SerialName("m")
    internal val messages: List<SubscribeMessage>,
    @SerialName("t")
    internal val metadata: Metadata
)

@Serializable
internal data class Metadata(
    @SerialName("t")
    internal val timetoken: String,
    @SerialName("r")
    internal val region: Long
)

@Serializable
internal data class SubscribeMessage(
    @SerialName("a")
    internal val shard: String?,

    @SerialName("c")
    internal val channel: String?,

    @SerialName("d")
    internal val payload: MessageData?,

    @SerialName("i")
    internal val issuingClientId: String? = null,

    @SerialName("k")
    internal val subscribeKey: String?,

    @SerialName("p")
    internal val publishMetaData: Metadata?,

    @SerialName("e")
    internal val type: Int? = null,

    @SerialName("u")
    internal val userMetadata: JsonElement? = null,

    ) {
    companion object {
        internal const val TYPE_MESSAGE = 0
        internal const val TYPE_SIGNAL = 1
        internal const val TYPE_OBJECT = 2
        internal const val TYPE_MESSAGE_ACTION = 3
        internal const val TYPE_FILES = 4
    }

    fun supportsEncryption() = type in arrayOf(null, TYPE_MESSAGE, TYPE_FILES)
}

internal suspend fun subscribeCall(
    httpClient: HttpClient,
    baseUrl: String,
    subKey: SubscribeKey,
    channels: Collection<PNChannel>,
    timetoken: Long,
    options: Map<String, String>
): SubscribeEnvelope {
    return httpClient.get {
        url("$baseUrl/v2/subscribe/${subKey.key}/${channels.joinToString(",") { it.name }}/0")
        options.entries.fold(this.url.parameters) { b, (k, v) -> b.append(k, v); b }
        url.parameters.append("tt", timetoken.toString())
    }
}

sealed class Command

data class SubscribeCommand(
    val subscription: Subscription
) : Command()

data class UnsubscribeCommand(
    val channel: PNChannel
) : Command()

data class UpdateTimetokenCommand(val timetoken: Long) : Command()

class WithUnsubscribeByCommand(
    private val commandChannel: Channel<Command>,
    private val pnChannel: PNChannel
) : WithUnsubscribe {
    override fun unsubscribe() {
        GlobalScope.launch {
            commandChannel.send(UnsubscribeCommand(pnChannel))
        }
    }
}

internal class StateManager(initialState: State) {
    var state: State = initialState
        private set

    fun handleCommand(command: Command) {
        state = state.handleCommand(command)
    }

}

fun State.handleCommand(command: Command): State {
    return when (command) {
        is SubscribeCommand -> {
            println("subscribe $command")
            copy(
                channels = channels + (command.subscription.channel to command.subscription),
                currentTimetoken = 0
            )
        }
        is UnsubscribeCommand -> {
            println("unsubscribe $command")
             channels[command.channel]?.data?.close()
            copy(channels = channels - listOf(command.channel), currentTimetoken = 0)
        }
        is UpdateTimetokenCommand -> {
            println("token update $command")
            copy(currentTimetoken = command.timetoken)
        }
    }

}

data class State(
    val channels: Map<PNChannel, Subscription>,
    val currentTimetoken: Long
)

internal class SubscriberManager(
    private val httpClient: HttpClient,
    private val configuration: Configuration,
    private val baseUrl: String
) {

    private val commandChannel: Channel<Command> = Channel(capacity = Channel.UNLIMITED)

    private suspend fun makeMockCall(stateManager: StateManager) = CoroutineScope(coroutineContext).launch {
        stateManager.let { println(it.state) }
        delay(5000)
        CoroutineScope(coroutineContext).launch {
            commandChannel.send(UpdateTimetokenCommand(0))
        }
    }

    private suspend fun makeCall(state: State) = CoroutineScope(coroutineContext).launch {
        val (channels, timetoken) = state.channels to state.currentTimetoken

        if (channels.isNotEmpty()) {
            val result = subscribeCall(
                baseUrl = baseUrl,
                subKey = configuration.subscribeKey,
                channels = channels.keys,
                timetoken = timetoken,
                options = mapOf(),
                httpClient = httpClient
            )

            CoroutineScope(coroutineContext).launch {
                commandChannel.send(UpdateTimetokenCommand(result.metadata.timetoken.toLong()))
            }

            result.messages.forEach { subscribeMessage ->
                subscribeMessage.channel?.let {
                    val content = subscribeMessage.payload?.content ?: "no content?"

                    val platform = when (val metadata = subscribeMessage.userMetadata) {
                        is JsonPrimitive -> metadata.content
                        else -> null
                    }
                    channels[PNChannel(it)]?.data?.send(
                        PublishedMessage(
                            payload = Payload(content = content),
                            timetoken = subscribeMessage.publishMetaData?.timetoken?.toLong() ?: 0,
                            publisher = subscribeMessage.payload?.userId ?: "N/A",
                            platform = subscribeMessage.payload?.platform
                        )
                    )
                }
            }
        }
    }

    internal fun subscriptionLoop() {
        GlobalScope.launch {
            var state = State(
                channels = emptyMap(),
                currentTimetoken = 0
            )
            while (true) {
                val currentCall = makeCall(state)
                select<Unit> {
                    commandChannel.onReceive { command ->
                        state = state.handleCommand(command)
                        //subscription has changed. Cancel the call and start over with changed state
                        currentCall.cancel()
                    }
                }
            }
        }
    }

    fun subscribe(pnChannel: PNChannel): Subscription {
        val dataChannel: Channel<PublishedMessage> =
            Channel(capacity = Channel.BUFFERED, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        val id = uuid4()
        val withUnsubscribe = WithUnsubscribeByCommand(
            commandChannel = commandChannel,
            pnChannel = pnChannel
        )
        val subscription = Subscription(
            id = id,
            channel = pnChannel,
            data = dataChannel,
            withUnsubscribe = withUnsubscribe
        )

        GlobalScope.launch {
            commandChannel.send(SubscribeCommand(subscription))
        }

        return subscription

    }
}
