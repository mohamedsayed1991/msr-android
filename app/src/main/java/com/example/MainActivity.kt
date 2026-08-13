package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SubscriberViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SubscriberViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val isDark = when (themeMode) {
                1 -> false // Light
                2 -> true  // Dark
                else -> androidx.compose.foundation.isSystemInDarkTheme() // System
            }

            MyApplicationTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MsrAppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

// Routes IDs
const val ROUTE_SPLASH = "splash"
const val ROUTE_LOGIN = "login"
const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_PLANS = "plans"
const val ROUTE_RECHARGE = "recharge"
const val ROUTE_VOUCHER = "voucher"
const val ROUTE_TRANSACTIONS = "transactions"

@Composable
fun MsrAppNavigation(viewModel: SubscriberViewModel = viewModel()) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState()

    var activeMessage by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    // Collect and display Custom In-App Snackbar / Alert
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { message ->
            activeMessage = message
            keyboardController?.hide()
        }
    }

    // Auto-dismiss custom snackbar after 4 seconds
    LaunchedEffect(activeMessage) {
        if (activeMessage != null) {
            keyboardController?.hide()
            kotlinx.coroutines.delay(4000)
            activeMessage = null
        }
    }

    // Determine initial route based on authentication
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val startDestination = ROUTE_SPLASH

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
        // 1. Splash Discovery Screen
        composable(ROUTE_SPLASH) {
            val isDiscovering by viewModel.isDiscovering.collectAsState()
            
            // Auto start DNS network discovery upon opening splash
            LaunchedEffect(Unit) {
                viewModel.runAutoDiscovery(onFinished = {
                    if (isAuthenticated) {
                        navController.navigate(ROUTE_DASHBOARD) {
                            popUpTo(ROUTE_SPLASH) { inclusive = true }
                        }
                    } else {
                        navController.navigate(ROUTE_LOGIN) {
                            popUpTo(ROUTE_SPLASH) { inclusive = true }
                        }
                    }
                })
            }

            SplashDiscoveryScreen(
                isDiscovering = isDiscovering,
                onDiscoveryFinished = {
                    if (isAuthenticated) {
                        navController.navigate(ROUTE_DASHBOARD) {
                            popUpTo(ROUTE_SPLASH) { inclusive = true }
                        }
                    } else {
                        navController.navigate(ROUTE_LOGIN) {
                            popUpTo(ROUTE_SPLASH) { inclusive = true }
                        }
                    }
                }
            )
        }

        // 2. Dual Login Screen
        composable(ROUTE_LOGIN) {
            val isLoggingIn by viewModel.isLoggingIn.collectAsState()
            val isCheckingVoucher by viewModel.isCheckingVoucher.collectAsState()
            val tenantSystemName by viewModel.tenantSystemName.collectAsState()

            LoginScreen(
                isLoggingIn = isLoggingIn,
                isCheckingVoucher = isCheckingVoucher,
                tenantSystemName = tenantSystemName,
                onLoginClick = { username, password ->
                    viewModel.loginSubscriber(username, password, onSuccess = {
                        navController.navigate(ROUTE_DASHBOARD) {
                            popUpTo(ROUTE_LOGIN) { inclusive = true }
                        }
                    })
                },
                onVoucherClick = { code ->
                    viewModel.checkVoucher(code, onSuccess = {
                        navController.navigate(ROUTE_VOUCHER)
                    })
                },
                themeMode = themeMode,
                onToggleTheme = { viewModel.toggleTheme() }
            )
        }

        // 3. Subscriber Dashboard
        composable(ROUTE_DASHBOARD) {
            val subscriberInfo by viewModel.subscriberInfo.collectAsState()
            val isLoadingMe by viewModel.isLoadingMe.collectAsState()
            val planSuccessDialog by viewModel.planSuccessDialog.collectAsState()
            val isOnline by viewModel.isOnline.collectAsState()
            val addonPlans by viewModel.addonPlans.collectAsState()
            val isLoadingAddons by viewModel.isLoadingAddons.collectAsState()
            val isBuyingAddon by viewModel.isBuyingAddon.collectAsState()

            // Fetch latest statistics when dashboard is launched
            LaunchedEffect(Unit) {
                viewModel.refreshMe()
            }

            DashboardScreen(
                subscriber = subscriberInfo,
                isLoading = isLoadingMe,
                isOnline = isOnline,
                onRefresh = { viewModel.refreshMe() },
                onViewPlans = {
                    if (subscriberInfo?.showPlans != false) {
                        viewModel.setManageAction(null)
                        viewModel.loadPlans()
                        navController.navigate("plans")
                    }
                },
                onGenerateVoucher = {
                    if (subscriberInfo?.allowBuyPlan != false && subscriberInfo?.showPlans != false) {
                        viewModel.setManageAction(null)
                        viewModel.loadVoucherPlans()
                        navController.navigate("plans?type=hotspot")
                    }
                },
                onRecharge = {
                    if (subscriberInfo?.showRechargePage != false) {
                        navController.navigate(ROUTE_RECHARGE)
                    }
                },
                onTransactionsClick = {
                    navController.navigate(ROUTE_TRANSACTIONS)
                },
                onLogout = {
                    viewModel.logout()
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(ROUTE_DASHBOARD) { inclusive = true }
                    }
                },
                onRenewPlan = { planId, planType ->
                    if (subscriberInfo?.allowRenew != false) {
                        viewModel.managePlan("renew", planId, planType)
                    }
                },
                onChangePlanClick = {
                    if (subscriberInfo?.allowChange != false && subscriberInfo?.showPlans != false) {
                        viewModel.setManageAction("change")
                        viewModel.loadPlans("subscriber")
                        navController.navigate("plans?type=subscriber")
                    }
                },
                planSuccessDialog = planSuccessDialog,
                onDismissSuccessDialog = {
                    viewModel.dismissPlanSuccessDialog()
                    viewModel.refreshMe()
                },
                addonPlans = addonPlans,
                isLoadingAddons = isLoadingAddons,
                isBuyingAddon = isBuyingAddon,
                onLoadAddons = { viewModel.loadAddonPlans() },
                onBuyAddon = { planId, name, price -> viewModel.buyAddonPlan(planId, name, price) },
                themeMode = themeMode,
                onToggleTheme = { viewModel.toggleTheme() }
            )
        }

        // 4. Plans List
        composable("plans?type={type}") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type")
            val isVoucher = type == "hotspot"
            val plans by (if (isVoucher) viewModel.voucherPlans else viewModel.plans).collectAsState()
            val isLoadingPlans by (if (isVoucher) viewModel.isLoadingVoucherPlans else viewModel.isLoadingPlans).collectAsState()
            val subscriberInfo by viewModel.subscriberInfo.collectAsState()
            val isPurchasing by viewModel.isPurchasing.collectAsState()
            val generatedVoucherCode by viewModel.generatedVoucherCode.collectAsState()
            val activeManageAction by viewModel.activeManageAction.collectAsState()
            val planSuccessDialog by viewModel.planSuccessDialog.collectAsState()

            val balance = subscriberInfo?.balance ?: 0.0

            PlansScreen(
                plans = plans,
                isLoading = isLoadingPlans,
                subscriberBalance = balance,
                generatedVoucherCode = generatedVoucherCode,
                isPurchasing = isPurchasing,
                planType = type,
                manageAction = activeManageAction,
                allowBuyPlan = subscriberInfo?.allowBuyPlan ?: true,
                allowChange = subscriberInfo?.allowChange ?: true,
                planSuccessDialog = planSuccessDialog,
                onBack = { 
                    viewModel.setManageAction(null)
                    navController.popBackStack() 
                },
                onPurchaseClick = { plan ->
                    if (activeManageAction != null) {
                        viewModel.managePlan(activeManageAction!!, plan, planType = type ?: "subscriber")
                    } else {
                        viewModel.purchasePlan(plan, planType = type)
                    }
                },
                onDismissVoucherDialog = {
                    viewModel.setManageAction(null)
                    viewModel.dismissVoucherDialog()
                    viewModel.refreshMe()
                },
                onDismissSuccessDialog = {
                    viewModel.setManageAction(null)
                    viewModel.dismissPlanSuccessDialog()
                    viewModel.refreshMe()
                    // Pop backstack to dashboard after subscribing successfully on the Plans screen
                    navController.popBackStack()
                }
            )
        }

        // 5. Wallet Recharge Config
        composable(ROUTE_RECHARGE) {
            val subscriberInfo by viewModel.subscriberInfo.collectAsState()
            val isUpdatingProfile by viewModel.isUpdatingProfile.collectAsState()
            val walletPhone by viewModel.walletPhone.collectAsState()
            val currentAltPhone = subscriberInfo?.altPhone ?: ""

            LaunchedEffect(Unit) {
                viewModel.loadWalletPhone()
            }

            RechargeScreen(
                walletPhone = walletPhone,
                currentAltPhone = currentAltPhone,
                isUpdating = isUpdatingProfile,
                onBack = { navController.popBackStack() },
                onSaveAltPhone = { altPhone ->
                    viewModel.saveAlternativePhone(altPhone)
                }
            )
        }

        // 6. Voucher details view
        composable(ROUTE_VOUCHER) {
            val voucherInfo by viewModel.voucherInfo.collectAsState()

            VoucherViewScreen(
                voucher = voucherInfo,
                onBack = { navController.popBackStack() }
            )
        }

        // 7. Transaction History
        composable(ROUTE_TRANSACTIONS) {
            val subscriberInfo by viewModel.subscriberInfo.collectAsState()
            val transactions = subscriberInfo?.transactions ?: emptyList()

            TransactionsScreen(
                transactions = transactions,
                onBack = { navController.popBackStack() }
            )
        }
    }
        activeMessage?.let { msg ->
            // Backdrop Scrim to block background interactions and focus on the notification
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { activeMessage = null },
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + scaleIn(initialScale = 0.9f),
                    exit = fadeOut() + scaleOut(targetScale = 0.9f),
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(0.85f)
                ) {
                    androidx.compose.material3.Card(
                        shape = RoundedCornerShape(com.example.ui.theme.RadiusLG),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 16.dp),
                        modifier = Modifier
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                RoundedCornerShape(com.example.ui.theme.RadiusLG)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { /* Prevent clicks inside the card from dismissing it */ }
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Icon indicating alert / information status
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(36.dp)
                                    .padding(bottom = 12.dp)
                            )
                            
                            androidx.compose.material3.Text(
                                text = msg,
                                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            androidx.compose.material3.Button(
                                onClick = { activeMessage = null },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(com.example.ui.theme.RadiusMD),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                androidx.compose.material3.Text(
                                    "حسناً",
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
