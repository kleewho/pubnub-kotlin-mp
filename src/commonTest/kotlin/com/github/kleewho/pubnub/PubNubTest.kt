package com.github.kleewho.pubnub

import com.github.kleewho.pubnub.publish.PublishResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertTrue


expect fun runTest(block: suspend () -> Unit)

class PubNubTest {
    @Test
    fun publish() = runTest {
        val pubNub = PubNub(
            Configuration(
                subscribeKey = SubscribeKey("***REMOVED***"),
                publishKey = PublishKey("***REMOVED***")
            )
        )
        val chan = pubNub.publish(channel = PNChannel("test"), message = Message("test"))

        val publishResult = withContext(Dispatchers.Default) {
            chan.receive()
        }
        println(publishResult)
        assertTrue(publishResult.timetoken > 0)
    }

    @Test
    fun subscribe() = runTest {
        val pubNub = PubNub(
            Configuration(
                subscribeKey = SubscribeKey("***REMOVED***"),
                publishKey = PublishKey("***REMOVED***")
            )
        )
        val subscription = pubNub.subscribe(channel = PNChannel("test"))

        delay(1000)
        val publishresChannel = pubNub.publish(channel = PNChannel("test"), message = Message("test"))

        val publishResult = withContext(Dispatchers.Default) {
            publishresChannel.receive()
        }
        println(publishResult)

        select<Any> {
            subscription.data.onReceive {
                println(it)
            }
        }
        subscription.unsubscribe()
        delay(100)
    }
}