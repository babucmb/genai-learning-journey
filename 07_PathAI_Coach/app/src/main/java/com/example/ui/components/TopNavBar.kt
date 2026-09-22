package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.AppNavTab

@Composable
fun TopNavBar(
    selectedTab: AppNavTab,
    onTabSelected: (AppNavTab) -> Unit,
    userName: String? = null,
    userPath: String? = null,
    unreadNotificationCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val initials = remember(userName) {
        if (!userName.isNullOrBlank()) {
            val parts = userName.trim().split(" ")
            if (parts.size >= 2) {
                "${parts[0].firstOrNull()?.uppercaseChar() ?: 'A'}${parts[1].firstOrNull()?.uppercaseChar() ?: 'I'}"
            } else {
                userName.take(2).uppercase()
            }
        } else {
            "AI"
        }
    }

    Surface(
        color = CharcoalBg,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 6.dp)
        ) {
            // App Title Header matching Clean Minimalism
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "PathAI Coach",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.5).sp,
                            fontSize = 24.sp
                        ),
                        color = LavenderHeader // #EADDFF
                    )
                    Text(
                        text = "${(userPath ?: "AIML").uppercase()} PREPARATION • WEEK 4",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp,
                            fontSize = 11.sp
                        ),
                        color = TextLightSecondary // #938F99
                    )
                }

                // Action area: Notification Bell + Candidate Avatar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.size(44.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationCount > 0) {
                                    Badge(
                                        containerColor = LavenderPrimary,
                                        contentColor = DeepPurple
                                    ) {
                                        Text(
                                            text = if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Daily Job Alerts & Notifications",
                                tint = if (unreadNotificationCount > 0) LavenderPrimary else TextLightSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Clean Minimalism circular candidate avatar
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CharcoalSurfaceVariant) // #49454F
                            .border(2.dp, LavenderPrimary, CircleShape)
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = LavenderPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Scrollable Navigation Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppNavTab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab
                    val icon = getTabIcon(tab)

                    FilterChip(
                        selected = isSelected,
                        onClick = { onTabSelected(tab) },
                        label = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) ActivePillText else TextLightSecondary
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ActivePillBg, // #E8DEF8
                            selectedLabelColor = ActivePillText, // #1D192B
                            containerColor = CharcoalSurface, // #2B2930
                            labelColor = TextLightSecondary // #938F99
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = CharcoalBorder.copy(alpha = 0.5f),
                            selectedBorderColor = LavenderPrimary
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }
    }
}

private fun getTabIcon(tab: AppNavTab): ImageVector {
    return when (tab) {
        AppNavTab.DASHBOARD -> Icons.Default.Dashboard
        AppNavTab.TODAY -> Icons.Default.Today
        AppNavTab.LEARNING -> Icons.Default.MenuBook
        AppNavTab.AGENTS -> Icons.Default.SmartToy
        AppNavTab.ROADMAP -> Icons.AutoMirrored.Filled.AltRoute
        AppNavTab.PRACTICE -> Icons.Default.Code
        AppNavTab.INTERVIEWS -> Icons.Default.RecordVoiceOver
        AppNavTab.JOBS -> Icons.Default.Work
        AppNavTab.SKILL_MAP -> Icons.Default.Hub
        AppNavTab.SETTINGS -> Icons.Default.Settings
        AppNavTab.NOTIFICATIONS -> Icons.Default.Notifications
    }
}
