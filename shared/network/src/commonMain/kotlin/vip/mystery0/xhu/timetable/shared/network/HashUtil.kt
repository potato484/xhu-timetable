package vip.mystery0.xhu.timetable.shared.network

import org.kotlincrypto.hash.md.MD5
import org.kotlincrypto.hash.sha2.SHA256

internal fun String.md5(): String = MD5().digest(encodeToByteArray()).toHexString()

internal fun String.sha256(): String = SHA256().digest(encodeToByteArray()).toHexString()

@OptIn(ExperimentalStdlibApi::class)
private fun ByteArray.toHexString(): String = toHexString(HexFormat.Default)
