package com.example.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
        shape = RoundedCornerShape(RadiusXL)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-20).dp)
                    .background(Color.White.copy(alpha = 0.06f), shape = CircleShape)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(RadiusSM))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        text = validityText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = planName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

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
                            style = MaterialTheme.typography.displayMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 28.sp
                            )
                        )
                        Text(
                            text = "جيجابايت متبقية",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.75f)
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Text(
                        text = "من أصل ${formatQuotaValue(totalGb)} جيجا",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${(progress * 100).toInt()}% متوفر",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun DashboardActionButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(RadiusXL)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(RadiusMD))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
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
                            text = "MSR WI-FI",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
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
                    IconButton(onClick = onToggleTheme, modifier = Modifier.testTag("theme_toggle_button")) {
                        Icon(
                            imageVector = if (themeMode == 2) Icons.Default.WbSunny else Icons.Default.NightsStay,
                            contentDescription = "تبديل المظهر",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onRefresh, modifier = Modifier.testTag("refresh_button")) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("logout_button")) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "خروج",
                            tint = ErrorRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize().testTag("dashboard_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val currentSub = subscriber ?: SubscriberInfo(
                fullName = "مستخدم افتراضي",
                username = "guest",
                balance = 0.0,
                altPhone = ""
            )

            if (isLoading && subscriber == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Balance Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(RadiusXL)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            if (isOnline) {
                                val welcomeName = currentSub.displayFullName.ifBlank { currentSub.displayUsername }
                                Text(
                                    text = "مرحباً يا $welcomeName",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "الرصيد المتوفر",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                    )
                                )
                                val balance = currentSub.displayBalance
                                val balanceColor = if (balance < 0) Color.White else MaterialTheme.colorScheme.onPrimary
                                Text(
                                    text = AppConfig.formatPriceWithCurrency(balance),
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        color = balanceColor,
                                        fontWeight = FontWeight.Black
                                    ),
                                    modifier = Modifier.testTag("balance_text")
                                )
                            }

                            if ((currentSub.displayBalance) < 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "تنبيه: حسابك مدين حالياً، يرجى الشحن لتفادي قطع الخدمة.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                    )
                                )
                            }
                        }
                    }

                    // Outstanding Debt
                    val outstandingDebt = currentSub.displayOutstandingDebt
                    if (outstandingDebt > 0) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = ErrorRed.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(RadiusXL)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = ErrorRed
                                    )
                                    Text(
                                        text = "المديونية المستحقة",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = ErrorRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Text(
                                    text = AppConfig.formatPriceWithCurrency(outstandingDebt),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = ErrorRed,
                                        fontWeight = FontWeight.Black
                                    ),
                                    modifier = Modifier.testTag("outstanding_debt_text")
                                )
                            }
                        }
                    }

                    // Active Plan Cards
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

                        PremiumPlanCard(
                            badgeText = "الباقة الشهرية",
                            planName = activePlan.planName ?: activePlan.name ?: "الباقة النشطة",
                            remainingGb = remainingBasicGb,
                            totalGb = basicQuota,
                            progress = progressBasic,
                            validityText = activePlan.validityDays ?: "نشطة",
                            gradient = Brush.linearGradient(
                                colors = listOf(GradientPurple, GradientPink)
                            ),
                            testTag = "basic_plan_card"
                        )

                        if (hasAddon && addonQuota > 0.0) {
                            PremiumPlanCard(
                                badgeText = "الباقة الإضافية",
                                planName = "جيجات إضافية",
                                remainingGb = remainingAddonGb,
                                totalGb = addonQuota,
                                progress = progressAddon,
                                validityText = "تنتهي مع الأساسية",
                                gradient = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.secondary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                ),
                                testTag = "addon_plan_card"
                            )
                        }

                        // Action Buttons
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("action_buttons_card"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(RadiusXL)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (currentSub.showAddons) {
                                    OutlinedButton(
                                        onClick = {
                                            onLoadAddons()
                                            showAddonsSheet = true
                                        },
                                        shape = RoundedCornerShape(RadiusMD),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("extra_plan_button"),
                                        border = ButtonDefaults.outlinedButtonBorder
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("باقة إضافية", fontWeight = FontWeight.Bold)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val renewEnabled = currentSub.allowRenew
                                    OutlinedButton(
                                        onClick = {
                                            if (!renewEnabled) return@OutlinedButton
                                            if (!isExpired) {
                                                android.widget.Toast.makeText(context, "الباقة الحالية لا تزال نشطة. لا يمكنك التجديد الآن.", android.widget.Toast.LENGTH_LONG).show()
                                            } else {
                                                showRenewConfirmation = true
                                            }
                                        },
                                        enabled = renewEnabled,
                                        shape = RoundedCornerShape(RadiusMD),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("renew_plan_button"),
                                        border = ButtonDefaults.outlinedButtonBorder
                                    ) {
                                        Icon(imageVector = Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تجديد", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                    }

                                    val changeEnabled = currentSub.allowChange && currentSub.showPlans
                                    OutlinedButton(
                                        onClick = {
                                            if (!changeEnabled) return@OutlinedButton
                                            if (!isExpired) {
                                                android.widget.Toast.makeText(context, "لا يمكنك تغيير الباقة إلا بعد انتهائها.", android.widget.Toast.LENGTH_LONG).show()
                                            } else {
                                                onChangePlanClick()
                                            }
                                        },
                                        enabled = changeEnabled,
                                        shape = RoundedCornerShape(RadiusMD),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("change_plan_button"),
                                        border = ButtonDefaults.outlinedButtonBorder
                                    ) {
                                        Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تغيير", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                    }
                                }
                            }
                        }

                        // Plan Success Dialog
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
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = successInfo.message,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = onDismissSuccessDialog,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(RadiusMD)
                                    ) {
                                        Text("موافق", fontWeight = FontWeight.Bold)
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(RadiusXL)
                            )
                        }

                        // Renew Confirmation Dialog
                        if (showRenewConfirmation) {
                            AlertDialog(
                                onDismissRequest = { showRenewConfirmation = false },
                                title = {
                                    Text(
                                        text = "تأكيد تجديد الباقة",
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                text = {
                                    Text(
                                        text = "هل تريد تجديد باقة \"${activePlan.planName ?: activePlan.name ?: ""}\"؟ سيتم خصم قيمتها من رصيدك."
                                    )
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            showRenewConfirmation = false
                                            onRenewPlan(activePlan.planId ?: 0, activePlan.planType ?: "hotspot")
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Text("تأكيد")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showRenewConfirmation = false }) {
                                        Text("إلغاء")
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(RadiusXL)
                            )
                        }
                    } else {
                        // Empty state
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(RadiusXL)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "لا توجد باقة نشطة حالياً. يرجى الاشتراك في إحدى الباقات.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Services Section
                    Text(
                        text = "الخدمات المتاحة",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )

                    if (currentSub.allowBuyPlan && currentSub.showPlans) {
                        DashboardActionButton(
                            title = "إنشاء كروت",
                            subtitle = "توليد كروت شحن هوتسبوت جديدة من رصيدك",
                            icon = Icons.Default.ConfirmationNumber,
                            onClick = onGenerateVoucher,
                            tag = "action_generate_voucher"
                        )
                    }

                    if (currentSub.showRechargePage) {
                        DashboardActionButton(
                            title = "شحن رصيد المحفظة",
                            subtitle = "تحويل الكاش المباشر لإدارة الشبكة",
                            icon = Icons.Default.AddCard,
                            onClick = onRecharge,
                            tag = "action_recharge_screen"
                        )
                    }

                    DashboardActionButton(
                        title = "سجل العمليات",
                        subtitle = "مراجعة عمليات الشحن وتوليد الكروت السابقة",
                        icon = Icons.Default.History,
                        onClick = onTransactionsClick,
                        tag = "action_transactions_screen"
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Addons Bottom Sheet
            if (showAddonsSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showAddonsSheet = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = MaterialTheme.colorScheme.surface,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)) }
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
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(16.dp)
                        )

                        if (isLoadingAddons) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        } else if (addonPlans.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "لا توجد باقات إضافية متاحة",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
                                            .testTag("addon_card_${addon.id}"),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        shape = RoundedCornerShape(RadiusLG)
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
                                                Text(
                                                    text = addon.name ?: "باقة إضافية",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                                Text(
                                                    text = AppConfig.formatPriceWithCurrency(addon.price ?: 0.0),
                                                    style = MaterialTheme.typography.titleLarge.copy(
                                                        color = MaterialTheme.colorScheme.primary,
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
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                                ),
                                                shape = RoundedCornerShape(RadiusMD),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(44.dp)
                                                    .testTag("addon_buy_button_${addon.id}")
                                            ) {
                                                Text("إضافة الآن", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Addon Confirmation Dialog
            selectedAddonForConfirmation?.let { addon ->
                AlertDialog(
                    onDismissRequest = { selectedAddonForConfirmation = null },
                    title = {
                        Text(
                            text = "تأكيد إضافة الباقة",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = "هل تريد إضافة باقة \"${addon.name ?: ""}\"؟ سيتم خصم ${AppConfig.formatPriceWithCurrency(addon.price ?: 0.0)} من محفظتك."
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("confirm_addon_buy_button")
                        ) {
                            Text("تأكيد الإضافة")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { selectedAddonForConfirmation = null },
                            modifier = Modifier.testTag("cancel_addon_buy_button")
                        ) {
                            Text("إلغاء")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(RadiusXL)
                )
            }

            // Full-screen loading overlay
            if (isBuyingAddon) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(RadiusXL),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "جاري إضافة الباقة...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
