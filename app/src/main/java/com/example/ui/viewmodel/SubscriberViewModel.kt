package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.config.AppConfig
import com.example.data.model.*
import com.example.data.repository.SubscriberRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubscriberViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SubscriberRepository(application)
    private val prefs = application.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)

    // Theme Mode: 0 = System, 1 = Light, 2 = Dark
    private val _themeMode = MutableStateFlow(prefs.getInt("theme_mode", 0))
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    fun setThemeMode(mode: Int) {
        _themeMode.value = mode
        prefs.edit().putInt("theme_mode", mode).apply()
    }

    fun toggleTheme() {
        val currentMode = _themeMode.value
        val nextMode = if (currentMode == 2) 1 else 2 // Switch strictly between Light (1) and Dark (2)
        setThemeMode(nextMode)
    }

    // General App Alert/Toast Event flow
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Manage Action Success Event flow (for add/change/renew navigation back)
    private val _manageSuccessEvent = MutableSharedFlow<Unit>()
    val manageSuccessEvent: SharedFlow<Unit> = _manageSuccessEvent.asSharedFlow()

    // Simplified Plan Success Dialog State
    private val _planSuccessDialog = MutableStateFlow<PlanSuccessInfo?>(null)
    val planSuccessDialog: StateFlow<PlanSuccessInfo?> = _planSuccessDialog.asStateFlow()

    fun dismissPlanSuccessDialog() {
        _planSuccessDialog.value = null
    }

    // 1. Discovery State
    private val _tenantSystemName = MutableStateFlow(repository.getSavedTenantSystemName())
    val tenantSystemName: StateFlow<String> = _tenantSystemName.asStateFlow()

    private val _tenantUsername = MutableStateFlow(repository.getSavedTenantUsername())
    val tenantUsername: StateFlow<String> = _tenantUsername.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    private val _discoveryCompleted = MutableStateFlow(false)
    val discoveryCompleted: StateFlow<Boolean> = _discoveryCompleted.asStateFlow()

    // 2. Auth State
    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(repository.getToken() != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    // 3. Current Subscriber Info
    private val _subscriberInfo = MutableStateFlow<SubscriberInfo?>(null)
    val subscriberInfo: StateFlow<SubscriberInfo?> = _subscriberInfo.asStateFlow()

    private val _isLoadingMe = MutableStateFlow(false)
    val isLoadingMe: StateFlow<Boolean> = _isLoadingMe.asStateFlow()

    // 3b. Wallet Phone State
    private val _walletPhone = MutableStateFlow(repository.getSavedWalletPhone())
    val walletPhone: StateFlow<String> = _walletPhone.asStateFlow()

    fun loadWalletPhone() {
        viewModelScope.launch {
            val fetched = repository.fetchWalletPhone()
            if (!fetched.isNullOrBlank()) {
                _walletPhone.value = fetched
            }
        }
    }

    // 4. Checked Voucher Info (Voucher Mode)
    private val _isCheckingVoucher = MutableStateFlow(false)
    val isCheckingVoucher: StateFlow<Boolean> = _isCheckingVoucher.asStateFlow()

    private val _voucherInfo = MutableStateFlow<VoucherInfoResponse?>(null)
    val voucherInfo: StateFlow<VoucherInfoResponse?> = _voucherInfo.asStateFlow()

    // 5. Available Network Plans
    private val _plans = MutableStateFlow<List<Plan>>(emptyList())
    val plans: StateFlow<List<Plan>> = _plans.asStateFlow()

    private val _isLoadingPlans = MutableStateFlow(false)
    val isLoadingPlans: StateFlow<Boolean> = _isLoadingPlans.asStateFlow()

    // 5b. Voucher specific plans (Isolated state)
    private val _voucherPlans = MutableStateFlow<List<Plan>>(emptyList())
    val voucherPlans: StateFlow<List<Plan>> = _voucherPlans.asStateFlow()

    private val _isLoadingVoucherPlans = MutableStateFlow(false)
    val isLoadingVoucherPlans: StateFlow<Boolean> = _isLoadingVoucherPlans.asStateFlow()

    // 5c. Available Addon Plans (Extra Plans)
    private val _addonPlans = MutableStateFlow<List<AddonPlan>>(emptyList())
    val addonPlans: StateFlow<List<AddonPlan>> = _addonPlans.asStateFlow()

    private val _isLoadingAddons = MutableStateFlow(false)
    val isLoadingAddons: StateFlow<Boolean> = _isLoadingAddons.asStateFlow()

    private val _isBuyingAddon = MutableStateFlow(false)
    val isBuyingAddon: StateFlow<Boolean> = _isBuyingAddon.asStateFlow()

    // 6. Buying / Voucher Generation
    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing.asStateFlow()

    private val _generatedVoucherCode = MutableStateFlow<String?>(null)
    val generatedVoucherCode: StateFlow<String?> = _generatedVoucherCode.asStateFlow()

    // 7. Profile updating
    private val _isUpdatingProfile = MutableStateFlow(false)
    val isUpdatingProfile: StateFlow<Boolean> = _isUpdatingProfile.asStateFlow()

    // 8. Active Manage Action (for Add or Change current plan)
    private val _activeManageAction = MutableStateFlow<String?>(null)
    val activeManageAction: StateFlow<String?> = _activeManageAction.asStateFlow()

    fun setManageAction(action: String?) {
        _activeManageAction.value = action
    }

    // 9. Real-time Connection status
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private fun sanitizeErrorMessage(message: String?): String {
        if (message == null || message.trim().isEmpty()) {
            return "حدث خطأ غير متوقع، يرجى المحاولة لاحقاً"
        }
        
        // Try to parse error if it is JSON
        val parsedMsg = if (message.trim().startsWith("{")) {
            try {
                val json = org.json.JSONObject(message)
                json.optString("error", json.optString("message", message))
            } catch (e: Exception) {
                message
            }
        } else {
            message
        }

        val techKeywords = listOf(
            "connect", "timeout", "socket", "host", "exception", "dns", "refused", "resolved", "failed to"
        )
        val hasTechDetail = techKeywords.any { parsedMsg.lowercase().contains(it) }
        
        if (hasTechDetail) {
            return "تعذر الاتصال بالسيرفر، يرجى المحاولة لاحقاً"
        }
        return parsedMsg
    }

    init {
        // If already authenticated, load offline fallback details immediately and fetch fresh server data
        if (_isAuthenticated.value) {
            _subscriberInfo.value = repository.getOfflineSubscriberInfo()
            refreshMe()
        }
        loadWalletPhone()
    }

    /**
     * Launch network/DNS discovery for Network Owner (Tenant ID)
     */
    fun runAutoDiscovery(onFinished: () -> Unit) {
        viewModelScope.launch {
            _isDiscovering.value = true
            val tenantInfo = repository.autoDiscoverTenant()
            if (tenantInfo != null) {
                _isOnline.value = true
                tenantInfo.accountId?.let { id ->
                    if (id.isNotBlank()) {
                        AppConfig.accountId = id
                        repository.saveAccountId(id)
                    }
                }
                val sysName = tenantInfo.systemName ?: AppConfig.tenantSystemName
                val usrName = tenantInfo.username ?: AppConfig.tenantUsername
                _tenantSystemName.value = sysName
                _tenantUsername.value = usrName
                tenantInfo.extractWalletPhone()?.let { phone ->
                    _walletPhone.value = phone
                }
            } else {
                _isOnline.value = false
                val savedAccountId = repository.getSavedAccountId()
                val savedUsername = repository.getSavedTenantUsername()
                if (savedAccountId.isNotEmpty() || savedUsername.isNotEmpty()) {
                    AppConfig.accountId = savedAccountId
                    AppConfig.tenantUsername = savedUsername
                    AppConfig.tenantSystemName = repository.getSavedTenantSystemName()
                    AppConfig.walletPhone = repository.getSavedWalletPhone()
                    _tenantSystemName.value = AppConfig.tenantSystemName
                    _tenantUsername.value = AppConfig.tenantUsername
                    _toastEvent.emit("تعذر الاتصال بالشبكة المحلية. تم استخدام الإعدادات المحفوظة: ${AppConfig.tenantSystemName}")
                } else {
                    AppConfig.tenantSystemName = "شبكة MSR"
                    _tenantSystemName.value = AppConfig.tenantSystemName
                    _tenantUsername.value = ""
                    _toastEvent.emit("تعذر اكتشاف الشبكة. تأكد من اتصالك بالشبكة المحلية")
                }
            }
            loadWalletPhone()
            _isDiscovering.value = false
            _discoveryCompleted.value = true
            onFinished()
        }
    }

    /**
     * Set Account ID manually if auto discovery failed
     */
    fun setManualAccountId(accountIdInput: String, onSuccess: () -> Unit) {
        val trimmed = accountIdInput.trim()
        if (trimmed.isEmpty()) {
            viewModelScope.launch { _toastEvent.emit("يرجى إدخال رقم الحساب") }
            return
        }
        viewModelScope.launch {
            _isDiscovering.value = true
            val info = repository.getTenantInfoByAccountId(trimmed)
            if (info != null) {
                _tenantSystemName.value = info.systemName ?: "شبكة MSR"
                _tenantUsername.value = info.username ?: ""
                _toastEvent.emit("تم التعرف على الشبكة بنجاح")
            } else {
                repository.saveAccountId(trimmed)
                _toastEvent.emit("تم حفظ رقم الحساب: $trimmed")
            }
            _isDiscovering.value = false
            onSuccess()
        }
    }

    /**
     * Subscriber login mode
     */
    fun loginSubscriber(usernameInput: String, passwordInput: String, onSuccess: () -> Unit) {
        if (usernameInput.trim().isEmpty() || passwordInput.trim().isEmpty()) {
            viewModelScope.launch { _toastEvent.emit("يرجى إدخال اسم المستخدم وكلمة المرور") }
            return
        }

        viewModelScope.launch {
            _isLoggingIn.value = true
            val activeAccountId = AppConfig.accountId.ifBlank {
                repository.getSavedAccountId()
            }
            val activeTenantUsername = AppConfig.tenantUsername.ifBlank {
                repository.getSavedTenantUsername()
            }
            if (activeAccountId.isBlank() && activeTenantUsername.isBlank()) {
                _toastEvent.emit("لم يتم التعرف على الشبكة. يرجى إدخال رقم الحساب أولاً")
                _isLoggingIn.value = false
                return@launch
            }
            if (activeAccountId.isNotBlank()) AppConfig.accountId = activeAccountId
            if (activeTenantUsername.isNotBlank()) AppConfig.tenantUsername = activeTenantUsername

            val req = LoginRequest(
                accountId = activeAccountId.ifBlank { null },
                tenantUsername = activeTenantUsername.ifBlank { null },
                username = usernameInput.trim(),
                password = passwordInput.trim()
            )
            val result = repository.login(req)
            result.fold(
                onSuccess = { res ->
                    _isAuthenticated.value = true
                    _subscriberInfo.value = res.subscriber
                    _isOnline.value = true
                    res.subscriber?.extractWalletPhone()?.let { phone ->
                        _walletPhone.value = phone
                    }
                    res.systemName?.let { name ->
                        repository.saveSystemName(name)
                        _tenantSystemName.value = name
                    }
                    _toastEvent.emit("تم تسجيل الدخول بنجاح")
                    onSuccess()
                },
                onFailure = { err ->
                    val msg = err.message ?: "فشل تسجيل الدخول"
                    _toastEvent.emit(sanitizeErrorMessage(msg))
                }
            )
            _isLoggingIn.value = false
        }
    }

    /**
     * Voucher Mode check
     */
    fun checkVoucher(code: String, onSuccess: () -> Unit) {
        if (code.trim().isEmpty()) {
            viewModelScope.launch { _toastEvent.emit("يرجى إدخال كود الكارت") }
            return
        }

        viewModelScope.launch {
            _isCheckingVoucher.value = true
            val tenant = AppConfig.tenantUsername.ifEmpty { AppConfig.accountId }
            val result = repository.getVoucherInfo(code.trim(), tenant)
            result.fold(
                onSuccess = { response ->
                    _voucherInfo.value = response
                    _isOnline.value = true
                    onSuccess()
                },
                onFailure = { err ->
                    _isOnline.value = false
                    val msg = err.message ?: ""
                    if (msg == "FORBIDDEN_NEW_VOUCHER") {
                        _toastEvent.emit("هذا الكارت جديد ولم يتم تفعيله بعد")
                    } else {
                        _toastEvent.emit(sanitizeErrorMessage("رقم الكارت غير صحيح أو تعذر الاتصال بالسيرفر"))
                    }
                }
            )
            _isCheckingVoucher.value = false
        }
    }

    /**
     * Fetch Subscriber dynamic data (Refresh Dashboard)
     */
    fun refreshMe() {
        viewModelScope.launch {
            _isLoadingMe.value = true
            val result = repository.getMe()
            result.fold(
                onSuccess = { info ->
                    _subscriberInfo.value = info
                    info.extractWalletPhone()?.let { phone ->
                        _walletPhone.value = phone
                    }
                    info.systemName?.let { name ->
                        repository.saveSystemName(name)
                        _tenantSystemName.value = name
                    }
                    _isOnline.value = true
                },
                onFailure = { err ->
                    _isOnline.value = false
                    // Load local offline data as fallback
                    _subscriberInfo.value = repository.getOfflineSubscriberInfo()
                    _toastEvent.emit(sanitizeErrorMessage("تعذر تحديث البيانات: " + err.message))
                }
            )
            loadWalletPhone()
            _isLoadingMe.value = false
        }
    }

    fun getSubscriberTarget(): String {
        val activePlan = _subscriberInfo.value?.activePlan
        val type = activePlan?.planType?.lowercase() ?: ""
        return if (type == "pppoe") "pppoe" else "hotspot_subscription"
    }

    /**
     * Fetch network plans (with optional type parameter like 'hotspot')
     */
    fun loadPlans(type: String? = null, mode: String? = null, target: String? = null) {
        viewModelScope.launch {
            _isLoadingPlans.value = true
            val finalTarget = target ?: getSubscriberTarget()
            val result = repository.getPlans(type, mode = mode, target = finalTarget)
            result.fold(
                onSuccess = { list ->
                    _plans.value = list
                    _isOnline.value = true
                },
                onFailure = { err ->
                    _isOnline.value = false
                    _toastEvent.emit(sanitizeErrorMessage("فشل تحميل الباقات: " + err.message))
                }
            )
            _isLoadingPlans.value = false
        }
    }

    /**
     * Fetch hotspot voucher plans specifically
     */
    fun loadVoucherPlans() {
        viewModelScope.launch {
            _isLoadingVoucherPlans.value = true
            val result = repository.getVoucherPlans()
            result.fold(
                onSuccess = { list ->
                    _voucherPlans.value = list
                    _isOnline.value = true
                },
                onFailure = { err ->
                    _isOnline.value = false
                    _toastEvent.emit(sanitizeErrorMessage("فشل تحميل باقات الكروت: " + err.message))
                }
            )
            _isLoadingVoucherPlans.value = false
        }
    }

    /**
     * Fetch available addon (extra) plans
     */
    fun loadAddonPlans() {
        viewModelScope.launch {
            _isLoadingAddons.value = true
            val result = repository.getAddons()
            result.fold(
                onSuccess = { list ->
                    _addonPlans.value = list
                    _isOnline.value = true
                },
                onFailure = { err ->
                    _isOnline.value = false
                    _toastEvent.emit(sanitizeErrorMessage("فشل جلب الباقات الإضافية: " + err.message))
                }
            )
            _isLoadingAddons.value = false
        }
    }

    /**
     * Buy an addon (extra) plan
     */
    fun buyAddonPlan(planId: Int, planName: String, price: Double) {
        val currentBalance = _subscriberInfo.value?.displayBalance ?: 0.0
        if (currentBalance < price) {
            viewModelScope.launch { _toastEvent.emit("رصيدك الحالي لا يكفي للاشتراك في هذه الباقة الإضافية. يرجى الشحن أولاً.") }
            return
        }

        viewModelScope.launch {
            _isBuyingAddon.value = true
            val result = repository.buyAddon(planId)
            result.fold(
                onSuccess = { response ->
                    _toastEvent.emit("تمت إضافة الباقة الإضافية وتجميع الجيجات بنجاح! 🎉")
                    // Instantly refresh subscriber details
                    refreshMe()
                },
                onFailure = { err ->
                    _toastEvent.emit(sanitizeErrorMessage("فشل شراء الباقة الإضافية: " + err.message))
                }
            )
            _isBuyingAddon.value = false
        }
    }

    /**
     * Purchase a specific plan / Generate Voucher from balance
     */
    fun purchasePlan(plan: Plan, planType: String? = null) {
        val currentBalance = _subscriberInfo.value?.displayBalance ?: 0.0
        val price = plan.price ?: 0.0
        if (currentBalance < price) {
            viewModelScope.launch { _toastEvent.emit("رصيدك الحالي لا يكفي للاشتراك في هذه الباقة. يرجى الشحن أولاً.") }
            return
        }

        _generatedVoucherCode.value = null

        viewModelScope.launch {
            _isPurchasing.value = true
            val target = getSubscriberTarget()
            val resolvedPlanType = if (planType == "subscriber" || planType == null) {
                if (target == "pppoe") "pppoe" else "hotspot"
            } else {
                planType
            }
            val result = repository.buyPlan(plan.id, AppConfig.tenantUsername.ifEmpty { AppConfig.accountId }, resolvedPlanType)
            result.fold(
                onSuccess = { response ->
                    val rawCode = response.displayVoucherCode ?: run {
                        val part1 = (1000..9999).random()
                        val part2 = (1000..9999).random()
                        val part3 = (1000..9999).random()
                        "$part1$part2$part3"
                    }
                    val generatedCode = rawCode.replace("-", "")
                    
                    if (planType == "hotspot") {
                        _generatedVoucherCode.value = generatedCode
                        // Save local voucher for history enrichment
                        repository.saveLocalVoucher(
                            planName = plan.name ?: "",
                            amount = price,
                            voucherCode = generatedCode
                        )
                    }
                    
                    // Deduct balance locally or use the new balance from the server
                    val currentInfo = _subscriberInfo.value
                    if (currentInfo != null) {
                        val expiryDateStr = run {
                            val cal = java.util.Calendar.getInstance()
                            val days = plan.validityDays?.toIntOrNull() ?: 30
                            cal.add(java.util.Calendar.DAY_OF_YEAR, days)
                            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).format(cal.time)
                        }
                        val newBal = response.newBalance ?: currentInfo.balance?.minus(price)
                        
                        val isHotspot = planType == "hotspot"
                        val txId = if (isHotspot) generatedCode else "TXN-${System.currentTimeMillis()}"
                        val txDesc = if (isHotspot) {
                            "توليد كارت شحن: ${plan.name} (كود الكارت: $generatedCode)"
                        } else {
                            "الاشتراك في باقة: ${plan.name}"
                        }

                        val newTxn = Transaction(
                            id = (100000..999999).random(),
                            amount = price,
                            type = "DEBIT",
                            txnId = txId,
                            description = txDesc,
                            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).format(java.util.Date())
                        )
                        val updatedTransactions = listOf(newTxn) + (currentInfo.transactions ?: emptyList())

                        val updatedInfo = currentInfo.copy(
                            balance = newBal,
                            wallet = currentInfo.wallet?.copy(balance = newBal),
                            activePlan = if (!isHotspot) ActivePlanInfo(
                                planId = plan.id,
                                name = plan.name,
                                totalGb = plan.totalGb,
                                usedGb = 0.0,
                                remainingGb = plan.totalGb,
                                validityDays = expiryDateStr
                            ) else currentInfo.activePlan,
                            transactions = updatedTransactions
                        )
                        _subscriberInfo.value = updatedInfo
                        repository.saveSubscriberInfo(updatedInfo)
                    }
                    if (planType == "hotspot") {
                        _toastEvent.emit("تم الإنشاء")
                    } else {
                        _toastEvent.emit("تم الاشتراك بنجاح! ✅")
                        _planSuccessDialog.value = PlanSuccessInfo(
                            action = "subscribe",
                            message = "تم الاشتراك في الباقة بنجاح! ✅",
                            voucherCode = null
                        )
                    }
                    _isOnline.value = true
                },
                onFailure = { err ->
                    _isOnline.value = false
                    val errorMsg = err.message ?: "فشل في إتمام العملية من السيرفر"
                    _toastEvent.emit(sanitizeErrorMessage(errorMsg))
                }
            )
            _isPurchasing.value = false
        }
    }

    /**
     * Manage subscriber plan (renew, add, change) via server endpoint
     */
    fun managePlan(action: String, plan: Plan, planType: String? = null) {
        val planId = plan.id
        val planName = plan.name ?: ""
        val price = plan.price ?: 0.0
        
        _generatedVoucherCode.value = null

        viewModelScope.launch {
            _isPurchasing.value = true
            val target = getSubscriberTarget()
            val isSubscriberAction = action in listOf("renew", "change")
            val resolvedPlanType = if (isSubscriberAction) {
                "subscriber"
            } else if (planType == "subscriber" || planType == null) {
                if (target == "pppoe") "pppoe" else "hotspot"
            } else {
                planType
            }
            val resolvedTarget = if (isSubscriberAction) null else target
            val result = repository.managePlan(action, planId, resolvedPlanType, mode = null, target = resolvedTarget)
            result.fold(
                onSuccess = { response ->
                    val successMsg = response.message ?: "تمت العملية بنجاح."
                    _toastEvent.emit(successMsg)
                    
                    val vCode = response.voucherCode
                    val isPlaceholder = vCode.isNullOrEmpty() || vCode == "9090" || vCode.length <= 4
                    val cleanCode = if (!isPlaceholder) vCode!!.replace("-", "") else null
                    
                    val isDashboardAction = action in listOf("renew", "change")

                    if (cleanCode != null && !isDashboardAction) {
                        _generatedVoucherCode.value = cleanCode
                        // Save local voucher for history enrichment
                        repository.saveLocalVoucher(
                            planName = planName,
                            amount = price,
                            voucherCode = cleanCode
                        )
                    }

                    // Update balance and transactions locally if returned
                    val currentInfo = _subscriberInfo.value
                    if (currentInfo != null) {
                        val newBal = response.newBalance ?: (if (price > 0.0) currentInfo.balance?.minus(price) else null)
                        var updatedTransactions = currentInfo.transactions ?: emptyList()
                        
                        val txId = if (cleanCode != null && !isDashboardAction) cleanCode else "TXN-${System.currentTimeMillis()}"
                        val txDesc = when (action) {
                            "renew" -> "تجديد الباقة الحالية: $planName"
                            "change" -> "تغيير الباقة إلى: $planName"
                            else -> "إجراء على الباقة: $planName"
                        }

                        val newTxn = Transaction(
                            id = (100000..999999).random(),
                            amount = price,
                            type = "DEBIT",
                            txnId = txId,
                            description = txDesc,
                            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).format(java.util.Date())
                        )
                        updatedTransactions = listOf(newTxn) + updatedTransactions
                        
                        val updatedInfo = currentInfo.copy(
                            balance = newBal ?: currentInfo.balance,
                            wallet = currentInfo.wallet?.copy(balance = newBal ?: currentInfo.balance),
                            transactions = updatedTransactions
                        )
                        _subscriberInfo.value = updatedInfo
                        repository.saveSubscriberInfo(updatedInfo)
                    }
                    
                    // Refresh me to load new active plan from server
                    refreshMe()
                    _isOnline.value = true
                    
                    val dispMsg = when (action) {
                        "renew" -> "تم تجديد الباقة بنجاح! ✅"
                        "change" -> "تم تغيير الباقة بنجاح! ✅"
                        else -> "تم تنفيذ العملية بنجاح! ✅"
                    }
                    _planSuccessDialog.value = PlanSuccessInfo(
                        action = action,
                        message = dispMsg,
                        voucherCode = null
                    )
                },
                onFailure = { err ->
                    _isOnline.value = false
                    _toastEvent.emit(sanitizeErrorMessage("فشل تنفيذ العملية: " + err.message))
                }
            )
            _isPurchasing.value = false
        }
    }

    fun managePlan(action: String, planId: Int, planType: String? = null) {
        val matchedPlan = _plans.value?.firstOrNull { it.id == planId }
            ?: _voucherPlans.value?.firstOrNull { it.id == planId }
        val planName = matchedPlan?.name ?: (_subscriberInfo.value?.activePlan?.name ?: "")
        val price = matchedPlan?.price ?: 0.0
        val planObj = matchedPlan ?: Plan(id = planId, name = planName, price = price, totalGb = null, validityDays = null)
        managePlan(action, planObj, planType)
    }

    fun dismissVoucherDialog() {
        _generatedVoucherCode.value = null
    }

    /**
     * Save alternative payment wallet phone
     */
    fun saveAlternativePhone(phone: String) {
        if (phone.trim().isEmpty()) {
            viewModelScope.launch { _toastEvent.emit("يرجى إدخال رقم هاتف صحيح") }
            return
        }

        viewModelScope.launch {
            _isUpdatingProfile.value = true
            val result = repository.sendPaymentRequest(phone.trim())
            result.fold(
                onSuccess = {
                    _toastEvent.emit("تم استلام الطلب. بانتظار تحويل المبلغ من نفس الرقم خلال ساعة كحد أقصى")
                },
                onFailure = { err ->
                    // Update local copy even on server failure/offline
                    val info = _subscriberInfo.value
                    if (info != null) {
                        val updated = info.copy(altPhone = phone.trim())
                        _subscriberInfo.value = updated
                        repository.saveSubscriberInfo(updated)
                    }
                    _toastEvent.emit(sanitizeErrorMessage("فشل إرسال الطلب: " + (err.message ?: "خطأ غير معروف")))
                }
            )
            _isUpdatingProfile.value = false
        }
    }

    /**
     * Logout
     */
    fun logout() {
        repository.clearSession()
        _isAuthenticated.value = false
        _subscriberInfo.value = null
        _voucherInfo.value = null
    }
}

data class PlanSuccessInfo(
    val action: String, // "renew", "add", "change"
    val message: String,
    val voucherCode: String? = null
)
