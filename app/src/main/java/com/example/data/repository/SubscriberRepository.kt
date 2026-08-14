package com.example.data.repository

import android.content.Context
import com.example.config.AppConfig
import com.example.data.model.*
import com.example.data.network.ApiService
import com.example.data.network.MikrotikApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

class SubscriberRepository(private val context: Context) {

    private val apiService = ApiService.create()
    private val mikrotikApiService = MikrotikApiService.create()
    private val prefs = context.getSharedPreferences("msr_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TENANT_ID = "tenant_id"
        private const val KEY_TENANT_USERNAME = "tenant_username"
        private const val KEY_TENANT_SYSTEM_NAME = "tenant_system_name"
        private const val KEY_WALLET_PHONE = "wallet_phone"
        private const val KEY_TOKEN = "token"
        private const val KEY_SUB_USERNAME = "sub_username"
        private const val KEY_SUB_FULLNAME = "sub_fullname"
        private const val KEY_SUB_BALANCE = "sub_balance"
        private const val KEY_ALT_PHONE = "alt_phone"
        private const val KEY_ACTIVE_PLAN_NAME = "active_plan_name"
        private const val KEY_ACTIVE_PLAN_TOTAL = "active_plan_total"
        private const val KEY_ACTIVE_PLAN_USED = "active_plan_used"
        private const val KEY_ACTIVE_PLAN_REMAINING = "active_plan_remaining"
        private const val KEY_ACTIVE_PLAN_VALIDITY = "active_plan_validity"
        private const val KEY_TRANSACTIONS = "transactions_json"
    }

    init {
        // Load configurations into AppConfig memory from SharedPreferences
        AppConfig.serverIp = prefs.getString("server_ip", "13.53.130.231") ?: "13.53.130.231"
        AppConfig.serverPort = prefs.getInt("server_port", 8080)
        AppConfig.accountId = prefs.getString("account_id", "") ?: ""
        AppConfig.tenantUsername = prefs.getString(KEY_TENANT_USERNAME, "") ?: ""
        AppConfig.tenantSystemName = prefs.getString(KEY_TENANT_SYSTEM_NAME, "شبكة MSR") ?: "شبكة MSR"
        AppConfig.walletPhone = prefs.getString(KEY_WALLET_PHONE, "") ?: ""
        AppConfig.currency = prefs.getString("currency", "ج.م") ?: "ج.م"
    }

