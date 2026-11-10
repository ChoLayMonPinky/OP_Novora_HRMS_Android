package dev.tint_wym.novora_hrms_android.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionUserTest {

    private fun user(roles: List<String>, email: String = "alex@novora.app", fullName: String? = null) =
        SessionUser(
            id = "1",
            email = email,
            roles = roles,
            organizationName = "Novora",
            fullName = fullName,
        )

    @Test
    fun canApprove_true_forHrRoles() {
        assertTrue(user(listOf("HR_ADMIN")).canApprove)
        assertTrue(user(listOf("hr_manager")).canApprove)
        assertTrue(user(listOf("EMPLOYEE", "SUPER_ADMIN")).canApprove)
    }

    @Test
    fun canApprove_false_forEmployeeOnly() {
        assertFalse(user(listOf("EMPLOYEE")).canApprove)
        assertFalse(user(emptyList()).canApprove)
    }

    @Test
    fun displayName_prefersFullName() {
        assertEquals("Alex Rivera", user(listOf("EMPLOYEE"), fullName = "Alex Rivera").displayName)
        assertEquals("alex", user(listOf("EMPLOYEE")).displayName)
    }

    @Test
    fun displayName_stripsPlaceholderEmployeeSuffix() {
        assertEquals("Tintwaiyanmin", user(listOf("EMPLOYEE"), fullName = "Tintwaiyanmin Employee").displayName)
        assertEquals("TintWaiYanMin", user(listOf("EMPLOYEE"), fullName = "TintWaiYanMin Employee").displayName)
    }

    @Test
    fun primaryRole_isFirstRole() {
        assertEquals("HR_ADMIN", user(listOf("HR_ADMIN", "EMPLOYEE")).primaryRole)
        assertEquals(null, user(emptyList()).primaryRole)
    }
}
