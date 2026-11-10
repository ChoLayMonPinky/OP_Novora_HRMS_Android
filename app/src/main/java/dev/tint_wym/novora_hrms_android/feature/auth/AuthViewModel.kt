package dev.tint_wym.novora_hrms_android.feature.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.tint_wym.novora_hrms_android.core.model.SessionUser
import dev.tint_wym.novora_hrms_android.core.network.DisplayText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AuthUiState(
    val bootstrapping: Boolean = true,
    val user: SessionUser? = null,
    val loginLoading: Boolean = false,
    val loginError: String? = null,
    val registerLoading: Boolean = false,
    val registerError: String? = null,
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = AuthRepository.get(application)

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        // Wake the free-tier backend while the user is still typing credentials.
        viewModelScope.launch(Dispatchers.IO) { repo.api.pingHealth() }

        // Instant resume: show MainShell from cache while /api/me refreshes in background.
        val cached = repo.cachedUser()
        if (cached != null) {
            _state.update {
                it.copy(
                    user = cached,
                    bootstrapping = false,
                )
            }
        }

        viewModelScope.launch {
            val restored = withContext(Dispatchers.IO) { repo.restoreSession() }
            _state.update {
                it.copy(
                    user = restored,
                    bootstrapping = false,
                )
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(loginError = "Enter email and password") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loginLoading = true, loginError = null) }
            try {
                val user = withContext(Dispatchers.IO) { repo.login(email, password) }
                _state.update {
                    it.copy(
                        loginLoading = false,
                        user = user,
                        loginError = null,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        loginLoading = false,
                        loginError = DisplayText.orFallback(e.message, "Sign-in failed"),
                    )
                }
            }
        }
    }

    fun register(email: String, password: String, companyName: String, fullName: String?) {
        viewModelScope.launch {
            _state.update { it.copy(registerLoading = true, registerError = null) }
            try {
                val user = withContext(Dispatchers.IO) {
                    repo.register(email, password, companyName, fullName)
                }
                _state.update {
                    it.copy(
                        registerLoading = false,
                        user = user,
                        registerError = null,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        registerLoading = false,
                        registerError = DisplayText.orFallback(e.message, "Registration failed"),
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            // Clear UI immediately; network logout continues in background.
            _state.update {
                it.copy(
                    user = null,
                    loginError = null,
                    registerError = null,
                )
            }
            withContext(Dispatchers.IO) { repo.logout() }
        }
    }

    fun clearLoginError() {
        _state.update { it.copy(loginError = null) }
    }

    fun clearRegisterError() {
        _state.update { it.copy(registerError = null) }
    }
}
