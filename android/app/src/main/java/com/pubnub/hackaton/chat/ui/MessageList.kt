package com.pubnub.hackaton.chat.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pubnub.hackaton.chat.ui.data.Message
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz

@Composable
fun MessageList(
    messages: List<Message>,
    modifier: Modifier = Modifier
){
    val lazyListState =
        rememberLazyListState(initialFirstVisibleItemIndex = messages.size)

//    val dateFormat = DateFormat("HH:mm:ss")
    val dateFormat: DateFormat = DateFormat("EEE, dd MMM yyyy HH:mm:ss z")
    LazyColumn(
        state = lazyListState,
//        reverseLayout = true,
        modifier = modifier,
    ){
        items(messages) { message ->
            val date = DateTimeTz.fromUnixLocal(message.timestamp).format(dateFormat)

            Spacer(modifier = Modifier.height(10.dp))

            ChatMessage(
                userId = message.userId,
                content = message.content,
                date = date,
                platform = message.platform,
                isOwn = message.isOwn,
            )
        }
    }
}