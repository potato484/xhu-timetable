package vip.mystery0.xhu.timetable.shared.domain.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import vip.mystery0.xhu.timetable.crypto.login.CryptoException
import vip.mystery0.xhu.timetable.crypto.login.LoginEncryptor
import vip.mystery0.xhu.timetable.model.AccountContext
import vip.mystery0.xhu.timetable.platform.ioDispatcher
import vip.mystery0.xhu.timetable.shared.database.XhuTimetableDatabase
import vip.mystery0.xhu.timetable.shared.domain.crypto.TokenCipher
import vip.mystery0.xhu.timetable.shared.domain.model.User
import vip.mystery0.xhu.timetable.shared.domain.model.toDomainUser
import vip.mystery0.xhu.timetable.shared.network.ApiException
import vip.mystery0.xhu.timetable.shared.network.UserApi
import vip.mystery0.xhu.timetable.shared.network.model.Gender

class UserRepositoryImpl(
    private val userApi: UserApi,
    private val loginEncryptor: LoginEncryptor,
    private val database: XhuTimetableDatabase,
    private val tokenCipher: TokenCipher,
) : UserRepository {
    private val stateMutex = Mutex()

    private val _currentAccountContext = MutableStateFlow<AccountContext?>(null)
    override val currentAccountContext: StateFlow<AccountContext?> = _currentAccountContext.asStateFlow()
    override val accountContextProvider: suspend () -> AccountContext? = { currentAccountContext.value }

    @Volatile
    private var accountJob: Job = SupervisorJob()

    override fun accountCoroutineContext(): CoroutineContext = accountJob + ioDispatcher

    override suspend fun login(username: String, password: String): Result<AccountContext> = runCatching {
        stateMutex.withLock {
            val previousContext = _currentAccountContext.value
            val previousJob = accountJob

            val token = try {
                userApi.login(
                    username = username,
                    password = password,
                    loginEncryptor = loginEncryptor,
                )
            } catch (_: CryptoException) {
                userApi.login(
                    username = username,
                    password = password,
                    loginEncryptor = loginEncryptor,
                )
            }

            var accountContext = AccountContext(
                studentId = username,
                token = token,
            )

            // Switch active account first so subsequent requests can attach sessionToken.
            accountJob = SupervisorJob()
            _currentAccountContext.value = accountContext

            val userInfoAttempt = runCatching {
                withContext(accountCoroutineContext()) { userApi.getUserInfo() }
            }
            val userInfo = userInfoAttempt.getOrNull()
            if (userInfo == null) {
                val e = userInfoAttempt.exceptionOrNull()
                // If server says the daily limit is reached, treat login as failed.
                // Otherwise, allow cache-first login and let the app operate with limited user info.
                if (e is ApiException && e.apiCode == 251) {
                    // Roll back the active context to avoid an immediate "session expired" loop.
                    val newJob = accountJob
                    accountJob = previousJob
                    _currentAccountContext.value = previousContext
                    newJob.cancel()
                    throw e
                }
            }

            val user = if (userInfo == null) {
                User(
                    studentId = username,
                    name = "",
                    gender = Gender.UNKNOWN,
                    xhuGrade = 0,
                    college = "",
                    majorName = "",
                    className = "",
                    majorDirection = "",
                )
            } else {
                val resolved = userInfo.toDomainUser()
                if (resolved.studentId != accountContext.studentId) {
                    accountContext = AccountContext(studentId = resolved.studentId, token = token)
                    _currentAccountContext.value = accountContext
                }
                resolved
            }

            withContext(ioDispatcher) {
                database.schemaQueries.upsertUser(
                    studentId = user.studentId,
                    tokenEncrypted = tokenCipher.encrypt(token),
                    name = user.name,
                    gender = user.gender.name,
                    xhuGrade = user.xhuGrade.toLong(),
                    college = user.college,
                    majorName = user.majorName,
                    className = user.className,
                    majorDirection = user.majorDirection,
                )
            }

            previousJob.cancel()
            accountContext
        }
    }

    override suspend fun logout(studentId: String) {
        stateMutex.withLock {
            withContext(ioDispatcher) {
                database.schemaQueries.deleteUser(studentId)
            }

            if (_currentAccountContext.value?.studentId == studentId) {
                val oldJob = accountJob
                accountJob = SupervisorJob()
                val nextUser = withContext(ioDispatcher) {
                    database.schemaQueries.selectAllUsers().executeAsList().firstOrNull()
                }
                _currentAccountContext.value = nextUser?.let { user ->
                    AccountContext(
                        studentId = user.studentId,
                        token = tokenCipher.decrypt(user.tokenEncrypted),
                    )
                }
                oldJob.cancel()
            }
        }
    }

    override fun getCurrentAccount(): AccountContext? = currentAccountContext.value

    override fun getAllAccounts(): Flow<List<User>> = database.schemaQueries
        .selectAllUsers()
        .asFlow()
        .mapToList(ioDispatcher)
        .map { list -> list.map { it.toDomainUser() } }

    override suspend fun switchAccount(studentId: String): Result<AccountContext> = runCatching {
        stateMutex.withLock {
            val dbUser = withContext(ioDispatcher) {
                database.schemaQueries.selectUser(studentId).executeAsOneOrNull()
            } ?: throw IllegalArgumentException("Account not found: $studentId")

            val token = tokenCipher.decrypt(dbUser.tokenEncrypted)
            val accountContext = AccountContext(
                studentId = dbUser.studentId,
                token = token,
            )

            val oldJob = accountJob
            accountJob = SupervisorJob()
            _currentAccountContext.value = accountContext
            oldJob.cancel()

            accountContext
        }
    }

    override suspend fun refreshUserInfo(): Result<User> = runCatching {
        val ctx = currentAccountContext.value ?: error("No active account")

        withContext(accountCoroutineContext()) {
            val userInfo = userApi.reloadUserInfo()
            val user = userInfo.toDomainUser()

            withContext(ioDispatcher) {
                database.schemaQueries.upsertUser(
                    studentId = user.studentId,
                    tokenEncrypted = tokenCipher.encrypt(ctx.token),
                    name = user.name,
                    gender = user.gender.name,
                    xhuGrade = user.xhuGrade.toLong(),
                    college = user.college,
                    majorName = user.majorName,
                    className = user.className,
                    majorDirection = user.majorDirection,
                )
            }

            user
        }
    }

    override suspend fun isLoggedIn(): Boolean = stateMutex.withLock {
        if (_currentAccountContext.value != null) return@withLock true

        // Auto-restore the latest local session token so users don't need to re-enter credentials
        // after relaunching the app. If the token is expired, the first request will trigger
        // the debounced 401 handler and navigate back to Login.
        val dbUser = withContext(ioDispatcher) {
            database.schemaQueries.selectAllUsers().executeAsList().firstOrNull()
        } ?: return@withLock false

        val token = tokenCipher.decrypt(dbUser.tokenEncrypted)
        val oldJob = accountJob
        accountJob = SupervisorJob()
        _currentAccountContext.value = AccountContext(
            studentId = dbUser.studentId,
            token = token,
        )
        oldJob.cancel()
        true
    }

    override suspend fun reloadUserInfo(studentId: String): Result<User> = runCatching {
        val current = currentAccountContext.value
        if (current?.studentId == studentId) {
            return@runCatching refreshUserInfo().getOrThrow()
        }

        val targetUser = withContext(ioDispatcher) {
            database.schemaQueries.selectUser(studentId).executeAsOneOrNull()
        } ?: throw IllegalArgumentException("Account not found: $studentId")

        val token = tokenCipher.decrypt(targetUser.tokenEncrypted)
        val userInfo = withContext(ioDispatcher) {
            userApi.reloadUserInfo(sessionToken = token)
        }
        val user = userInfo.toDomainUser()

        withContext(ioDispatcher) {
            database.schemaQueries.upsertUser(
                studentId = user.studentId,
                tokenEncrypted = tokenCipher.encrypt(token),
                name = user.name,
                gender = user.gender.name,
                xhuGrade = user.xhuGrade.toLong(),
                college = user.college,
                majorName = user.majorName,
                className = user.className,
                majorDirection = user.majorDirection,
            )
        }

        user
    }
}
