package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TenantInfoResponse(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "system_name") val systemName: String? = null,
    @Json(name = "wallet_phone") val walletPhone: String? = null,
    @Json(name = "cash_phone") val cashPhone: String? = null,
    @Json(name = "phone_number") val phoneNumber: String? = null,
    @Json(name = "vf_phone1") val vfPhone1: String? = null,
    @Json(name = "recharge_phone") val rechargePhone: String? = null,
    @Json(name = "wallet_number") val walletNumber: String? = null,
    @Json(name = "currency") val currency: String? = null
) {
    fun extractWalletPhone(): String? {
        val candidates = listOfNotNull(walletPhone, cashPhone, phoneNumber, vfPhone1, rechargePhone, walletNumber)
        return candidates.firstOrNull { it.isNotBlank() && it != "null" }
    }
}

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "mode") val mode: String = "subscriber",
    @Json(name = "tenant_username") val tenantUsername: String,
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class ActivePlanInfo(
    @Json(name = "plan_id") val planId: Int? = null,
    @Json(name = "plan_type") val planType: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "plan_name") val planName: String? = null,
    @Json(name = "total_gb") val totalGb: Double? = null,
    @Json(name = "used_gb") val usedGb: Double? = null,
    @Json(name = "remaining_gb") val remainingGb: Double? = null,
    @Json(name = "validity_days") val validityDays: String? = null, // Flexibly can be Int or String, parsed as String
    @Json(name = "basic_quota_gb") val basicQuotaGb: Double? = null,
    @Json(name = "addon_quota_gb") val addonQuotaGb: Double? = null,
    @Json(name = "has_addon") val hasAddon: Boolean? = null
) {
    val displayName: String
        get() = planName ?: name ?: ""
}

@JsonClass(generateAdapter = true)
data class WalletInfo(
    @Json(name = "fullname") val fullName: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "balance") val balance: Double? = null,
    @Json(name = "outstanding_debt") val outstandingDebt: Double? = null,
    @Json(name = "wallet_phone") val walletPhone: String? = null,
    @Json(name = "cash_phone") val cashPhone: String? = null,
    @Json(name = "phone_number") val phoneNumber: String? = null,
    @Json(name = "vf_phone1") val vfPhone1: String? = null,
    @Json(name = "recharge_phone") val rechargePhone: String? = null,
    @Json(name = "wallet_number") val walletNumber: String? = null
)

@JsonClass(generateAdapter = true)
data class Transaction(
    @Json(name = "id") val id: Int?,
    @Json(name = "amount") val amount: Double?,
    @Json(name = "type") val type: String?,
    @Json(name = "txn_id") val txnId: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "created_at") val createdAt: String?
)

@JsonClass(generateAdapter = true)
data class SubscriberInfo(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "balance") val balance: Double? = null,
    @Json(name = "alt_phone") val altPhone: String? = null,
    @Json(name = "wallet_phone") val walletPhone: String? = null,
    @Json(name = "cash_phone") val cashPhone: String? = null,
    @Json(name = "phone_number") val phoneNumber: String? = null,
    @Json(name = "vf_phone1") val vfPhone1: String? = null,
    @Json(name = "recharge_phone") val rechargePhone: String? = null,
    @Json(name = "wallet_number") val walletNumber: String? = null,
    @Json(name = "active_plan") val activePlan: ActivePlanInfo? = null,
    @Json(name = "wallet") val wallet: WalletInfo? = null,
    @Json(name = "transactions") val transactions: List<Transaction>? = null,
    @Json(name = "outstanding_debt") val outstandingDebt: Double? = null,
    @Json(name = "currency") val currency: String? = null,
    @Json(name = "sub_allow_renew") val subAllowRenew: Boolean? = true,
    @Json(name = "sub_allow_change") val subAllowChange: Boolean? = true,
    @Json(name = "sub_allow_buy_plan") val subAllowBuyPlan: Boolean? = true,
    @Json(name = "sub_allow_buy_addon") val subAllowBuyAddon: Boolean? = true,
    @Json(name = "sub_show_recharge_page") val subShowRechargePage: Boolean? = true,
    @Json(name = "sub_show_plans") val subShowPlans: Boolean? = true,
    @Json(name = "sub_show_addons") val subShowAddons: Boolean? = true
) {
    val allowRenew: Boolean get() = subAllowRenew ?: true
    val allowChange: Boolean get() = subAllowChange ?: true
    val allowBuyPlan: Boolean get() = subAllowBuyPlan ?: true
    val allowBuyAddon: Boolean get() = subAllowBuyAddon ?: true
    val showRechargePage: Boolean get() = subShowRechargePage ?: true
    val showPlans: Boolean get() = subShowPlans ?: true
    val showAddons: Boolean get() = subShowAddons ?: true
    fun extractWalletPhone(): String? {
        val candidates = listOfNotNull(
            walletPhone,
            cashPhone,
            phoneNumber,
            vfPhone1,
            rechargePhone,
            walletNumber,
            wallet?.walletPhone,
            wallet?.cashPhone,
            wallet?.phoneNumber,
            wallet?.vfPhone1,
            wallet?.rechargePhone,
            wallet?.walletNumber
        )
        return candidates.firstOrNull { it.isNotBlank() && it != "null" }
    }
    val displayFullName: String
        get() = wallet?.fullName ?: fullName ?: ""

    val displayUsername: String
        get() = wallet?.username ?: username ?: ""

    val displayBalance: Double
        get() = wallet?.balance ?: balance ?: 0.0

    val displayOutstandingDebt: Double
        get() = wallet?.outstandingDebt ?: outstandingDebt ?: 0.0
}

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "token") val token: String?,
    @Json(name = "subscriber") val subscriber: SubscriberInfo?
)

