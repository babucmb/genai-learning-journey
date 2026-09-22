package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PracticeItem
import com.example.data.model.ProjectItem
import com.example.data.model.QuizItem
import com.example.ui.theme.*

enum class PracticeSection(val label: String) {
    DSA("DSA Problem Sets"),
    QUIZZES("ML / LLM Quizzes"),
    PROJECTS("Mini-Projects")
}

@Composable
fun PracticeScreen(
    practiceItems: List<PracticeItem>,
    quizzes: List<QuizItem>,
    projects: List<ProjectItem>,
    onTogglePracticeSolved: (PracticeItem) -> Unit,
    onSubmitQuizAnswer: (quizId: Long, selectedIndex: Int, isCorrect: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf(PracticeSection.DSA) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Section Segmented Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PracticeSection.entries.forEach { section ->
                val isSelected = selectedSection == section
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedSection = section },
                    label = {
                        Text(
                            text = section.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when (section) {
                                PracticeSection.DSA -> Icons.Default.Terminal
                                PracticeSection.QUIZZES -> Icons.Default.Quiz
                                PracticeSection.PROJECTS -> Icons.Default.FolderSpecial
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isSelected) CyanNeon else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        when (selectedSection) {
            PracticeSection.DSA -> {
                DsaProblemList(
                    items = practiceItems.filter { it.category == "DSA" || it.category == "ML_CODING" },
                    onToggleSolved = onTogglePracticeSolved
                )
            }
            PracticeSection.QUIZZES -> {
                QuizList(
                    quizzes = quizzes,
                    onSubmitAnswer = onSubmitQuizAnswer
                )
            }
            PracticeSection.PROJECTS -> {
                ProjectList(projects = projects)
            }
        }
    }
}

@Composable
private fun DsaProblemList(
    items: List<PracticeItem>,
    onToggleSolved: (PracticeItem) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 36.dp)
    ) {
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Checklist,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Master algorithmic patterns & tensor operations tested in tech screens.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(items) { item ->
            DsaProblemCard(item = item, onToggleSolved = { onToggleSolved(item) })
        }
    }
}

@Composable
private fun DsaProblemCard(
    item: PracticeItem,
    onToggleSolved: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showSolution by remember { mutableStateOf(false) }
    var showHints by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DifficultyBadge(difficulty = item.difficulty)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.topic,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = item.isSolved,
                        onCheckedChange = { onToggleSolved() },
                        colors = CheckboxDefaults.colors(checkedColor = EmeraldNeon)
                    )
                    Text(
                        text = if (item.isSolved) "Solved" else "Mark Solved",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.isSolved) EmeraldNeon else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = { showHints = !showHints },
                    colors = ButtonDefaults.textButtonColors(contentColor = AmberNeon)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Hint",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showHints) "Hide Hint" else "Hint")
                }

                TextButton(
                    onClick = {
                        expanded = !expanded
                        if (!expanded) showSolution = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = CyanNeon)
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Code",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (expanded) "Hide Code" else "Starter Code")
                }

                if (expanded) {
                    TextButton(
                        onClick = { showSolution = !showSolution },
                        colors = ButtonDefaults.textButtonColors(contentColor = VioletLight)
                    ) {
                        Text(if (showSolution) "Starter" else "Solution")
                    }
                }
            }

            AnimatedVisibility(visible = showHints) {
                Surface(
                    color = AmberNeon.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = "💡 ${item.hints}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AmberNeon,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Surface(
                        color = Color(0xFF0D1117),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (showSolution) item.solutionCode else item.starterCode,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = if (showSolution) Color(0xFF7EE787) else Color(0xFFC9D1D9),
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    if (item.testCases.isNotBlank()) {
                        Text(
                            text = "Test Case: ${item.testCases}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizList(
    quizzes: List<QuizItem>,
    onSubmitAnswer: (quizId: Long, selectedIndex: Int, isCorrect: Boolean) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 36.dp)
    ) {
        items(quizzes) { quiz ->
            QuizCard(quiz = quiz, onSubmit = { idx, isCorr -> onSubmitAnswer(quiz.id, idx, isCorr) })
        }
    }
}

@Composable
private fun QuizCard(
    quiz: QuizItem,
    onSubmit: (selectedIndex: Int, isCorrect: Boolean) -> Unit
) {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var submitted by remember { mutableStateOf(false) }

    val options = listOf(quiz.optionA, quiz.optionB, quiz.optionC, quiz.optionD)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Surface(
                color = VioletNeon.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = quiz.category.replace("_", " "),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = VioletLight
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = quiz.question,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            options.forEachIndexed { index, optionText ->
                val isChosen = selectedOption == index
                val isCorrect = quiz.correctIndex == index

                val (borderColor, containerColor) = when {
                    submitted && isCorrect -> EmeraldNeon to EmeraldNeon.copy(alpha = 0.15f)
                    submitted && isChosen && !isCorrect -> RoseNeon to RoseNeon.copy(alpha = 0.15f)
                    isChosen -> CyanNeon to CyanNeon.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) to MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = !submitted) {
                            selectedOption = index
                            submitted = true
                            onSubmit(index, index == quiz.correctIndex)
                        },
                    color = containerColor,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${('A' + index)}. ",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = CyanNeon
                        )
                        Text(
                            text = optionText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            AnimatedVisibility(visible = submitted) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    val wasCorrect = selectedOption == quiz.correctIndex
                    Text(
                        text = if (wasCorrect) "✅ Correct!" else "❌ Incorrect. Correct answer is Option ${'A' + quiz.correctIndex}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (wasCorrect) EmeraldNeon else RoseNeon
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = quiz.explanation,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectList(projects: List<ProjectItem>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 36.dp)
    ) {
        item {
            Text(
                text = "Portfolio-Differentiating Capstone Mini-Projects",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "These production-grade architectures demonstrate real-world engineering to hiring leads.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        items(projects) { project ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = CyanNeon.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = project.domain,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CyanNeon
                            )
                        }

                        Surface(
                            color = if (project.status == "IN_PROGRESS") AmberNeon.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = project.status.replace("_", " "),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (project.status == "IN_PROGRESS") AmberNeon else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = project.description,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "Tech Stack: ${project.techStack}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = VioletLight
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Deliverables: ${project.deliverables}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DifficultyBadge(difficulty: String) {
    val (color, text) = when (difficulty.lowercase()) {
        "easy" -> EmeraldNeon to "Easy"
        "medium" -> AmberNeon to "Medium"
        "hard" -> RoseNeon to "Hard"
        else -> CyanNeon to difficulty
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            ),
            color = color
        )
    }
}
