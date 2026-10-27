package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(
    currentProfile: UserProfile?,
    isAILoading: Boolean,
    onComplete: (role: String, tier: String, deadline: Int, hours: Int, skills: String, resume: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) }

    var targetRole by remember { mutableStateOf(currentProfile?.targetRole ?: "AIML & GenAI Engineer") }
    var targetTier by remember { mutableStateOf(currentProfile?.targetCompanyTier ?: "Tier 1 Tech & AI Labs") }
    var deadlineWeeks by remember { mutableIntStateOf(currentProfile?.deadlineWeeks ?: 12) }
    var hoursPerDay by remember { mutableIntStateOf(currentProfile?.hoursPerDay ?: 4) }
    var skillsText by remember { mutableStateOf(currentProfile?.skillsText ?: "Python, NumPy, Pandas, Scikit-Learn, PyTorch, Git, CS Fundamentals") }
    var resumeText by remember { mutableStateOf(currentProfile?.resumeSummary ?: "CS Graduate with project building an image classifier and FastAPI backend. Looking to break into AI/ML engineering.") }

    val roleSuggestions = listOf(
        "AIML & GenAI Engineer",
        "MLOps & Platform Engineer",
        "NLP & LLM Specialist",
        "Data Scientist & Classical ML"
    )

    val skillPresets = listOf(
        "Python", "PyTorch", "Transformers", "LangChain", "Vector DBs", "Docker", "FastAPI", "SQL", "DSA Medium"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp)
    ) {
        // Wizard Step Progress Bar
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Step $step of 3: ${
                            when (step) {
                                1 -> "Target Role & Tier"
                                2 -> "Timeline & Daily Commitment"
                                else -> "Skills & Resume Profile"
                            }
                        }",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = CyanNeon
                    )

                    Text(
                        text = "AI Onboarding",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { step.toFloat() / 3f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = CyanNeon,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // STEP 1: TARGET ROLE & COMPANY TIER
        if (step == 1) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "What role are you targeting?",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "PathAI customizes your DSA topics, ML interview loop, and roadmap around this role.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = targetRole,
                            onValueChange = { targetRole = it },
                            label = { Text("Target Role") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Popular Career Tracks:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            roleSuggestions.forEach { role ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { targetRole = role },
                                    color = if (targetRole == role) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = if (targetRole == role) androidx.compose.foundation.BorderStroke(1.dp, CyanNeon) else null
                                ) {
                                    Text(
                                        text = role,
                                        modifier = Modifier.padding(10.dp),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = if (targetRole == role) CyanNeon else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = targetTier,
                            onValueChange = { targetTier = it },
                            label = { Text("Target Company Tier") },
                            placeholder = { Text("e.g. FAANG, AI Labs, Early Stage Startups") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // STEP 2: TIMELINE & HOURS/DAY
        if (step == 2) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Timeline & Daily Commitment",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Set your preparation runway and how many focused hours you can invest daily.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        // Weeks Slider
                        Text(
                            text = "Preparation Deadline: $deadlineWeeks Weeks",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = CyanNeon
                        )
                        Slider(
                            value = deadlineWeeks.toFloat(),
                            onValueChange = { deadlineWeeks = it.toInt() },
                            valueRange = 4f..24f,
                            steps = 19,
                            colors = SliderDefaults.colors(thumbColor = CyanNeon, activeTrackColor = CyanNeon)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Hours/Day Slider
                        Text(
                            text = "Daily Commitment: $hoursPerDay Hours / Day",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldNeon
                        )
                        Slider(
                            value = hoursPerDay.toFloat(),
                            onValueChange = { hoursPerDay = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(thumbColor = EmeraldNeon, activeTrackColor = EmeraldNeon)
                        )

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                text = "Total planned capacity: ${deadlineWeeks * 7 * hoursPerDay} hours of structured prep.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // STEP 3: SKILLS & RESUME TEXT
        if (step == 3) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Your Skills & Resume Background",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Gemini will compare these skills against the target role to identify exact high-priority gaps.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = skillsText,
                            onValueChange = { skillsText = it },
                            label = { Text("Known Skills & Technologies") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Quick Presets:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Multi-chip toggle presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            skillPresets.take(4).forEach { preset ->
                                val hasSkill = skillsText.contains(preset, ignoreCase = true)
                                FilterChip(
                                    selected = hasSkill,
                                    onClick = {
                                        skillsText = if (hasSkill) {
                                            skillsText.replace(preset, "").replace(", ,", ",").trim()
                                        } else {
                                            if (skillsText.isBlank()) preset else "$skillsText, $preset"
                                        }
                                    },
                                    label = { Text(preset, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = resumeText,
                            onValueChange = { resumeText = it },
                            label = { Text("Resume Summary / Projects / Coursework") },
                            placeholder = { Text("Paste academic projects, internships, or degrees...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                        )
                    }
                }
            }
        }

        // Navigation Controls
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step-- },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Back")
                    }
                }

                Button(
                    onClick = {
                        if (step < 3) {
                            step++
                        } else {
                            onComplete(targetRole, targetTier, deadlineWeeks, hoursPerDay, skillsText, resumeText)
                        }
                    },
                    enabled = !isAILoading,
                    modifier = Modifier.weight(if (step == 1) 1f else 1.5f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LavenderPrimary,
                        contentColor = DeepPurple
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isAILoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DeepPurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generating Plan with Gemini...")
                    } else {
                        Text(
                            text = if (step < 3) "Continue" else "Generate My Roadmap & Gaps",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
