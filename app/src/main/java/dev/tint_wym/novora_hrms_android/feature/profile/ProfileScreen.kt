package dev.tint_wym.novora_hrms_android.feature.profile

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.tint_wym.novora_hrms_android.core.data.WorkRepository
import dev.tint_wym.novora_hrms_android.core.model.BenefitEnrollment
import dev.tint_wym.novora_hrms_android.core.model.DocumentItem
import dev.tint_wym.novora_hrms_android.core.model.EducationRecord
import dev.tint_wym.novora_hrms_android.core.model.FamilyMember
import dev.tint_wym.novora_hrms_android.core.model.MyProfileDetails
import dev.tint_wym.novora_hrms_android.core.model.SessionUser
import dev.tint_wym.novora_hrms_android.core.network.DisplayText
import dev.tint_wym.novora_hrms_android.core.ui.components.SecondaryActionButton
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBlue
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraBorder
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraCard
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraDanger
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraInfoSoft
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraInk
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSlate
import dev.tint_wym.novora_hrms_android.core.ui.theme.NovoraSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(user: SessionUser, onLogout: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { WorkRepository(context) }
    val scope = rememberCoroutineScope()

    var details by remember { mutableStateOf<MyProfileDetails?>(null) }
    var documents by remember { mutableStateOf<List<DocumentItem>>(emptyList()) }
    var benefits by remember { mutableStateOf<List<BenefitEnrollment>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var openingDocId by remember { mutableStateOf<String?>(null) }
    var showAddFamily by remember { mutableStateOf(false) }
    var showAddEducation by remember { mutableStateOf(false) }

    fun openDocument(doc: DocumentItem) {
        if (openingDocId != null) return
        scope.launch {
            openingDocId = doc.id
            try {
                val resolved = withContext(Dispatchers.IO) { repo.resolveDocumentOpen(doc) }
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(resolved.uri, resolved.mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooser = Intent.createChooser(intent, "Open ${resolved.displayName}")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    e.message?.takeIf { it.isNotBlank() } ?: "Could not open document",
                    Toast.LENGTH_LONG,
                ).show()
            } finally {
                openingDocId = null
            }
        }
    }

    fun reload() {
        scope.launch {
            loading = true
            error = null
            try {
                val result = coroutineScope {
                    val profileD = async { repo.fetchMyProfile() }
                    val docsD = async {
                        withContext(Dispatchers.IO) {
                            try {
                                repo.fetchMyDocuments()
                            } catch (_: Exception) {
                                emptyList()
                            }
                        }
                    }
                    val benefitsD = async {
                        withContext(Dispatchers.IO) {
                            try {
                                repo.fetchBenefitEnrollments()
                            } catch (_: Exception) {
                                emptyList()
                            }
                        }
                    }
                    Triple(profileD.await(), docsD.await(), benefitsD.await())
                }
                details = result.first
                documents = result.second
                benefits = result.third
            } catch (e: Exception) {
                error = e.message ?: "Could not load profile"
            }
            loading = false
        }
    }

    LaunchedEffect(user.id) { reload() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovoraSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = NovoraInk,
        )
        Text(
            text = "Your employee record. Name and job details are managed by HR; personal contact can be updated with email verification.",
            style = MaterialTheme.typography.bodySmall,
            color = NovoraSlate,
        )

        if (loading && details == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = NovoraBlue)
            }
        } else {
            ProfileHeaderCard(
                name = details?.personName?.takeIf { it.isNotBlank() } ?: user.displayName,
                initials = details?.initials ?: user.displayName
                    .split(Regex("\\s+"))
                    .filter { it.isNotBlank() }
                    .take(2)
                    .joinToString("") { it.first().uppercaseChar().toString() }
                    .ifBlank { "N" },
                jobTitle = details?.jobTitle.orEmpty(),
                department = details?.departmentName.orEmpty(),
                email = details?.email?.takeIf { it.isNotBlank() } ?: user.email,
            )

            ProfileSectionCard(title = "Identity") {
                ProfileLabeled("Employee ID", details?.employeeId.orEmpty())
                ProfileLabeled("Full name", details?.personName?.takeIf { it.isNotBlank() } ?: user.displayName)
                ProfileLabeled("Email", details?.email?.takeIf { it.isNotBlank() } ?: user.email)
                ProfileLabeled("Role", roleLabel(user.primaryRole))
                if (!user.organizationName.isNullOrBlank()) {
                    ProfileLabeled("Organization", user.organizationName)
                }
                ProfileLabeled("Date of birth", details?.dateOfBirth.orEmpty())
            }

            ProfileSectionCard(title = "Work") {
                ProfileLabeled("Department", details?.departmentName.orEmpty())
                ProfileLabeled("Job title", details?.jobTitle.orEmpty())
                ProfileLabeled("Manager", details?.managerName.orEmpty())
            }

            ProfileSectionCard(title = "Contact & address") {
                ProfileLabeled("Phone", details?.phone.orEmpty())
                if (details?.addressLines.isNullOrEmpty()) {
                    ProfileLabeled("Address", "")
                } else {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = "Address",
                            style = MaterialTheme.typography.labelMedium,
                            color = NovoraSlate,
                        )
                        details!!.addressLines.forEach { line ->
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodyLarge,
                                color = NovoraInk,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                OutlinedButton(
                    onClick = { showEditor = true },
                    enabled = details != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                ) {
                    Text("Edit personal details")
                }
            }

            ProfileSectionCard(title = "Emergency contact") {
                ProfileLabeled("Name", details?.emergencyContactName.orEmpty())
                ProfileLabeled("Phone", details?.emergencyContactPhone.orEmpty())
            }

            ProfileSectionCard(title = "Family") {
                val family = details?.family.orEmpty()
                if (family.isEmpty()) {
                    EmptyHint("No family members on file.")
                } else {
                    family.forEachIndexed { index, member ->
                        FamilyRow(
                            member = member,
                            onDelete = {
                                scope.launch {
                                    try {
                                        withContext(Dispatchers.IO) { repo.deleteFamily(member.id) }
                                        details = details?.copy(
                                            family = details!!.family.filterNot { it.id == member.id },
                                        )
                                    } catch (e: Exception) {
                                        error = e.message ?: "Could not delete family member"
                                    }
                                }
                            },
                        )
                        if (index < family.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = NovoraBorder,
                            )
                        }
                    }
                }
                TextButton(onClick = { showAddFamily = true }) { Text("Add family member") }
            }

            ProfileSectionCard(title = "Education") {
                val education = details?.education.orEmpty()
                if (education.isEmpty()) {
                    EmptyHint("No education records on file.")
                } else {
                    education.forEachIndexed { index, record ->
                        EducationRow(
                            record = record,
                            onDelete = {
                                scope.launch {
                                    try {
                                        withContext(Dispatchers.IO) { repo.deleteEducation(record.id) }
                                        details = details?.copy(
                                            education = details!!.education.filterNot { it.id == record.id },
                                        )
                                    } catch (e: Exception) {
                                        error = e.message ?: "Could not delete education"
                                    }
                                }
                            },
                        )
                        if (index < education.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = NovoraBorder,
                            )
                        }
                    }
                }
                TextButton(onClick = { showAddEducation = true }) { Text("Add education") }
            }

            ProfileSectionCard(title = "Benefits") {
                if (benefits.isEmpty()) {
                    EmptyHint("No benefit enrollments from API.")
                } else {
                    benefits.forEachIndexed { index, benefit ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = DisplayText.orDash(benefit.planName),
                                fontWeight = FontWeight.SemiBold,
                                color = NovoraInk,
                            )
                            ProfileLabeled("Status", benefit.status)
                            ProfileLabeled("Enrolled", benefit.enrolledAt.take(10))
                        }
                        if (index < benefits.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = NovoraBorder,
                            )
                        }
                    }
                }
            }

            ProfileSectionCard(title = "Documents") {
                if (documents.isEmpty()) {
                    EmptyHint("No documents uploaded yet.")
                } else {
                    documents.forEachIndexed { index, doc ->
                        DocumentRow(
                            doc = doc,
                            opening = openingDocId == doc.id,
                            onOpen = { openDocument(doc) },
                            onDelete = {
                                scope.launch {
                                    try {
                                        withContext(Dispatchers.IO) { repo.deleteDocument(doc.id) }
                                        documents = documents.filterNot { it.id == doc.id }
                                    } catch (e: Exception) {
                                        error = e.message ?: "Could not delete document"
                                    }
                                }
                            },
                        )
                        if (index < documents.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = NovoraBorder,
                            )
                        }
                    }
                }
            }
        }

        if (!error.isNullOrBlank() && DisplayText.orEmpty(error).isNotBlank()) {
            Text(
                text = DisplayText.orEmpty(error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
            TextButton(onClick = { reload() }) {
                Text("Retry")
            }
        }

        SecondaryActionButton(
            text = "Sign out",
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
        )
    }

    if (showEditor && details != null) {
        PersonalProfileEditorDialog(
            initial = details!!,
            repo = repo,
            onDismiss = { showEditor = false },
            onSaved = {
                showEditor = false
                reload()
            },
        )
    }

    if (showAddFamily) {
        SimpleTwoFieldDialog(
            title = "Add family member",
            field1 = "Name",
            field2 = "Relationship",
            onDismiss = { showAddFamily = false },
            onSubmit = { name, relationship ->
                scope.launch {
                    try {
                        val created = withContext(Dispatchers.IO) {
                            repo.createFamily(name, relationship, "", "")
                        }
                        details = details?.copy(family = details!!.family + created)
                        showAddFamily = false
                    } catch (e: Exception) {
                        error = e.message ?: "Could not add family member"
                    }
                }
            },
        )
    }

    if (showAddEducation) {
        SimpleTwoFieldDialog(
            title = "Add education",
            field1 = "Institution",
            field2 = "Degree",
            onDismiss = { showAddEducation = false },
            onSubmit = { institution, degree ->
                scope.launch {
                    try {
                        val created = withContext(Dispatchers.IO) {
                            repo.createEducation(institution, degree, "", "", "", "")
                        }
                        details = details?.copy(education = details!!.education + created)
                        showAddEducation = false
                    } catch (e: Exception) {
                        error = e.message ?: "Could not add education"
                    }
                }
            },
        )
    }
}

