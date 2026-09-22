package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JobItem
import com.example.data.model.MatchedJobItem
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

val KANBAN_STAGES = listOf("SAVED", "APPLIED", "INTERVIEW", "OFFER", "REJECTED")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobsScreen(
    // Live Agent Matches
    matchedJobs: List<MatchedJobItem> = emptyList(),
    isSearchingJobs: Boolean = false,
    onNaturalLanguageSearch: (String) -> Unit = {},
    onFilterChange: (role: String, location: String, jobType: String, days: Int) -> Unit = { _, _, _, _ -> },
    onUpdateMatchStatus: (matchId: String, newStatus: String) -> Unit = { _, _ -> },

    // Existing Pipeline & Tailoring
    jobs: List<JobItem> = emptyList(),
    activeJobForTailoring: JobItem? = null,
    tailoredResumeResult: String? = null,
    isAILoading: Boolean = false,
    onMoveJobStatus: (JobItem, String) -> Unit = { _, _ -> },
    onAddNewJob: (company: String, role: String, location: String, salary: String, jd: String) -> Unit = { _, _, _, _, _ -> },
    onDeleteJob: (JobItem) -> Unit = {},
    onOpenTailorModal: (JobItem) -> Unit = {},
    onGenerateTailoredResume: (JobItem) -> Unit = {},
    onCloseTailorModal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeSubTab by remember { mutableStateOf(0) } // 0: Live Agent Matches, 1: Application Kanban

    // Search and filter state
    var searchQuery by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("ALL") }
    var selectedLocation by remember { mutableStateOf("ALL") }
    var selectedJobType by remember { mutableStateOf("ALL") }
    var selectedDays by remember { mutableStateOf(7) }

    // Selected job for detail dialog
    var selectedJobForDetails by remember { mutableStateOf<MatchedJobItem?>(null) }
    var showAddJobDialog by remember { mutableStateOf(false) }

    val roleOptions = listOf("ALL", "AI/ML", "Data Science", "Python", "Full Stack")
    val locationOptions = listOf("ALL", "Remote", "Bengaluru", "Hyderabad", "Pune")
    val jobTypeOptions = listOf("ALL", "Intern", "Fresher", "Apprentice")
    val daysOptions = listOf(3, 7, 14)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Sub-Tab Switcher (Live Job Agent vs Kanban Tracker)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Live Job Search Agent (${matchedJobs.size})", fontWeight = FontWeight.Bold)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LavenderPrimary,
                    selectedLabelColor = DeepPurple,
                    containerColor = CharcoalSurfaceVariant,
                    labelColor = TextLightSecondary
                )
            )

            FilterChip(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.ViewKanban, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pipeline Kanban (${jobs.size})", fontWeight = FontWeight.Bold)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LavenderPrimary,
                    selectedLabelColor = DeepPurple,
                    containerColor = CharcoalSurfaceVariant,
                    labelColor = TextLightSecondary
                )
            )
        }

        if (activeSubTab == 0) {
            // ================= LIVE JOB SEARCH AGENT VIEW =================
            // Natural Language Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "e.g. Show me remote ML intern jobs posted in last 3 days",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLightMuted
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { onNaturalLanguageSearch(searchQuery) },
                        modifier = Modifier.testTag("job_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Jobs",
                            tint = LavenderPrimary
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("job_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LavenderPrimary,
                    unfocusedBorderColor = CharcoalBorder,
                    focusedTextColor = TextLightPrimary,
                    unfocusedTextColor = TextLightPrimary,
                    focusedContainerColor = CharcoalSurface,
                    unfocusedContainerColor = CharcoalSurface
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Scrollable Row
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Role filter
                roleOptions.forEach { role ->
                    FilterChip(
                        selected = selectedRole == role,
                        onClick = {
                            selectedRole = role
                            onFilterChange(selectedRole, selectedLocation, selectedJobType, selectedDays)
                        },
                        label = { Text(if (role == "ALL") "Role: All" else role, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LavenderPrimary.copy(alpha = 0.25f),
                            selectedLabelColor = LavenderHeader,
                            containerColor = CharcoalSurfaceVariant,
                            labelColor = TextLightSecondary
                        )
                    )
                }

                // Location filter
                locationOptions.filter { it != "ALL" }.forEach { loc ->
                    FilterChip(
                        selected = selectedLocation == loc,
                        onClick = {
                            selectedLocation = if (selectedLocation == loc) "ALL" else loc
                            onFilterChange(selectedRole, selectedLocation, selectedJobType, selectedDays)
                        },
                        label = { Text(loc, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanNeon.copy(alpha = 0.25f),
                            selectedLabelColor = CyanNeon,
                            containerColor = CharcoalSurfaceVariant,
                            labelColor = TextLightSecondary
                        )
                    )
                }

                // Job Type filter
                jobTypeOptions.filter { it != "ALL" }.forEach { type ->
                    FilterChip(
                        selected = selectedJobType == type,
                        onClick = {
                            selectedJobType = if (selectedJobType == type) "ALL" else type
                            onFilterChange(selectedRole, selectedLocation, selectedJobType, selectedDays)
                        },
                        label = { Text(type, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldNeon.copy(alpha = 0.25f),
                            selectedLabelColor = EmeraldNeon,
                            containerColor = CharcoalSurfaceVariant,
                            labelColor = TextLightSecondary
                        )
                    )
                }

                // Days filter
                daysOptions.forEach { days ->
                    FilterChip(
                        selected = selectedDays == days,
                        onClick = {
                            selectedDays = days
                            onFilterChange(selectedRole, selectedLocation, selectedJobType, selectedDays)
                        },
                        label = { Text("< ${days}d", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberNeon.copy(alpha = 0.25f),
                            selectedLabelColor = AmberNeon,
                            containerColor = CharcoalSurfaceVariant,
                            labelColor = TextLightSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isSearchingJobs) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = LavenderPrimary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Agent matching fresher & intern roles...", style = MaterialTheme.typography.bodySmall, color = LavenderHeader)
                    }
                }
            } else if (matchedJobs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.WorkOutline, contentDescription = null, tint = TextLightSecondary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No jobs found matching current filters", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextLightPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Try clearing filters or changing search keywords.", style = MaterialTheme.typography.bodySmall, color = TextLightSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(matchedJobs, key = { it.match.id }) { item ->
                        MatchedJobCard(
                            item = item,
                            onViewDetails = { selectedJobForDetails = item },
                            onApply = {
                                onUpdateMatchStatus(item.match.id, "applied")
                                if (item.post.apply_url.isNotBlank()) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.post.apply_url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.util.Log.e("JobsScreen", "Failed opening link", e)
                                    }
                                }
                            },
                            onIgnore = {
                                onUpdateMatchStatus(item.match.id, "ignored")
                            }
                        )
                    }
                }
            }
        } else {
            // ================= KANBAN TRACKER VIEW =================
            KanbanTrackerView(
                jobs = jobs,
                onMoveJobStatus = onMoveJobStatus,
                onAddNewJobClick = { showAddJobDialog = true },
                onDeleteJob = onDeleteJob,
                onOpenTailorModal = onOpenTailorModal
            )
        }
    }

    // Detail Dialog
    selectedJobForDetails?.let { matchedItem ->
        AlertDialog(
            onDismissRequest = { selectedJobForDetails = null },
            title = {
                Text(
                    text = matchedItem.post.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextLightPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${matchedItem.post.company} • ${matchedItem.post.location} • ${matchedItem.post.job_type}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = LavenderPrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldNeon.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, EmeraldNeon.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldNeon, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${matchedItem.match.match_score}% Match: ${matchedItem.match.why_it_matches}",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldNeon
                            )
                        }
                    }

                    Text("AI Summary:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = TextLightPrimary)
                    Text(matchedItem.post.ai_summary.ifBlank { matchedItem.post.description }, style = MaterialTheme.typography.bodySmall, color = TextLightSecondary)

                    if (matchedItem.post.required_skills.isNotEmpty()) {
                        Text("Required Skills:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = TextLightPrimary)
                        Text(matchedItem.post.required_skills.joinToString(", "), style = MaterialTheme.typography.bodySmall, color = TextLightSecondary)
                    }

                    Text("Source: ${matchedItem.post.source} • Level: ${matchedItem.post.experience_level}", style = MaterialTheme.typography.labelSmall, color = AmberNeon)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateMatchStatus(matchedItem.match.id, "applied")
                        selectedJobForDetails = null
                        if (matchedItem.post.apply_url.isNotBlank()) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(matchedItem.post.apply_url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.util.Log.e("JobsScreen", "Error opening apply url", e)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary, contentColor = DeepPurple)
                ) {
                    Text("Apply Now ↗")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedJobForDetails = null }) {
                    Text("Close", color = TextLightSecondary)
                }
            },
            containerColor = CharcoalSurface
        )
    }

    // Resume Tailor Dialog
    if (activeJobForTailoring != null) {
        AlertDialog(
            onDismissRequest = onCloseTailorModal,
            title = {
                Text(
                    text = "Tailor Resume for ${activeJobForTailoring.company}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextLightPrimary
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Role: ${activeJobForTailoring.role}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = LavenderPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isAILoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = LavenderPrimary)
                    } else if (!tailoredResumeResult.isNullOrBlank()) {
                        Text(tailoredResumeResult, style = MaterialTheme.typography.bodySmall, color = TextLightSecondary)
                    } else {
                        Text("Click Generate to compare your profile against this job description and draft tailored impact bullets.", style = MaterialTheme.typography.bodySmall, color = TextLightSecondary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { onGenerateTailoredResume(activeJobForTailoring) },
                    enabled = !isAILoading,
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary, contentColor = DeepPurple)
                ) {
                    Text("Generate")
                }
            },
            dismissButton = {
                TextButton(onClick = onCloseTailorModal) {
                    Text("Close", color = TextLightSecondary)
                }
            },
            containerColor = CharcoalSurface
        )
    }
}

