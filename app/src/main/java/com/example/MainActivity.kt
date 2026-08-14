package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SubscriberViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SubscriberViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val isDark = when (themeMode) {
                1 -> false
                2 -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
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

const val ROUTE_SPLASH = "splash"
const val ROUTE_LOGIN = "login"
const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_PLANS = "plans"
const val ROUTE_RECHARGE = "recharge"
const val ROUTE_VOUCHER = "voucher"
const val ROUTE_TRANSACTIONS = "transactions"
const val ROUTE_SETTINGS = "settings"

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(ROUTE_DASHBOARD, "الرئيسية", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(ROUTE_PLANS, "الخطط", Icons.Filled.CreditCard, Icons.Outlined.CreditCard),
    BottomNavItem(ROUTE_TRANSACTIONS, "العمليات", Icons.Filled.Receipt, Icons.Outlined.Receipt),
    BottomNavItem(ROUTE_SETTINGS, "الإعدادات", Icons.Filled.Settings, Icons.Outlined.Settings),
)

@Composable
fun MsrAppNavigation(viewModel: SubscriberViewModel = viewModel()) {
    val navController = rememberNavController()
    val keyboardController = LocalSoftwareKeyboardController.current

    var activeMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { message ->
            activeMessage = message
            keyboardController?.hide()
        }
    }

    LaunchedEffect(activeMessage) {
        if (activeMessage != null) {
            keyboardController?.hide()
            kotlinx.coroutines.delay(4000)
            activeMessage = null
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = ROUTE_SPLASH,
                modifier = Modifier.weight(1f)
            ) {
                composable(ROUTE_SPLASH) {
                    val isDiscovering by viewModel.isDiscovering.collectAsState()

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
                        },
                        onManualAccountSubmit = { accountId ->
                            viewModel.setManualAccountId(accountId, onSuccess = {
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
                    )
                }

                composable(ROUTE_LOGIN) {
                    val isLoggingIn by viewModel.isLoggingIn.collectAsState()
                    val isCheckingVoucher by viewModel.isCheckingVoucher.collectAsState()
                    val tenantSystemName by viewModel.tenantSystemName.collectAsState()
                    val themeMode by viewModel.themeMode.collectAsState()

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

                composable(ROUTE_DASHBOARD) {
                    val subscriberInfo by viewModel.subscriberInfo.collectAsState()
                    val isLoadingMe by viewModel.isLoadingMe.collectAsState()
                    val planSuccessDialog by viewModel.planSuccessDialog.collectAsState()
                    val isOnline by viewModel.isOnline.collectAsState()
                    val addonPlans by viewModel.addonPlans.collectAsState()
                    val isLoadingAddons by viewModel.isLoadingAddons.collectAsState()
                    val isBuyingAddon by viewModel.isBuyingAddon.collectAsState()
                    val themeMode by viewModel.themeMode.collectAsState()

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
                                navController.navigate(ROUTE_PLANS)
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
                            navController.popBackStack()
                        }
                    )
                }

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

                composable(ROUTE_VOUCHER) {
                    val voucherInfo by viewModel.voucherInfo.collectAsState()

                    VoucherViewScreen(
                        voucher = voucherInfo,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(ROUTE_TRANSACTIONS) {
                    val subscriberInfo by viewModel.subscriberInfo.collectAsState()
                    val transactions = subscriberInfo?.transactions ?: emptyList()

                    TransactionsScreen(
                        transactions = transactions,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(ROUTE_SETTINGS) {
                    val themeMode by viewModel.themeMode.collectAsState()
                    val subscriberInfo by viewModel.subscriberInfo.collectAsState()

                    SettingsScreen(
                        themeMode = themeMode,
                        onToggleTheme = { viewModel.toggleTheme() },
                        subscriberName = subscriberInfo?.fullName ?: subscriberInfo?.username ?: "",
                        onLogout = {
                            viewModel.logout()
                            navController.navigate(ROUTE_LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }

            if (showBottomBar) {
                BottomNavBar(
                    items = bottomNavItems,
                    currentRoute = currentRoute,
                    onNavigate = { item ->
                        if (item.route != currentRoute) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }

        activeMessage?.let { msg ->
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
                Card(
                    shape = RoundedCornerShape(RadiusXL),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(0.85f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { activeMessage = null },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(RadiusMD),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("حسناً", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onNavigate: (BottomNavItem) -> Unit
) {
    Surface(
        tonalElevation = 0.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                val icon = if (selected) item.selectedIcon else item.unselectedIcon

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(RadiusMD))
                        .clickable { onNavigate(item) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(RadiusMD))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = item.label,
                            tint = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
