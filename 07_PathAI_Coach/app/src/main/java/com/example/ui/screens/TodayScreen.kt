package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyLearningStats
import com.example.data.model.FirestoreTask
import com.example.data.model.LearningModule
import com.example.data.model.LearningPath
import com.example.data.model.UserProfile
import com.example.ui.theme.*

@Composable
fun TodayScreen(
    userProfile: UserProfile?,
    activePath: LearningPath?,
    currentModule: LearningModule?,
    firestoreTasks: List<FirestoreTask>,
    dailyStats: DailyLearningStats,
    isAILoading: Boolean,
    onToggleTask: (FirestoreTask) -> Unit,
    onGenerateMoreTasks: () -> Unit,
    onAskTutor: (LearningModule) -> Unit,
    onTakeQuiz: (LearningModule) -> Unit,
    onNavigateToLearning: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = firestoreTasks.count { it.status == "done" }
    val totalCount = firestoreTasks.size
    val totalMins = firestoreTasks.sumOf { it.estimate_minutes }
    val completedMins = firestoreTasks.filter { it.status == "done" }.sumOf { it.estimate_minutes }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp)
    ) {
        // Active Learning Module & Tutor Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.5.dp, LavenderPrimary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Top tag: Path name + Switcher link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = ActivePillBg,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "${activePath?.name ?: "Python"} Track",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ActivePillText
                            )
                        }

                        TextButton(
                            onClick = onNavigateToLearning,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "All Paths →",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = LavenderPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Current Module Title
                    Text(
                        text = currentModule?.title ?: "Foundations & Syntax",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = LavenderHeader
                    )

                    currentModule?.let { mod ->
                        Text(
                            text = mod.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLightSecondary,
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Module Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Module Progress",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextLightSecondary
                        )
                        Text(
                            text = "${dailyStats.currentModuleProgress}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldNeon
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { dailyStats.currentModuleProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = LavenderPrimary,
                        trackColor = CharcoalBorder
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Interactive Action Buttons: "Ask Tutor" & "Take Quiz"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        currentModule?.let { mod ->
                            Button(
                                onClick = { onAskTutor(mod) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "Ask Tutor",
                                    tint = DeepPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ask Tutor",
                                    fontWeight = FontWeight.Bold,
                                    color = DeepPurple,
                                    fontSize = 13.sp
                                )
                            }

                            OutlinedButton(
                                onClick = { onTakeQuiz(mod) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, LavenderPrimary.copy(alpha = 0.8f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = LavenderPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Quiz,
                                    contentDescription = "Quiz",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Module Quiz",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Daily Sprint Quick Stats Ribbon
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Streak Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = BorderStroke(1.dp, CharcoalBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AmberNeon.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = AmberNeon,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "${dailyStats.currentStreakDays} Days",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = LavenderHeader
                            )
                            Text(
                                text = "Daily Streak",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextLightSecondary
                            )
                        }
                    }
                }

                // Completed Today Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = BorderStroke(1.dp, CharcoalBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(EmeraldNeon.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Done",
                                tint = EmeraldNeon,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "$completedCount / $totalCount Done",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (completedCount == totalCount && totalCount > 0) EmeraldNeon else LavenderHeader
                            )
                            Text(
                                text = "${completedMins}m / ${totalMins}m",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextLightSecondary
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Today's Tasks
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S ATOMIC DELIVERABLES (${firestoreTasks.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = TextLightSecondary
                )

                TextButton(
                    onClick = onGenerateMoreTasks,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Replenish",
                        tint = LavenderPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Replenish Tasks",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LavenderPrimary
                    )
                }
            }
        }

        // List of Daily Firestore Tasks
        if (firestoreTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = BorderStroke(1.dp, CharcoalBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = LavenderPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "All tasks completed or none scheduled!",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = LavenderHeader
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Generate 2–4 targeted tasks from your active module to continue learning.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLightSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onGenerateMoreTasks,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary)
                        ) {
                            Text("Generate Today's Tasks", color = DeepPurple, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(firestoreTasks) { task ->
                val isDone = task.status == "done"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleTask(task) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDone) CharcoalSurface.copy(alpha = 0.6f) else CharcoalSurface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isDone) EmeraldNeon.copy(alpha = 0.4f) else CharcoalBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Checkbox(
                            checked = isDone,
                            onCheckedChange = { onToggleTask(task) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = EmeraldNeon,
                                uncheckedColor = TextLightSecondary,
                                checkmarkColor = DeepPurple
                            )
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    color = if (isDone) TextLightSecondary else LavenderHeader
                                )

                                Surface(
                                    color = CharcoalBg,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${task.estimate_minutes} min",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = if (isDone) TextLightSecondary else CyanNeon
                                    )
                                }
                            }

                            if (task.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = TextLightSecondary,
                                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when (task.task_type) {
                                                "QUIZ" -> AmberNeon.copy(alpha = 0.15f)
                                                "CODING" -> LavenderPrimary.copy(alpha = 0.15f)
                                                "PROJECT" -> CyanNeon.copy(alpha = 0.15f)
                                                else -> EmeraldNeon.copy(alpha = 0.15f)
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = task.task_type,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = when (task.task_type) {
                                            "QUIZ" -> AmberNeon
                                            "CODING" -> LavenderPrimary
                                            "PROJECT" -> CyanNeon
                                            else -> EmeraldNeon
                                        }
                                    )
                                }

                                if (isDone) {
                                    Text(
                                        text = "✓ Completed",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = EmeraldNeon
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
