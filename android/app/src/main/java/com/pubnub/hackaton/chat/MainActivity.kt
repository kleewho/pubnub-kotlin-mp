package com.pubnub.hackaton.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.pubnub.hackaton.chat.network.MessageService
import com.pubnub.hackaton.chat.network.PubNubFramework
import com.pubnub.hackaton.chat.ui.Header
import com.pubnub.hackaton.chat.ui.MessageInput
import com.pubnub.hackaton.chat.ui.MessageList
import com.pubnub.hackaton.chat.ui.Settings
import com.pubnub.hackaton.chat.ui.data.Message
import com.pubnub.hackaton.chat.ui.theme.ChatMultiplatformAndroidTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {


    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PubNubFramework.initialize(
            publishKey = "***REMOVED***",
            subscribeKey = "***REMOVED***"
        )

        setContent {
            ChatMultiplatformAndroidTheme {

                Surface(color = Color.LightGray) {

                    val messages = mutableStateListOf<Message>()
                    GlobalScope.launch(Dispatchers.IO) {
                        MessageService.subscribe(Settings.channel)
                            .collect { message ->
                                println("Received $message")
                                messages += message
                            }
                    }

                    Column {
                        Header("#${Settings.channel}")
                        MessageList(
                            messages = messages,
                            modifier = Modifier.weight(1f)
                        )
                        MessageInput(
                            onMessageSent = { message ->
                                println("Sending message $message")
                                MessageService.sendMessage(
                                    channel = Settings.channel,
                                    message = message,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}