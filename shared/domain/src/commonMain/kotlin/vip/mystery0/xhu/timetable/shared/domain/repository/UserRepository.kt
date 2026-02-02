package vip.mystery0.xhu.timetable.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext
import vip.mystery0.xhu.timetable.model.AccountContext
import vip.mystery0.xhu.timetable.shared.domain.model.User

interface UserRepository {
    /**
     * Current active account context.
     *
     * NOTE: This is intended to be used by SessionInterceptor to auto-attach sessionToken.
     */
    val currentAccountContext: StateFlow<AccountContext?>

    /**
     * Provider for Ktor's [vip.mystery0.xhu.timetable.shared.network.SessionInterceptor].
     */
    val accountContextProvider: suspend () -> AccountContext?

    /**
     * A coroutine context that is tied to the current active account.
     *
     * Cancelled and replaced on account switch to cancel in-flight requests.
     */
    fun accountCoroutineContext(): CoroutineContext

    /**
     * Login flow:
     * - The `/publicKey/native` nonce is single-use; retry must re-run the full flow.
     * - Implementations may do a single retry on crypto failures by re-fetching `/publicKey/native`.
     * - Callers can always re-run [login] to retry.
     */
    suspend fun login(username: String, password: String): Result<AccountContext>

    suspend fun logout(studentId: String)

    fun getCurrentAccount(): AccountContext?

    fun getAllAccounts(): Flow<List<User>>

    suspend fun switchAccount(studentId: String): Result<AccountContext>

    suspend fun refreshUserInfo(): Result<User>

    /**
     * Check whether the user has a valid local login state.
     *
     * Implementations may restore the active account from local storage during this call.
     */
    suspend fun isLoggedIn(): Boolean

    /**
     * Force refresh user info cache for the given account.
     *
     * Used by account management long-press behavior.
     */
    suspend fun reloadUserInfo(studentId: String): Result<User>
}
