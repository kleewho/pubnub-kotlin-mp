package com.github.kleewho.base64

actual object Base64Factory {
    actual fun createEncoder(): Base64Encoder {
        return JsBase64Encoder
    }
}

object JsBase64Encoder : Base64Encoder {
    override fun encode(src: ByteArray): ByteArray {
        val buffer = js("Buffer").from(src)
        val string = buffer.toString("base64") as String
        return string.encodeToByteArray()
    }

}