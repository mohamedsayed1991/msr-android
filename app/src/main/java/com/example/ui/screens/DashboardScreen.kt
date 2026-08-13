package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.AppConfig
import com.example.data.model.SubscriberInfo
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import java.util.Locale

fun calculateRemainingDays(validityDaysStr: String?): String {
    return validityDaysStr ?: "نشطة"
}

fun formatArabicDataSize(gb: Double?): String {
    val actualGb = gb ?: 0.0
    val mb = actualGb * 1024.0
    return if (actualGb < 1.0) {
        String.format(Locale.US, "%.1f ميجا", mb)
    } else {
        String.format(Locale.US, "%.2f جيجا", actualGb)
    }
}

fun formatQuotaValue(value: Double?): String {
    val actual = value ?: 0.0
    return if (actual % 1.0 == 0.0) {
        actual.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", actual)
    }
}

@Composable
fun PremiumPlanCard(
    badgeText: String,
    planName: String,
    remainingGb: Double,
    totalGb: Double,
    progress: Float,
    validityText: String,
    gradient: Brush,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Subtle decorative background circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp)
                    .background(Color.White.copy(alpha = 0.08f), shape = CircleShape)
            )

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Badge and Validity row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.22f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Text(
                        text = validityText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Plan Name
                Text(
                    text = planName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Numbers layout (Arabic RTL friendly, where the text flows logically)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f", remainingGb),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 32.sp
                            )
                        )
                        Text(
                            text = "جيجابايت متبقية",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    
                    Text(
                        text = "من أصل ${formatQuotaValue(totalGb)} جيجا",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Percentage Indicator
                Text(
                    text = "${(progress * 100).toInt()}% متوفر",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    subscriber: SubscriberInfo?,
    isLoading: Boolean,
    isOnline: Boolean,
    onRefresh: () -> Unit,
    onViewPlans: () -> Unit,
    onGenerateVoucher: () -> Unit,
    onRecharge: () -> Unit,
    onTransactionsClick: () -> Unit,
    onLogout: () -> Unit,
    onRenewPlan: (planId: Int, planType: String) -> Unit,
    onChangePlanClick: () -> Unit,
    planSuccessDialog: com.example.ui.viewmodel.PlanSuccessInfo? = null,
    onDismissSuccessDialog: () -> Unit = {},
    addonPlans: List<com.example.data.model.AddonPlan> = emptyList(),
    isLoadingAddons: Boolean = false,
    isBuyingAddon: Boolean = false,
    onLoadAddons: () -> Unit = {},
    onBuyAddon: (Int, String, Double) -> Unit = { _, _, _ -> },
    themeMode: Int = 0,
    onToggleTheme: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var showRenewConfirmation by remember { mutableStateOf(false) }
    var showAddonsSheet by remember { mutableStateOf(false) }
    var selectedAddonForConfirmation by remember { mutableStateOf<com.example.data.model.AddonPlan?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "MSR Online",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GoldGold
                            )
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) SuccessGreen else ErrorRed)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        val themeIcon = if (themeMode == 2) {
                            Icons.Default.WbSunny
                        } else {
                            Icons.Default.NightsStay
                        }
                        Icon(imageVector = themeIcon, contentDescription = "تبديل المظهر", tint = GoldGold)
                    }
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "تحديث", tint = GoldGold)
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "خروج", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgDark,
                    titleContentColor = GoldGold
                )
            )
        },
        containerColor = BgDark,
        modifier = Modifier.fillMaxSize().testTag("dashboard_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgDark)
        ) {
            val currentSub = subscriber ?: SubscriberInfo(
                fullName = "مستخدم افتراضي",
                username = "guest",
                balance = 0.0,
                altPhone = ""
            )

            if (isLoading && subscriber == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldGold)
                }
            } else {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Part 1: Glassmorphism Top Balance Card (رصيد الحساب)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x22FFFFFF),
                                        Color(0x0AFFFFFF)
                                    )
                                )
                            )
                            .border(1.dp, GoldGold.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            if (isOnline) {
                                val welcomeName = currentSub.displayFullName.ifBlank { currentSub.displayUsername }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Text(
                                        text = "مرحباً يا $welcomeName 👋",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextBody
                                        ),
                                        textAlign = TextAlign.Start
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = GoldGold.copy(alpha = 0.15f), thickness = 1.dp)
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            val balance = currentSub.displayBalance
                            val balanceColor = if (balance < 0) Color.Red else GoldGold
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💰 الرصيد المتوفر :",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = TextBody,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = com.example.config.AppConfig.formatPriceWithCurrency(balance),
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        color = balanceColor,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 28.sp
                                    ),
                                    modifier = Modifier.testTag("balance_text")
                                )
                            }
                            
                            if (balance < 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "تنبيه: حسابك مدين حالياً، يرجى الشحن لتفادي قطع الخدمة.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Red),
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    val outstandingDebt = currentSub.displayOutstandingDebt
                    if (outstandingDebt > 0) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0x1AEF5350)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFEF5350).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                .testTag("outstanding_debt_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "تحذير المديونية",
                                            tint = Color(0xFFEF5350)
                                        )
                                        Text(
                                            text = "المديونية المستحقة",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = Color(0xFFEF5350),
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                    Text(
                                        text = com.example.config.AppConfig.formatPriceWithCurrency(outstandingDebt),
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            color = Color(0xFFEF5350),
                                            fontWeight = FontWeight.Black
                                        ),
                                        modifier = Modifier.testTag("outstanding_debt_text")
                                    )
                                }
                            }
                        }
                    }

                    // 2. Part 2: Middle Section Active Plan Card (حاوية الباقة الحالية)
                    val activePlan = currentSub.activePlan
                    if (activePlan != null) {
                        val hasAddon = activePlan.hasAddon == true
                        val total = activePlan.totalGb ?: 0.0
                        val rem = activePlan.remainingGb ?: (total - (activePlan.usedGb ?: 0.0)).coerceAtLeast(0.0)
                        val isExpired = rem <= 0.0 || activePlan.validityDays?.contains("منتهي") == true

                        val addonQuota = if (hasAddon) activePlan.addonQuotaGb ?: 0.0 else 0.0
                        val basicQuota = if (hasAddon) {
                            activePlan.basicQuotaGb ?: (total - addonQuota).coerceAtLeast(0.0)
                        } else {
                            activePlan.basicQuotaGb ?: total
                        }

                        val remainingBasicGb = if (hasAddon) {
                            (rem - addonQuota).coerceAtLeast(0.0)
                        } else {
                            rem
                        }
                        val remainingAddonGb = if (hasAddon) rem.coerceAtMost(addonQuota) else 0.0

                        val progressBasic = if (basicQuota > 0.0) (remainingBasicGb / basicQuota).toFloat().coerceIn(0f, 1f) else 0f
                        val progressAddon = if (addonQuota > 0.0) (remainingAddonGb / addonQuota).toFloat().coerceIn(0f, 1f) else 0f

                        // 💳 Basic Plan Card
                        PremiumPlanCard(
                            badgeText = "الباقة الشهرية",
                            planName = activePlan.planName ?: activePlan.name ?: "الباقة النشطة",
                            remainingGb = remainingBasicGb,
                            totalGb = basicQuota,
                            progress = progressBasic,
                            validityText = activePlan.validityDays ?: "نشطة",
                            gradient = Brush.linearGradient(colors = listOf(Color(0xFF8E5A35), Color(0xFFD48858))),
                            testTag = "basic_plan_card"
                        )

                        // 💳 Addon Plan Card (if active)
                        if (hasAddon && addonQuota > 0.0) {
                            PremiumPlanCard(
                                badgeText = "الباقة الإضافية",
                                planName = "جيجات إضافية",
                                remainingGb = remainingAddonGb,
                                totalGb = addonQuota,
                                progress = progressAddon,
                                validityText = "تنتهي مع الأساسية",
                                gradient = Brush.linearGradient(colors = listOf(Color(0xFF423B36), Color(0xFF8C7969))),
                                testTag = "addon_plan_card"
                            )
                        }

                        // ⚙️ Action Buttons Card Container
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("action_buttons_card"),
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Top Row: Add-on Package button (Full width) - Check sub_show_addons
                                if (currentSub.showAddons) {
                                    Button(
                                        onClick = {
                                            onLoadAddons()
                                            showAddonsSheet = true
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = GoldGold
                                        ),
                                        border = BorderStroke(1.5.dp, GoldGold),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("extra_plan_button")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("باقة إضافية", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // Bottom Row: Renew Plan and Change Plan side-by-side
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Renew Plan Button (sub_allow_renew)
                                    val renewEnabled = currentSub.allowRenew
                                    Button(
                                        onClick = {
                                            if (!renewEnabled) return@Button
                                            if (!isExpired) {
                                                android.widget.Toast.makeText(context, "⚠️ الباقة الحالية لا تزال نشطة وبها صلاحية وجيجات متبقية. لا يمكنك التجديد الآن.", android.widget.Toast.LENGTH_LONG).show()
                                            } else {
                                                showRenewConfirmation = true
                                            }
                                        },
                                        enabled = renewEnabled,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = if (isExpired && renewEnabled) GoldGold else Color.White,
                                            disabledContainerColor = Color.Transparent,
                                            disabledContentColor = Color.Gray
                                        ),
                                        border = BorderStroke(1.5.dp, if (isExpired && renewEnabled) GoldGold else Color.White.copy(alpha = if (renewEnabled) 0.4f else 0.15f)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("renew_plan_button")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(imageVector = Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("تجديد الباقة", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                        }
                                    }

                                    // Change Plan Button (sub_allow_change)
                                    val changeEnabled = currentSub.allowChange && currentSub.showPlans
                                    Button(
                                        onClick = {
                                            if (!changeEnabled) return@Button
                                            if (!isExpired) {
                                                android.widget.Toast.makeText(context, "⚠️ لا يمكنك تغيير الباقة الحالية إلا بعد انتهاء الجيجات أو صلاحية الوقت.", android.widget.Toast.LENGTH_LONG).show()
                                            } else {
                                                onChangePlanClick()
                                            }
                                        },
                                        enabled = changeEnabled,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = if (isExpired && changeEnabled) GoldGold else Color.White,
                                            disabledContainerColor = Color.Transparent,
                                            disabledContentColor = Color.Gray
                                        ),
                                        border = BorderStroke(1.5.dp, if (isExpired && changeEnabled) GoldGold else Color.White.copy(alpha = if (changeEnabled) 0.4f else 0.15f)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("change_plan_button")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("تغيير الباقة", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }

                        // 2.5 Simplified Plan Action Success Dialog
                        planSuccessDialog?.let { successInfo ->
                            AlertDialog(
                                onDismissRequest = onDismissSuccessDialog,
                                title = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(72.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = successInfo.message,
                                            color = GoldGold,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = onDismissSuccessDialog,
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldGold, contentColor = BgDark),
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("موافق", color = BgDark, fontWeight = FontWeight.Bold)
                                    }
                                },
                                containerColor = CardDark,
                                shape = RoundedCornerShape(24.dp)
                            )
                        }

                        // Renew Confirmation Dialog
                        if (showRenewConfirmation) {
                            AlertDialog(
                                onDismissRequest = { showRenewConfirmation = false },
                                title = {
                                    Text(
                                        text = "تأكيد تجديد الباقة الحالية",
                                        color = GoldGold,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Start
                                    )
                                },
                                text = {
                                    Text(
                                        text = "هل أنت متأكد من رغبتك في تجديد نفس الباقة الحالية: \"${activePlan.planName ?: activePlan.name ?: ""}\"؟ سيتم خصم قيمتها من رصيد محفظتك وتجديدها فوراً.",
                                        color = TextBody,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            showRenewConfirmation = false
                                            onRenewPlan(activePlan.planId ?: 0, activePlan.planType ?: "hotspot")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldGold, contentColor = BgDark)
                                    ) {
                                        Text("تأكيد التجديد", color = BgDark)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showRenewConfirmation = false }) {
                                        Text("إلغاء", color = TextSecondary)
                                    }
                                }
                            )
                        }
                    } else {
                        // Empty active plan layout
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GoldGold.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                                .background(CardDark)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "لا توجد باقة نشطة حالياً. يرجى الاشتراك في إحدى الباقات أدناه.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextSecondary,
                                        lineHeight = 22.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Part 3: Lower Section Control Actions (أزرار التحكم والتفاعل)
                    Text(
                        text = "الخدمات المتاحة",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = GoldGold,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Action 2: Generate Hotspot Voucher (sub_allow_buy_plan & sub_show_plans)
                        if (currentSub.allowBuyPlan && currentSub.showPlans) {
                            DashboardActionButton(
                                title = "إنشاء كروت",
                                subtitle = "توليد كروت شحن هوتسبوت جديدة من رصيدك",
                                icon = Icons.Default.ConfirmationNumber,
                                onClick = onGenerateVoucher,
                                tag = "action_generate_voucher"
                            )
                        }

                        // Action 3: Recharge balance (sub_show_recharge_page)
                        if (currentSub.showRechargePage) {
                            DashboardActionButton(
                                title = "شحن رصيد المحفظة",
                                subtitle = "تحويل الكاش المباشر لإدارة الشبكة لتفعيل رصيدك",
                                icon = Icons.Default.AddCard,
                                onClick = onRecharge,
                                tag = "action_recharge_screen"
                            )
                        }

                        // Action 4: Transaction History
                        DashboardActionButton(
                            title = "سجل العمليات واستعادة الكروت",
                            subtitle = "راجع عمليات الشحن وتوليد الكروت السابقة بكل سهولة",
                            icon = Icons.Default.History,
                            onClick = onTransactionsClick,
                            tag = "action_transactions_screen"
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // --- BOTTOM SHEET AND OVERLAYS FOR ADDONS ---
            if (showAddonsSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showAddonsSheet = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = CardDark,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = GoldGold.copy(alpha = 0.5f)) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "الباقات الإضافية المتاحة",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = GoldGold,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(16.dp)
                        )

                        if (isLoadingAddons) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = GoldGold)
                            }
                        } else if (addonPlans.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("لا توجد باقات إضافية متاحة حالياً", color = TextSecondary)
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                addonPlans.forEach { addon ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                            .border(1.dp, GoldGold.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                            .testTag("addon_card_${addon.id}"),
                                        colors = CardDefaults.cardColors(containerColor = BgDark),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = addon.name ?: "باقة إضافية",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        color = GoldGold,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                                Text(
                                                    text = com.example.config.AppConfig.formatPriceWithCurrency(addon.price ?: 0.0),
                                                    style = MaterialTheme.typography.titleLarge.copy(
                                                        color = GoldGold,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(12.dp))
                                            
                                            val buyAddonEnabled = currentSub.allowBuyAddon
                                            Button(
                                                onClick = {
                                                    if (buyAddonEnabled) {
                                                        selectedAddonForConfirmation = addon
                                                    }
                                                },
                                                enabled = buyAddonEnabled,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = GoldGold,
                                                    contentColor = BgDark,
                                                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                                                    disabledContentColor = Color.LightGray
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(44.dp)
                                                    .testTag("addon_buy_button_${addon.id}")
                                            ) {
                                                Text("إضافة الآن", fontWeight = FontWeight.Bold, color = if (buyAddonEnabled) BgDark else Color.LightGray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Addon confirmation dialog
            selectedAddonForConfirmation?.let { addon ->
                AlertDialog(
                    onDismissRequest = { selectedAddonForConfirmation = null },
                    title = {
                        Text(
                            text = "تأكيد إضافة الباقة",
                            color = GoldGold,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    },
                    text = {
                        Text(
                            text = "هل تريد إضافة باقة \"${addon.name ?: ""}\" التكميلية وتجميعها مع رصيد باقتك الحالية؟ سيتم خصم ${com.example.config.AppConfig.formatPriceWithCurrency(addon.price ?: 0.0)} من محفظتك.",
                            color = TextBody,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val targetId = addon.id
                                val targetName = addon.name ?: ""
                                val targetPrice = addon.price ?: 0.0
                                selectedAddonForConfirmation = null
                                showAddonsSheet = false
                                onBuyAddon(targetId, targetName, targetPrice)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldGold, contentColor = BgDark),
                            modifier = Modifier.testTag("confirm_addon_buy_button")
                        ) {
                            Text("تأكيد الإضافة", color = BgDark)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { selectedAddonForConfirmation = null },
                            modifier = Modifier.testTag("cancel_addon_buy_button")
                        ) {
                            Text("إلغاء", color = TextSecondary)
                        }
                    }
                )
            }

            // Full-screen loading spinner overlay for purchase in progress
            if (isBuyingAddon) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable(enabled = false) {}, // Disable background interaction
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = GoldGold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "جاري تفعيل الباقة الإضافية...",
                            color = GoldGold,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isHighlight) GoldGold.copy(alpha = 0.08f) else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (isHighlight) GoldGold else TextSecondary,
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
                color = if (isHighlight) GoldGold else TextBody
            )
        )
    }
}

@Composable
fun DashboardActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main action icon on the right (Start in RTL)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoldGold.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GoldGold,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Action text labels in the middle
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextBody
                    ),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Disclosure indicator arrow on the left (End in RTL)
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = GoldGold,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
