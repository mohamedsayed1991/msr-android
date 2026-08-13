# Android Auto-Detect Network Prompt

## Goal
Update the Android Kotlin app to automatically detect which network (tenant) it belongs to by reading the `tenant-info.json` file from the MikroTik's local web server (192.168.88.1).

## How It Works

### Flow:
```
1. User connects to WiFi (MikroTik hotspot)
2. App tries to reach http://192.168.88.1/tenant-info.json
3. MikroTik returns:
   {
     "tenant_id": 1491,
     "username": "mm33mm",
     "system_name": "المجنون",
     "server_ip": "13.53.130.231",
     "server_port": 8080
   }
4. App sends to server: /api/subscriber/tenant-info?tenant=mm33mm
5. Server returns full network info
6. App proceeds automatically - NO user selection needed
```

### Why This Works:
- Each MikroTik has a unique `tenant-info.json` file with its own tenant info
- The Android app always knows the MikroTik is at 192.168.88.1 (standard LAN IP)
- When user connects to WiFi, the app can reach the MikroTik locally
- The app gets the exact tenant info from the MikroTik itself

## Required Changes

### 1. Create new file `app/src/main/java/com/example/data/network/MikrotikApiService.kt`

```kotlin
package com.example.data.network

import com.example.data.model.MikrotikTenantInfo
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class MikrotikApiService {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)  // Short timeout for local network
        .readTimeout(3, TimeUnit.SECONDS)
        .build()
    
    private val MIKROTIK_IP = "192.168.88.1"
    private val MIKROTIK_PORT = 80
    
    /**
     * Try to get tenant info from MikroTik's local web server
     * Returns null if MikroTik is not reachable (not on WiFi)
     */
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
                // Parse JSON manually or use Gson
                parseMikrotikResponse(body)
            } else {
                null
            }
        } catch (e: Exception) {
            // MikroTik not reachable - user is not on WiFi
            null
        }
    }
    
    private fun parseMikrotikResponse(json: String?): MikrotikTenantInfo? {
        if (json == null) return null
        
        return try {
            // Simple JSON parsing
            val tenantId = extractJsonValue(json, "tenant_id")?.toIntOrNull()
            val username = extractJsonValue(json, "username")
            val systemName = extractJsonValue(json, "system_name")
            val serverIp = extractJsonValue(json, "server_ip")
            val serverPort = extractJsonValue(json, "server_port")?.toIntOrNull()
            
            if (tenantId != null && username != null) {
                MikrotikTenantInfo(
                    tenant_id = tenantId,
                    username = username,
                    system_name = systemName ?: "",
                    server_ip = serverIp ?: "",
                    server_port = serverPort ?: 8080
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun extractJsonValue(json: String, key: String): String? {
        val regex = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        val match = regex.find(json)
        return match?.groupValues?.get(1)
    }
}
```

### 2. Create new file `app/src/main/java/com/example/data/model/MikrotikTenantInfo.kt`

```kotlin
package com.example.data.model

data class MikrotikTenantInfo(
    val tenant_id: Int,
    val username: String,
    val system_name: String,
    val server_ip: String,
    val server_port: Int
)
```

### 3. Update `app/src/main/java/com/example/data/repository/SubscriberRepository.kt`

Add new method to auto-detect from MikroTik:

```kotlin
/**
 * Auto-detect tenant from MikroTik's local API
 * This is the PRIMARY method - works when user is on WiFi
 */
suspend fun autoDetectFromMikrotik(): Result<TenantInfoResponse> {
    return try {
        val mikrotikApi = MikrotikApiService()
        val mikrotikInfo = mikrotikApi.getMikrotikTenantInfo()
        
        if (mikrotikInfo != null) {
            // Found MikroTik on local network!
            // Save the tenant info
            saveTenantUsername(mikrotikInfo.username)
            
            // Now get full info from server
            val response = apiService.getTenantInfo(tenant = mikrotikInfo.username)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                saveTenantInfo(body)
                Result.success(body)
            } else {
                Result.failure(Exception("Failed to get network info from server"))
            }
        } else {
            // MikroTik not reachable - not on WiFi
            // Try using saved tenant info
            val savedUsername = getSavedTenantUsername()
            if (savedUsername != null) {
                val response = apiService.getTenantInfo(tenant = savedUsername)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    saveTenantInfo(body)
                    Result.success(body)
                } else {
                    Result.failure(Exception("No network detected"))
                }
            } else {
                Result.failure(Exception("Not connected to WiFi and no saved network"))
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Save tenant username for later use
 */
private fun saveTenantUsername(username: String) {
    prefs.edit().putString("tenant_username", username).apply()
}

/**
 * Get saved tenant username
 */
private fun getSavedTenantUsername(): String? {
    return prefs.getString("tenant_username", null)
}
```

