package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    userProfile: UserProfile?,
    firebaseUser: FirebaseUserData?,
    onResetData: () -> Unit,
    onLaunchOnboarding: () -> Unit,
    onSignOut: () -> Unit,
    onUpdateFocusAreas: (focusAreas: List<String>, currentPath: String) -> Unit,
    onSaveFullProfile: (FirebaseUserData) -> Unit = {},
    onTriggerTestAlert: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Education state
    var degree by remember(firebaseUser) { mutableStateOf(firebaseUser?.education?.degree ?: "B.Tech") }
    var major by remember(firebaseUser) { mutableStateOf(firebaseUser?.education?.major ?: "Computer Science & Engineering") }
    var college by remember(firebaseUser) { mutableStateOf(firebaseUser?.education?.college ?: "National Institute of Technology") }
    var gradYear by remember(firebaseUser) { mutableStateOf(firebaseUser?.education?.graduation_year ?: "2025") }
    var cgpa by remember(firebaseUser) { mutableStateOf(firebaseUser?.education?.cgpa ?: "8.6 / 10") }

    // Skills state
    val skillsList = remember(firebaseUser) {
        mutableStateListOf(*(firebaseUser?.skills ?: listOf("Python", "PyTorch", "NumPy", "Pandas", "Scikit-Learn", "Git", "REST APIs")).toTypedArray())
    }
    var newSkillInput by remember { mutableStateOf("") }

    // Experience Level
    var experienceLevel by remember(firebaseUser) { mutableStateOf(firebaseUser?.experience_level ?: "Fresher") }

    // Job Preferences state
    val rolesList = remember(firebaseUser) {
        mutableStateListOf(*(firebaseUser?.job_preferences?.roles ?: listOf("AI/ML Intern", "Python Developer", "Data Science Intern")).toTypedArray())
    }
    var newRoleInput by remember { mutableStateOf("") }

    val locationsList = remember(firebaseUser) {
        mutableStateListOf(*(firebaseUser?.job_preferences?.locations ?: listOf("Remote", "Bengaluru", "Hyderabad")).toTypedArray())
    }
    var newLocationInput by remember { mutableStateOf("") }

    val workModesList = remember(firebaseUser) {
        mutableStateListOf(*(firebaseUser?.job_preferences?.work_mode ?: listOf("Remote", "Hybrid")).toTypedArray())
    }

    var minSalary by remember(firebaseUser) { mutableStateOf(firebaseUser?.job_preferences?.min_stipend_or_salary ?: "₹25,000 / month") }
    var willingToRelocate by remember(firebaseUser) { mutableStateOf(firebaseUser?.job_preferences?.willing_to_relocate ?: true) }
    var resumeUrl by remember(firebaseUser) { mutableStateOf(firebaseUser?.resume_url ?: "") }

    // Notification settings
    var alertsEnabled by remember(firebaseUser) { mutableStateOf(firebaseUser?.notifications?.job_alerts_enabled ?: true) }
    val alertTime by remember(firebaseUser) { mutableStateOf(firebaseUser?.notifications?.alert_time ?: "09:00") }
    val timezone by remember(firebaseUser) { mutableStateOf(firebaseUser?.notifications?.timezone ?: "Asia/Kolkata") }

    var saveNotice by remember { mutableStateOf<String?>(null) }

    // Validation
    val isDegreeValid = degree.isNotBlank()
    val isCollegeValid = college.isNotBlank()
    val isSkillsValid = skillsList.isNotEmpty()
    val isRolesValid = rolesList.isNotEmpty()
    val isProfileValidForAlerts = isDegreeValid && isCollegeValid && isSkillsValid && isRolesValid

    val presetSkills = listOf("Python", "PyTorch", "NumPy", "Pandas", "Scikit-Learn", "TensorFlow", "Git", "SQL", "REST APIs", "FastAPI", "Docker", "LLM / RAG", "Computer Vision")
    val presetRoles = listOf("AI/ML Intern", "Python Developer", "Data Science Intern", "Junior ML Engineer", "Apprentice", "Software Engineer Intern")
    val presetLocations = listOf("Remote", "Bengaluru", "Hyderabad", "Pune", "Mumbai", "Delhi-NCR")
    val expLevels = listOf("Fresher", "Intern", "0–1 YOE", "1–3 YOE")
    val workModeOptions = listOf("Remote", "Hybrid", "In-Office")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp)
    ) {
        // User Account Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("firebase_profile_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, LavenderPrimary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(LavenderPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Firebase User",
                                tint = LavenderPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = firebaseUser?.name.takeIf { !it.isNullOrBlank() } ?: userProfile?.name ?: "AI Candidate",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextLightPrimary
                            )
                            Text(
                                text = firebaseUser?.email.takeIf { !it.isNullOrBlank() } ?: "candidate@pathai.io",
                                style = MaterialTheme.typography.bodySmall,
                                color = LavenderHeader
                            )
                            Text(
                                text = "UID: ${firebaseUser?.uid?.take(12) ?: "CloudUser"}...",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextLightMuted
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = EmeraldNeon.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, EmeraldNeon.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldNeon)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Synced",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = EmeraldNeon
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sign_out_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, RoseNeon.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = RoseNeon.copy(alpha = 0.08f),
                            contentColor = RoseNeon
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = RoseNeon,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign Out of PathAI",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }

        // Section 1: Education Details
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, CharcoalBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.School, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Education Details", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextLightPrimary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = degree,
                            onValueChange = { degree = it },
                            label = { Text("Degree *") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LavenderPrimary,
                                unfocusedBorderColor = CharcoalBorder,
                                focusedTextColor = TextLightPrimary,
                                unfocusedTextColor = TextLightPrimary
                            ),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = gradYear,
                            onValueChange = { gradYear = it },
                            label = { Text("Grad Year *") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LavenderPrimary,
                                unfocusedBorderColor = CharcoalBorder,
                                focusedTextColor = TextLightPrimary,
                                unfocusedTextColor = TextLightPrimary
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = major,
                        onValueChange = { major = it },
                        label = { Text("Major / Department *") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderPrimary,
                            unfocusedBorderColor = CharcoalBorder,
                            focusedTextColor = TextLightPrimary,
                            unfocusedTextColor = TextLightPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = college,
                        onValueChange = { college = it },
                        label = { Text("College / University *") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderPrimary,
                            unfocusedBorderColor = CharcoalBorder,
                            focusedTextColor = TextLightPrimary,
                            unfocusedTextColor = TextLightPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = cgpa,
                        onValueChange = { cgpa = it },
                        label = { Text("CGPA / Percentage") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderPrimary,
                            unfocusedBorderColor = CharcoalBorder,
                            focusedTextColor = TextLightPrimary,
                            unfocusedTextColor = TextLightPrimary
                        ),
                        singleLine = true
                    )
                }
            }
        }

        // Section 2: Experience Level
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, CharcoalBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Experience Level", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextLightPrimary)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        expLevels.forEach { level ->
                            val isSelected = experienceLevel == level
                            FilterChip(
                                selected = isSelected,
                                onClick = { experienceLevel = level },
                                label = { Text(level) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LavenderPrimary,
                                    selectedLabelColor = DeepPurple,
                                    containerColor = CharcoalSurfaceVariant,
                                    labelColor = TextLightSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = CharcoalBorder,
                                    selectedBorderColor = LavenderPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Technical Skills
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, CharcoalBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = EmeraldNeon, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Skills Inventory (${skillsList.size}) *", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextLightPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Select skills or type a custom one. Used by the Job Search Agent for match scoring.", style = MaterialTheme.typography.bodySmall, color = TextLightSecondary)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Selected Skills
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetSkills.forEach { skill ->
                            val isSelected = skillsList.contains(skill)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) skillsList.remove(skill) else skillsList.add(skill)
                                },
                                label = { Text(skill) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldNeon.copy(alpha = 0.2f),
                                    selectedLabelColor = EmeraldNeon,
                                    containerColor = CharcoalSurfaceVariant,
                                    labelColor = TextLightSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = CharcoalBorder,
                                    selectedBorderColor = EmeraldNeon
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Add Custom Skill Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newSkillInput,
                            onValueChange = { newSkillInput = it },
                            placeholder = { Text("Add custom skill (e.g. OpenCV, RAG)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LavenderPrimary,
                                unfocusedBorderColor = CharcoalBorder,
                                focusedTextColor = TextLightPrimary,
                                unfocusedTextColor = TextLightPrimary
                            )
                        )
                        Button(
                            onClick = {
                                if (newSkillInput.isNotBlank() && !skillsList.contains(newSkillInput.trim())) {
                                    skillsList.add(newSkillInput.trim())
                                    newSkillInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary, contentColor = DeepPurple),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }

        // Section 4: Job Preferences
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, CharcoalBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Work, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Job Preferences", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextLightPrimary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Target Roles *", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = TextLightSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetRoles.forEach { role ->
                            val isSelected = rolesList.contains(role)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) rolesList.remove(role) else rolesList.add(role)
                                },
                                label = { Text(role) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LavenderPrimary.copy(alpha = 0.25f),
                                    selectedLabelColor = LavenderHeader,
                                    containerColor = CharcoalSurfaceVariant,
                                    labelColor = TextLightSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = CharcoalBorder,
                                    selectedBorderColor = LavenderPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Preferred Locations", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = TextLightSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetLocations.forEach { loc ->
                            val isSelected = locationsList.contains(loc)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) locationsList.remove(loc) else locationsList.add(loc)
                                },
                                label = { Text(loc) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanNeon.copy(alpha = 0.2f),
                                    selectedLabelColor = CyanNeon,
                                    containerColor = CharcoalSurfaceVariant,
                                    labelColor = TextLightSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Work Mode", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = TextLightSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        workModeOptions.forEach { mode ->
                            val isSelected = workModesList.contains(mode)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) workModesList.remove(mode) else workModesList.add(mode)
                                },
                                label = { Text(mode) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ActivePillBg,
                                    selectedLabelColor = ActivePillText,
                                    containerColor = CharcoalSurfaceVariant,
                                    labelColor = TextLightSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = minSalary,
                        onValueChange = { minSalary = it },
                        label = { Text("Minimum Stipend / Salary Expected") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderPrimary,
                            unfocusedBorderColor = CharcoalBorder,
                            focusedTextColor = TextLightPrimary,
                            unfocusedTextColor = TextLightPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = resumeUrl,
                        onValueChange = { resumeUrl = it },
                        label = { Text("Resume URL (Google Drive / GitHub / PDF)") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderPrimary,
                            unfocusedBorderColor = CharcoalBorder,
                            focusedTextColor = TextLightPrimary,
                            unfocusedTextColor = TextLightPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Willing to Relocate", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextLightPrimary)
                            Text("Open to on-site fresher/intern hubs", style = MaterialTheme.typography.labelSmall, color = TextLightSecondary)
                        }
                        Switch(
                            checked = willingToRelocate,
                            onCheckedChange = { willingToRelocate = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = LavenderPrimary,
                                checkedTrackColor = LavenderPrimary.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }

        // Section 5: 9:00 AM Daily Job Alerts Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, LavenderPrimary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("9:00 AM Daily Job Alerts", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextLightPrimary)
                        }

                        Switch(
                            checked = alertsEnabled,
                            onCheckedChange = { alertsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = LavenderPrimary,
                                checkedTrackColor = LavenderPrimary.copy(alpha = 0.4f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Automated AI alerts scan fresh fresher, intern, apprentice, and student roles every morning at 9:00 AM (every 24-25 hours).",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLightSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CharcoalSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Alert Time", style = MaterialTheme.typography.labelSmall, color = TextLightSecondary)
                                Text("09:00 AM", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = LavenderHeader)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CharcoalSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Timezone", style = MaterialTheme.typography.labelSmall, color = TextLightSecondary)
                                Text("Asia/Kolkata", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = CyanNeon)
                            }
                        }
                    }

                    // Validation Notice
                    if (alertsEnabled && !isProfileValidForAlerts) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmberNeon.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, AmberNeon.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AmberNeon, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "To enable daily alerts, please ensure your Degree, College, at least 1 Skill, and 1 Target Role are filled.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberNeon
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onTriggerTestAlert,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, LavenderPrimary.copy(alpha = 0.5f))
                    ) {
                        Icon(imageVector = Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp), tint = LavenderPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Trigger 9:00 AM Alert (Test Run)", color = LavenderPrimary)
                    }
                }
            }
        }

        // Save Profile Button
        item {
            Button(
                onClick = {
                    val updatedUser = (firebaseUser ?: FirebaseUserData()).copy(
                        education = UserEducation(
                            degree = degree,
                            major = major,
                            college = college,
                            graduation_year = gradYear,
                            cgpa = cgpa
                        ),
                        skills = skillsList.toList(),
                        experience_level = experienceLevel,
                        job_preferences = UserJobPreferences(
                            roles = rolesList.toList(),
                            locations = locationsList.toList(),
                            work_mode = workModesList.toList(),
                            min_stipend_or_salary = minSalary,
                            willing_to_relocate = willingToRelocate
                        ),
                        resume_url = resumeUrl.takeIf { it.isNotBlank() },
                        notifications = NotificationPreferences(
                            job_alerts_enabled = alertsEnabled,
                            alert_time = alertTime,
                            timezone = timezone
                        )
                    )
                    onSaveFullProfile(updatedUser)
                    saveNotice = "Profile & preferences saved successfully!"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_profile_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LavenderPrimary,
                    contentColor = DeepPurple
                )
            ) {
                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Profile & Job Preferences", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }

            if (saveNotice != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = saveNotice!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldNeon,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        // Database & System Reset Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, CharcoalBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Curriculum & Data Reset",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Reset all local roadmap tasks, challenges, and mock interview questions to the fresh seed curriculum.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onResetData,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseNeon),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset to Fresh Seed Curriculum & Challenges")
                    }
                }
            }
        }
    }
}
