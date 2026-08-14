package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BgDark
import com.example.ui.theme.GoldGold
import com.example.ui.theme.TextBody
import com.example.ui.theme.TextSecondary

@Composable
fun SplashDiscoveryScreen(
    isDiscovering: Boolean,
    onDiscoveryFinished: () -> Unit,
    onManualAccountSubmit: (String) -> Unit = {}
) {
    var showManualInput by remember { mutableStateOf(false) }

    if (showManualInput) {
        ManualAccountScreen(
            onSubmit = { accountId ->
                onManualAccountSubmit(accountId)
                showManualInput = false
            },
            onBack = { showManualInput = false }
        )
        return
    }

    // Pulse animation for logo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(24.dp)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Application Name in Premium Gold Gold
            Text(
                text = "MSR WI-FI",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = GoldGold,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("splash_title")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = "نظام إدارة وااشتراك المشتركين الذكي",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = TextBody,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            if (isDiscovering) {
                CircularProgressIndicator(
                    color = GoldGold,
                    strokeWidth = 3.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("splash_loading")
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "جاري اكتشاف الشبكة تلقائياً عبر DNS...",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                    textAlign = TextAlign.Center
                )
            } else {
                Button(
                    onClick = onDiscoveryFinished,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldGold,
                        contentColor = BgDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp)
                        .testTag("splash_continue_button")
                ) {
                    Text(
                        text = "دخول التطبيق",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BgDark
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { showManualInput = true },
                    modifier = Modifier.testTag("manual_account_button")
                ) {
                    Text(
                        text = "إدخال رقم الحساب يدوياً",
                        color = GoldGold,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        // Footer version info
        Text(
            text = "الإصدار 1.0.0 © MSR Systems",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
