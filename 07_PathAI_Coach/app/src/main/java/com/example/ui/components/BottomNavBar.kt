package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.AppNavTab

data class BottomNavItem(
    val tab: AppNavTab,
    val label: String,
    val icon: ImageVector
)

@Composable
fun CleanBottomNavBar(
    selectedTab: AppNavTab,
    onTabSelected: (AppNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        BottomNavItem(AppNavTab.DASHBOARD, "Home", Icons.Default.Dashboard),
        BottomNavItem(AppNavTab.TODAY, "Today", Icons.Default.Today),
        BottomNavItem(AppNavTab.LEARNING, "Learning", Icons.Default.MenuBook),
        BottomNavItem(AppNavTab.AGENTS, "Agents", Icons.Default.SmartToy),
        BottomNavItem(AppNavTab.JOBS, "Jobs", Icons.Default.Work)
    )

    Surface(
        color = CharcoalSurface, // #2B2930
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder) // border-t #49454F
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = when (item.tab) {
                    AppNavTab.DASHBOARD -> selectedTab == AppNavTab.DASHBOARD
                    AppNavTab.TODAY -> selectedTab == AppNavTab.TODAY
                    AppNavTab.LEARNING -> selectedTab == AppNavTab.LEARNING || selectedTab == AppNavTab.ROADMAP
                    AppNavTab.AGENTS -> selectedTab == AppNavTab.AGENTS
                    AppNavTab.JOBS -> selectedTab == AppNavTab.JOBS
                    else -> selectedTab == item.tab
                }

                val interactionSource = remember { MutableInteractionSource() }

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onTabSelected(item.tab) }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isSelected) {
                        // Clean Minimalist Active Pill: bg-[#E8DEF8] text-[#1D192B] px-5 py-1 rounded-full
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(ActivePillBg)
                                .padding(horizontal = 18.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = ActivePillText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextLightPrimary
                        )
                    } else {
                        // Inactive item: opacity-60, text-[#938F99]
                        Box(
                            modifier = Modifier.padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = TextLightSecondary.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextLightSecondary.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
