package vip.mystery0.xhu.timetable.shared.network

import io.ktor.client.plugins.api.SendingRequest
import io.ktor.client.plugins.api.createClientPlugin
import vip.mystery0.xhu.timetable.model.AccountContext

class SessionInterceptorConfig {
    var headerName: String = "sessionToken"
    var accountContextProvider: suspend () -> AccountContext? = { null }
}

val SessionInterceptor = createClientPlugin<SessionInterceptorConfig>("SessionInterceptor", ::SessionInterceptorConfig) {
    val headerName = pluginConfig.headerName
    val provider = pluginConfig.accountContextProvider
    // Attach the session token as late as possible (Send pipeline) so other plugins
    // (e.g. request signing) can reliably read it from headers.
    on(SendingRequest) { request, _ ->
        // If caller explicitly set a session token for this request, respect it.
        val explicitToken = request.headers[headerName]
        if (!explicitToken.isNullOrBlank()) {
            return@on
        }

        request.headers.remove(headerName)
        val ctx = provider()
        if (ctx != null && ctx.token.isNotBlank()) {
            request.headers.append(headerName, ctx.token)
        }
    }
}
