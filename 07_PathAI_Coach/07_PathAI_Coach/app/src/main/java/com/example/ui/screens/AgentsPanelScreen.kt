package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgentChatMessage
import com.example.data.model.AgentItem
import com.example.data.model.TechRadarItem
import com.example.ui.theme.*

@Composable
fun AgentsPanelScreen(
    agents: List<AgentItem>,
    selectedAgent: AgentItem?,
    activeAgentChat: List<AgentChatMessage>,
    techRadarItems: List<TechRadarItem>,
    isAgentThinking: Boolean,
    onSelectAgent: (AgentItem) -> Unit,
    onCloseChat: () -> Unit,
    onSendMessage: (String) -> Unit,
    onRefreshTechRadar: () -> Unit,
    onToggleRadarLike: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (selectedAgent != null) {
        AgentChatConversationView(
            agent = selectedAgent,
            chatMessages = activeAgentChat,
            isAgentThinking = isAgentThinking,
            onCloseChat = onCloseChat,
            onSendMessage = onSendMessage,
            modifier = modifier
        )
    } else {
        AgentOverviewList(
            agents = agents,
            techRadarItems = techRadarItems,
            onSelectAgent = onSelectAgent,
            onRefreshTechRadar = onRefreshTechRadar,
            onToggleRadarLike = onToggleRadarLike,
            modifier = modifier
        )
    }
}

@Composable
private fun AgentOverviewList(
    agents: List<AgentItem>,
    techRadarItems: List<TechRadarItem>,
    onSelectAgent: (AgentItem) -> Unit,
    onRefreshTechRadar: () -> Unit,
    onToggleRadarLike: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 36.dp)
    ) {
        // Header Banner: Multi-Agent OS
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CharcoalSurface,
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
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
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(LavenderPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = "Multi-Agent",
                                    tint = LavenderPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Active Agents Panel",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = LavenderHeader
                                )
                                Text(
                                    text = "Multi-Agent AI Learning OS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextLightSecondary
                                )
                            }
                        }

                        // Pulse indicator
                        Surface(
                            color = CharcoalSurfaceVariant,
                            shape = RoundedCornerShape(50),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldNeon.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldNeon)
                                )
                                Text(
                                    text = "4 ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = EmeraldNeon
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Specialized AI agents monitor your velocity, diagnose math concepts, review GitHub projects, and scan cutting-edge AI research to accelerate your learning.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = TextLightSecondary
                    )
                }
            }
        }

        // Section Title: Autonomous Agents
        item {
            Text(
                text = "Specialized Sub-Agents",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                ),
                color = LavenderHeader
            )
        }

        // Active Agents Cards
        items(agents) { agent ->
            AgentCard(
                agent = agent,
                onClick = { onSelectAgent(agent) }
            )
        }

        // Section Title: Tech Radar
        item {
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
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = LavenderPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Tech Radar Intelligence",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = LavenderHeader
                    )
                }

                Text(
                    text = "SYNC RADAR",
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onRefreshTechRadar() }
                        .padding(4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = LavenderPrimary
                )
            }
        }

        // Tech Radar items
        items(techRadarItems) { item ->
            TechRadarCard(
                item = item,
                onToggleLike = { onToggleRadarLike(item.id) }
            )
        }
    }
}

