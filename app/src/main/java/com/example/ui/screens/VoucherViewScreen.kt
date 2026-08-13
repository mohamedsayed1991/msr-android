package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VoucherInfoResponse
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherViewScreen(
    voucher: VoucherInfoResponse?,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "بيانات تفاصيل الكارت",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldGold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("voucher_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = GoldGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark,
        modifier = Modifier.fillMaxSize().testTag("voucher_view_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgDark)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            val currentVoucher = voucher ?: VoucherInfoResponse(
                code = "N/A",
                status = "expired",
                validityDays = "منتهي",
                totalGb = 0.0,
                usedGb = 0.0,
                remainingGb = 0.0
            )

            // Main Status Display Icon
            val isExpired = currentVoucher.status == "expired" || currentVoucher.validityDays?.contains("منتهي") == true
            val statusIcon = if (isExpired) Icons.Default.Cancel else Icons.Default.CheckCircle
            val statusColor = if (isExpired) ErrorRed else SuccessGreen
            val statusText = if (isExpired) "الكارت منتهي الصلاحية" else "الكارت مستخدم ونشط"

            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier
                    .size(72.dp)
                    .padding(bottom = 12.dp)
            )

            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    fontSize = 20.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Premium Voucher Card Layout
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, GoldGold, RoundedCornerShape(24.dp))
                    .testTag("voucher_info_card")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header label
                    Text(
                        text = "رمز كود الكارت المفحوص",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentVoucher.code ?: "رمز غير معروف",
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = GoldGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            letterSpacing = 1.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("voucher_code_display")
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Divider(color = GoldGold.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(20.dp))

                    // Progress metrics
                    val total = currentVoucher.totalGb ?: 0.0
                    val used = currentVoucher.usedGb ?: 0.0
                    val rem = (total - used).coerceAtLeast(0.0)
                    val ratio = if (total > 0) (used / total).toFloat().coerceIn(0f, 1f) else 0f

                    // Horizontal Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "معدل استهلاك البيانات كارت",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = String.format("%.1f%% مستهلك", ratio * 100),
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = ratio,
                        color = GoldGold,
                        trackColor = GoldGold.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Metrics Rows
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatRow(label = "صلاحية الكارت", value = calculateRemainingDays(currentVoucher.validityDays))
                        StatRow(label = "إجمالي سعة الباقة", value = formatArabicDataSize(total))
                        StatRow(label = "حجم التحميل المستخدم", value = formatArabicDataSize(used))
                        StatRow(label = "الرصيد المتبقي المتاح", value = formatArabicDataSize(rem), isHighlight = true)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action button back
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldGold,
                    contentColor = BgDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("voucher_back_to_login_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "العودة لشاشة الدخول",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BgDark
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = BgDark
                    )
                }
            }
        }
    }
}
