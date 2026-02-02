package vip.mystery0.xhu.timetable.crypto.login

interface EcdhKeyExchange {
    suspend fun generateKeyPair(): EcdhKeyPair
}

interface EcdhKeyPair {
    val publicKeyDer: ByteArray
    suspend fun sharedSecret(peerPublicKeyDer: ByteArray): ByteArray
}
