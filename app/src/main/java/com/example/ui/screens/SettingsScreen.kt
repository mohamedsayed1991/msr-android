package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: Int,
    onToggleTheme: () -> Unit,
    subscriberName: String,
    onLogout: () -> Unit
) {
    val themeLabel = when (themeMode) {
        1 -> "فاتح"
        2 -> "داكن"
        else -> "تلقائي"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "الإعدادات",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Profile Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(RadiusXL)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(RadiusLG))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column {
                        Text(
                            text = subscriberName.ifBlank { "مستخدم" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "حساب مشترك",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // App Settings Section
            Text(
                text = "إعدادات التطبيق",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(RadiusXL)
            ) {
                Column {
                    // Theme toggle
                    SettingsItem(
                        icon = Icons.Filled.DarkMode,
                        title = "المظهر",
                        subtitle = themeLabel,
                        onClick = onToggleTheme
                    )
                }
            }

            // About Section
            Text(
                text = "حول التطبيق",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(RadiusXL)
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Outlined.Info,
                        title = "الإصدار",
                        subtitle = "1.0.0",
                        onClick = {}
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outline
                    )
                    SettingsItem(
                        icon = Icons.Outlined.Info,
                        title = "MSR Systems",
                        subtitle = "نظام إدارة واشتراك المشتركين",
                        onClick = {}
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logout Button
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed.copy(alpha = 0.1f),
                    contentColor = ErrorRed
                ),
                shape = RoundedCornerShape(RadiusXL),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("settings_logout_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تسجيل الخروج",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(RadiusMD))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowLeft,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
