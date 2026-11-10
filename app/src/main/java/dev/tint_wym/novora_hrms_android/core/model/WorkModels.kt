package dev.tint_wym.novora_hrms_android.core.model

data class KpiItem(
    val label: String,
    val value: String,
    val delta: String,
)

data class LeaveItem(
    val id: String,
    val employeeName: String,
    val leaveType: String,
    val startDate: String,
    val endDate: String,
    val dateRange: String,
    val reason: String,
    val status: String,
)

data class AttendanceItem(
    val id: String,
    val workDate: String,
    val status: String,
    val checkInTime: String?,
    val checkOutTime: String?,
    val workHours: Double?,
)

data class HomeSnapshot(
    val kpis: List<KpiItem>,
    val leaveRequests: List<LeaveItem>,
    val attendanceRate: Double?,
)

data class LeaveBalance(
    val id: String,
    val leaveType: String,
    val leaveTypeCode: String,
    val balanceYear: Int,
    val totalDays: Double,
    val usedDays: Double,
    val pendingDays: Double,
    val remainingDays: Double,
)

data class FeedPost(
    val id: String,
    val title: String,
    val body: String,
    val authorName: String,
    val createdAt: String,
)

data class HolidayItem(
    val id: String,
    val name: String,
    val holidayDate: String,
    val type: String,
    val description: String,
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val type: String,
    val read: Boolean,
    val createdAt: String,
)

data class DocumentItem(
    val id: String,
    val name: String,
    val docType: String,
    val url: String,
    val uploadedAt: String,
)

data class DocumentContent(
    val name: String,
    val docType: String,
    val contentBase64: String?,
)

data class PayslipItem(
    val id: String,
    val payMonth: Int,
    val payYear: Int,
    val basicSalary: Double,
    val allowances: Double,
    val overtimePay: Double,
    val bonus: Double,
    val deductions: Double,
    val tax: Double,
    val netPay: Double,
    val status: String,
    val processedAt: String,
    val paidAt: String,
) {
    val periodLabel: String
        get() {
            val month = when (payMonth) {
                1 -> "Jan"
                2 -> "Feb"
                3 -> "Mar"
                4 -> "Apr"
                5 -> "May"
                6 -> "Jun"
                7 -> "Jul"
                8 -> "Aug"
                9 -> "Sep"
                10 -> "Oct"
                11 -> "Nov"
                12 -> "Dec"
                else -> payMonth.toString()
            }
            return "$month $payYear"
        }
}

data class FamilyMember(
    val id: String,
    val name: String,
    val relationship: String,
    val dateOfBirth: String,
    val phone: String,
)

data class EducationRecord(
    val id: String,
    val institution: String,
    val degree: String,
    val fieldOfStudy: String,
    val startDate: String,
    val endDate: String,
    val grade: String,
)

data class ClaimItem(
    val id: String,
    val employeeName: String,
    val category: String,
    val claimDate: String,
    val amount: Double,
    val currency: String,
    val vendor: String,
    val description: String,
    val status: String,
    val decisionNote: String,
)

data class OnboardingTask(
    val id: String,
    val title: String,
    val dueDate: String,
    val completed: Boolean,
)

data class HelpdeskTicket(
    val id: String,
    val subject: String,
    val description: String,
    val category: String,
    val priority: String,
    val status: String,
    val createdAt: String,
)

data class BenefitEnrollment(
    val id: String,
    val planName: String,
    val status: String,
    val enrolledAt: String,
    val notes: String,
)

data class TimeLogItem(
    val id: String,
    val workDate: String,
    val hours: Double,
    val project: String,
    val notes: String,
)

/** Employee self-service profile from `GET /api/my/profile` (+ family / education). */
data class MyProfileDetails(
    val employeeId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val departmentName: String,
    val jobTitle: String,
    val managerName: String,
    val dateOfBirth: String,
    val phone: String,
    val addressLine1: String,
    val addressLine2: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val country: String,
    val emergencyContactName: String,
    val emergencyContactPhone: String,
    val family: List<FamilyMember> = emptyList(),
    val education: List<EducationRecord> = emptyList(),
) {
    val personName: String
        get() {
            fun clean(part: String) =
                part.trim().takeUnless { it.isEmpty() || it.equals("null", ignoreCase = true) }.orEmpty()
            val first = clean(firstName)
            val last = clean(lastName)
            if (last.equals("Employee", ignoreCase = true)) {
                return first.ifBlank { last }
            }
            return listOf(first, last).filter { it.isNotEmpty() }.joinToString(" ")
        }

    val initials: String
        get() = personName
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifBlank { "N" }

    val addressLines: List<String>
        get() = listOf(
            addressLine1,
            addressLine2,
            listOf(city, state, postalCode).filter {
                it.isNotBlank() && !it.equals("null", ignoreCase = true)
            }.joinToString(", "),
            country,
        ).map { it.trim() }.filter { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
}
