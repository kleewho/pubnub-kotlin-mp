package com.pubnub.hackaton.chat.network

import com.github.kleewho.pubnub.Configuration
import com.github.kleewho.pubnub.PubNub
import com.github.kleewho.pubnub.PublishKey
import com.github.kleewho.pubnub.SubscribeKey

// Should be moved to framework initialization
object PubNubFramework {

    private lateinit var pubNub: PubNub

//    val userId get() = pubNub.configuration.uuid

    val instance
        get() =
            if (PubNubFramework::pubNub.isInitialized) pubNub
            else throw PubNubNotInitializedException()

    /**
     * Initialize PubNub instance
     */
    fun initialize(
        publishKey: String,
        subscribeKey: String,
        userId: String? = null,
        cipherKey: String? = null,
    ) {
        val config = Configuration(
            subscribeKey = SubscribeKey(subscribeKey),
            publishKey = PublishKey(publishKey),
//            this.uuid = userId
//            this.logVerbosity = PNLogVerbosity.BODY
//            if (!cipherKey.isNullOrBlank())
//                this.cipherKey = cipherKey
        )
        pubNub = PubNub(config)
    }
}

class PubNubNotInitializedException :
    Exception("PubNub instance not initialized. Did you forget to call `PubNubFramework.initialize`?")