### 4. Update `app/src/main/java/com/example/ui/viewmodel/SubscriberViewModel.kt`

Update `runAutoDiscovery()` method:

```kotlin
private fun runAutoDiscovery() {
    viewModelScope.launch {
        _isLoading.value = true
        _uiState.value = SubscriberUiState.Loading("جاري اكتشاف الشبكة...")
        
        // Step 1: Try to detect from MikroTik local API (WiFi)
        val result = repository.autoDetectFromMikrotik()
        
        result.onSuccess { response ->
            // Success! We know which network this is
            AppConfig.tenantUsername = response.username
            AppConfig.tenantSystemName = response.system_name
            
            // Save server IP from MikroTik if available
            val mikrotikInfo = MikrotikApiService().getMikrotikTenantInfo()
            if (mikrotikInfo != null && mikrotikInfo.server_ip.isNotEmpty()) {
                AppConfig.serverIP = mikrotikInfo.server_ip
                AppConfig.serverPort = mikrotikInfo.server_port
            }
            
            _uiState.value = SubscriberUiState.Success
        }.onFailure { error ->
            _uiState.value = SubscriberUiState.Error(error.message ?: "خطأ غير معروف")
        }
        
        _isLoading.value = false
    }
}
```

### 5. Update `app/src/main/java/com/example/config/AppConfig.kt`

Add server config from MikroTik:

```kotlin
object AppConfig {
    // Existing
    var tenantUsername: String = ""
    var tenantSystemName: String = ""
    
    // NEW: Server config (can be overridden by MikroTik)
    var serverIP: String = "13.53.130.231"
    var serverPort: Int = 8080
    
    // Base URL - use dynamic server IP
    val BASE_URL: String
        get() = "http://$serverIP:$serverPort/"
}
```

### 6. Update `app/src/main/java/com/example/data/network/ApiService.kt`

Add tenant parameter to getTenantInfo:

```kotlin
@GET("api/subscriber/tenant-info")
suspend fun getTenantInfo(
    @Query("tenant") tenant: String? = null
): Response<TenantInfoResponse>
```

## Complete Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    Auto-Detect Flow                          │
└─────────────────────────────────────────────────────────────┘

1. User opens app
   │
   ├─→ Try MikroTik Local API: http://192.168.88.1/tenant-info.json
   │   │
   │   ├─→ SUCCESS (on WiFi):
   │   │   {
   │   │     "tenant_id": 1491,
   │   │     "username": "mm33mm",
   │   │     "system_name": "المجنون"
   │   │   }
   │   │   │
   │   │   └─→ Use username="mm33mm" to call server API
   │   │
   │   └─→ FAIL (not on WiFi):
   │       │
   │       └─→ Use saved username from SharedPreferences
   │           │
   │           ├─→ Have saved: use it
   │           └─→ No saved: show error
   │
   └─→ App proceeds with correct network!
```

## Testing

1. **On WiFi (with MikroTik):**
   - Connect to MikroTik WiFi
   - Open app
   - Should automatically detect "المجنون" or "انتاتان"
   - No user interaction needed

2. **Off WiFi (mobile data):**
   - Use mobile data
   - Open app
   - Should use last saved network
   - If no saved network, show error

3. **Switch networks:**
   - Connect to different MikroTik WiFi
   - Open app
   - Should detect new network automatically

## Key Points

1. **MikroTik IP is always 192.168.88.1** - Standard LAN IP
2. **File is `tenant-info.json`** - Created by setup script
3. **Short timeout (3 seconds)** - Quick check, no waiting
4. **Fallback to saved** - Works offline after first detection
5. **No user selection needed** - Fully automatic!

## MikroTik Setup Script Already Creates:

```rsc
/file set [find name="tenant-info.json"] contents="{
\"tenant_id\": 1491,
\"username\": \"mm33mm\",
\"system_name\": \"المجنون\",
\"server_ip\": \"13.53.130.231\",
\"server_port\": 8080
}"
```

And enables web server:
```rsc
/ip service
set www disabled=no port=80 address=0.0.0.0/0
```

And allows hotspot users to access it:
```rsc
/ip hotspot walled-garden
add dst-address=192.168.88.1 action=allow
```
