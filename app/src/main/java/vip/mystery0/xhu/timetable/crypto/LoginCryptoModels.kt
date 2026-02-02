package vip.mystery0.xhu.timetable.crypto

data class LoginCryptoEnvelope(
    val passwordEncryptedBase64: String,
    val clientPublicKeyDerBase64: String,
)

class NonceAlreadyUsedException(message: String) : IllegalStateException(message)

