package com.example.data.network

import com.example.data.model.MikrotikTenantInfo
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class MikrotikApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    private val MIKROTIK_IP = "192.168.88.1"
    private val MIKROTIK_PORT = 80

    fun getMikrotikTenantInfo(): MikrotikTenantInfo? {
        return try {
            val url = "http://$MIKROTIK_IP:$MIKROTIK_PORT/tenant-info.json"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                parseResponse(body)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseResponse(json: String?): MikrotikTenantInfo? {
        if (json == null) return null
        return try {
            val tenantId = extract(json, "tenant_id")?.toIntOrNull()
            val username = extract(json, "username")
            val systemName = extract(json, "system_name")
            val serverIp = extract(json, "server_ip")
            val serverPort = extract(json, "server_port")?.toIntOrNull()

            if (tenantId != null && username != null) {
                MikrotikTenantInfo(
                    tenant_id = tenantId,
                    username = username,
                    system_name = systemName ?: "",
                    server_ip = serverIp ?: "",
                    server_port = serverPort ?: 8080
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun extract(json: String, key: String): String? {
        val regex = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        val match = regex.find(json)
        return match?.groupValues?.get(1)
    }
}
