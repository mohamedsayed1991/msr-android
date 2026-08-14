package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MikrotikTenantInfo(
    @Json(name = "tenant_id") val tenantId: Int? = null,
    @Json(name = "account_id") val accountId: String? = null,
    @Json(name = "user_id") val userId: Int? = null,
    @Json(name = "router_id") val routerId: Int? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "system_name") val systemName: String? = null,
    @Json(name = "server_ip") val serverIp: String? = null,
    @Json(name = "server_port") val serverPort: Int? = null
)
