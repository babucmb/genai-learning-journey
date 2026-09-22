package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNotification
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationsScreen(
    notifications: List<AppNotification>,
    isAILoading: Boolean,
    onNotificationClick: (AppNotification) -> Unit,
    onTriggerSimulatedDailyAlert: () -> Unit,
    onNavigateToJobs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unreadCount = notifications.count { !it.read }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Daily Job Alerts",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (unreadCount > 0) "$unreadCount unread daily alert" + (if (unreadCount > 1) "s" else "") else "All caught up",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (unreadCount > 0) LavenderPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Simulate 9:00 AM Cron Alert Button
            Button(
                onClick = onTriggerSimulatedDailyAlert,
                enabled = !isAILoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LavenderPrimary,
                    contentColor = DeepPurple
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("simulate_alert_button")
            ) {
                if (isAILoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = DeepPurple
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Run Alert",
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Simulate 9 AM Run",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Schedule banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = CharcoalSurfaceVariant.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, CharcoalBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Schedule",
                    tint = EmeraldNeon,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Automated Daily Schedule: 09:00 AM (Asia/Kolkata)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextLightPrimary
                    )
                    Text(
                        text = "Scans fresher, intern & apprentice roles matching your profile every 24-25 hours.",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextLightSecondary
                    )
                }
            }
        }

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(LavenderPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "No alerts",
                            tint = LavenderPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No job alerts received yet",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextLightPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Daily alerts at 9:00 AM will appear here when new matching intern and fresher roles are indexed. Tap 'Simulate 9 AM Run' above to test now!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLightSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    NotificationCard(
                        notification = notif,
                        onClick = {
                            onNotificationClick(notif)
                            onNavigateToJobs()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: AppNotification,
    onClick: () -> Unit
) {
    val formattedTime = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(notification.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("notification_card_${notification.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read) CharcoalSurface else CharcoalSurfaceVariant
        ),
        border = BorderStroke(
            1.dp,
            if (!notification.read) LavenderPrimary.copy(alpha = 0.6f) else CharcoalBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!notification.read) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(LavenderPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (!notification.read) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        color = if (!notification.read) LavenderHeader else TextLightPrimary
                    )
                }

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextLightMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notification.body,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = TextLightSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "View Matching Jobs →",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LavenderPrimary
                    )
                }
            }
        }
    }
}
