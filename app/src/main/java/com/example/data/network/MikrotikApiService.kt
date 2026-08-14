package com.example.data.network

import com.example.data.model.MikrotikTenantInfo
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

interface MikrotikApiService {

    @GET("tenant-info.json")
    suspend fun getMikrotikTenantInfo(): Response<MikrotikTenantInfo>

    companion object {
        private const val MIKROTIK_BASE_URL = "http://192.168.88.1/"

        fun create(): MikrotikApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(MIKROTIK_BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            return retrofit.create(MikrotikApiService::class.java)
        }
    }
}