    /**
     * Reads tenant info from local MikroTik (192.168.88.1/tenant-info.json)
     */
    suspend fun autoDetectFromMikrotik(): TenantInfoResponse? = withContext(Dispatchers.IO) {
        try {
            val response = mikrotikApiService.getMikrotikTenantInfo()
            if (response.isSuccessful && response.body() != null) {
                val mikrotikData = response.body()!!
                
                // Update server IP & Port if provided by Mikrotik JSON
                mikrotikData.serverIp?.takeIf { it.isNotBlank() }?.let { ip ->
                    AppConfig.serverIp = ip
                    prefs.edit().putString("server_ip", ip).apply()
                }
                mikrotikData.serverPort?.let { port ->
                    AppConfig.serverPort = port
                    prefs.edit().putInt("server_port", port).apply()
                }

                val accountId = mikrotikData.accountId
                if (!accountId.isNullOrBlank()) {
                    AppConfig.accountId = accountId
                    prefs.edit().putString("account_id", accountId).apply()
                    
                    val parts = accountId.split("_")
                    if (parts.size == 2) {
                        try {
                            val serverResp = apiService.getTenantInfo(
                                userId = parts[0],
                                routerId = parts[1]
                            )
                            if (serverResp.isSuccessful && serverResp.body() != null) {
                                val serverData = serverResp.body()!!
                                if (!serverData.username.isNullOrBlank() || !serverData.accountId.isNullOrBlank()) {
                                    saveTenantInfo(serverData)
                                    return@withContext serverData
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                val tenantUsername = mikrotikData.username
                if (!tenantUsername.isNullOrBlank()) {
                    try {
                        val serverResp = apiService.getTenantInfo(tenant = tenantUsername)
                        if (serverResp.isSuccessful && serverResp.body() != null) {
                            val serverData = serverResp.body()!!
                            if (!serverData.username.isNullOrBlank() || !serverData.accountId.isNullOrBlank()) {
                                saveTenantInfo(serverData)
                                return@withContext serverData
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Fallback to local Mikrotik data if main server query fails
                val fallbackTenant = TenantInfoResponse(
                    id = mikrotikData.tenantId,
                    accountId = mikrotikData.accountId,
                    userId = mikrotikData.userId,
                    routerId = mikrotikData.routerId,
                    username = mikrotikData.username,
                    systemName = mikrotikData.systemName ?: "شبكة MSR"
                )
                saveTenantInfo(fallbackTenant)
                return@withContext fallbackTenant
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Tries to discover Tenant info via Mikrotik hotspot then direct HTTP GET
     */
    suspend fun autoDiscoverTenant(): TenantInfoResponse? = withContext(Dispatchers.IO) {
        // Step 1: Try Mikrotik auto-detection from 192.168.88.1/tenant-info.json
        val mikrotikTenant = autoDetectFromMikrotik()
        if (mikrotikTenant != null) {
            return@withContext mikrotikTenant
        }

        // Step 2: Direct server GET /api/subscriber/tenant-info
        try {
            val response = apiService.getTenantInfo()
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                if (!data.username.isNullOrBlank() || !data.accountId.isNullOrBlank()) {
                    saveTenantInfo(data)
                    return@withContext data
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Step 3: Try server tenant-info with saved tenant username
        val savedUsername = getSavedTenantUsername()
        if (savedUsername.isNotBlank()) {
            try {
                val response = apiService.getTenantInfo(tenant = savedUsername)
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    if (!data.username.isNullOrBlank() || !data.accountId.isNullOrBlank()) {
                        saveTenantInfo(data)
                        return@withContext data
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Step 4: Try account_id from SharedPreferences
        val savedAccountId = prefs.getString("account_id", "") ?: ""
        if (savedAccountId.isNotBlank()) {
            val parts = savedAccountId.split("_")
            if (parts.size == 2) {
                try {
                    val response = apiService.getTenantInfo(
                        userId = parts[0],
                        routerId = parts[1]
                    )
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!
                        if (!data.username.isNullOrBlank() || !data.accountId.isNullOrBlank()) {
                            saveTenantInfo(data)
                            return@withContext data
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        null
    }

    fun clearSubscriberSession() {
        AppConfig.token = ""
        clearSession()
    }

    fun saveTenantInfo(data: TenantInfoResponse) {
        val id = data.id ?: 0
        val accountId = data.accountId?.takeIf { it.isNotBlank() }
            ?: if (data.userId != null && data.routerId != null) "${data.userId}_${data.routerId}" else ""
        val username = data.username ?: ""
        val systemName = data.systemName ?: "شبكة MSR"
        val walletPhone = data.extractWalletPhone() ?: data.walletPhone ?: ""
        val currency = data.currency ?: "ج.م"

        val currentSavedAccountId = prefs.getString("account_id", "") ?: ""
        val currentSavedUsername = prefs.getString(KEY_TENANT_USERNAME, "") ?: ""

        if ((accountId.isNotBlank() && accountId != currentSavedAccountId) ||
            (username.isNotBlank() && username != currentSavedUsername && currentSavedUsername.isNotBlank())) {
            clearSubscriberSession()
        }

        if (accountId.isNotBlank()) {
            AppConfig.accountId = accountId
            prefs.edit().putString("account_id", accountId).apply()
        }

        AppConfig.tenantUsername = username
        AppConfig.tenantSystemName = systemName
        if (walletPhone.isNotBlank()) {
            AppConfig.walletPhone = walletPhone
        }
        if (currency.isNotBlank()) {
            AppConfig.currency = currency
        }

        prefs.edit().apply {
            putInt(KEY_TENANT_ID, id)
            if (accountId.isNotBlank()) putString("account_id", accountId)
            putString(KEY_TENANT_USERNAME, username)
            putString(KEY_TENANT_SYSTEM_NAME, systemName)
            if (walletPhone.isNotBlank()) putString(KEY_WALLET_PHONE, walletPhone)
            if (currency.isNotBlank()) putString("currency", currency)
            apply()
        }
    }

    fun saveSystemName(name: String) {
        AppConfig.tenantSystemName = name
        prefs.edit().putString(KEY_TENANT_SYSTEM_NAME, name).apply()
    }

    fun clearTenantInfo() {
        AppConfig.accountId = ""
        AppConfig.tenantUsername = ""
        AppConfig.tenantSystemName = "شبكة MSR"
        prefs.edit().apply {
            remove(KEY_TENANT_ID)
            remove("account_id")
            remove(KEY_TENANT_USERNAME)
            remove(KEY_TENANT_SYSTEM_NAME)
            apply()
        }
    }

    private fun getSavedTenantInfoAsResponse(): TenantInfoResponse {
        var savedUsername = prefs.getString(KEY_TENANT_USERNAME, "") ?: ""
        var savedAccountId = prefs.getString("account_id", "") ?: ""
        var savedSystemName = prefs.getString(KEY_TENANT_SYSTEM_NAME, "شبكة MSR") ?: "شبكة MSR"
        var savedWalletPhone = prefs.getString(KEY_WALLET_PHONE, "") ?: ""
        var savedCurrency = prefs.getString("currency", "ج.م") ?: "ج.م"
        val savedId = prefs.getInt(KEY_TENANT_ID, 0)

        if (savedUsername.isEmpty() && savedAccountId.isEmpty()) {
            savedUsername = ""
            savedSystemName = "شبكة MSR"
            savedWalletPhone = prefs.getString(KEY_WALLET_PHONE, "") ?: ""
            savedCurrency = "ج.م"
        }

        AppConfig.accountId = savedAccountId
        AppConfig.tenantUsername = savedUsername
        AppConfig.tenantSystemName = savedSystemName
        AppConfig.walletPhone = savedWalletPhone
        AppConfig.currency = savedCurrency

        return TenantInfoResponse(
            id = savedId,
            accountId = savedAccountId,
            username = savedUsername,
            systemName = savedSystemName,
            walletPhone = savedWalletPhone,
            currency = savedCurrency
        )
    }

    fun getSavedAccountId(): String = prefs.getString("account_id", "") ?: ""

    fun saveAccountId(accountId: String) {
        AppConfig.accountId = accountId
        prefs.edit().putString("account_id", accountId).apply()
    }

    suspend fun getTenantInfoByAccountId(accountId: String): TenantInfoResponse? = withContext(Dispatchers.IO) {
        val currentSavedAccountId = prefs.getString("account_id", "") ?: ""
        if (accountId.isNotBlank() && accountId != currentSavedAccountId) {
            clearSubscriberSession()
        }
        val parts = accountId.split("_")
        if (parts.size == 2) {
            try {
                val response = apiService.getTenantInfo(
                    userId = parts[0],
                    routerId = parts[1]
                )
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    saveTenantInfo(data)
                    return@withContext data
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val fallback = TenantInfoResponse(
            accountId = accountId,
            systemName = "شبكة MSR"
        )
        saveTenantInfo(fallback)
        fallback
    }

    fun getSavedTenantUsername(): String = prefs.getString(KEY_TENANT_USERNAME, "") ?: ""
    fun getSavedTenantSystemName(): String = prefs.getString(KEY_TENANT_SYSTEM_NAME, "شبكة MSR") ?: "شبكة MSR"
    fun getSavedWalletPhone(): String = prefs.getString(KEY_WALLET_PHONE, "") ?: ""

    // Token
    fun saveToken(token: String) {
        AppConfig.token = token
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        val t = AppConfig.token.ifEmpty { prefs.getString(KEY_TOKEN, null) }
        return if (t.isNullOrEmpty()) null else t
    }

    fun clearSession() {
        prefs.edit().apply {
            remove(KEY_TOKEN)
            remove(KEY_SUB_USERNAME)
            remove(KEY_SUB_FULLNAME)
            remove(KEY_SUB_BALANCE)
            remove(KEY_ACTIVE_PLAN_NAME)
            remove("sub_allow_renew")
            remove("sub_allow_change")
            remove("sub_allow_buy_plan")
            remove("sub_allow_buy_addon")
            remove("sub_show_recharge_page")
            remove("sub_show_plans")
            remove("sub_show_addons")
            // مسح بيانات الشبكة القديمة عند تسجيل الخروج
            remove("account_id")
            remove(KEY_TENANT_USERNAME)
            remove(KEY_TENANT_SYSTEM_NAME)
            remove(KEY_WALLET_PHONE)
            apply()
        }
    }

    // Save subscriber info (for Fallback)
    fun saveSubscriberInfo(info: SubscriberInfo) {
        info.currency?.let {
            AppConfig.currency = it
        }
        prefs.edit().apply {
            putString(KEY_SUB_USERNAME, info.username)
            putString(KEY_SUB_FULLNAME, info.fullName)
            putFloat(KEY_SUB_BALANCE, (info.balance ?: 0.0).toFloat())
            putString(KEY_ALT_PHONE, info.altPhone)
            putFloat("outstanding_debt", (info.displayOutstandingDebt ?: 0.0).toFloat())
            info.currency?.let {
                putString("currency", it)
            }

            putBoolean("sub_allow_renew", info.subAllowRenew ?: true)
            putBoolean("sub_allow_change", info.subAllowChange ?: true)
            putBoolean("sub_allow_buy_plan", info.subAllowBuyPlan ?: true)
            putBoolean("sub_allow_buy_addon", info.subAllowBuyAddon ?: true)
            putBoolean("sub_show_recharge_page", info.subShowRechargePage ?: true)
            putBoolean("sub_show_plans", info.subShowPlans ?: true)
            putBoolean("sub_show_addons", info.subShowAddons ?: true)
            
            info.activePlan?.let { plan ->
                putString(KEY_ACTIVE_PLAN_NAME, plan.name)
                putFloat(KEY_ACTIVE_PLAN_TOTAL, (plan.totalGb ?: 0.0).toFloat())
                putFloat(KEY_ACTIVE_PLAN_USED, (plan.usedGb ?: 0.0).toFloat())
                putFloat(KEY_ACTIVE_PLAN_REMAINING, (plan.remainingGb ?: 0.0).toFloat())
                putString(KEY_ACTIVE_PLAN_VALIDITY, plan.validityDays)
            } ?: run {
                remove(KEY_ACTIVE_PLAN_NAME)
            }

            info.transactions?.let { txns ->
                try {
                    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                    val type = Types.newParameterizedType(List::class.java, Transaction::class.java)
                    val adapter = moshi.adapter<List<Transaction>>(type)
                    putString(KEY_TRANSACTIONS, adapter.toJson(txns))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } ?: run {
                remove(KEY_TRANSACTIONS)
            }

            apply()
        }
    }

    fun getOfflineSubscriberInfo(): SubscriberInfo {
        val activePlanName = prefs.getString(KEY_ACTIVE_PLAN_NAME, null)
        val activePlan = if (activePlanName != null) {
            ActivePlanInfo(
                name = activePlanName,
                totalGb = prefs.getFloat(KEY_ACTIVE_PLAN_TOTAL, 0f).toDouble(),
                usedGb = prefs.getFloat(KEY_ACTIVE_PLAN_USED, 0f).toDouble(),
                remainingGb = prefs.getFloat(KEY_ACTIVE_PLAN_REMAINING, 0f).toDouble(),
                validityDays = prefs.getString(KEY_ACTIVE_PLAN_VALIDITY, null)
            )
        } else null

        val txnsJson = prefs.getString(KEY_TRANSACTIONS, null)
        val transactionsList = if (txnsJson != null) {
            try {
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val type = Types.newParameterizedType(List::class.java, Transaction::class.java)
                val adapter = moshi.adapter<List<Transaction>>(type)
                adapter.fromJson(txnsJson)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else null

        val finalTransactions = transactionsList ?: emptyList()
        val enrichedTransactions = enrichTransactions(finalTransactions, getLocalVouchers())
        val savedCurrency = prefs.getString("currency", "ج.م") ?: "ج.م"
        AppConfig.currency = savedCurrency

        return SubscriberInfo(
            username = prefs.getString(KEY_SUB_USERNAME, "مشترك offline"),
            fullName = prefs.getString(KEY_SUB_FULLNAME, "مستخدم محفوظ"),
            balance = prefs.getFloat(KEY_SUB_BALANCE, 0f).toDouble(),
            altPhone = prefs.getString(KEY_ALT_PHONE, ""),
            activePlan = activePlan,
            transactions = enrichedTransactions,
            outstandingDebt = prefs.getFloat("outstanding_debt", 0f).toDouble(),
            currency = savedCurrency,
            subAllowRenew = prefs.getBoolean("sub_allow_renew", true),
            subAllowChange = prefs.getBoolean("sub_allow_change", true),
            subAllowBuyPlan = prefs.getBoolean("sub_allow_buy_plan", true),
            subAllowBuyAddon = prefs.getBoolean("sub_allow_buy_addon", true),
            subShowRechargePage = prefs.getBoolean("sub_show_recharge_page", true),
            subShowPlans = prefs.getBoolean("sub_show_plans", true),
            subShowAddons = prefs.getBoolean("sub_show_addons", true)
        )
    }

    fun saveLocalVoucher(planName: String, amount: Double, voucherCode: String) {
        val existing = getLocalVouchers().toMutableList()
        existing.add(LocalVoucherRecord(planName, amount, voucherCode, System.currentTimeMillis()))
        
        try {
            val moshi = Moshi.Builder().build()
            val type = Types.newParameterizedType(List::class.java, LocalVoucherRecord::class.java)
            val adapter = moshi.adapter<List<LocalVoucherRecord>>(type)
            prefs.edit().putString("local_vouchers", adapter.toJson(existing)).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLocalVouchers(): List<LocalVoucherRecord> {
        val json = prefs.getString("local_vouchers", null) ?: return emptyList()
        return try {
            val moshi = Moshi.Builder().build()
            val type = Types.newParameterizedType(List::class.java, LocalVoucherRecord::class.java)
            val adapter = moshi.adapter<List<LocalVoucherRecord>>(type)
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun isValidVoucherCode(code: String?): Boolean {
        if (code == null) return false
        val clean = code.replace("-", "").trim()
        if (clean.isEmpty()) return false
        if (clean.startsWith("BUY", ignoreCase = true) || 
            clean.startsWith("GEN", ignoreCase = true) || 
            clean.startsWith("DEP", ignoreCase = true) ||
            clean.startsWith("TXN", ignoreCase = true)) {
            return false
        }
        if (clean.length > 16 || clean.length < 6) return false
        return clean.any { it.isDigit() }
    }

    private fun isVoucherTransaction(description: String?): Boolean {
        if (description == null) return false
        val lower = description.lowercase()
        
        // First, check if it contains explicit voucher terms
        val hasVoucherKeyword = lower.contains("كارت") || 
                                lower.contains("توليد") || 
                                lower.contains("كروت") || 
                                lower.contains("إنشاء") || 
                                lower.contains("كود الكارت") ||
                                lower.contains("voucher")
                                
        if (!hasVoucherKeyword) return false
        
        // Exclude direct renewals or activations that aren't voucher generations
        if (lower.contains("مباشرة على الحساب") || lower.contains("شحن الرصيد") || lower.contains("إيداع") || lower.contains("deposit")) {
            // Unless it explicitly contains "توليد كارت" or "توليد كروت" or "إنشاء كارت"
            if (!lower.contains("توليد كارت") && !lower.contains("إنشاء كارت") && !lower.contains("توليد كروت")) {
                return false
            }
        }
        
        // If it's a direct package renewal/activation without card generation, ignore
        if ((lower.contains("تجديد باقة") || lower.contains("تفعيل باقة") || lower.contains("اشتراك بالباقة")) && 
            !lower.contains("توليد") && !lower.contains("كارت") && !lower.contains("إنشاء")) {
            return false
        }
        
        return true
    }

    fun enrichTransactions(txns: List<Transaction>, localVouchers: List<LocalVoucherRecord>): List<Transaction> {
        val usedVouchers = mutableSetOf<LocalVoucherRecord>()
        
        return txns.map { txn ->
            val existingCode = if (!txn.txnId.isNullOrEmpty() && txn.txnId != "null" && isValidVoucherCode(txn.txnId) && isVoucherTransaction(txn.description)) {
                txn.txnId
            } else {
                val fromDesc = extractVoucherCode(txn.description)
                if (isValidVoucherCode(fromDesc) && isVoucherTransaction(txn.description)) fromDesc else null
            }
            if (existingCode != null) {
                val cleanCode = existingCode.replace("-", "")
                if (txn.txnId != cleanCode) {
                    txn.copy(txnId = cleanCode)
                } else {
                    txn
                }
            } else {
                val matchedRec = localVouchers.lastOrNull { rec ->
                    rec !in usedVouchers && 
                    txn.amount == rec.amount &&
                    (txn.description?.contains("كارت") == true || txn.description?.contains("توليد") == true || txn.description?.contains(rec.planName) == true)
                }
                if (matchedRec != null) {
                    usedVouchers.add(matchedRec)
                    val cleanCode = matchedRec.voucherCode.replace("-", "")
                    txn.copy(
                        txnId = cleanCode,
                        description = txn.description + " (كود الكارت: $cleanCode)"
                    )
                } else {
                    txn
                }
            }
        }
    }

    private fun extractVoucherCode(description: String?): String? {
        if (description == null) return null
        if (description.contains("كود الكارت:")) {
            val part = description.substringAfter("كود الكارت:").trim()
            val codePart = part.substringBefore(")").trim()
            if (codePart.isNotEmpty() && isValidVoucherCode(codePart)) return codePart
        }
        val regex = """(?:\b\d{3,}(?:-\d{3,})+\b)|(?:\b\d{6,16}\b)""".toRegex()
        val found = regex.find(description)?.value
        return if (isValidVoucherCode(found)) found else null
    }

    // APIs
    suspend fun login(req: LoginRequest): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(req)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                body.token?.let { saveToken(it) }
                body.systemName?.let { saveSystemName(it) }
                body.subscriber?.let { 
                    saveSubscriberInfo(it)
                    it.systemName?.let { name -> saveSystemName(name) }
                }
                Result.success(body)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "فشل تسجيل الدخول"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getVoucherInfo(code: String, tenantUsername: String): Result<VoucherInfoResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getVoucherInfo(VoucherInfoRequest(code, tenantUsername))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val codeNum = response.code()
                val errorMsg = response.errorBody()?.string() ?: ""
                if (codeNum == 403 || errorMsg.contains("جديد") || errorMsg.contains("تفعيل")) {
                    Result.failure(Exception("FORBIDDEN_NEW_VOUCHER"))
                } else {
                    Result.failure(Exception(errorMsg.ifEmpty { "الكارت غير موجود أو منتهي الصلاحية" }))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun saveWalletPhone(phone: String) {
        if (phone.isNotBlank()) {
            AppConfig.walletPhone = phone
            prefs.edit().putString(KEY_WALLET_PHONE, phone).apply()
        }
    }

    fun extractWalletPhoneFromJson(jsonString: String?): String? {
        if (jsonString.isNullOrBlank()) return null
        return try {
            val keysOrder = listOf("wallet_phone", "cash_phone", "phone_number", "vf_phone1", "recharge_phone", "wallet_number")
            
            fun findInObject(obj: org.json.JSONObject): String? {
                for (key in keysOrder) {
                    if (obj.has(key) && !obj.isNull(key)) {
                        val value = obj.optString(key, "").trim()
                        if (value.isNotEmpty() && value != "null") {
                            return value
                        }
                    }
                }
                val nestedKeys = listOf("data", "gateway", "gateways", "tenant", "wallet", "config", "info")
                for (nk in nestedKeys) {
                    if (obj.has(nk) && !obj.isNull(nk)) {
                        val nestedObj = obj.optJSONObject(nk)
                        if (nestedObj != null) {
                            val found = findInObject(nestedObj)
                            if (found != null) return found
                        }
                        val nestedArr = obj.optJSONArray(nk)
                        if (nestedArr != null) {
                            for (i in 0 until nestedArr.length()) {
                                val itemObj = nestedArr.optJSONObject(i)
                                if (itemObj != null) {
                                    val found = findInObject(itemObj)
                                    if (found != null) return found
                                }
                            }
                        }
                    }
                }
                return null
            }

            val trimmed = jsonString.trim()
            if (trimmed.startsWith("{")) {
                findInObject(org.json.JSONObject(trimmed))
            } else if (trimmed.startsWith("[")) {
                val arr = org.json.JSONArray(trimmed)
                for (i in 0 until arr.length()) {
                    val itemObj = arr.optJSONObject(i)
                    if (itemObj != null) {
                        val found = findInObject(itemObj)
                        if (found != null) return found
                    }
                }
                null
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchWalletPhone(): String? = withContext(Dispatchers.IO) {
        val existingSaved = getSavedWalletPhone()
        if (existingSaved.isNotBlank()) {
            AppConfig.walletPhone = existingSaved
            return@withContext existingSaved
        }

        // Step 1: GET /api/subscriber/tenant-info
        try {
            val tenantResp = apiService.getTenantInfo()
            if (tenantResp.isSuccessful && tenantResp.body() != null) {
                val body = tenantResp.body()!!
                val phone = body.extractWalletPhone()
                if (!phone.isNullOrBlank()) {
                    saveWalletPhone(phone)
                    return@withContext phone
                }
            } else {
                val rawJson = tenantResp.errorBody()?.string()
                val phone = extractWalletPhoneFromJson(rawJson)
                if (!phone.isNullOrBlank()) {
                    saveWalletPhone(phone)
                    return@withContext phone
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Step 2: GET /api/subscriber/me
        val token = getToken()
        if (!token.isNullOrEmpty()) {
            try {
                val meResp = apiService.getMe("Bearer $token")
                if (meResp.isSuccessful && meResp.body() != null) {
                    val info = meResp.body()!!
                    val phone = info.extractWalletPhone()
                    if (!phone.isNullOrBlank()) {
                        saveWalletPhone(phone)
                        return@withContext phone
                    }
                } else {
                    val rawJson = meResp.errorBody()?.string()
                    val phone = extractWalletPhoneFromJson(rawJson)
                    if (!phone.isNullOrBlank()) {
                        saveWalletPhone(phone)
                        return@withContext phone
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Step 3: GET /api/v1/admin/available-gateways or /api/subscriber/available-gateways
        try {
            val gwResp = if (!token.isNullOrEmpty()) {
                apiService.getSubscriberAvailableGateways("Bearer $token")
            } else {
                apiService.getAvailableGateways()
            }
            val rawJson = if (gwResp.isSuccessful) gwResp.body()?.string() else gwResp.errorBody()?.string()
            val phone = extractWalletPhoneFromJson(rawJson)
            if (!phone.isNullOrBlank()) {
                saveWalletPhone(phone)
                return@withContext phone
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Unauthenticated fallback try for available-gateways
        try {
            val gwResp = apiService.getAvailableGateways()
            val rawJson = if (gwResp.isSuccessful) gwResp.body()?.string() else gwResp.errorBody()?.string()
            val phone = extractWalletPhoneFromJson(rawJson)
            if (!phone.isNullOrBlank()) {
                saveWalletPhone(phone)
                return@withContext phone
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext getSavedWalletPhone().ifEmpty { null }
    }

    suspend fun getMe(): Result<SubscriberInfo> = withContext(Dispatchers.IO) {
        try {
            val token = getToken() ?: return@withContext Result.failure(Exception("جلسة منتهية"))
            val response = apiService.getMe("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                val info = response.body()!!
                info.systemName?.let { saveSystemName(it) }
                val phone = info.extractWalletPhone()
                if (!phone.isNullOrBlank()) {
                    saveWalletPhone(phone)
                }
                val enrichedInfo = info.copy(
                    transactions = enrichTransactions(info.transactions ?: emptyList(), getLocalVouchers())
                )
                saveSubscriberInfo(enrichedInfo)
                Result.success(enrichedInfo)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "خطأ في جلب البيانات من السيرفر"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getPlans(type: String? = null, category: String? = null, mode: String? = null, target: String? = null): Result<List<Plan>> = withContext(Dispatchers.IO) {
        try {
            val token = getToken() ?: return@withContext Result.failure(Exception("جلسة منتهية"))
            val response = apiService.getPlans("Bearer $token", type, category, mode, target)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "فشل جلب الباقات من السيرفر"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getVoucherPlans(): Result<List<Plan>> = withContext(Dispatchers.IO) {
        val token = getToken() ?: return@withContext Result.failure(Exception("جلسة منتهية"))
        
        var lastErrorMessage = "فشل جلب باقات الكروت من السيرفر"

        // Step 1: Try mode=voucher query parameter (GET /api/subscriber/plans?mode=voucher)
        try {
            val response = apiService.getPlans("Bearer $token", mode = "voucher")
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                return@withContext Result.success(response.body()!!)
            } else {
                lastErrorMessage = response.errorBody()?.string() ?: "فشل طلب /api/subscriber/plans?mode=voucher"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            lastErrorMessage = e.message ?: e.toString()
        }

        // Step 2: Try dedicated endpoint /api/vouchers/packages
        try {
            val response = apiService.getVoucherPackages("Bearer $token")
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                return@withContext Result.success(response.body()!!)
            } else {
                lastErrorMessage = response.errorBody()?.string() ?: "فشل طلب /api/vouchers/packages"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            lastErrorMessage = e.message ?: e.toString()
        }

        // Step 3: Try type=voucher query parameter
        try {
            val response = apiService.getPlans("Bearer $token", type = "voucher")
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                return@withContext Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string()
                if (err != null) lastErrorMessage = err
            }
        } catch (e: Exception) {
            e.printStackTrace()
            lastErrorMessage = e.message ?: e.toString()
        }

        // Step 3: Try category=hotspot query parameter
        try {
            val response = apiService.getPlans("Bearer $token", category = "hotspot")
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                return@withContext Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string()
                if (err != null) lastErrorMessage = err
            }
        } catch (e: Exception) {
            e.printStackTrace()
            lastErrorMessage = e.message ?: e.toString()
        }

        // Step 4: Try type=hotspot query parameter
        try {
            val response = apiService.getPlans("Bearer $token", type = "hotspot")
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                return@withContext Result.success(response.body()!!)
            } else {
                val err = response.errorBody()?.string()
                if (err != null) lastErrorMessage = err
            }
        } catch (e: Exception) {
            e.printStackTrace()
            lastErrorMessage = e.message ?: e.toString()
        }

        // Step 5: Fallback - Fetch all plans and filter to those matching voucher/hotspot keywords
        try {
            val response = apiService.getPlans("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                val allPlans = response.body()!!
                val filtered = allPlans.filter { 
                    it.name?.lowercase()?.contains("voucher") == true || 
                    it.name?.lowercase()?.contains("hotspot") == true ||
                    it.name?.contains("كارت") == true ||
                    it.name?.contains("كروت") == true
                }
                if (filtered.isNotEmpty()) {
                    return@withContext Result.success(filtered)
                }
                // If filtered list is empty, return all plans so the user still has something to select!
                return@withContext Result.success(allPlans)
            } else {
                val err = response.errorBody()?.string()
                if (err != null) lastErrorMessage = err
            }
        } catch (e: Exception) {
            e.printStackTrace()
            lastErrorMessage = e.message ?: e.toString()
        }

        Result.failure(Exception(lastErrorMessage))
    }

    suspend fun buyPlan(planId: Int, tenantUsername: String, planType: String? = null, mode: String? = null, action: String? = null): Result<BuyPlanResponse> = withContext(Dispatchers.IO) {
        try {
            val token = getToken() ?: return@withContext Result.failure(Exception("جلسة منتهية"))
            
            val initialMode = mode ?: if (planType == "hotspot" || planType == "voucher") "voucher" else null
            
            val request = if (initialMode == "voucher") {
                BuyPlanRequest(planId = planId, mode = "voucher")
            } else {
                BuyPlanRequest(planId, tenantUsername, planType, mode = initialMode, action = action)
            }

            // Try with the provided planType or mode
            var response = apiService.buyPlan("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                return@withContext Result.success(response.body()!!)
            }
 
            var lastErr = response.errorBody()?.string() ?: "فشل شراء الباقة أو توليد الكارت"
 
            // If it failed and planType was "hotspot", try self-healing with other potential configurations
            if (planType == "hotspot" || planType == "voucher" || initialMode == "voucher") {
                val alternateRequests = listOf(
                    BuyPlanRequest(planId = planId, mode = "voucher"),
                    BuyPlanRequest(planId = planId, mode = "voucher", action = action),
                    BuyPlanRequest(planId, tenantUsername, "voucher", mode = "voucher", action = action),
                    BuyPlanRequest(planId, tenantUsername, "hotspot", mode = "voucher", action = action),
                    BuyPlanRequest(planId, tenantUsername, "subscriber", mode = "voucher", action = action)
                )
                for (altRequest in alternateRequests) {
                    try {
                        val responseAlt = apiService.buyPlan("Bearer $token", altRequest)
                        if (responseAlt.isSuccessful && responseAlt.body() != null) {
                            return@withContext Result.success(responseAlt.body()!!)
                        } else {
                            val altErr = responseAlt.errorBody()?.string()
                            if (altErr != null) {
                                lastErr = altErr
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        lastErr = ex.message ?: ex.toString()
                    }
                }
            }
            
            Result.failure(Exception(lastErr))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun managePlan(action: String, planId: Int, planType: String? = null, mode: String? = null, target: String? = null): Result<ManagePlanResponse> = withContext(Dispatchers.IO) {
        try {
            val token = getToken() ?: return@withContext Result.failure(Exception("جلسة منتهية"))
            val response = apiService.managePlan("Bearer $token", ManagePlanRequest(action, planId, planType, mode, target))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "فشل معالجة طلب الباقة"
                val errorMsgParsed = try {
                    val json = org.json.JSONObject(errorMsg)
                    json.optString("error", json.optString("message", errorMsg))
                } catch (je: Exception) {
                    errorMsg
                }
                Result.failure(Exception(errorMsgParsed))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun updateProfile(altPhone: String): Result<UpdateProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val token = getToken() ?: return@withContext Result.failure(Exception("جلسة منتهية"))
            val response = apiService.updateProfile("Bearer $token", UpdateProfileRequest(altPhone))
            if (response.isSuccessful && response.body() != null) {
                // Update local copy too
                val current = getOfflineSubscriberInfo()
                saveSubscriberInfo(current.copy(altPhone = altPhone))
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "فشل تحديث البيانات"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun sendPaymentRequest(senderPhone: String): Result<PaymentRequestResponse> = withContext(Dispatchers.IO) {
        try {
            val token = getToken() ?: return@withContext Result.failure(Exception("جلسة منتهية"))
            val response = apiService.sendPaymentRequest("Bearer $token", PaymentRequest(senderPhone))
            if (response.isSuccessful && response.body() != null) {
                // Update local copy too for display
                val current = getOfflineSubscriberInfo()
                saveSubscriberInfo(current.copy(altPhone = senderPhone))
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "فشل معالجة طلب الدفع"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getAddons(): Result<List<AddonPlan>> = withContext(Dispatchers.IO) {
        try {
            val token = getToken() ?: return@withContext Result.failure(Exception("جلسة منتهية"))
            val response = apiService.getAddons("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "فشل جلب الباقات الإضافية"
                val errorMsgParsed = try {
                    val json = org.json.JSONObject(errorMsg)
                    json.optString("error", json.optString("message", errorMsg))
                } catch (je: Exception) {
                    errorMsg
                }
                Result.failure(Exception(errorMsgParsed))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun buyAddon(planId: Int): Result<BuyAddonResponse> = withContext(Dispatchers.IO) {
        try {
            val token = getToken() ?: return@withContext Result.failure(Exception("جلسة منتهية"))
            val response = apiService.buyAddon("Bearer $token", BuyAddonRequest(id = planId, planId = planId))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "فشل شراء الباقة الإضافية"
                val errorMsgParsed = try {
                    val json = org.json.JSONObject(errorMsg)
                    json.optString("error", json.optString("message", errorMsg))
                } catch (je: Exception) {
                    errorMsg
                }
                Result.failure(Exception(errorMsgParsed))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun getFallbackPlans(): List<Plan> {
        return listOf(
            Plan(1, "باقة 10 جيجا هوم", 20.0, 10.0, "30"),
            Plan(2, "باقة 30 جيجا برونزية", 45.0, 30.0, "30"),
            Plan(3, "باقة 50 جيجا ذهبية", 70.0, 50.0, "30"),
            Plan(4, "باقة 100 جيجا توربو", 120.0, 100.0, "30"),
            Plan(5, "باقة غير محدودة يومية", 15.0, 999.0, "1")
        )
    }
}
