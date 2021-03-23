package com.pubnub.hackaton.chat.network

import com.github.kleewho.pubnub.Message
import com.github.kleewho.pubnub.PNChannel
import com.pubnub.hackaton.chat.network.data.MessageData
import com.pubnub.hackaton.chat.network.utils.fromJson
import com.pubnub.hackaton.chat.network.utils.toJson
import com.pubnub.hackaton.chat.ui.data.toUi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

object MessageService {
    fun sendMessage(channel: String, message: String){
        println("Send message: $message")
        GlobalScope.launch {
            PubNubFramework.instance.publish(
                channel = PNChannel(channel),
                message = Message(message),
            )
        }
    }

    fun subscribe(channel: String) =
        PubNubFramework.instance.subscribe(PNChannel(channel)).data.consumeAsFlow().map {
            //it.payload.content.fromJson<MessageData>().toUi()
            it.toUi()
        }

}

