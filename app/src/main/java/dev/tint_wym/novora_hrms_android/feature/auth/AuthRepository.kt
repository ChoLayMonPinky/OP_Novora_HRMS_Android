package dev.tint_wym.novora_hrms_android.feature.auth

import android.content.Context
import dev.tint_wym.novora_hrms_android.core.model.SessionUser
import dev.tint_wym.novora_hrms_android.core.network.ApiException
import dev.tint_wym.novora_hrms_android.core.network.JsonStrings
import dev.tint_wym.novora_hrms_android.core.network.NovoraApiClient
import org.json.JSONArray
import org.json.JSONObject

class AuthRepository private constructor(context: Context) {
    private val appContext = context.applicationContext
    val api = NovoraApiClient(appContext)
    private val prefs = appContext.getSharedPreferences("novora_session", Context.MODE_PRIVATE)

    fun cachedUser(): SessionUser? {
        val id = prefs.getString("userId", null) ?: return null
        val email = prefs.getString("email", null) ?: return null
        val roles = prefs.getStringSet("roles", emptySet())?.toList().orEmpty()
        return SessionUser(
            id = id,
            email = email,
            roles = roles,
            organizationName = prefs.getString("orgName", null),
            fullName = prefs.getString("fullName", null),
        )
    }

    fun login(email: String, password: String): SessionUser {
        api.ensureCsrf()
        val body = JSONObject()
            .put("email", email.trim())
            .put("password", password)
        api.postJson("/api/auth/login", body, withCsrf = true)
        // Session rotation invalidates the pre-login CSRF token; next mutation refetches.
        api.clearCsrfCache()
        val user = fetchMe()
            ?: throw ApiException(
                "Login succeeded but session was not confirmed. " +
                    "If using HTTP locally, run Spring with profile `local` (Secure cookies off).",
                401,
            )
        saveUser(user)
        return user
    }

    fun register(
        email: String,
        password: String,
        companyName: String,
        fullName: String?,
    ): SessionUser {
        api.ensureCsrf()
        val body = JSONObject()
            .put("email", email.trim())
            .put("password", password)
            .put("companyName", companyName.trim())
        if (!fullName.isNullOrBlank()) {
            body.put("fullName", fullName.trim())
        }
        api.postJson("/api/auth/register", body, withCsrf = true)
        api.clearCsrfCache()
        val user = fetchMe()
            ?: throw ApiException(
                "Account created but session was not confirmed. " +
                    "If using HTTP locally, run Spring with profile `local` (Secure cookies off).",
                401,
            )
        saveUser(user)
        return user
    }

    fun restoreSession(): SessionUser? {
        return try {
            val user = fetchMe()
            if (user != null) {
                saveUser(user)
                user
            } else {
                clear()
                null
            }
        } catch (e: ApiException) {
            if (e.status == 401 || e.status == 403) {
                clear()
                null
            } else {
                cachedUser()
            }
        } catch (_: Exception) {
            cachedUser()
        }
    }

    fun logout() {
        try {
            api.postEmpty("/api/auth/logout", withCsrf = true)
        } catch (_: Exception) {
            // Always clear local session even if the network call fails
        } finally {
            clear()
        }
    }

    private fun fetchMe(): SessionUser? {
        return try {
            parseAuthBody(api.getJson("/api/me"))
        } catch (e: ApiException) {
            if (e.status == 401 || e.status == 403) null else throw e
        }
    }

    private fun parseAuthBody(json: JSONObject): SessionUser? {
        val id = JsonStrings.of(json, "userId").ifBlank { return null }
        val email = JsonStrings.of(json, "email").ifBlank { return null }
        val roles = mutableListOf<String>()
        val arr: JSONArray? = json.optJSONArray("roles")
        if (arr != null) {
            for (i in 0 until arr.length()) {
                val role = JsonStrings.clean(arr.opt(i))
                if (role.isNotEmpty()) roles.add(role.uppercase())
            }
        }
        val org = json.optJSONObject("organization")
        val fullName = JsonStrings.ofOrNull(json, "fullName")
        return SessionUser(
            id = id,
            email = email,
            roles = roles,
            organizationName = org?.let { JsonStrings.ofOrNull(it, "name") },
            fullName = fullName,
        )
    }

    private fun saveUser(user: SessionUser) {
        prefs.edit()
            .putString("userId", user.id)
            .putString("email", user.email)
            .putStringSet("roles", user.roles.toSet())
            .putString("orgName", user.organizationName)
            .putString("fullName", user.fullName)
            .apply()
    }

    private fun clear() {
        api.clearSession()
        prefs.edit().clear().apply()
    }

    companion object {
        @Volatile
        private var instance: AuthRepository? = null

        fun get(context: Context): AuthRepository {
            return instance ?: synchronized(this) {
                instance ?: AuthRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
