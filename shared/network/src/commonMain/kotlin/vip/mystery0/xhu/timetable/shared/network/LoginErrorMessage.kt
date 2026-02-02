package vip.mystery0.xhu.timetable.shared.network

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import vip.mystery0.xhu.timetable.crypto.login.CryptoException

fun Throwable.toLoginErrorMessage(): String = when (this) {
    is CryptoException -> "加密失败，请重试"
    is ApiException -> when {
        // API-level limits / auth states surfaced via body `{code,message}` with HTTP 4xx.
        // Example: HTTP 400 + {code:251,message:"今日请求次数已达上限"}
        // Example: HTTP 401 + {code:405,message:"用户会话过期，请重新登录"}
        apiCode == 251 -> "今日请求次数已达上限，请明天再试（251）"
        apiCode == 405 -> "用户会话过期，请重新登录（405）"
        code == HttpStatusCode.Unauthorized.value || code == HttpStatusCode.Forbidden.value -> {
            val msg = message?.takeIf { it.isNotBlank() }
            if (msg == null) "账号或密码错误（$code）" else "$msg（$code）"
        }
        else -> {
            val msg = message?.takeIf { it.isNotBlank() }
            if (msg != null && msg.isLikelyCryptoFailure()) "加密失败，请重试" else msg ?: "登录失败"
        }
    }
    is ClientRequestException -> when (response.status) {
        HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> "账号或密码错误（HTTP ${response.status.value}）"
        HttpStatusCode.NotFound -> "服务地址错误或接口已变更"
        else -> "请求失败：${response.status.value} ${response.status.description}"
    }
    is ServerResponseException -> "服务器异常：${response.status.value} ${response.status.description}"
    is IOException -> "网络异常：请检查网络或稍后重试"
    is SerializationException -> message?.takeIf { it.isNotBlank() } ?: "服务器响应异常，请稍后重试或更新应用"
    else -> message?.takeIf { it.isNotBlank() } ?: "登录失败"
}

private fun String.isLikelyCryptoFailure(): Boolean {
    val lower = lowercase()
    return contains("解密") ||
        contains("加密") ||
        contains("公钥") ||
        lower.contains("decrypt") ||
        lower.contains("encrypt") ||
        lower.contains("nonce") ||
        lower.contains("publickey") ||
        lower.contains("public key")
}
