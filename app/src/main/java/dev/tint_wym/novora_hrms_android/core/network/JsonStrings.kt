package dev.tint_wym.novora_hrms_android.core.network

import org.json.JSONObject

/** Safe JSON string helpers — `JSONObject.optString` returns the literal `"null"` for JSON null. */
object JsonStrings {
    fun clean(value: Any?): String {
        if (value == null || value == JSONObject.NULL) return ""
        val text = value.toString().trim()
        return if (text.isEmpty() || text.equals("null", ignoreCase = true)) "" else text
    }

    fun of(json: JSONObject, key: String): String = clean(json.opt(key))

    fun ofOrNull(json: JSONObject, key: String): String? = of(json, key).ifBlank { null }
}

/** UI display helpers for empty / null-looking API values. */
object DisplayText {
    fun orDash(value: String?): String {
        val t = clean(value)
        return t.ifBlank { "—" }
    }

    fun orEmpty(value: String?): String = clean(value)

    fun orFallback(value: String?, fallback: String): String {
        val t = clean(value)
        return t.ifBlank { fallback }
    }

    fun clean(value: String?): String {
        val t = value?.trim().orEmpty()
        return if (t.isEmpty() || t.equals("null", ignoreCase = true) || t.equals("undefined", ignoreCase = true)) {
            ""
        } else {
            t
        }
    }
}
