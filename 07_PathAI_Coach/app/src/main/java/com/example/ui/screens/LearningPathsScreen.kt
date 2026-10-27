package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun LearningPathsScreen(
    paths: List<LearningPath>,
    modules: List<LearningModule>,
    activePathId: String,
    userProgress: List<UserProgressDoc>,
    dailyStats: DailyLearningStats,
    pathStatsList: List<PathCompletionStats>,
    isLoading: Boolean,
    onSelectPath: (String) -> Unit,
    onAskTutor: (LearningModule) -> Unit,
    onTakeQuiz: (LearningModule) -> Unit,
    onNavigateToToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Curriculum & Modules, 1: Progress & Insights
    var selectedPathId by remember(activePathId) { mutableStateOf(activePathId) }
    var pathSwitchConfirmDialog by remember { mutableStateOf<LearningPath?>(null) }
    var expandedModuleId by remember { mutableStateOf<String?>(null) }

    val currentPath = paths.firstOrNull { it.id == selectedPathId } ?: paths.firstOrNull()
    val pathModules = modules.filter { it.path_id == selectedPathId }.sortedBy { it.order_index }

    // Path Switch Confirmation Dialog
    pathSwitchConfirmDialog?.let { targetPath ->
        AlertDialog(
            onDismissRequest = { pathSwitchConfirmDialog = null },
            title = {
                Text(
                    text = "Switch Primary Learning Path?",
                    color = LavenderHeader,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Set '${targetPath.name}' as your active track? Your progress in other paths is securely preserved in Firebase, and your daily 'Today' tasks will update to focus on this track.",
                    color = TextLightPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSelectPath(targetPath.id)
                        pathSwitchConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary)
                ) {
                    Text("Confirm Switch", color = DeepPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pathSwitchConfirmDialog = null }) {
                    Text("Cancel", color = TextLightSecondary)
                }
            },
            containerColor = CharcoalSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp)
    ) {
        // Screen Header & Mode Toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CharcoalBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Structured Learning Paths",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = LavenderHeader
                            )
                            Text(
                                text = "Sequential curriculum grounded in industry competencies",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextLightSecondary
                            )
                        }

                        // Today shortcut button
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = ActivePillBg,
                            modifier = Modifier.clickable { onNavigateToToday() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Today,
                                    contentDescription = "Today",
                                    tint = ActivePillText,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Today",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ActivePillText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Secondary Sub-tab Navigation: Curriculum vs Insights
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CharcoalBg)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SubTabPill(
                            title = "Curriculum & Modules",
                            icon = Icons.Default.MenuBook,
                            isSelected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            modifier = Modifier.weight(1f)
                        )
                        SubTabPill(
                            title = "Progress & Insights",
                            icon = Icons.Default.Insights,
                            isSelected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        if (selectedTab == 0) {
            // --- CURRICULUM & MODULES VIEW ---

            // Path Selector Horizontal Row
            item {
                Text(
                    text = "SELECT LEARNING PATH",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = TextLightSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    paths.forEach { path ->
                        val isSelected = path.id == selectedPathId
                        val isPrimary = path.id == activePathId

                        Card(
                            modifier = Modifier
                                .width(200.dp)
                                .clickable { selectedPathId = path.id },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) CharcoalSurfaceVariant else CharcoalSurface
                            ),
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) LavenderPrimary else CharcoalBorder
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = path.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) LavenderHeader else TextLightPrimary
                                    )
                                    if (isPrimary) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(EmeraldNeon.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp
                                                ),
                                                color = EmeraldNeon
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = path.description,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextLightSecondary,
                                    maxLines = 2
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${path.modules.size} Modules",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextLightSecondary
                                    )
                                    Text(
                                        text = "${path.total_estimated_hours}h total",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = LavenderPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Primary Track Switcher / Status Banner
            item {
                currentPath?.let { path ->
                    val isCurrentPrimary = path.id == activePathId

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrentPrimary) CharcoalSurface else CharcoalSurfaceVariant
                        ),
                        border = BorderStroke(1.dp, CharcoalBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${path.name} Track (${path.total_estimated_hours} hrs)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LavenderHeader
                                )
                                Text(
                                    text = if (isCurrentPrimary)
                                        "Primary active path. Daily sprint tasks are synced to this track."
                                    else
                                        "Viewing curriculum. Tap switch to make this your primary track.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextLightSecondary
                                )
                            }

                            if (!isCurrentPrimary) {
                                Button(
                                    onClick = { pathSwitchConfirmDialog = path },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary)
                                ) {
                                    Text(
                                        text = "Set Primary",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = DeepPurple
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Modules Header
            item {
                Text(
                    text = "MODULES IN SEQUENCE (${pathModules.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = TextLightSecondary
                )
            }

            // Modules List
            items(pathModules) { module ->
                val progressDoc = userProgress.firstOrNull { it.module_id == module.id }
                val status = progressDoc?.status ?: if (module.order_index == 0 && selectedPathId == activePathId) "in_progress" else "locked"
                val score = progressDoc?.score ?: 0
                val isExpanded = expandedModuleId == module.id

                ModuleItemCard(
                    module = module,
                    status = status,
                    score = score,
                    isExpanded = isExpanded,
                    onToggleExpand = {
                        expandedModuleId = if (isExpanded) null else module.id
                    },
                    onAskTutor = { onAskTutor(module) },
                    onTakeQuiz = { onTakeQuiz(module) }
                )
            }
        } else {
            // --- PROGRESS & INSIGHTS VIEW ---

            // Streak & All-Time Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Daily Streak Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                        border = BorderStroke(1.dp, CharcoalBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Daily Streak",
                                tint = AmberNeon,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${dailyStats.currentStreakDays} Days",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = LavenderHeader
                            )
                            Text(
                                text = "Current Streak",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextLightSecondary
                            )
                        }
                    }

                    // Total Tasks Completed Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                        border = BorderStroke(1.dp, CharcoalBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed Tasks",
                                tint = EmeraldNeon,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${dailyStats.totalTasksCompleted}",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = LavenderHeader
                            )
                            Text(
                                text = "Tasks Completed",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextLightSecondary
                            )
                        }
                    }
                }
            }

            // Active Track Velocity Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = BorderStroke(1.dp, CharcoalBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Current Focus: ${dailyStats.activePathName}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = LavenderHeader
                                )
                                Text(
                                    text = "Active Module: ${dailyStats.currentModuleTitle}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextLightSecondary
                                )
                            }

                            Text(
                                text = "${dailyStats.currentModuleProgress}%",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldNeon
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { dailyStats.currentModuleProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = LavenderPrimary,
                            trackColor = CharcoalBorder
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Today's Deliverables: ${dailyStats.todayCompletedTasks}/${dailyStats.todayTotalTasks}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextLightSecondary
                            )
                            Text(
                                text = if (dailyStats.todayCompletedTasks >= dailyStats.todayTotalTasks && dailyStats.todayTotalTasks > 0)
                                    "Daily Goal Met! 🔥"
                                else "${(dailyStats.todayTotalTasks - dailyStats.todayCompletedTasks).coerceAtLeast(0)} remaining",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (dailyStats.todayCompletedTasks >= dailyStats.todayTotalTasks && dailyStats.todayTotalTasks > 0) EmeraldNeon else CyanNeon
                            )
                        }
                    }
                }
            }

            // Path Completion Breakdown
            item {
                Text(
                    text = "LEARNING PATH COMPLETION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = TextLightSecondary
                )
            }

            items(pathStatsList) { stat ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = BorderStroke(
                        1.dp,
                        if (stat.isPrimary) LavenderPrimary.copy(alpha = 0.5f) else CharcoalBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stat.path.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = LavenderHeader
                                )
                                if (stat.isPrimary) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(LavenderPrimary.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "PRIMARY",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = LavenderPrimary
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${stat.percentCompleted}%",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (stat.percentCompleted == 100) EmeraldNeon else TextLightPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { stat.percentCompleted / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (stat.percentCompleted == 100) EmeraldNeon else LavenderPrimary,
                            trackColor = CharcoalBorder
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${stat.completedModules}/${stat.totalModules} modules completed",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextLightSecondary
                            )
                            Text(
                                text = "${stat.path.total_estimated_hours}h estimated",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextLightSecondary
                            )
                        }
                    }
                }
            }

            // Milestone Journey Map
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = BorderStroke(1.dp, CharcoalBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Roadmap Sequence Milestones",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = LavenderHeader
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val journeySteps = listOf(
                            "1. Python Foundations & Systems" to "Basics, OOP, Async APIs, Testing",
                            "2. AI/ML Core & PyTorch" to "Linear Algebra, Supervised, Deep Learning",
                            "3. Modern LLM & RAG Systems" to "Prompting, Vector DBs, Hybrid Retrieval",
                            "4. Portfolio Capstone Projects" to "Production RAG, LoRA Fine-tuning, MLOps"
                        )

                        journeySteps.forEachIndexed { idx, step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (idx == 0) LavenderPrimary else CharcoalSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (idx == 0) DeepPurple else TextLightSecondary
                                    )
                                }
                                Column {
                                    Text(
                                        text = step.first,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextLightPrimary
                                    )
                                    Text(
                                        text = step.second,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = TextLightSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuleItemCard(
    module: LearningModule,
    status: String,
    score: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAskTutor: () -> Unit,
    onTakeQuiz: () -> Unit
) {
    val isLocked = status == "locked"
    val isInProgress = status == "in_progress"
    val isCompleted = status == "completed"

    val statusBadgeColor = when {
        isCompleted -> EmeraldNeon
        isInProgress -> LavenderPrimary
        else -> TextLightSecondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInProgress) CharcoalSurfaceVariant else CharcoalSurface
        ),
        border = BorderStroke(
            1.dp,
            if (isInProgress) LavenderPrimary.copy(alpha = 0.6f) else CharcoalBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Order + Title + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> EmeraldNeon.copy(alpha = 0.2f)
                                    isInProgress -> LavenderPrimary.copy(alpha = 0.2f)
                                    else -> CharcoalBg
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = EmeraldNeon,
                                modifier = Modifier.size(16.dp)
                            )
                        } else if (isLocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = TextLightSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Text(
                                text = "${module.order_index + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = LavenderPrimary
                            )
                        }
                    }

                    Column {
                        Text(
                            text = module.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isLocked) TextLightSecondary else LavenderHeader
                        )
                        Text(
                            text = "${module.estimated_hours}h estimated • ${module.skills.size} competencies",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = TextLightSecondary
                        )
                    }
                }

                // Status Badge
                Surface(
                    color = statusBadgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = when {
                                isCompleted && score > 0 -> "PASSED ($score%)"
                                isCompleted -> "COMPLETED"
                                isInProgress -> "IN PROGRESS"
                                else -> "LOCKED"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = statusBadgeColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = module.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isLocked) TextLightSecondary.copy(alpha = 0.8f) else TextLightPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Skills Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                module.skills.forEach { skill ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CharcoalBg)
                            .border(0.5.dp, CharcoalBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = skill,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (isLocked) TextLightSecondary else LavenderPrimary
                        )
                    }
                }
            }

            // Expandable Content Notes Preview
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CharcoalBg)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Module Reference & Concept Notes",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LavenderHeader
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = module.content,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = TextLightSecondary
                    )

                    if (module.prerequisites.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Prerequisites: ${module.prerequisites.joinToString(", ")}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = AmberNeon
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: Expand Notes, Ask Tutor, Take Quiz
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onToggleExpand,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Hide Notes ▲" else "View Notes ▼",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextLightSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onAskTutor,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LavenderPrimary),
                        border = BorderStroke(1.dp, LavenderPrimary.copy(alpha = 0.7f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ask Tutor", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (!isLocked) {
                        Button(
                            onClick = onTakeQuiz,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = null,
                                tint = DeepPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isCompleted) "Retake Quiz" else "Take Quiz",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepPurple
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubTabPill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) ActivePillBg else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) ActivePillText else TextLightSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) ActivePillText else TextLightSecondary
            )
        }
    }
}
