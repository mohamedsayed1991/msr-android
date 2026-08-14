package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.AppConfig
import com.example.data.model.Plan
import com.example.ui.theme.*
import com.example.ui.viewmodel.PlanSuccessInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    plans: List<Plan>,
    isLoading: Boolean,
    subscriberBalance: Double,
    generatedVoucherCode: String?,
    isPurchasing: Boolean,
    planType: String? = null,
    manageAction: String? = null,
    allowBuyPlan: Boolean = true,
    allowChange: Boolean = true,
    planSuccessDialog: PlanSuccessInfo? = null,
    onBack: () -> Unit,
    onPurchaseClick: (Plan) -> Unit,
    onDismissVoucherDialog: () -> Unit,
    onDismissSuccessDialog: () -> Unit
) {
    if (planType == "hotspot") {
        VoucherBottomSheetScreen(
            plans = plans,
            isLoading = isLoading,
            subscriberBalance = subscriberBalance,
            generatedVoucherCode = generatedVoucherCode,
            isPurchasing = isPurchasing,
            allowBuyPlan = allowBuyPlan,
            onBack = onBack,
            onPurchaseClick = onPurchaseClick,
            onDismissVoucherDialog = onDismissVoucherDialog
        )
        return
    }

    var selectedPlanForConfirmation by remember { mutableStateOf<Plan?>(null) }
    var lastPurchasedPlan by remember { mutableStateOf<Plan?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "الباقات المتاحة",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("plans_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize().testTag("plans_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading && plans.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(RadiusXL)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "رصيدك المتوفر للشحن حالياً",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = AppConfig.formatPriceWithCurrency(subscriberBalance),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black
                                    ),
                                    modifier = Modifier.testTag("plans_balance_text")
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "اختر الباقة المناسبة للاشتراك:",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(top = 4.dp, bottom = 0.dp)
                        )
                    }

                    items(plans) { plan ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("plan_card_${plan.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(RadiusXL)
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
                                        text = AppConfig.formatPriceWithCurrency(plan.price ?: 0.0),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Black
                                        )
                                    )
                                    Text(
                                        text = plan.name ?: "باقة شبكة",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.height(8.dp))

                                val validityText = remember(plan.validityDays) {
                                    val v = plan.validityDays ?: "30"
                                    if (v.contains("يوم") || v.contains("ساعة") || v.contains("day") || v.contains("hour")) v else "$v يوم"
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "مدة الصلاحية: $validityText",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                val canPerformAction = if (manageAction == "change") allowChange else allowBuyPlan
                                val buttonText = when (manageAction) {
                                    "change" -> "تغيير إلى هذه الباقة"
                                    else -> "الاشتراك بالباقة"
                                }
                                Button(
                                    onClick = {
                                        if (canPerformAction) selectedPlanForConfirmation = plan
                                    },
                                    enabled = canPerformAction,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    shape = RoundedCornerShape(RadiusLG),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("subscribe_button_${plan.id}")
                                ) {
                                    Text(buttonText, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Confirmation Dialog
            selectedPlanForConfirmation?.let { plan ->
                val title = when (manageAction) {
                    "change" -> "تأكيد تغيير الباقة"
                    else -> "تأكيد الاشتراك"
                }
                val text = when (manageAction) {
                    "change" -> "هل تريد تغيير الباقة إلى \"${plan.name}\" بقيمة ${AppConfig.formatPriceWithCurrency(plan.price ?: 0.0)}؟"
                    else -> "هل تريد الاشتراك في \"${plan.name}\" بقيمة ${AppConfig.formatPriceWithCurrency(plan.price ?: 0.0)}؟"
                }
                val confirmText = when (manageAction) {
                    "change" -> "تغيير الباقة"
                    else -> "تأكيد الاشتراك"
                }

                AlertDialog(
                    onDismissRequest = { selectedPlanForConfirmation = null },
                    title = { Text(title, fontWeight = FontWeight.Bold) },
                    text = { Text(text) },
                    confirmButton = {
                        Button(
                            onClick = {
                                lastPurchasedPlan = plan
                                selectedPlanForConfirmation = null
                                onPurchaseClick(plan)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(confirmText, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedPlanForConfirmation = null }) {
                            Text("إلغاء")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(RadiusXL)
                )
            }

            // Success Dialog
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
        }
    }
}

@Composable
fun AnimatedSuccessCheckmark(modifier: Modifier = Modifier) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.2f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "Scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "Alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(80.dp)
            .scale(scale)
            .alpha(alpha)
            .background(SuccessGreen.copy(alpha = 0.12f), CircleShape)
            .border(3.dp, SuccessGreen, CircleShape)
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = SuccessGreen,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherBottomSheetScreen(
    plans: List<Plan>,
    isLoading: Boolean,
    subscriberBalance: Double,
    generatedVoucherCode: String?,
    isPurchasing: Boolean,
    allowBuyPlan: Boolean = true,
    onBack: () -> Unit,
    onPurchaseClick: (Plan) -> Unit,
    onDismissVoucherDialog: () -> Unit
) {
    var selectedPlanForConfirmation by remember { mutableStateOf<Plan?>(null) }
    var lastPurchasedPlan by remember { mutableStateOf<Plan?>(null) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var sheetVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { sheetVisible = true }

    val handleDismiss = {
        sheetVisible = false
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { handleDismiss() }
    ) {
        AnimatedVisibility(
            visible = sheetVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                shape = RoundedCornerShape(topStart = RadiusXL, topEnd = RadiusXL),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clickable(enabled = false) { }
                    .testTag("voucher_bottom_sheet")
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 12.dp, bottom = 8.dp)
                            .size(40.dp, 4.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                RoundedCornerShape(2.dp)
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { handleDismiss() }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق")
                        }
                        Text(
                            text = if (generatedVoucherCode == null) "إنشاء كروت هوتسبوت" else "تم التوليد بنجاح!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    if (generatedVoucherCode == null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp)
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
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
                                    Text(
                                        text = AppConfig.formatPriceWithCurrency(subscriberBalance),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black
                                        )
                                    )
                                    Text(
                                        text = "رصيدك المتوفر",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "اختر كارت لشحنه:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (isLoading && plans.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(bottom = 24.dp)
                                ) {
                                    items(plans) { plan ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(RadiusXL))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                .clickable(enabled = allowBuyPlan) { selectedPlanForConfirmation = plan }
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(RadiusMD))
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = AppConfig.formatPriceWithCurrency(plan.price ?: 0.0),
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = FontWeight.Black
                                                    ),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            Column(
                                                horizontalAlignment = Alignment.End,
                                                modifier = Modifier.weight(1f).padding(end = 12.dp)
                                            ) {
                                                Text(
                                                    text = plan.name ?: "كارت شحن",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                val validityStr = remember(plan.validityDays) {
                                                    val v = plan.validityDays ?: "30"
                                                    if (v.contains("يوم") || v.contains("ساعة")) v else "$v يوم"
                                                }
                                                Text(
                                                    text = "صلاحية $validityStr",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                if (plan.totalGb != null && plan.totalGb > 0.0) {
                                                    Text(
                                                        text = "السعة: ${formatArabicDataSize(plan.totalGb)}",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        val dashedBorderModifier = Modifier.drawBehind {
                            val strokeWidth = 2.dp.toPx()
                            val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                            drawRoundRect(
                                color = MaterialTheme.colorScheme.primary,
                                style = Stroke(width = strokeWidth, pathEffect = dashPathEffect),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(RadiusXL.toPx())
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            AnimatedSuccessCheckmark()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "تم توليد الكارت بنجاح!",
                                color = SuccessGreen,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "باقة: ${lastPurchasedPlan?.name ?: "هوتسبوت"}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .then(dashedBorderModifier)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(RadiusXL)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = generatedVoucherCode,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.testTag("generated_voucher_code_text")
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(generatedVoucherCode))
                                    android.widget.Toast.makeText(context, "تم نسخ الكود", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(RadiusLG),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("copy_generated_code_button")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("نسخ كود الكارت", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "كود كارت الهوتسبوت: $generatedVoucherCode")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "مشاركة كود الكارت"))
                                },
                                shape = RoundedCornerShape(RadiusLG),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("مشاركة الكود", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            TextButton(onClick = onDismissVoucherDialog) {
                                Text("العودة للرئيسية", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Confirmation Dialog
        selectedPlanForConfirmation?.let { plan ->
            AlertDialog(
                onDismissRequest = { selectedPlanForConfirmation = null },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("تأكيد توليد كارت", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text("هل تريد توليد كارت \"${plan.name}\"؟ سيتم خصم ${AppConfig.formatPriceWithCurrency(plan.price ?: 0.0)} من محفظتك.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            lastPurchasedPlan = plan
                            selectedPlanForConfirmation = null
                            onPurchaseClick(plan)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(RadiusMD)
                    ) {
                        Text("تأكيد", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedPlanForConfirmation = null }) {
                        Text("تراجع")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(RadiusXL)
            )
        }

        // Loading Overlay
        if (isPurchasing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(RadiusXL)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "جاري توليد الكارت...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
