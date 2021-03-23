package com.pubnub.hackaton.chat.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ChatBubbleShape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 0.dp)

@Composable
fun ChatMessage(
    userId: String,
    content: String,
    date: String,
    platform: String,
    isOwn: Boolean,
) {
    val backgroundBubbleColor = if (isOwn) Color(0x8aa2e1db)
    else Color.White

    Surface(color = backgroundBubbleColor, shape = ChatBubbleShape) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
            Column(Modifier.padding(8.dp)) {
                Title(text = userId, isOwn = isOwn)
                Spacer(modifier = Modifier.width(5.dp))
                ChatText(content)
                Date(text = date)
            }
        }
    }
}


@Composable
private fun Title(text: String, isOwn: Boolean, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        color = if (isOwn) Color(0xFFc78502) else Color(0xDE02a7fa),
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        modifier = modifier
    )
}

@Composable
fun Date(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.54f),
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        modifier = modifier
    )
}

@Composable
fun ChatText(
    message: String,
) {
    Text(
        text = message,
        style = TextStyle(
            color = MaterialTheme.colors.onSurface,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
        )
    )
}