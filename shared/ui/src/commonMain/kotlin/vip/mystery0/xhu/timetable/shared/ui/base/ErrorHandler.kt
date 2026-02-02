package vip.mystery0.xhu.timetable.shared.ui.base

import kotlinx.coroutines.CancellationException

object ErrorHandler {
    fun getDisplayMessage(throwable: Throwable): String {
        if (throwable is CancellationException) throw throwable

        val message = throwable.message ?: ""
        return when {
            message.contains("Unexpected response body", ignoreCase = true) -> "服务器返回数据格式异常，请稍后重试或更新应用"

            message.contains("UnknownHost", ignoreCase = true) ||
            message.contains("Unable to resolve host", ignoreCase = true) -> "网络连接失败，请检查网络设置"

            message.contains("timeout", ignoreCase = true) ||
            message.contains("timed out", ignoreCase = true) -> "网络请求超时，请稍后重试"

            message.contains("Connection refused", ignoreCase = true) ||
            message.contains("Connect", ignoreCase = true) -> "无法连接到服务器"

            message.contains("SSL", ignoreCase = true) ||
            message.contains("certificate", ignoreCase = true) -> "安全连接失败，请检查网络环境"

            message.contains("401") -> "登录已过期，请重新登录"
            message.contains("403") -> "没有访问权限"
            message.contains("404") -> "请求的资源不存在"
            message.contains("429") ||
            message.contains("Too Many Requests", ignoreCase = true) ||
            message.contains("请求过于频繁") ||
            message.contains("超出限制") -> "请求过于频繁，请稍后再试"
            message.contains("500") -> "服务器内部错误"

            message.isNotBlank() -> message
            else -> "未知错误"
        }
    }

    fun isNetworkError(throwable: Throwable): Boolean {
        val message = throwable.message ?: ""
        return message.contains("UnknownHost", ignoreCase = true) ||
            message.contains("Unable to resolve host", ignoreCase = true) ||
            message.contains("timeout", ignoreCase = true) ||
            message.contains("Connection refused", ignoreCase = true) ||
            message.contains("SSL", ignoreCase = true)
    }
}
