package com.pubnub.hackaton.chat.network

import com.github.kleewho.pubnub.PNChannel
import com.pubnub.hackaton.chat.network.PubNubFramework

class ChannelService {
    fun subscribe(channel: String) {
        PubNubFramework.instance.subscribe(PNChannel(channel))
    }
}