@Composable
private fun AgentCard(
    agent: AgentItem,
    onClick: () -> Unit
) {
    val (icon, badgeColor) = when (agent.iconName) {
        "Coach" -> Pair(Icons.Default.Schedule, CyanNeon)
        "Tutor" -> Pair(Icons.Default.Psychology, VioletLight)
        "Project" -> Pair(Icons.Default.Architecture, AmberNeon)
        "Research" -> Pair(Icons.Default.TravelExplore, EmeraldNeon)
        else -> Pair(Icons.Default.SmartToy, LavenderPrimary)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = CharcoalSurface,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CharcoalSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = agent.name,
                            tint = badgeColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = agent.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextLightPrimary
                        )
                        Text(
                            text = agent.roleTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor
                        )
                    }
                }

                Surface(
                    color = CharcoalSurfaceVariant,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "START CHAT",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = LavenderPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = agent.description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                color = TextLightSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Current Autonomous Task Box
            Surface(
                color = CharcoalSurfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                    Text(
                        text = agent.activeTask,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextLightPrimary,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
private fun TechRadarCard(
    item: TechRadarItem,
    onToggleLike: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CharcoalSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (item.isLiked) RoseNeon.copy(alpha = 0.5f) else CharcoalBorder.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = CharcoalSurfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.domain,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = LavenderPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = EmeraldNeon.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldNeon.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = item.impact,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = EmeraldNeon
                        )
                    }

                    Text(
                        text = item.timeAgo,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextLightSecondary.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextLightPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.summary,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = TextLightSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Source,
                        contentDescription = null,
                        tint = TextLightSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = item.source,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextLightSecondary.copy(alpha = 0.6f)
                    )
                }

                // Interactive Like / Save Feedback Button
                Surface(
                    onClick = onToggleLike,
                    shape = RoundedCornerShape(8.dp),
                    color = if (item.isLiked) RoseNeon.copy(alpha = 0.15f) else CharcoalSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (item.isLiked) RoseNeon.copy(alpha = 0.5f) else Color.Transparent
                    ),
                    modifier = Modifier.testTag("radar_like_${item.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (item.isLiked) "Liked" else "Like",
                            tint = if (item.isLiked) RoseNeon else TextLightSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (item.isLiked) "Saved" else "Save",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = if (item.isLiked) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (item.isLiked) RoseNeon else TextLightSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentChatConversationView(
    agent: AgentItem,
    chatMessages: List<AgentChatMessage>,
    isAgentThinking: Boolean,
    onCloseChat: () -> Unit,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(chatMessages.size, isAgentThinking) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CharcoalBg)
    ) {
        // Chat Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CharcoalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onCloseChat) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = LavenderPrimary
                        )
                    }

                    Column {
                        Text(
                            text = agent.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextLightPrimary
                        )
                        Text(
                            text = agent.roleTitle,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = LavenderPrimary
                        )
                    }
                }

                Surface(
                    color = EmeraldNeon.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldNeon.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "ONLINE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = EmeraldNeon
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Initial agent intro bubble
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CharcoalSurfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Connected to ${agent.name}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = LavenderPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = agent.description,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                            color = TextLightSecondary
                        )
                    }
                }
            }

            // Quick Starter Prompts Chips
            if (chatMessages.isEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Suggested Questions:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextLightSecondary
                        )
                        agent.samplePrompts.forEach { prompt ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSendMessage(prompt) },
                                color = CharcoalSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LavenderPrimary.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChatBubbleOutline,
                                        contentDescription = null,
                                        tint = LavenderPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = prompt,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = TextLightPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Chat Messages
            items(chatMessages) { message ->
                val isUser = message.sender == "USER"
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
                    Surface(
                        color = if (isUser) LavenderPrimary else CharcoalSurface,
                        contentColor = if (isUser) ActivePillText else TextLightPrimary,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        border = if (isUser) null else androidx.compose.foundation.BorderStroke(
                            1.dp,
                            CharcoalBorder.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.widthIn(max = 320.dp)
                    ) {
                        Text(
                            text = message.message,
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp, fontSize = 13.sp)
                        )
                    }
                }
            }

            if (isAgentThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = LavenderPrimary
                        )
                        Text(
                            text = "${agent.name} is processing reasoning...",
                            style = MaterialTheme.typography.labelSmall,
                            color = LavenderPrimary
                        )
                    }
                }
            }
        }

        // Chat Input Row
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CharcoalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = "Ask ${agent.name}...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLightSecondary.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = CharcoalBorder,
                        focusedContainerColor = CharcoalSurfaceVariant,
                        unfocusedContainerColor = CharcoalSurfaceVariant,
                        focusedTextColor = TextLightPrimary,
                        unfocusedTextColor = TextLightPrimary
                    ),
                    maxLines = 3
                )

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            val msg = textInput.trim()
                            textInput = ""
                            onSendMessage(msg)
                        }
                    },
                    enabled = textInput.isNotBlank() && !isAgentThinking,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (textInput.isNotBlank()) LavenderPrimary else CharcoalSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (textInput.isNotBlank()) ActivePillText else TextLightSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
