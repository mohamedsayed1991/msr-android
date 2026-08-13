package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
    var showPackageDetailsDialog by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "الباقات المتاحة للشبكة",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldGold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("plans_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = GoldGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark,
        modifier = Modifier.fillMaxSize().testTag("plans_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgDark)
        ) {
            if (isLoading && plans.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldGold)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
                ) {
                    // Header card warning or info
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "رصيدك المتوفر للشحن حالياً",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = com.example.config.AppConfig.formatPriceWithCurrency(subscriberBalance),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = GoldGold,
                                        fontWeight = FontWeight.Black
                                    ),
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth().testTag("plans_balance_text")
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "اختر الباقة المناسبة للاشتراك أو لتوليد الكارت:",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextBody, fontWeight = FontWeight.Bold),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                            textAlign = TextAlign.Start
                        )
                    }

                    items(plans) { plan ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GoldGold.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                .testTag("plan_card_${plan.id}"),
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                              ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Price Label
                                    Text(
                                        text = com.example.config.AppConfig.formatPriceWithCurrency(plan.price ?: 0.0),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = GoldGold,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 20.sp
                                        )
                                    )
                                    // Title Label
                                    Text(
                                        text = plan.name ?: "باقة شبكة",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = TextBody,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = GoldGold.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    // Validity statistic
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        val validityText = remember(plan.validityDays) {
                                            val v = plan.validityDays ?: "30"
                                            if (v.contains("يوم") || v.contains("ساعة") || v.contains("day") || v.contains("hour")) {
                                                v
                                            } else {
                                                "$v يوم"
                                            }
                                        }
                                        Text(
                                            text = validityText,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                color = TextBody,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = "مدة الصلاحية",
                                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                val canPerformAction = if (manageAction == "change") allowChange else allowBuyPlan
                                Button(
                                    onClick = {
                                        if (canPerformAction) {
                                            selectedPlanForConfirmation = plan
                                        }
                                    },
                                    enabled = canPerformAction,
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
                                        .testTag("subscribe_button_${plan.id}")
                                ) {
                                    val buttonText = when (manageAction) {
                                        "change" -> "تغيير إلى هذه الباقة"
                                        else -> if (planType == "hotspot") "إنشاء كارت" else "الاشتراك بالباقة وتوليد كارت"
                                    }
                                    Text(
                                        text = buttonText,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (canPerformAction) BgDark else Color.LightGray
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 1. Plan Subscription Confirmation Dialog
            selectedPlanForConfirmation?.let { plan ->
                val dialogTitle = when (manageAction) {
                    "change" -> "تأكيد تغيير الباقة"
                    else -> if (planType == "hotspot") "تأكيد إنشاء كارت" else "تأكيد الاشتراك وتوليد الكارت"
                }

                val dialogText = when (manageAction) {
                    "change" -> "هل أنت متأكد من رغبتك في تغيير الباقة إلى: \"${plan.name}\" بقيمة ${com.example.config.AppConfig.formatPriceWithCurrency(plan.price ?: 0.0)}؟ سيتم اقتطاع القيمة من رصيدك، ومسح باقتك السابقة وتفعيل الباقة الجديدة بصلاحيتها وحدودها الكاملة."
                    else -> if (planType == "hotspot") "هل أنت متأكد من رغبتك في إنشاء كارت باقة: \"${plan.name}\" بقيمة ${com.example.config.AppConfig.formatPriceWithCurrency(plan.price ?: 0.0)}؟ سيتم اقتطاع القيمة من رصيدك الحالي." else "هل أنت متأكد من رغبتك في الاشتراك في الباقة: \"${plan.name}\" بقيمة ${com.example.config.AppConfig.formatPriceWithCurrency(plan.price ?: 0.0)}؟ سيتم اقتطاع القيمة من رصيدك الحالي."
                }

                val confirmButtonText = when (manageAction) {
                    "change" -> "تغيير الباقة"
                    else -> if (planType == "hotspot") "تأكيد الإنشاء" else "تأكيد الاشتراك"
                }

                AlertDialog(
                    onDismissRequest = { selectedPlanForConfirmation = null },
                    title = {
                        Text(
                            text = dialogTitle,
                            color = GoldGold,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    },
                    text = {
                        Text(
                            text = dialogText,
                            color = TextBody,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                lastPurchasedPlan = plan
                                selectedPlanForConfirmation = null
                                onPurchaseClick(plan)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldGold, contentColor = BgDark)
                        ) {
                            Text(confirmButtonText, color = BgDark)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedPlanForConfirmation = null }) {
                            Text("إلغاء", color = TextSecondary)
                        }
                    },
                    containerColor = CardDark
                )
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

            // 3. Voucher Package Details Dialog
            if (showPackageDetailsDialog && lastPurchasedPlan != null) {
                AlertDialog(
                    onDismissRequest = { showPackageDetailsDialog = false },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = GoldGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تفاصيل باقة الكارت",
                                color = GoldGold,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BgDark),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, GoldGold.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Package Name
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("اسم الباقة:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                                        Text(lastPurchasedPlan?.name ?: "", color = TextBody, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    
                                    HorizontalDivider(color = GoldGold.copy(alpha = 0.1f), thickness = 1.dp)

                                    // Validity
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("فترة الصلاحية:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                                        val validityText = remember(lastPurchasedPlan?.validityDays) {
                                            val v = lastPurchasedPlan?.validityDays ?: "30"
                                            if (v.contains("يوم") || v.contains("ساعة") || v.contains("day") || v.contains("hour")) {
                                                v
                                            } else {
                                                "$v يوم"
                                            }
                                        }
                                        Text(validityText, color = TextBody, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    }

                                    HorizontalDivider(color = GoldGold.copy(alpha = 0.1f), thickness = 1.dp)

                                    // Price
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("سعر الاشتراك:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                                        Text(com.example.config.AppConfig.formatPriceWithCurrency(lastPurchasedPlan?.price ?: 0.0), color = GoldGold, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showPackageDetailsDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldGold, contentColor = BgDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("إغلاق", color = BgDark, fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = CardDark
                )
            }
        }
    }
}

@Composable
fun AnimatedSuccessCheckmark(modifier: Modifier = Modifier) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
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
            .background(SuccessGreen.copy(alpha = 0.15f), CircleShape)
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
    
    // Animate bottom sheet visibility on start
    var sheetVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        sheetVisible = true
    }

    // Helper closure to handle dismiss back to dashboard
    val handleDismiss = {
        sheetVisible = false
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { handleDismiss() }
    ) {
        // Slide up animation container
        AnimatedVisibility(
            visible = sheetVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clickable(enabled = false) { } // prevent clicks passing to backdrop
                    .border(1.dp, GoldGold.copy(alpha = 0.2f), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .testTag("voucher_bottom_sheet")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp)
                ) {
                    // 1. Drag Handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 12.dp, bottom = 8.dp)
                            .size(40.dp, 4.dp)
                            .background(GoldGold.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                    )

                    // 2. Title & Close bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { handleDismiss() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = GoldGold
                            )
                        }
                        
                        Text(
                            text = if (generatedVoucherCode == null) "إنشاء كروت هوتسبوت" else "تم التوليد بنجاح!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GoldGold
                            ),
                            textAlign = TextAlign.End
                        )
                    }

                    HorizontalDivider(color = GoldGold.copy(alpha = 0.1f), thickness = 1.dp)

                    if (generatedVoucherCode == null) {
                        // Display balance and selection list
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp)
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Balance card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BgDark),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, GoldGold.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = com.example.config.AppConfig.formatPriceWithCurrency(subscriberBalance),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            color = GoldGold,
                                            fontWeight = FontWeight.Black
                                        )
                                    )
                                    
                                    Text(
                                        text = "رصيدك المتوفر حالياً",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "اختر كارت لشحنه وتوليد الكود:",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextBody,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (isLoading && plans.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = GoldGold)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(bottom = 24.dp)
                                ) {
                                    items(plans) { plan ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(BgDark)
                                                .border(
                                                    1.dp,
                                                    GoldGold.copy(alpha = 0.15f),
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .clickable(enabled = allowBuyPlan) { selectedPlanForConfirmation = plan }
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            // Left: Price Badge (Highlight)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(GoldGold.copy(alpha = 0.12f))
                                                    .border(
                                                        1.dp,
                                                        GoldGold.copy(alpha = 0.25f),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = com.example.config.AppConfig.formatPriceWithCurrency(plan.price ?: 0.0),
                                                    color = GoldGold,
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = FontWeight.Black
                                                    )
                                                )
                                            }

                                            // Right: Plan details
                                            Column(
                                                horizontalAlignment = Alignment.End,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 12.dp)
                                            ) {
                                                Text(
                                                    text = plan.name ?: "كارت شحن هوتسبوت",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextBody
                                                    )
                                                )
                                                
                                                Spacer(modifier = Modifier.height(4.dp))
                                                
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    val validityStr = remember(plan.validityDays) {
                                                        val v = plan.validityDays ?: "30"
                                                        if (v.contains("يوم") || v.contains("ساعة") || v.contains("day") || v.contains("hour")) {
                                                            v
                                                        } else {
                                                            "$v يوم"
                                                        }
                                                    }
                                                    Text(
                                                        text = "صلاحية $validityStr",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = TextSecondary
                                                        )
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.CalendarToday,
                                                        contentDescription = null,
                                                        tint = GoldGold,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                
                                                if (plan.totalGb != null && plan.totalGb > 0.0) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "السعة: ${formatArabicDataSize(plan.totalGb)}",
                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                color = GoldGold,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        )
                                                        Icon(
                                                            imageVector = Icons.Default.DataUsage,
                                                            contentDescription = null,
                                                            tint = GoldGold,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Success Screen (Seamless receipt)
                        val primaryColor = GoldGold
                        val dashedBorderModifier = Modifier.drawBehind {
                            val strokeWidth = 2.dp.toPx()
                            val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                            drawRoundRect(
                                color = primaryColor,
                                style = Stroke(width = strokeWidth, pathEffect = dashPathEffect),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
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
                                text = "تم توليد الكارت بنجاح! 🎉",
                                color = SuccessGreen,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "باقة: ${lastPurchasedPlan?.name ?: "هوتسبوت"}",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Highlighted dashed-border box containing the voucher code
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .then(dashedBorderModifier)
                                    .background(BgDark, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = generatedVoucherCode,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        color = GoldGold,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.testTag("generated_voucher_code_text")
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Action: Copy Code
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(generatedVoucherCode))
                                    android.widget.Toast.makeText(context, "تم نسخ كود الكارت بنجاح", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldGold,
                                    contentColor = BgDark
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("copy_generated_code_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        tint = BgDark
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "نسخ كود الكارت",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge.copy(color = BgDark)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Action: Share Code
                            OutlinedButton(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "كود كارت الهوتسبوت الخاص بك لشبكة ${AppConfig.tenantSystemName}: $generatedVoucherCode"
                                        )
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "مشاركة كود الكارت")
                                    context.startActivity(shareIntent)
                                },
                                border = BorderStroke(1.dp, GoldGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        tint = GoldGold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "مشاركة الكود",
                                        color = GoldGold,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Done / Back to main button
                            TextButton(
                                onClick = onDismissVoucherDialog
                            ) {
                                Text(
                                    text = "العودة للرئيسية",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Confirmation dialog
        selectedPlanForConfirmation?.let { plan ->
            AlertDialog(
                onDismissRequest = { selectedPlanForConfirmation = null },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = GoldGold
                        )
                        Text(
                            text = "تأكيد توليد كارت",
                            color = GoldGold,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                text = {
                    Text(
                        text = "هل أنت متأكد من توليد كارت \"${plan.name}\"؟ سيتم خصم ${com.example.config.AppConfig.formatPriceWithCurrency(plan.price ?: 0.0)} من محفظتك.",
                        color = TextBody,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            lastPurchasedPlan = plan
                            selectedPlanForConfirmation = null
                            onPurchaseClick(plan)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldGold,
                            contentColor = BgDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تأكيد", color = BgDark, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { selectedPlanForConfirmation = null }
                    ) {
                        Text("تراجع", color = TextSecondary)
                    }
                },
                containerColor = CardDark,
                shape = RoundedCornerShape(24.dp)
            )
        }

        // Loading Overlay
        if (isPurchasing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.50f))
                    .clickable(enabled = false) { }, // Block clicks
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = GoldGold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "جاري توليد الكارت، يرجى الانتظار...",
                            color = TextBody,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
