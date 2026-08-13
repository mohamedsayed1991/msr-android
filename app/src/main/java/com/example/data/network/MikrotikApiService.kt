package com.example.data.network

import com.example.data.model.MikrotikTenantInfo

class MikrotikApiService {

    private val MIKROTIK_IP = "192.168.88.1"
    private val MIKROTIK_PORT = 8728
    private val MIKROTIK_USER = "msr_read"
    private val MIKROTIK_PASS = "msr_read"

    fun getMikrotikTenantInfo(): MikrotikTenantInfo? {
        return try {
            val api = RouterOsApi(MIKROTIK_IP, MIKROTIK_PORT)
            val systemName = api.getSystemIdentity(MIKROTIK_USER, MIKROTIK_PASS)

            if (!systemName.isNullOrBlank()) {
                MikrotikTenantInfo(
                    tenant_id = 0,
                    username = systemName,
                    system_name = systemName,
                    server_ip = MIKROTIK_IP,
                    server_port = MIKROTIK_PORT
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