@Composable
private fun ProfileHeaderCard(
    name: String,
    initials: String,
    jobTitle: String,
    department: String,
    email: String,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = NovoraCard,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NovoraBorder, RoundedCornerShape(16.dp)),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(NovoraInfoSoft),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NovoraBlue,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = DisplayText.orDash(name),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NovoraInk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitle = listOf(jobTitle, department)
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
                    .joinToString(" · ")
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NovoraSlate,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = DisplayText.orDash(email),
                    style = MaterialTheme.typography.bodySmall,
                    color = NovoraSlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = NovoraCard,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NovoraBorder, RoundedCornerShape(16.dp)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NovoraBlue,
            )
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
private fun DocumentRow(
    doc: DocumentItem,
    opening: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NovoraInfoSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                tint = NovoraBlue,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = DisplayText.orDash(doc.name),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = NovoraInk,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = listOf(
                    DisplayText.orDash(doc.docType),
                    doc.uploadedAt.take(10).takeIf { it.isNotBlank() },
                ).filterNotNull().joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = NovoraSlate,
            )
        }
        TextButton(onClick = onOpen, enabled = !opening) {
            if (opening) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = NovoraBlue,
                )
            } else {
                Text("Open")
            }
        }
        TextButton(onClick = onDelete) {
            Text("Delete", color = NovoraDanger)
        }
    }
}