@Composable
fun MatchedJobCard(
    item: MatchedJobItem,
    onViewDetails: () -> Unit,
    onApply: () -> Unit,
    onIgnore: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("job_match_card_${item.match.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.match.status == "ignored") CharcoalSurfaceVariant.copy(alpha = 0.5f) else CharcoalSurface
        ),
        border = BorderStroke(
            1.dp,
            if (item.match.status == "applied") EmeraldNeon.copy(alpha = 0.5f) else CharcoalBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Title, Job Type badge, Match Score badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.post.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextLightPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.post.company} • ${item.post.location}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = LavenderHeader
                    )
                }

                // Match Score Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (item.match.match_score >= 85) EmeraldNeon.copy(alpha = 0.2f) else LavenderPrimary.copy(alpha = 0.2f),
                    border = BorderStroke(
                        1.dp,
                        if (item.match.match_score >= 85) EmeraldNeon.copy(alpha = 0.4f) else LavenderPrimary.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = "${item.match.match_score}% Match",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (item.match.match_score >= 85) EmeraldNeon else LavenderHeader
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // AI 2-3 line Summary
            Text(
                text = item.post.ai_summary.ifBlank { item.post.description.take(160) + "..." },
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = TextLightSecondary,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tags & Meta Row (Job type, source, days ago)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CharcoalSurfaceVariant
                ) {
                    Text(
                        text = item.post.job_type,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = CyanNeon
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CharcoalSurfaceVariant
                ) {
                    Text(
                        text = item.post.experience_level,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = AmberNeon
                    )
                }

                Text(
                    text = item.post.posted_at,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextLightMuted
                )

                Spacer(modifier = Modifier.weight(1f))

                if (item.match.status != "new") {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (item.match.status == "applied") EmeraldNeon.copy(alpha = 0.2f) else CharcoalSurfaceVariant
                    ) {
                        Text(
                            text = item.match.status.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = if (item.match.status == "applied") EmeraldNeon else TextLightMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    border = BorderStroke(1.dp, CharcoalBorder)
                ) {
                    Text("View Details", style = MaterialTheme.typography.labelSmall, color = TextLightPrimary)
                }

                if (item.match.status != "applied") {
                    Button(
                        onClick = onApply,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LavenderPrimary,
                            contentColor = DeepPurple
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Apply", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }

                if (item.match.status == "new") {
                    IconButton(
                        onClick = onIgnore,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Ignore Job", tint = TextLightMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun KanbanTrackerView(
    jobs: List<JobItem>,
    onMoveJobStatus: (JobItem, String) -> Unit,
    onAddNewJobClick: () -> Unit,
    onDeleteJob: (JobItem) -> Unit,
    onOpenTailorModal: (JobItem) -> Unit
) {
    var selectedStageFilter by remember { mutableStateOf<String?>("ALL") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${jobs.size} Applications Tracked",
                style = MaterialTheme.typography.labelMedium,
                color = TextLightSecondary
            )

            Button(
                onClick = onAddNewJobClick,
                colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary, contentColor = DeepPurple),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Job", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
        }

        // Kanban stages filter
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedStageFilter == "ALL",
                onClick = { selectedStageFilter = "ALL" },
                label = { Text("ALL (${jobs.size})", fontSize = 11.sp) }
            )
            KANBAN_STAGES.forEach { stage ->
                val count = jobs.count { it.status.uppercase() == stage }
                FilterChip(
                    selected = selectedStageFilter == stage,
                    onClick = { selectedStageFilter = stage },
                    label = { Text("$stage ($count)", fontSize = 11.sp) }
                )
            }
        }

        val filteredJobs = if (selectedStageFilter == "ALL") jobs else jobs.filter { it.status.uppercase() == selectedStageFilter }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(filteredJobs, key = { it.id }) { job ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = BorderStroke(1.dp, CharcoalBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(job.role, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextLightPrimary)
                                Text("${job.company} • ${job.location}", style = MaterialTheme.typography.bodySmall, color = LavenderHeader)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = LavenderPrimary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = job.status,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LavenderHeader
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { onOpenTailorModal(job) }) {
                                Text("Tailor Resume ✨", color = LavenderPrimary, style = MaterialTheme.typography.labelSmall)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (job.status != "APPLIED") {
                                    TextButton(onClick = { onMoveJobStatus(job, "APPLIED") }) {
                                        Text("Mark Applied", style = MaterialTheme.typography.labelSmall, color = EmeraldNeon)
                                    }
                                }
                                IconButton(onClick = { onDeleteJob(job) }, modifier = Modifier.size(32.dp)) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RoseNeon, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
