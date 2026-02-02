@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package vip.mystery0.xhu.timetable.crypto.login.internal

import vip.mystery0.xhu.timetable.crypto.login.CryptoException
import kotlin.io.encoding.Base64

internal fun decodeBase64(field: String, value: String): ByteArray {
    val normalized = normalizeBase64(value)
    var lastError: Throwable? = null

    for (decoder in listOf(Base64.Default, Base64.UrlSafe)) {
        try {
            return decoder.decode(normalized)
        } catch (t: Throwable) {
            lastError = t
        }
    }

    throw CryptoException.Base64Decoding(
        field = field,
        cause = lastError ?: IllegalArgumentException("Base64 decoding failed"),
    )
}

internal fun encodeBase64(bytes: ByteArray): String = Base64.Default.encode(bytes)

private fun normalizeBase64(raw: String): String {
    val value = raw.trim().filterNot { it.isWhitespace() }
    val mod = value.length % 4
    if (mod == 0) return value
    return when (mod) {
        2 -> value + "=="
        3 -> value + "="
        else -> value
    }
}
