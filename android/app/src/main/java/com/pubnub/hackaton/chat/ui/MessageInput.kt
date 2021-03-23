package com.pubnub.hackaton.chat.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


@OptIn(ExperimentalComposeApi::class)
@ExperimentalMaterialApi
@Composable
fun MessageInput(
    onMessageSent: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var textState by remember { mutableStateOf(TextFieldValue()) }

    // Used to decide if the keyboard should be shown
    var textFieldFocusState by remember { mutableStateOf(false) }


    val sendMessage = {
        if (textState.text.isNotEmpty()) onMessageSent(textState.text)
        // Reset text field and close keyboard
        textState = TextFieldValue()
        // Move scroll to bottom
        scope.launch {
            scrollState.animateScrollTo(0)
        }
    }

        Row(modifier
            .fillMaxWidth()
            .background(Color.White)) {
            ChatInputText(
                textFieldValue = textState,
                onTextChanged = {
                    textState = it
                },
                // Only show the keyboard if there's no input selector and text field has focus
                keyboardShown = textFieldFocusState,
                // Close extended selector if text field receives focus
                onTextFieldFocused = { focused ->
                    if (focused) {
                        scope.launch {
                            scrollState?.animateScrollTo(0)
                        }
                    }
                    textFieldFocusState = focused
                },
                focusState = textFieldFocusState,
                onImeAction = { action ->
                    if (action == ImeAction.Send) {
                        sendMessage()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(ChatInputPreferredHeight)
            )

            ChatInputSelector(
                sendMessageEnabled = textState.text.isNotBlank(),
                onMessageSent = sendMessage,
            )
    }
}

val KeyboardShownKey = SemanticsPropertyKey<Boolean>("KeyboardShownKey")
var SemanticsPropertyReceiver.keyboardShownProperty by KeyboardShownKey

val ChatInputPreferredHeight = 50.dp

@Composable
private fun ChatInputText(
    onTextChanged: (TextFieldValue) -> Unit,
    textFieldValue: TextFieldValue,
    keyboardShown: Boolean,
    onTextFieldFocused: (Boolean) -> Unit,
    focusState: Boolean,
    onImeAction: (ImeAction) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {

    Row(
        modifier = modifier
            .semantics {
                keyboardShownProperty = keyboardShown
            },
        horizontalArrangement = Arrangement.End
    ) {
        //Surface {
        Row {
            Box(
                modifier = Modifier
                    .height(ChatInputPreferredHeight)
                    .weight(1f)
                    .align(Alignment.Bottom)
            ) {
                var lastFocusState by remember { mutableStateOf(FocusState.Inactive) }
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { onTextChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                        .align(Alignment.CenterStart)
                        .onFocusChanged { state ->
                            if (lastFocusState != state) {
                                onTextFieldFocused(state == FocusState.Active)
                            }
                            lastFocusState = state
                        },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardAllActions { action -> onImeAction(action) },
                    singleLine = true,
                    cursorBrush = SolidColor(LocalContentColor.current),
                    textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current)
                )

                val disableContentColor =
                    MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)
                if (textFieldValue.text.isEmpty() && !focusState) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 16.dp),
                        text = "Type a message…",
                        style = MaterialTheme.typography.body1.copy(color = disableContentColor)
                    )
                }

            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun ChatInputSelector(
    sendMessageEnabled: Boolean,
    onMessageSent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .wrapContentHeight()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val border = if (!sendMessageEnabled) {
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
            )
        } else {
            null
        }
        val disabledContentColor =
            MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)

        val buttonColors =
            ButtonDefaults.buttonColors(
                disabledBackgroundColor = MaterialTheme.colors.surface,
                contentColor = Color(0xFF1fb7a9),
                disabledContentColor = disabledContentColor
            )

        // Send button
        IconButton(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(36.dp),
            enabled = sendMessageEnabled,
            onClick = onMessageSent,
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = null,
                tint = buttonColors.contentColor(sendMessageEnabled).value
            )
        }
    }
}


fun KeyboardAllActions(action: (ImeAction) -> Unit): KeyboardActions = KeyboardActions(
    onDone = { action(ImeAction.Done) },
    onGo = { action(ImeAction.Go) },
    onNext = { action(ImeAction.Next) },
    onPrevious = { action(ImeAction.Previous) },
    onSearch = { action(ImeAction.Search) },
    onSend = { action(ImeAction.Send) }
)
