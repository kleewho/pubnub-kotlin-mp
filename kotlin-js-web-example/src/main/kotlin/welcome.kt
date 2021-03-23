import com.github.kleewho.pubnub.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.input

private const val CHANNEL = "testChannel"

external interface KotlinJSDemoProps : RProps {
    var message: String
}

data class KotlinJSDemoState(val messageToSend: String, val messagesReceived: List<PublishedMessage> = listOf()) : RState

@JsExport
class Welcome(props: KotlinJSDemoProps) : RComponent<KotlinJSDemoProps, KotlinJSDemoState>(props) {
    private val subscribeKey = SubscribeKey("***REMOVED***")
    private val publishKey = PublishKey("***REMOVED***")
    private val configuration = Configuration(subscribeKey, publishKey)
    private val pn = PubNub(configuration)

    init {
        state = KotlinJSDemoState(props.message)
        GlobalScope.launch {
            val receivingChannel = pn.subscribe(channel = PNChannel(CHANNEL))

            async {
                while (true) {
                    select<Any> {
                        receivingChannel.data.onReceive {
                            val newMessagesReceived = state.messagesReceived + it
                            setState(
                                KotlinJSDemoState(
                                    messageToSend = state.messageToSend,
                                    messagesReceived = newMessagesReceived
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    override fun RBuilder.render() {
        input {
            attrs {
                type = InputType.text
                value = state.messageToSend
                onChangeFunction = { event ->
                    setState(
                        KotlinJSDemoState(
                            messageToSend = (event.target as HTMLInputElement).value,
                            messagesReceived = state.messagesReceived
                        )
                    )
                }
            }
        }
        input {
            attrs {
                type = InputType.button
                value = "Send"
                onClickFunction = {
                    GlobalScope.launch {
                        pn.publish(channel = PNChannel(CHANNEL), message = Message(state.messageToSend)).receive()
                        setState(
                            KotlinJSDemoState(
                                messageToSend = "",
                                messagesReceived = state.messagesReceived
                            )
                        )
                    }
                }
            }
        }
        div {
            attrs {
                attributes["width"] = "1000px"
            }
            state.messagesReceived.forEach {
                if ("js" == it.platform) {
                    div {
                        attrs {
                            attributes["align"] = "left"
                        }
                        +it.payload.content
                    }
                }
                else {
                    div {
                        attrs {
                            attributes["align"] = "right"
                        }
                        +it.payload.content
                    }
                }
            }
        }
    }
}
