package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.LearningModule
import com.example.data.model.ModuleQuizQuestion
import com.example.data.model.ModuleQuizSubmission
import com.example.ui.theme.*

@Composable
fun TutorModuleDialog(
    module: LearningModule,
    explanation: String?,
    isExplaining: Boolean,
    quizQuestions: List<ModuleQuizQuestion>,
    quizSubmission: ModuleQuizSubmission?,
    isGeneratingQuiz: Boolean,
    onAskQuestion: (String) -> Unit,
    onRequestQuiz: () -> Unit,
    onSubmitQuiz: (Map<Int, Int>) -> Unit,
    onDismiss: () -> Unit,
    onOpenFullChat: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Explain, 1: Quiz
    var userQueryText by remember { mutableStateOf("") }
    val userAnswers = remember { mutableStateMapOf<Int, Int>() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ActivePillBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Tutor Agent",
                                tint = ActivePillText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Tutor Agent",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = LavenderHeader
                            )
                            Text(
                                text = module.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextLightSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextLightSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs: Explain & Concept | Practice Quiz
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CharcoalBg)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TabPill(
                        text = "Concept Walkthrough",
                        icon = Icons.Default.AutoAwesome,
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    TabPill(
                        text = "Module Quiz",
                        icon = Icons.Default.Quiz,
                        isSelected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            if (quizQuestions.isEmpty()) {
                                onRequestQuiz()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (selectedTab == 0) {
                        // EXPLAIN TAB
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Quick prompt suggestions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                module.related_concepts.take(2).forEach { concept ->
                                    SuggestionChip(
                                        onClick = {
                                            onAskQuestion("Explain $concept with a clear Python code snippet and analogy.")
                                        },
                                        label = {
                                            Text(
                                                text = concept,
                                                fontSize = 11.sp,
                                                color = LavenderPrimary
                                            )
                                        },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = CharcoalSurfaceVariant
                                        ),
                                        border = SuggestionChipDefaults.suggestionChipBorder(
                                            enabled = true,
                                            borderColor = CharcoalBorder
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Explanation Text or Loading
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CharcoalBg)
                                    .padding(14.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                if (isExplaining) {
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            color = LavenderPrimary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Text(
                                            text = "Tutor is crafting clear explanation...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextLightSecondary
                                        )
                                    }
                                } else {
                                    Text(
                                        text = explanation ?: "Tap 'Explain Concept' or ask any question regarding ${module.title}.",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            lineHeight = 22.sp,
                                            fontSize = 13.sp
                                        ),
                                        color = TextLightPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Ask Question Input Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = userQueryText,
                                    onValueChange = { userQueryText = it },
                                    placeholder = { Text("Ask tutor about ${module.title}...", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = LavenderPrimary,
                                        unfocusedBorderColor = CharcoalBorder,
                                        focusedTextColor = TextLightPrimary,
                                        unfocusedTextColor = TextLightPrimary,
                                        focusedContainerColor = CharcoalBg,
                                        unfocusedContainerColor = CharcoalBg
                                    )
                                )

                                Button(
                                    onClick = {
                                        if (userQueryText.isNotBlank()) {
                                            onAskQuestion(userQueryText)
                                            userQueryText = ""
                                        } else {
                                            onAskQuestion("Explain the key concepts of ${module.title}")
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send",
                                        tint = DeepPurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // QUIZ TAB
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (isGeneratingQuiz) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        CircularProgressIndicator(color = LavenderPrimary)
                                        Text(
                                            text = "Tutor is preparing socratic questions...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextLightSecondary
                                        )
                                    }
                                }
                            } else if (quizQuestions.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CharcoalBg),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Ready to test your knowledge?",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = LavenderHeader
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Complete 3 socratic questions to verify concept mastery and unlock the next module.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextLightSecondary
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = onRequestQuiz,
                                            colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary)
                                        ) {
                                            Text("Generate Practice Quiz", color = DeepPurple)
                                        }
                                    }
                                }
                            } else {
                                // Display Quiz Questions
                                quizQuestions.forEachIndexed { qIndex, question ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = CharcoalBg),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = "Q${qIndex + 1}. ${question.question}",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = LavenderHeader
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))

                                            question.options.forEachIndexed { optIndex, optionText ->
                                                val isSelected = userAnswers[qIndex] == optIndex
                                                val isSubmitted = quizSubmission != null
                                                val isCorrectAnswer = question.correctIndex == optIndex

                                                val bgColor = when {
                                                    isSubmitted && isCorrectAnswer -> EmeraldNeon.copy(alpha = 0.2f)
                                                    isSubmitted && isSelected && !isCorrectAnswer -> CrimsonNeon.copy(alpha = 0.2f)
                                                    isSelected -> ActivePillBg.copy(alpha = 0.3f)
                                                    else -> CharcoalSurface
                                                }

                                                val borderColor = when {
                                                    isSubmitted && isCorrectAnswer -> EmeraldNeon
                                                    isSubmitted && isSelected && !isCorrectAnswer -> CrimsonNeon
                                                    isSelected -> LavenderPrimary
                                                    else -> CharcoalBorder.copy(alpha = 0.5f)
                                                }

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(bgColor)
                                                        .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                                        .clickable(enabled = quizSubmission == null) {
                                                            userAnswers[qIndex] = optIndex
                                                        }
                                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    RadioButton(
                                                        selected = isSelected,
                                                        onClick = { if (quizSubmission == null) userAnswers[qIndex] = optIndex },
                                                        colors = RadioButtonDefaults.colors(
                                                            selectedColor = LavenderPrimary,
                                                            unselectedColor = TextLightSecondary
                                                        )
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = optionText,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = TextLightPrimary
                                                    )
                                                }
                                            }

                                            if (quizSubmission != null) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "💡 ${question.explanation}",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                    color = LavenderPrimary
                                                )
                                            }
                                        }
                                    }
                                }

                                // Submission feedback or Submit Button
                                if (quizSubmission != null) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (quizSubmission.scorePercentage >= 70)
                                                EmeraldNeon.copy(alpha = 0.15f)
                                            else AmberNeon.copy(alpha = 0.15f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = "Score: ${quizSubmission.scorePercentage}% (${quizSubmission.correctCount}/${quizSubmission.totalQuestions} correct)",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (quizSubmission.scorePercentage >= 70) EmeraldNeon else AmberNeon
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = quizSubmission.feedback,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextLightPrimary
                                            )
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            onSubmitQuiz(userAnswers)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary),
                                        enabled = userAnswers.size == quizQuestions.size
                                    ) {
                                        Text(
                                            text = "Submit Quiz to Tutor",
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
        }
    }
}

@Composable
private fun TabPill(
    text: String,
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
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) ActivePillText else TextLightSecondary
            )
        }
    }
}
