package com.example.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.viewmodel.AppNavTab

@Composable
fun DashboardScreen(
    userProfile: UserProfile?,
    tasks: List<TaskItem>,
    skillGaps: List<SkillGap>,
    roadmapWeeks: List<RoadmapWeek>,
    jobs: List<JobItem>,
    practiceItems: List<PracticeItem>,
    nextBestAction: String,
    onNavigateTab: (AppNavTab) -> Unit,
    onToggleTask: (TaskItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val completedTasksCount = tasks.count { it.isCompleted }
    val totalTasksCount = tasks.size.coerceAtLeast(1)
    val taskCompletionRate = (completedTasksCount * 100) / totalTasksCount

    val streakDays = userProfile?.currentStreak ?: 14
    val currentProgressPercent = ((completedTasksCount * 50 / totalTasksCount) + (roadmapWeeks.count { it.isCompleted } * 50 / roadmapWeeks.size.coerceAtLeast(1))).coerceIn(15, 95)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CharcoalBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
    ) {
        // Section 1: Grid of 2 Metric Cards (Streak & Progress)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Streak Card (bg-[#2B2930] p-4 rounded-3xl)
                Surface(
                    modifier = Modifier.weight(1f),
                    color = CharcoalSurface, // #2B2930
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .height(84.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "STREAK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp,
                                fontSize = 11.sp
                            ),
                            color = LavenderPrimary // #D0BCFF
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$streakDays",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                ),
                                color = TextLightPrimary
                            )
                            Text(
                                text = "days",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextLightSecondary, // #938F99
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }

                // Progress Card (bg-[#2B2930] p-4 rounded-3xl)
                Surface(
                    modifier = Modifier.weight(1f),
                    color = CharcoalSurface, // #2B2930
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .height(84.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "PROGRESS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp,
                                fontSize = 11.sp
                            ),
                            color = LavenderPrimary // #D0BCFF
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$currentProgressPercent",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                ),
                                color = TextLightPrimary
                            )
                            Text(
                                text = "%",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextLightSecondary, // #938F99
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Next Best Action Card (bg-[#D0BCFF] text-[#381E72] p-5 rounded-3xl shadow-lg)
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = LavenderPrimary.copy(alpha = 0.25f)),
                color = LavenderPrimary, // #D0BCFF
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = DeepPurple.copy(alpha = 0.12f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "NEXT BEST ACTION",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = DeepPurple // #381E72
                            )
                        }

                        Text(
                            text = "15 mins",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = DeepPurple
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (nextBestAction.isBlank()) "Mock Interview: Transformers & RAG Architecture" else nextBestAction,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            lineHeight = 26.sp
                        ),
                        color = DeepPurple
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Button: w-full py-3 bg-[#381E72] text-white rounded-2xl font-semibold text-sm
                    Button(
                        onClick = { onNavigateTab(AppNavTab.TODAY) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepPurple, // #381E72
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Start Now",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }

        // Section 3: Weekly Roadmap (bg-[#2B2930] p-4 rounded-3xl)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CharcoalSurface, // #2B2930
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Weekly Roadmap",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = LavenderHeader // #EADDFF
                        )

                        Text(
                            text = "VIEW PATH",
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onNavigateTab(AppNavTab.ROADMAP) }
                                .padding(4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = LavenderPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Task Row 1 - Completed item
                        val currentWeek = roadmapWeeks.firstOrNull { !it.isCompleted } ?: roadmapWeeks.firstOrNull()
                        val previousWeek = roadmapWeeks.firstOrNull { it.isCompleted }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onNavigateTab(AppNavTab.ROADMAP) }
                                .padding(vertical = 4.dp)
                        ) {
                            // Vertical pill indicator (w-2 h-12 bg-[#D0BCFF] rounded-full)
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(LavenderPrimary)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Week 1: Algorithmic Foundations",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextLightSecondary // #938F99
                                )
                                Text(
                                    text = "DSA: Trees & Graphs High-Frequency Bar",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = TextLightPrimary
                                )
                            }

                            // Checked badge: bg-[#49454F] text-[#D0BCFF] p-2 rounded-full
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(CharcoalSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = LavenderPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Task Row 2 - Active / Today item
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onNavigateTab(AppNavTab.TODAY) }
                                .padding(vertical = 4.dp)
                        ) {
                            // Vertical pill indicator inactive (w-2 h-12 bg-[#49454F] rounded-full)
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(CharcoalSurfaceVariant)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Week 2: Today's Sprint",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextLightSecondary
                                )
                                Text(
                                    text = "LLM: Fine-Tuning, LoRA & RAG Indexing",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = TextLightPrimary
                                )
                            }

                            // Circle outline: w-6 h-6 border-2 border-[#D0BCFF] rounded-full
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, LavenderPrimary, CircleShape)
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Multi-Agent OS & Tech Radar Panel (bg-[#2B2930] p-4 rounded-3xl)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CharcoalSurface, // #2B2930
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder.copy(alpha = 0.4f))
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
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = LavenderPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Active Agents & Tech Radar",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                ),
                                color = LavenderHeader // #EADDFF
                            )
                        }

                        Text(
                            text = "AGENTS PANEL",
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onNavigateTab(AppNavTab.AGENTS) }
                                .padding(4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = LavenderPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Active 4 Agent Avatars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val agentPills = listOf(
                            Pair("Coach", CyanNeon),
                            Pair("Tutor", VioletLight),
                            Pair("Project", AmberNeon),
                            Pair("Research", EmeraldNeon)
                        )

                        agentPills.forEach { (agentName, color) ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onNavigateTab(AppNavTab.AGENTS) },
                                color = CharcoalSurfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Text(
                                        text = agentName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = TextLightPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Latest Tech Radar Snippet
                    Surface(
                        color = CharcoalSurfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onNavigateTab(AppNavTab.AGENTS) }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LavenderPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Radar,
                                    contentDescription = null,
                                    tint = LavenderPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tech Radar: DeepSeek-R1 & Test-Time Compute",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LavenderHeader,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Research Agent indexed new arXiv breakthroughs",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextLightSecondary,
                                    maxLines = 1
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = LavenderPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section 5: Skill Map Gap (bg-[#2B2930] p-4 rounded-3xl)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CharcoalSurface, // #2B2930
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Skill Map Gap",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = LavenderHeader // #EADDFF
                        )

                        Text(
                            text = "VIEW ALL",
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onNavigateTab(AppNavTab.SKILL_MAP) }
                                .padding(4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = LavenderPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Wrap of Clean Minimalist Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val displayGaps = if (skillGaps.isNotEmpty()) {
                            skillGaps.take(3).map { it.category }
                        } else {
                            listOf("System Design", "Vector DBs", "MLOps")
                        }

                        displayGaps.forEach { gapName ->
                            Surface(
                                color = CharcoalSurfaceVariant, // #49454F
                                shape = RoundedCornerShape(50),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    TextLightSecondary.copy(alpha = 0.25f) // border-[#938F99]/20
                                )
                            ) {
                                Text(
                                    text = gapName,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = TextLightPrimary // #E6E1E5
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 5: Today's Atomic Checklist
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CharcoalSurface, // #2B2930
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Atomic Tasks",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = LavenderHeader
                        )
                        Text(
                            text = "PLANNER (${tasks.size})",
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onNavigateTab(AppNavTab.TODAY) }
                                .padding(4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = LavenderPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    tasks.take(3).forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onToggleTask(task) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { onToggleTask(task) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = LavenderPrimary,
                                    checkmarkColor = DeepPurple,
                                    uncheckedColor = TextLightSecondary.copy(alpha = 0.6f)
                                )
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                    ),
                                    color = if (task.isCompleted) TextLightSecondary else TextLightPrimary
                                )
                                Text(
                                    text = "${task.category} • ${task.estimatedMinutes} mins",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = TextLightSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 6: Quick Launch Shortcuts (Practice & Mock Interviews)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onNavigateTab(AppNavTab.PRACTICE) },
                    color = CharcoalSurface,
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CharcoalSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = "Practice",
                                tint = LavenderPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Practice Hub",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = TextLightPrimary
                        )
                        Text(
                            text = "DSA, Quizzes & Projects",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextLightSecondary
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onNavigateTab(AppNavTab.INTERVIEWS) },
                    color = CharcoalSurface,
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CharcoalSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "Interview",
                                tint = LavenderPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Mock Interview",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = TextLightPrimary
                        )
                        Text(
                            text = "Voice & Text Scoring",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextLightSecondary
                        )
                    }
                }
            }
        }
    }
}
