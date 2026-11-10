package dev.tint_wym.novora_hrms_android.core.data

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import dev.tint_wym.novora_hrms_android.core.model.AttendanceItem
import dev.tint_wym.novora_hrms_android.core.model.BenefitEnrollment
import dev.tint_wym.novora_hrms_android.core.model.ClaimItem
import dev.tint_wym.novora_hrms_android.core.model.DocumentContent
import dev.tint_wym.novora_hrms_android.core.model.DocumentItem
import dev.tint_wym.novora_hrms_android.core.model.EducationRecord
import dev.tint_wym.novora_hrms_android.core.model.FamilyMember
import dev.tint_wym.novora_hrms_android.core.model.FeedPost
import dev.tint_wym.novora_hrms_android.core.model.HelpdeskTicket
import dev.tint_wym.novora_hrms_android.core.model.HolidayItem
import dev.tint_wym.novora_hrms_android.core.model.HomeSnapshot
import dev.tint_wym.novora_hrms_android.core.model.KpiItem
import dev.tint_wym.novora_hrms_android.core.model.LeaveBalance
import dev.tint_wym.novora_hrms_android.core.model.LeaveItem
import dev.tint_wym.novora_hrms_android.core.model.MyProfileDetails
import dev.tint_wym.novora_hrms_android.core.model.NotificationItem
import dev.tint_wym.novora_hrms_android.core.model.OnboardingTask
import dev.tint_wym.novora_hrms_android.core.model.PayslipItem
import dev.tint_wym.novora_hrms_android.core.model.TimeLogItem
import dev.tint_wym.novora_hrms_android.core.network.ApiException
import dev.tint_wym.novora_hrms_android.core.network.JsonStrings
import dev.tint_wym.novora_hrms_android.feature.auth.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class ResolvedDocument(
    val uri: Uri,
    val mimeType: String,
    val displayName: String,
)

class WorkRepository(context: Context) {
    private val appContext = context.applicationContext
    private val api = AuthRepository.get(context).api

    fun fetchHome(): HomeSnapshot {
        val json = api.getJson("/api/my/dashboard")
        val kpis = mutableListOf<KpiItem>()
        val kpiArr = json.optJSONArray("kpis")
        if (kpiArr != null) {
            for (i in 0 until kpiArr.length()) {
                val row = kpiArr.optJSONObject(i) ?: continue
                kpis.add(
                    KpiItem(
                        label = JsonStrings.of(row, "label"),
                        value = JsonStrings.of(row, "value"),
                        delta = JsonStrings.of(row, "delta"),
                    ),
                )
            }
        }
        val leaves = mutableListOf<LeaveItem>()
        val leaveArr = json.optJSONArray("leaveRequests")
        if (leaveArr != null) {
            for (i in 0 until leaveArr.length()) {
                val row = leaveArr.optJSONObject(i) ?: continue
                leaves.add(parseLeave(row))
            }
        }
        val attendance = json.optJSONObject("attendanceOverview")
        val rate = attendance?.optDouble("attendanceRate")?.takeIf { !it.isNaN() }
        return HomeSnapshot(kpis = kpis, leaveRequests = leaves, attendanceRate = rate)
    }

    suspend fun fetchHomeExtras(): Pair<List<FeedPost>, List<HolidayItem>> = coroutineScope {
        val feeds = async(Dispatchers.IO) {
            try {
                parseFeedArray(api.getJsonArray("/api/feeds"))
            } catch (_: Exception) {
                emptyList()
            }
        }
        val holidays = async(Dispatchers.IO) {
            try {
                parseHolidayArray(api.getJsonArray("/api/holidays"))
            } catch (_: Exception) {
                emptyList()
            }
        }
        feeds.await() to holidays.await()
    }

    fun fetchMyAttendance(): List<AttendanceItem> {
        return parseAttendanceArray(api.getJsonArray("/api/my/attendance"))
    }

    fun checkIn(): AttendanceItem {
        return parseAttendance(api.postEmpty("/api/my/attendance/check-in"))
    }

    fun checkOut(): AttendanceItem {
        return parseAttendance(api.postEmpty("/api/my/attendance/check-out"))
    }

    fun fetchMyLeave(): List<LeaveItem> {
        return parseLeaveArray(api.getJsonArray("/api/my/leave"))
    }

