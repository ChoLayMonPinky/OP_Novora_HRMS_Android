package dev.tint_wym.novora_hrms_android.core.model

data class SessionUser(
    val id: String,
    val email: String,
    val roles: List<String>,
    val organizationName: String?,
    val fullName: String? = null,
) {
    val displayName: String
        get() {
            val trimmed = fullName?.trim().orEmpty()
            if (trimmed.isNotEmpty()) {
                // Registration often stores placeholder last name "Employee".
                val parts = trimmed.split(Regex("\\s+"))
                if (parts.size >= 2 && parts.last().equals("Employee", ignoreCase = true)) {
                    return parts.dropLast(1).joinToString(" ")
                }
                return trimmed
            }
            return email.substringBefore('@').ifBlank { email }
        }

    val primaryRole: String?
        get() = roles.firstOrNull()

    /** Matches web FULL_SYSTEM_ROLES — any matching role grants Approvals. */
    val canApprove: Boolean
        get() = roles.any { it.uppercase() in APPROVER_ROLES }

    companion object {
        val APPROVER_ROLES = setOf("SUPER_ADMIN", "HR_ADMIN", "HR_MANAGER")
    }
}