@Composable
private fun FamilyRow(member: FamilyMember, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = DisplayText.orDash(member.name),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = NovoraInk,
            )
            ProfileLabeled("Relationship", member.relationship)
            ProfileLabeled("Date of birth", member.dateOfBirth)
            ProfileLabeled("Phone", member.phone)
        }
        TextButton(onClick = onDelete) { Text("Delete", color = NovoraDanger) }
    }
}

@Composable
private fun EducationRow(record: EducationRecord, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = DisplayText.orDash(record.institution),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = NovoraInk,
            )
            ProfileLabeled("Degree", record.degree)
            ProfileLabeled("Field", record.fieldOfStudy)
            val dates = listOf(record.startDate, record.endDate)
                .map { it.take(10) }
                .filter { it.isNotBlank() }
                .joinToString(" – ")
            ProfileLabeled("Period", dates)
            ProfileLabeled("Grade", record.grade)
        }
        TextButton(onClick = onDelete) { Text("Delete", color = NovoraDanger) }
    }
}

@Composable
private fun SimpleTwoFieldDialog(
    title: String,
    field1: String,
    field2: String,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit,
) {
    var value1 by remember { mutableStateOf("") }
    var value2 by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = androidx.compose.ui.graphics.Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, NovoraBorder),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(title, fontWeight = FontWeight.Bold, color = NovoraInk, style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = value1,
                    onValueChange = { value1 = it },
                    label = { Text(field1) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = value2,
                    onValueChange = { value2 = it },
                    label = { Text(field2) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = { onSubmit(value1.trim(), value2.trim()) },
                    enabled = value1.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save")
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = NovoraSlate,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
private fun PersonalProfileEditorDialog(
    initial: MyProfileDetails,
    repo: WorkRepository,
    onDismiss: () -> Unit,
    onSaved: (MyProfileDetails) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var phone by remember { mutableStateOf(initial.phone) }
    var addressLine1 by remember { mutableStateOf(initial.addressLine1) }
    var addressLine2 by remember { mutableStateOf(initial.addressLine2) }
    var city by remember { mutableStateOf(initial.city) }
    var state by remember { mutableStateOf(initial.state) }
    var postalCode by remember { mutableStateOf(initial.postalCode) }
    var country by remember { mutableStateOf(initial.country) }
    var emergencyName by remember { mutableStateOf(initial.emergencyContactName) }
    var emergencyPhone by remember { mutableStateOf(initial.emergencyContactPhone) }
    var otpCode by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var debugHint by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var sendingOtp by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Edit personal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "HR manages name and job details. Personal contact changes need a verification code.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    placeholder = { Text("e.g. +95 9 123 456 789") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = addressLine1,
                    onValueChange = { addressLine1 = it },
                    label = { Text("Address line 1") },
                    placeholder = { Text("Street address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = addressLine2,
                    onValueChange = { addressLine2 = it },
                    label = { Text("Address line 2") },
                    placeholder = { Text("Apartment, suite (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    placeholder = { Text("City") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text("State / Region") },
                    placeholder = { Text("State or region") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = postalCode,
                    onValueChange = { postalCode = it },
                    label = { Text("Postal code") },
                    placeholder = { Text("Postal / ZIP code") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country") },
                    placeholder = { Text("Country") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = emergencyName,
                    onValueChange = { emergencyName = it },
                    label = { Text("Emergency contact") },
                    placeholder = { Text("Contact full name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = emergencyPhone,
                    onValueChange = { emergencyPhone = it },
                    label = { Text("Emergency phone") },
                    placeholder = { Text("Emergency contact phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            sendingOtp = true
                            error = null
                            statusMessage = null
                            debugHint = null
                            try {
                                val result = withContext(Dispatchers.IO) { repo.requestPersonalOtp() }
                                statusMessage = result.second
                                debugHint = result.third
                            } catch (e: Exception) {
                                error = e.message ?: "Could not send code"
                            }
                            sendingOtp = false
                        }
                    },
                    enabled = !sendingOtp && !saving,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (sendingOtp) {
                        CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Send verification code")
                    }
                }

                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { otpCode = it.filter { ch -> ch.isDigit() }.take(6) },
                    label = { Text("Verification code") },
                    placeholder = { Text("6-digit code") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                statusMessage?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                debugHint?.let {
                    Text("Dev code: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                }
                error?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = {
                        scope.launch {
                            saving = true
                            error = null
                            try {
                                val updated = withContext(Dispatchers.IO) {
                                    repo.updatePersonal(
                                        otpCode = otpCode,
                                        phone = phone,
                                        addressLine1 = addressLine1,
                                        addressLine2 = addressLine2,
                                        city = city,
                                        state = state,
                                        postalCode = postalCode,
                                        country = country,
                                        emergencyContactName = emergencyName,
                                        emergencyContactPhone = emergencyPhone,
                                    )
                                }
                                onSaved(updated)
                            } catch (e: Exception) {
                                error = e.message ?: "Save failed"
                            }
                            saving = false
                        }
                    },
                    enabled = !saving && otpCode.length == 6,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (saving) "Saving…" else "Save")
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun ProfileLabeled(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = NovoraSlate,
            maxLines = 1,
        )
        Text(
            text = DisplayText.orDash(value),
            style = MaterialTheme.typography.bodyLarge,
            color = NovoraInk,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun roleLabel(role: String?): String {
    if (role.isNullOrBlank()) return "Employee"
    return role.lowercase()
        .split('_')
        .joinToString(" ") { part ->
            part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}