    fun fetchLeaveBalances(): List<LeaveBalance> {
        return parseLeaveBalanceArray(api.getJsonArray("/api/my/leave/balances"))
    }

    fun fetchLeaveTypes(): List<String> {
        val arr = api.getJsonArray("/api/leave-types")
        val out = mutableListOf<String>()
        for (i in 0 until arr.length()) {
            val row = arr.optJSONObject(i) ?: continue
            val name = JsonStrings.of(row, "name")
            if (name.isNotBlank()) out.add(name)
        }
        return out
    }

    fun fetchMyPayslips(): List<PayslipItem> {
        return parsePayslipArray(api.getJsonArray("/api/my/payslips"))
    }

    fun fetchNotifications(): List<NotificationItem> {
        return parseNotificationArray(api.getJsonArray("/api/my/notifications"))
    }

    fun markNotificationRead(id: String): NotificationItem {
        return parseNotification(api.postEmpty("/api/my/notifications/$id/read"))
    }

    fun fetchMyDocuments(): List<DocumentItem> {
        return parseDocumentArray(api.getJsonArray("/api/my/documents"))
    }

    fun fetchDocumentContent(id: String): DocumentContent {
        val json = api.getJson("/api/my/documents/$id/content")
        return DocumentContent(
            name = JsonStrings.of(json, "name"),
            docType = JsonStrings.of(json, "docType"),
            contentBase64 = JsonStrings.ofOrNull(json, "contentBase64"),
        )
    }