@JsonClass(generateAdapter = true)
data class VoucherInfoRequest(
    @Json(name = "code") val code: String,
    @Json(name = "tenant_username") val tenantUsername: String
)

@JsonClass(generateAdapter = true)
data class VoucherInfoResponse(
    @Json(name = "code") val code: String?,
    @Json(name = "status") val status: String?, // active, used, expired
    @Json(name = "validity_days") val validityDays: String?,
    @Json(name = "total_gb") val totalGb: Double?,
    @Json(name = "used_gb") val usedGb: Double?,
    @Json(name = "remaining_gb") val remainingGb: Double?
)

@JsonClass(generateAdapter = true)
data class BuyPlanRequest(
    @Json(name = "plan_id") val planId: Int,
    @Json(name = "tenant_username") val tenantUsername: String? = null,
    @Json(name = "plan_type") val planType: String? = null,
    @Json(name = "mode") val mode: String? = null,
    @Json(name = "action") val action: String? = null
)

@JsonClass(generateAdapter = true)
data class BuyPlanResponse(
    @Json(name = "code") val code: String? = null,
    @Json(name = "voucher_code") val voucherCode: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "success") val success: Boolean? = null,
    @Json(name = "new_balance") val newBalance: Double? = null
) {
    val displayVoucherCode: String?
        get() = voucherCode ?: code
}

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    @Json(name = "alt_phone") val altPhone: String
)

@JsonClass(generateAdapter = true)
data class UpdateProfileResponse(
    @Json(name = "message") val message: String? = null,
    @Json(name = "success") val success: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class PaymentRequest(
    @Json(name = "sender_phone") val senderPhone: String
)

@JsonClass(generateAdapter = true)
data class PaymentRequestResponse(
    @Json(name = "message") val message: String? = null,
    @Json(name = "success") val success: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class Plan(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "price") val price: Double?,
    @Json(name = "total_gb") val totalGb: Double?,
    @Json(name = "validity_days") val validityDays: String?
)

@JsonClass(generateAdapter = true)
data class AddonPlan(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "price") val price: Double? = null,
    @Json(name = "total_gb") val totalGb: Double? = null,
    @Json(name = "validity_days") val validityDays: String? = null,
    @Json(name = "download_speed") val downloadSpeed: String? = null,
    @Json(name = "upload_speed") val uploadSpeed: String? = null,
    @Json(name = "speed") val speed: String? = null
)

@JsonClass(generateAdapter = true)
data class BuyAddonRequest(
    @Json(name = "id") val id: Int,
    @Json(name = "plan_id") val planId: Int? = null
)

@JsonClass(generateAdapter = true)
data class BuyAddonResponse(
    @Json(name = "success") val success: Boolean? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "new_balance") val newBalance: Double? = null
)

@JsonClass(generateAdapter = true)
data class PlansResponse(
    @Json(name = "plans") val plans: List<Plan>? = null
)

@JsonClass(generateAdapter = true)
data class ManagePlanRequest(
    @Json(name = "action") val action: String,
    @Json(name = "plan_id") val planId: Int,
    @Json(name = "plan_type") val planType: String? = null,
    @Json(name = "mode") val mode: String? = null,
    @Json(name = "target") val target: String? = null
)

@JsonClass(generateAdapter = true)
data class ManagePlanResponse(
    @Json(name = "success") val success: Boolean? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "voucher_code") val voucherCode: String? = null,
    @Json(name = "plan_name") val planName: String? = null,
    @Json(name = "new_balance") val newBalance: Double? = null
)

@JsonClass(generateAdapter = true)
data class LocalVoucherRecord(
    val planName: String,
    val amount: Double,
    val voucherCode: String,
    val timestamp: Long
)

