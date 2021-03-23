package com.pubnub.hackaton.chat.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
internal fun Header(title: String) {
    Column(
        verticalArrangement = Arrangement.Bottom,
        modifier = HeaderSize.then(HeaderPadding),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.h5,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
internal fun SubHeader(title: String) {
    Text(
        text = title.toUpperCase(),
        style = MaterialTheme.typography.subtitle1,
        modifier = Modifier.padding(0.dp, 8.dp, 0.dp, 16.dp),
    )
}

val HeaderSize = Modifier.fillMaxWidth().height(48.dp)

private val HeaderPadding =
    Modifier.padding(start = 0.dp, top = 16.dp, end = 0.dp, bottom = 20.dp)
