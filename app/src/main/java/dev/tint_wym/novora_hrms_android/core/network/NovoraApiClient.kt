package dev.tint_wym.novora_hrms_android.core.network

import android.content.Context
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class ApiException(message: String, val status: Int? = null) : IOException(message)

/**
 * Cookie-session + CSRF client for the Novora backend.
 *
 * Default ([ApiConfig.baseUrl]): `https://novora-backend-wiem.onrender.com`.
 *
 * Local override:
 * `./gradlew :app:installDebug -Pnovora.apiBaseUrl=http://10.0.2.2:8081`
 */
class NovoraApiClient(context: Context) {
    private val prefs = context.getSharedPreferences("novora_api", Context.MODE_PRIVATE)
    private val cookieJar = PersistentCookieJar(prefs)
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()
    private val emptyBody = ByteArray(0).toRequestBody(null)

    private val client: OkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        // Render free tier cold-starts the JVM in 30–90s; shorter timeouts fail the first login.
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(100, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(110, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    val baseUrl: String = dev.tint_wym.novora_hrms_android.core.config.ApiConfig.baseUrl

    @Volatile
    private var csrfToken: String? = null

    @Volatile
    private var csrfHeaderName: String = "X-XSRF-TOKEN"

    fun clearSession() {
        cookieJar.clear()
        clearCsrfCache()
    }

    fun clearCsrfCache() {
        csrfToken = null
    }

    /** Lightweight reachability check (liveness skips the DB, so it also wakes a sleeping host). */
    fun pingHealth(): Boolean {
        return try {
            client.newCall(
                Request.Builder().url("$baseUrl/actuator/health/liveness").get().build(),
            ).execute().use { it.isSuccessful }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Ensures a CSRF token is available. Skips the network round-trip when a token
     * is already cached (or can be read from the XSRF-TOKEN cookie).
     */
    fun ensureCsrf() {
        if (!csrfToken.isNullOrBlank()) return
        seedCsrfFromCookie()
        if (!csrfToken.isNullOrBlank()) return

        val body = getJson("/api/auth/csrf")
        csrfHeaderName = JsonStrings.of(body, "headerName").ifBlank { "X-XSRF-TOKEN" }
        csrfToken = JsonStrings.ofOrNull(body, "token")
            ?: throw ApiException("CSRF token missing from server")
    }

    private fun seedCsrfFromCookie() {
        val raw = cookieJar.findCookieValue("XSRF-TOKEN") ?: return
        csrfToken = try {
            URLDecoder.decode(raw, StandardCharsets.UTF_8.name())
        } catch (_: Exception) {
            raw
        }
    }

    fun getJson(path: String): JSONObject = executeObject(getRequest(path))

    fun getJsonArray(path: String): JSONArray = executeArray(getRequest(path))

    fun postJson(path: String, payload: JSONObject, withCsrf: Boolean = true): JSONObject {
        return executeMutatingObject(path, "POST", payload.toString().toRequestBody(jsonMedia), withCsrf)
    }

    fun putJson(path: String, payload: JSONObject, withCsrf: Boolean = true): JSONObject {
        return executeMutatingObject(path, "PUT", payload.toString().toRequestBody(jsonMedia), withCsrf)
    }

    fun postEmpty(path: String, withCsrf: Boolean = true): JSONObject {
        return executeMutatingObject(path, "POST", emptyBody, withCsrf)
    }

    fun delete(path: String, withCsrf: Boolean = true): JSONObject {
        return executeMutatingObject(path, "DELETE", emptyBody, withCsrf)
    }

    private fun getRequest(path: String): Request =
        Request.Builder().url(url(path)).get().build()

    private fun mutatingRequest(
        path: String,
        method: String,
        body: okhttp3.RequestBody,
        withCsrf: Boolean,
    ): Request {
        if (withCsrf && csrfToken.isNullOrBlank()) {
            ensureCsrf()
        }
        val builder = Request.Builder().url(url(path)).method(method, body)
        if (method == "POST" || method == "PUT" || method == "PATCH") {
            builder.header("Content-Type", "application/json")
        }
        if (withCsrf) {
            val token = csrfToken
            if (!token.isNullOrBlank()) {
                builder.header(csrfHeaderName, token)
            }
        }
        return builder.build()
    }

    private fun executeMutatingObject(
        path: String,
        method: String,
        body: okhttp3.RequestBody,
        withCsrf: Boolean,
        csrfRetried: Boolean = false,
    ): JSONObject {
        return try {
            executeObject(mutatingRequest(path, method, body, withCsrf))
        } catch (e: ApiException) {
            if (e.status == 403 && withCsrf && !csrfRetried) {
                clearCsrfCache()
                ensureCsrf()
                executeMutatingObject(path, method, body, withCsrf, csrfRetried = true)
            } else {
                throw e
            }
        }
    }

    private fun url(path: String): String {
        val p = if (path.startsWith("/")) path else "/$path"
        return "$baseUrl$p"
    }

    private fun executeObject(request: Request): JSONObject {
        val raw = executeRaw(request)
        if (raw.isBlank()) return JSONObject()
        return try {
            JSONObject(raw)
        } catch (_: Exception) {
            throw ApiException("Expected JSON object but got: ${raw.take(120)}")
        }
    }

    private fun executeArray(request: Request): JSONArray {
        val raw = executeRaw(request)
        if (raw.isBlank()) return JSONArray()
        return try {
            JSONArray(raw)
        } catch (_: Exception) {
            throw ApiException("Expected JSON array but got: ${raw.take(120)}")
        }
    }

    private fun executeRaw(request: Request): String {
        val call = client.newCall(request)
        val response = try {
            call.execute()
        } catch (e: java.io.InterruptedIOException) {
            throw ApiException("Server is waking up or unreachable. Please try again in a minute.")
        } catch (e: java.net.UnknownHostException) {
            throw ApiException("No internet connection.")
        } catch (e: ApiException) {
            throw e
        } catch (e: IOException) {
            throw ApiException("Cannot reach server: ${e.message ?: "network error"}")
        }
        response.use {
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = try {
                    val obj = JSONObject(raw)
                    DisplayText.orEmpty(JsonStrings.of(obj, "message"))
                        .ifBlank { DisplayText.orEmpty(JsonStrings.of(obj, "error")) }
                        .ifBlank { DisplayText.orEmpty(raw) }
                        .ifBlank { "HTTP ${response.code}" }
                } catch (_: Exception) {
                    DisplayText.orEmpty(raw).ifBlank { "HTTP ${response.code}" }
                }
                throw ApiException(message, response.code)
            }
            return raw
        }
    }
}
