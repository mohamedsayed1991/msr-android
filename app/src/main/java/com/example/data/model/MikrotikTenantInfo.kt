package com.example.data.model

data class MikrotikTenantInfo(
    val tenant_id: Int,
    val username: String,
    val system_name: String,
    val server_ip: String,
    val server_port: Int
)