    /**
     * Resolves a document to a shareable content URI.
     * Prefers inline base64 from `/content`; falls back to an https `url` when present.
     */
    fun resolveDocumentOpen(doc: DocumentItem): ResolvedDocument {
        val httpsUrl = doc.url.trim().takeIf { it.startsWith("https://", ignoreCase = true) }
        val content = try {
            fetchDocumentContent(doc.id)
        } catch (_: Exception) {
            null
        }
        val rawB64 = content?.contentBase64?.trim().orEmpty()
        if (rawB64.isNotEmpty()) {
            val payload = rawB64.substringAfter("base64,", rawB64)
            val bytes = try {
                Base64.decode(payload, Base64.DEFAULT)
            } catch (_: Exception) {
                throw ApiException("Document content is not valid")
            }
            if (bytes.isEmpty()) throw ApiException("Document is empty")
            val fileName = sanitizeFileName(
                content?.name?.ifBlank { null } ?: doc.name.ifBlank { "document" },
                content?.docType ?: doc.docType,
            )
            val dir = File(appContext.cacheDir, "documents").apply { mkdirs() }
            val file = File(dir, "${doc.id}_$fileName")
            file.writeBytes(bytes)
            val uri = FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.fileprovider",
                file,
            )
            return ResolvedDocument(
                uri = uri,
                mimeType = guessMime(fileName, content?.docType ?: doc.docType),
                displayName = fileName,
            )
        }
        if (httpsUrl != null) {
            return ResolvedDocument(
                uri = Uri.parse(httpsUrl),
                mimeType = guessMime(doc.name, doc.docType),
                displayName = doc.name.ifBlank { "document" },
            )
        }
        throw ApiException("No downloadable content for this document")
    }

    fun createLeave(leaveType: String, startDate: String, endDate: String, reason: String): LeaveItem {
        val body = JSONObject()
            .put("leaveType", leaveType)
            .put("startDate", startDate)
            .put("endDate", endDate)
            .put("reason", reason.ifBlank { JSONObject.NULL })
        return parseLeave(api.postJson("/api/my/leave", body))
    }

    fun cancelLeave(id: String) {
        api.delete("/api/my/leave/$id")
    }

    fun fetchPendingLeave(): List<LeaveItem> {
        return parseLeaveArray(api.getJsonArray("/api/admin/leave/pending"))
    }

    fun decideLeave(id: String, approve: Boolean, note: String = ""): LeaveItem {
        val body = JSONObject()
            .put("decision", if (approve) "APPROVE" else "REJECT")
            .put("note", note.ifBlank { JSONObject.NULL })
        return parseLeave(api.postJson("/api/admin/leave/$id/decide", body))
    }

    fun fetchMyClaims(): List<ClaimItem> {
        return parseClaimArray(api.getJsonArray("/api/my/claims"))
    }

    fun createClaim(
        category: String,
        claimDate: String,
        amount: Double,
        currency: String,
        vendor: String,
        description: String,
    ): ClaimItem {
        val body = JSONObject()
            .put("category", category)
            .put("claimDate", claimDate)
            .put("amount", amount)
            .put("currency", currency.trim().uppercase().ifBlank { JSONObject.NULL })
            .put("vendor", vendor.ifBlank { JSONObject.NULL })
            .put("description", description.ifBlank { JSONObject.NULL })
        return parseClaim(api.postJson("/api/my/claims", body))
    }

    fun fetchPendingClaims(): List<ClaimItem> {
        return parseClaimArray(api.getJsonArray("/api/admin/claims?status=pending"))
    }

    fun decideClaim(id: String, approve: Boolean, note: String = ""): ClaimItem {
        val body = JSONObject()
            .put("decision", if (approve) "APPROVE" else "REJECT")
            .put("note", note.ifBlank { JSONObject.NULL })
        return parseClaim(api.postJson("/api/admin/claims/$id/decide", body))
    }

    fun fetchOnboarding(): List<OnboardingTask> {
        return parseOnboardingArray(api.getJsonArray("/api/my/onboarding"))
    }

    fun setOnboardingCompleted(taskId: String, completed: Boolean): OnboardingTask {
        val body = JSONObject().put("completed", completed)
        return parseOnboarding(api.putJson("/api/my/onboarding/$taskId", body))
    }

    fun fetchHelpdeskTickets(): List<HelpdeskTicket> {
        return parseHelpdeskArray(api.getJsonArray("/api/my/helpdesk/tickets"))
    }

    fun createHelpdeskTicket(
        subject: String,
        description: String,
        category: String,
        priority: String,
    ): HelpdeskTicket {
        val body = JSONObject()
            .put("subject", subject)
            .put("description", description.ifBlank { JSONObject.NULL })
            .put("category", category.ifBlank { JSONObject.NULL })
            .put("priority", priority.ifBlank { "normal" })
        return parseHelpdesk(api.postJson("/api/my/helpdesk/tickets", body))
    }

    fun fetchBenefitEnrollments(): List<BenefitEnrollment> {
        return parseBenefitArray(api.getJsonArray("/api/my/benefit-enrollments"))
    }

    fun fetchTimeLogs(): List<TimeLogItem> {
        return parseTimeLogArray(api.getJsonArray("/api/my/time-logs"))
    }

    fun createTimeLog(workDate: String, hours: Double, project: String, notes: String): TimeLogItem {
        val body = JSONObject()
            .put("workDate", workDate)
            .put("hours", hours)
            .put("project", project.ifBlank { JSONObject.NULL })
            .put("notes", notes.ifBlank { JSONObject.NULL })
        return parseTimeLog(api.postJson("/api/my/time-logs", body))
    }

    fun createFamily(name: String, relationship: String, dateOfBirth: String, phone: String): FamilyMember {
        val body = JSONObject()
            .put("name", name)
            .put("relationship", relationship.ifBlank { JSONObject.NULL })
            .put("dateOfBirth", dateOfBirth.ifBlank { JSONObject.NULL })
            .put("phone", phone.ifBlank { JSONObject.NULL })
        return parseFamily(api.postJson("/api/my/family", body))
    }

    fun deleteFamily(id: String) {
        api.delete("/api/my/family/$id")
    }

    fun createEducation(
        institution: String,
        degree: String,
        fieldOfStudy: String,
        startDate: String,
        endDate: String,
        grade: String,
    ): EducationRecord {
        val body = JSONObject()
            .put("institution", institution)
            .put("degree", degree.ifBlank { JSONObject.NULL })
            .put("fieldOfStudy", fieldOfStudy.ifBlank { JSONObject.NULL })
            .put("startDate", startDate.ifBlank { JSONObject.NULL })
            .put("endDate", endDate.ifBlank { JSONObject.NULL })
            .put("grade", grade.ifBlank { JSONObject.NULL })
        return parseEducation(api.postJson("/api/my/education", body))
    }

    fun deleteEducation(id: String) {
        api.delete("/api/my/education/$id")
    }

    fun deleteDocument(id: String) {
        api.delete("/api/my/documents/$id")
    }

    suspend fun fetchMyProfile(): MyProfileDetails = coroutineScope {
        val baseDeferred = async(Dispatchers.IO) {
            parseMyProfile(api.getJson("/api/my/profile"))
        }
        val familyDeferred = async(Dispatchers.IO) {
            try {
                parseFamilyArray(api.getJsonArray("/api/my/family"))
            } catch (_: Exception) {
                emptyList()
            }
        }
        val educationDeferred = async(Dispatchers.IO) {
            try {
                parseEducationArray(api.getJsonArray("/api/my/education"))
            } catch (_: Exception) {
                emptyList()
            }
        }
        baseDeferred.await().copy(
            family = familyDeferred.await(),
            education = educationDeferred.await(),
        )
    }

    fun requestPersonalOtp(): Triple<Long, String, String?> {
        val json = api.postEmpty("/api/my/profile/personal/otp")
        val expires = json.optLong("expiresInSeconds", 600)
        val message = JsonStrings.of(json, "message").ifBlank { "Verification code sent." }
        val debug = JsonStrings.ofOrNull(json, "debugCode")
        return Triple(expires, message, debug)
    }

    fun updatePersonal(
        otpCode: String,
        phone: String,
        addressLine1: String,
        addressLine2: String,
        city: String,
        state: String,
        postalCode: String,
        country: String,
        emergencyContactName: String,
        emergencyContactPhone: String,
    ): MyProfileDetails {
        val personal = JSONObject()
            .put("phone", phone)
            .put("addressLine1", addressLine1)
            .put("addressLine2", addressLine2)
            .put("city", city)
            .put("state", state)
            .put("postalCode", postalCode)
            .put("country", country)
            .put("emergencyContactName", emergencyContactName)
            .put("emergencyContactPhone", emergencyContactPhone)
        val body = JSONObject()
            .put("otpCode", otpCode)
            .put("personal", personal)
        return parseMyProfile(api.putJson("/api/my/profile/personal", body))
    }

    companion object {
        fun todayIso(): String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        fun sanitizeFileName(name: String, docType: String): String {
            val cleaned = name.trim()
                .replace(Regex("[\\\\/:*?\"<>|]"), "_")
                .ifBlank { "document" }
            if (cleaned.contains('.')) return cleaned.take(120)
            val ext = when (docType.trim().lowercase()) {
                "pdf" -> "pdf"
                "image", "photo", "png" -> "png"
                "jpg", "jpeg" -> "jpg"
                "id_card", "passport", "contract" -> "pdf"
                else -> "bin"
            }
            return "${cleaned.take(100)}.$ext"
        }

        fun guessMime(fileName: String, docType: String): String {
            val lower = fileName.lowercase()
            return when {
                lower.endsWith(".pdf") -> "application/pdf"
                lower.endsWith(".png") -> "image/png"
                lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
                lower.endsWith(".webp") -> "image/webp"
                lower.endsWith(".gif") -> "image/gif"
                lower.endsWith(".txt") -> "text/plain"
                lower.endsWith(".doc") -> "application/msword"
                lower.endsWith(".docx") ->
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                else -> when (docType.trim().lowercase()) {
                    "pdf" -> "application/pdf"
                    "image", "photo", "png" -> "image/png"
                    "jpg", "jpeg" -> "image/jpeg"
                    else -> "application/octet-stream"
                }
            }
        }

        fun formatClock(raw: String?): String {
            val value = JsonStrings.clean(raw)
            if (value.isEmpty()) return "--:--"
            return try {
                LocalTime.parse(value).format(DateTimeFormatter.ofPattern("HH:mm"))
            } catch (_: DateTimeParseException) {
                when {
                    value.length >= 5 && value[2] == ':' -> value.take(5)
                    'T' in value && value.substringAfter('T').length >= 5 ->
                        value.substringAfter('T').take(5)
                    else -> value
                }
            }
        }

        private fun parseMyProfile(json: JSONObject): MyProfileDetails {
            val personal = json.optJSONObject("personal")
            return MyProfileDetails(
                employeeId = JsonStrings.of(json, "employeeId"),
                firstName = JsonStrings.of(json, "firstName"),
                lastName = JsonStrings.of(json, "lastName"),
                email = JsonStrings.of(json, "email"),
                departmentName = JsonStrings.of(json, "departmentName"),
                jobTitle = JsonStrings.of(json, "jobTitle"),
                managerName = JsonStrings.of(json, "managerName"),
                dateOfBirth = JsonStrings.of(json, "dateOfBirth"),
                phone = personal?.let { JsonStrings.of(it, "phone") }.orEmpty(),
                addressLine1 = personal?.let { JsonStrings.of(it, "addressLine1") }.orEmpty(),
                addressLine2 = personal?.let { JsonStrings.of(it, "addressLine2") }.orEmpty(),
                city = personal?.let { JsonStrings.of(it, "city") }.orEmpty(),
                state = personal?.let { JsonStrings.of(it, "state") }.orEmpty(),
                postalCode = personal?.let { JsonStrings.of(it, "postalCode") }.orEmpty(),
                country = personal?.let { JsonStrings.of(it, "country") }.orEmpty(),
                emergencyContactName = personal?.let { JsonStrings.of(it, "emergencyContactName") }.orEmpty(),
                emergencyContactPhone = personal?.let { JsonStrings.of(it, "emergencyContactPhone") }.orEmpty(),
            )
        }

        private fun parseFamilyArray(arr: JSONArray): List<FamilyMember> {
            val out = mutableListOf<FamilyMember>()
            for (i in 0 until arr.length()) {
                val row = arr.optJSONObject(i) ?: continue
                out.add(parseFamily(row))
            }
            return out
        }

        private fun parseFamily(row: JSONObject): FamilyMember {
            return FamilyMember(
                id = JsonStrings.of(row, "id"),
                name = JsonStrings.of(row, "name"),
                relationship = JsonStrings.of(row, "relationship"),
                dateOfBirth = JsonStrings.of(row, "dateOfBirth"),
                phone = JsonStrings.of(row, "phone"),
            )
        }

        private fun parseEducationArray(arr: JSONArray): List<EducationRecord> {
            val out = mutableListOf<EducationRecord>()
            for (i in 0 until arr.length()) {
                val row = arr.optJSONObject(i) ?: continue
                out.add(parseEducation(row))
            }
            return out
        }

        private fun parseEducation(row: JSONObject): EducationRecord {
            return EducationRecord(
                id = JsonStrings.of(row, "id"),
                institution = JsonStrings.of(row, "institution"),
                degree = JsonStrings.of(row, "degree"),
                fieldOfStudy = JsonStrings.of(row, "fieldOfStudy"),
                startDate = JsonStrings.of(row, "startDate"),
                endDate = JsonStrings.of(row, "endDate"),
                grade = JsonStrings.of(row, "grade"),
            )
        }

        private fun parsePayslipArray(arr: JSONArray): List<PayslipItem> {
            val out = mutableListOf<PayslipItem>()
            for (i in 0 until arr.length()) {
                val row = arr.optJSONObject(i) ?: continue
                out.add(
                    PayslipItem(
                        id = JsonStrings.of(row, "id"),
                        payMonth = row.optInt("payMonth", 0),
                        payYear = row.optInt("payYear", 0),
                        basicSalary = money(row, "basicSalary"),
                        allowances = money(row, "allowances"),
                        overtimePay = money(row, "overtimePay"),
                        bonus = money(row, "bonus"),
                        deductions = money(row, "deductions"),
                        tax = money(row, "tax"),
                        netPay = money(row, "netPay"),
                        status = JsonStrings.of(row, "status").ifBlank { "draft" },
                        processedAt = JsonStrings.of(row, "processedAt"),
                        paidAt = JsonStrings.of(row, "paidAt"),
                    ),
                )
            }
            return out
        }

        private fun parseLeaveBalanceArray(arr: JSONArray): List<LeaveBalance> {
            val out = mutableListOf<LeaveBalance>()
            for (i in 0 until arr.length()) {
                val row = arr.optJSONObject(i) ?: continue
                out.add(
                    LeaveBalance(
                        id = JsonStrings.of(row, "id"),
                        leaveType = JsonStrings.of(row, "leaveType"),
                        leaveTypeCode = JsonStrings.of(row, "leaveTypeCode"),
                        balanceYear = row.optInt("balanceYear", LocalDate.now().year),
                        totalDays = money(row, "totalDays"),
                        usedDays = money(row, "usedDays"),
                        pendingDays = money(row, "pendingDays"),
                        remainingDays = money(row, "remainingDays"),
                    ),
                )
            }
            return out
        }

        private fun parseFeedArray(arr: JSONArray): List<FeedPost> {
            val out = mutableListOf<FeedPost>()
            for (i in 0 until arr.length()) {
                val row = arr.optJSONObject(i) ?: continue
                out.add(
                    FeedPost(
                        id = JsonStrings.of(row, "id"),
                        title = JsonStrings.of(row, "title"),
                        body = JsonStrings.of(row, "body"),
                        authorName = JsonStrings.of(row, "authorName"),
                        createdAt = JsonStrings.of(row, "createdAt"),
                    ),
                )
            }
            return out
        }

        private fun parseHolidayArray(arr: JSONArray): List<HolidayItem> {
            val out = mutableListOf<HolidayItem>()
            for (i in 0 until arr.length()) {
                val row = arr.optJSONObject(i) ?: continue
                out.add(
                    HolidayItem(
                        id = JsonStrings.of(row, "id"),
                        name = JsonStrings.of(row, "name"),
                        holidayDate = JsonStrings.of(row, "holidayDate"),
                        type = JsonStrings.of(row, "type"),
                        description = JsonStrings.of(row, "description"),
                    ),
                )
            }
            return out.sortedBy { it.holidayDate }
        }

        private fun parseNotificationArray(arr: JSONArray): List<NotificationItem> {
            val out = mutableListOf<NotificationItem>()
            for (i in 0 until arr.length()) {
                val row = arr.optJSONObject(i) ?: continue
                out.add(parseNotification(row))
            }
            return out
        }

        private fun parseNotification(row: JSONObject): NotificationItem {
            return NotificationItem(
                id = JsonStrings.of(row, "id"),
                title = JsonStrings.of(row, "title"),
                message = JsonStrings.of(row, "message"),
                type = JsonStrings.of(row, "type"),
                read = row.optBoolean("read", false),
                createdAt = JsonStrings.of(row, "createdAt"),
            )
        }

        private fun parseDocumentArray(arr: JSONArray): List<DocumentItem> {
            val out = mutableListOf<DocumentItem>()
            for (i in 0 until arr.length()) {
                val row = arr.optJSONObject(i) ?: continue
                out.add(
                    DocumentItem(
                        id = JsonStrings.of(row, "id"),
                        name = JsonStrings.of(row, "name"),
                        docType = JsonStrings.of(row, "docType"),
                        url = JsonStrings.of(row, "url"),
                        uploadedAt = JsonStrings.of(row, "uploadedAt"),
                    ),
                )
            }
            return out
        }

        private fun parseClaimArray(arr: JSONArray): List<ClaimItem> {
            val out = mutableListOf<ClaimItem>()
            for (i in 0 until arr.length()) {
                val row = arr.optJSONObject(i) ?: continue
                out.add(parseClaim(row))
            }
            return out
        }

        private fun parseClaim(row: JSONObject): ClaimItem {
            return ClaimItem(
                id = JsonStrings.of(row, "id"),
                employeeName = JsonStrings.of(row, "employeeName"),
                category = JsonStrings.of(row, "category"),
                claimDate = JsonStrings.of(row, "claimDate"),
                amount = money(row, "amount"),
                currency = JsonStrings.of(row, "currency"),
                vendor = JsonStrings.of(row, "vendor"),
                description = JsonStrings.of(row, "description"),
                status = JsonStrings.of(row, "status").ifBlank { "pending" },
                decisionNote = JsonStrings.of(row, "decisionNote"),
            )
        }

        private fun parseOnboardingArray(arr: JSONArray): List<OnboardingTask> {
            val out = mutableListOf<OnboardingTask>()
            for (i in 0 until arr.length()) {
                val row = arr.optJSONObject(i) ?: continue
                out.add(parseOnboarding(row))
            }
            return out
        }

        private fun parseOnboarding(row: JSONObject): OnboardingTask {
            return OnboardingTask(
                id = JsonStrings.of(row, "id"),
                title = JsonStrings.of(row, "title"),
                dueDate = JsonStrings.of(row, "dueDate"),
                completed = row.optBoolean("completed", false),
            )
        }

        private fun parseHelpdeskArray(arr: JSONArray): List<HelpdeskTicket> {
            val out = mutableListOf<HelpdeskTicket>()
            for (i in 0 until arr.length()) {
                val row = arr.optJSONObject(i) ?: continue
                out.add(parseHelpdesk(row))
            }
            return out
        }

        private fun parseHelpdesk(row: JSONObject): HelpdeskTicket {
            return HelpdeskTicket(
                id = JsonStrings.of(row, "id"),
                subject = JsonStrings.of(row, "subject"),
                description = JsonStrings.of(row, "description"),
                category = JsonStrings.of(row, "category"),
                priority = JsonStrings.of(row, "priority"),
                status = JsonStrings.of(row, "status"),
                createdAt = JsonStrings.of(row, "createdAt"),
            )
        }

        private fun parseBenefitArray(arr: JSONArray): List<BenefitEnrollment> {
            val out = mutableListOf<BenefitEnrollment>()
            for (i in 0 until arr.length()) {
                val row = arr.optJSONObject(i) ?: continue
                out.add(
                    BenefitEnrollment(
                        id = JsonStrings.of(row, "id"),
                        planName = JsonStrings.of(row, "planName"),
                        status = JsonStrings.of(row, "status"),
                        enrolledAt = JsonStrings.of(row, "enrolledAt"),
                        notes = JsonStrings.of(row, "notes"),
                    ),
                )
            }
            return out
        }

        private fun parseTimeLogArray(arr: JSONArray): List<TimeLogItem> {
            val out = mutableListOf<TimeLogItem>()
            for (i in 0 until arr.length()) {
                val row = arr.optJSONObject(i) ?: continue
                out.add(parseTimeLog(row))
            }
            return out
        }

        private fun parseTimeLog(row: JSONObject): TimeLogItem {
            return TimeLogItem(
                id = JsonStrings.of(row, "id"),
                workDate = JsonStrings.of(row, "workDate"),
                hours = money(row, "hours"),
                project = JsonStrings.of(row, "project"),
                notes = JsonStrings.of(row, "notes"),
            )
        }

        private fun money(row: JSONObject, key: String): Double {
            val raw = row.opt(key)
            return when (raw) {
                is Number -> raw.toDouble()
                JSONObject.NULL, null -> 0.0
                else -> JsonStrings.clean(raw).toDoubleOrNull() ?: 0.0
            }
        }

        private fun parseAttendanceArray(arr: JSONArray): List<AttendanceItem> {
            val out = mutableListOf<AttendanceItem>()
            for (i in 0 until arr.length()) {
                val row = arr.optJSONObject(i) ?: continue
                out.add(parseAttendance(row))
            }
            return out
        }

        private fun parseAttendance(row: JSONObject): AttendanceItem {
            val hours = row.opt("workHours")
            val workHours = when (hours) {
                is Number -> hours.toDouble()
                JSONObject.NULL, null -> null
                else -> JsonStrings.clean(hours).toDoubleOrNull()
            }
            return AttendanceItem(
                id = JsonStrings.of(row, "id"),
                workDate = JsonStrings.of(row, "workDate"),
                status = JsonStrings.of(row, "status").ifBlank { "—" },
                checkInTime = JsonStrings.ofOrNull(row, "checkInTime"),
                checkOutTime = JsonStrings.ofOrNull(row, "checkOutTime"),
                workHours = workHours,
            )
        }

        private fun parseLeaveArray(arr: JSONArray): List<LeaveItem> {
            val out = mutableListOf<LeaveItem>()
            for (i in 0 until arr.length()) {
                val row = arr.optJSONObject(i) ?: continue
                out.add(parseLeave(row))
            }
            return out
        }

        private fun parseLeave(row: JSONObject): LeaveItem {
            val start = JsonStrings.of(row, "startDate")
            val end = JsonStrings.of(row, "endDate")
            val range = when {
                start.isNotBlank() && end.isNotBlank() -> "$start — $end"
                else -> JsonStrings.of(row, "dateRange")
            }
            val employee = JsonStrings.of(row, "employeeName")
                .ifBlank { JsonStrings.of(row, "name") }
            return LeaveItem(
                id = JsonStrings.of(row, "id"),
                employeeName = employee,
                leaveType = JsonStrings.of(row, "leaveType").ifBlank { "Leave" },
                startDate = start,
                endDate = end,
                dateRange = range,
                reason = JsonStrings.of(row, "reason"),
                status = JsonStrings.of(row, "status").uppercase().ifBlank { "PENDING" },
            )
        }
    }
}
