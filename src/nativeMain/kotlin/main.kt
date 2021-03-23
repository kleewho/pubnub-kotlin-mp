import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.findOrSetObject
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.kleewho.pubnub.Configuration
import com.github.kleewho.pubnub.Message
import com.github.kleewho.pubnub.PNChannel
import com.github.kleewho.pubnub.PNLogVerbosity
import com.github.kleewho.pubnub.PubNub
import com.github.kleewho.pubnub.PublishKey
import com.github.kleewho.pubnub.SubscribeKey
import kotlinx.coroutines.runBlocking


fun pubnub(verbose: Boolean): PubNub {
    val config = Configuration(
        subscribeKey = SubscribeKey("***REMOVED***"),
        publishKey = PublishKey("***REMOVED***"),
        logVerbosity = if (verbose) PNLogVerbosity.BODY else PNLogVerbosity.NONE
    )
    return PubNub(config)
}

class ModeCommand : CliktCommand() {
    private val verbose by option("-v").flag()
    private val config by findOrSetObject { mutableMapOf<String, String>() }
    override fun run() {
        runBlocking {
            val subcommand = currentContext.invokedSubcommand
            if (subcommand == null) {
                echo(getFormattedHelp())
                return@runBlocking
            } else {
                config["VERBOSE"] = verbose.toString()
                return@runBlocking
            }
        }
    }
}

class Subscribe : CliktCommand() {
    private val config by requireObject<Map<String, String>>()
    override fun run() {
        runBlocking {
            val sub = pubnub(config["VERBOSE"].toBoolean()).subscribe(PNChannel("testChannel"))
            while (true) {
                val received = sub.data.receive()
                echo("${received.timetoken}: ${received.payload.content}\n")
            }
        }
    }
}

class Publish : CliktCommand() {
    private val message by argument()
    private val config by requireObject<Map<String, String>>()
    override fun run() {
        val pubnub = pubnub(config["VERBOSE"].toBoolean())
        runBlocking {

            val result = pubnub.publish(
                channel = PNChannel("testChannel"),
                message = Message(message)
            )
            println(result.receive())
        }
        pubnub.close()
    }
}

fun main(args: Array<String>) = ModeCommand().subcommands(Subscribe(), Publish()).main(args)
