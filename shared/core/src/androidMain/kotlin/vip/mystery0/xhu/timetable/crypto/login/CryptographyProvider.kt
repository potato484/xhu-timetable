package vip.mystery0.xhu.timetable.crypto.login

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.providers.jdk.JDK

actual val cryptographyProvider: CryptographyProvider = CryptographyProvider.JDK
