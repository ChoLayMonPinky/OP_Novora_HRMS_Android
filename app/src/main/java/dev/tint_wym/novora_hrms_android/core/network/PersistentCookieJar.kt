package dev.tint_wym.novora_hrms_android.core.network

import android.content.SharedPreferences
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import org.json.JSONArray
import org.json.JSONObject

/** Cookie jar that keeps JSESSIONID / XSRF-TOKEN across process restarts. */
class PersistentCookieJar(
    private val prefs: SharedPreferences,
) : CookieJar {
    private val lock = Any()
    private val store = mutableMapOf<String, MutableList<Cookie>>()

    init {
        load()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        synchronized(lock) {
            val host = url.host
            val existing = store.getOrPut(host) { mutableListOf() }
            var changed = false
            for (cookie in cookies) {
                val before = existing.firstOrNull { it.name == cookie.name && it.path == cookie.path }
                existing.removeAll { it.name == cookie.name && it.path == cookie.path }
                if (!cookie.persistent || cookie.expiresAt > System.currentTimeMillis()) {
                    existing.add(cookie)
                    if (before == null || before.value != cookie.value || before.expiresAt != cookie.expiresAt) {
                        changed = true
                    }
                } else if (before != null) {
                    changed = true
                }
            }
            if (changed) persist()
        }
    }

    fun findCookieValue(name: String): String? {
        synchronized(lock) {
            val now = System.currentTimeMillis()
            for (list in store.values) {
                val match = list.firstOrNull { it.name == name && it.expiresAt >= now }
                if (match != null) return match.value
            }
            return null
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        synchronized(lock) {
            val now = System.currentTimeMillis()
            val hostCookies = store[url.host] ?: return emptyList()
            hostCookies.removeAll { it.expiresAt < now }
            return hostCookies.filter { it.matches(url) }
        }
    }

    fun clear() {
        synchronized(lock) {
            store.clear()
            prefs.edit().remove(KEY).apply()
        }
    }

    private fun persist() {
        val root = JSONObject()
        for ((host, cookies) in store) {
            val arr = JSONArray()
            for (c in cookies) {
                arr.put(
                    JSONObject()
                        .put("name", c.name)
                        .put("value", c.value)
                        .put("domain", c.domain)
                        .put("path", c.path)
                        .put("expiresAt", c.expiresAt)
                        .put("secure", c.secure)
                        .put("httpOnly", c.httpOnly)
                        .put("hostOnly", c.hostOnly),
                )
            }
            root.put(host, arr)
        }
        prefs.edit().putString(KEY, root.toString()).apply()
    }

    private fun load() {
        val raw = prefs.getString(KEY, null) ?: return
        try {
            val root = JSONObject(raw)
            val keys = root.keys()
            while (keys.hasNext()) {
                val host = keys.next()
                val arr = root.getJSONArray(host)
                val list = mutableListOf<Cookie>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val builder = Cookie.Builder()
                        .name(o.getString("name"))
                        .value(o.getString("value"))
                        .path(o.optString("path", "/"))
                        .expiresAt(o.optLong("expiresAt", Long.MAX_VALUE))
                    val domain = o.getString("domain")
                    if (o.optBoolean("hostOnly", false)) {
                        builder.hostOnlyDomain(domain)
                    } else {
                        builder.domain(domain)
                    }
                    if (o.optBoolean("secure", false)) builder.secure()
                    if (o.optBoolean("httpOnly", false)) builder.httpOnly()
                    list.add(builder.build())
                }
                store[host] = list
            }
        } catch (_: Exception) {
            prefs.edit().remove(KEY).apply()
        }
    }

    companion object {
        private const val KEY = "novora_cookies"
    }
}
