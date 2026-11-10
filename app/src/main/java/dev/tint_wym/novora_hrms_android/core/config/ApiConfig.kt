package dev.tint_wym.novora_hrms_android.core.config

import dev.tint_wym.novora_hrms_android.BuildConfig

object ApiConfig {
    /** Render production by default; override at build with -Pnovora.apiBaseUrl=... */
    val baseUrl: String
        get() = BuildConfig.API_BASE_URL.trimEnd('/')

    val isStandalone: Boolean
        get() = BuildConfig.STANDALONE
}
