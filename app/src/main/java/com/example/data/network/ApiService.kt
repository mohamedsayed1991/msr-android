package com.example.data.network

import com.example.config.AppConfig
import com.example.data.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ApiService {

    @GET("/api/subscriber/tenant-info")
    suspend fun getTenantInfo(): Response<TenantInfoResponse>

    @GET("/api/subscriber/tenant-info")
    suspend fun getTenantInfoWithTenant(
        @Query("tenant") tenant: String
    ): Response<TenantInfoResponse>

    @POST("/api/subscriber/login")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<LoginResponse>

    @POST("/api/subscriber/voucher-info")
    suspend fun getVoucherInfo(
        @Body body: VoucherInfoRequest
    ): Response<VoucherInfoResponse>

    @GET("/api/subscriber/me")
    suspend fun getMe(
        @Header("Authorization") authHeader: String
    ): Response<SubscriberInfo>

    @GET("/api/subscriber/plans")
    suspend fun getPlans(
        @Header("Authorization") authHeader: String,
        @Query("type") type: String? = null,
        @Query("category") category: String? = null,
        @Query("mode") mode: String? = null,
        @Query("target") target: String? = null
    ): Response<List<Plan>>

    @GET("/api/vouchers/packages")
    suspend fun getVoucherPackages(
        @Header("Authorization") authHeader: String
    ): Response<List<Plan>>

    @POST("/api/subscriber/buy-plan")
    suspend fun buyPlan(
        @Header("Authorization") authHeader: String,
        @Body body: BuyPlanRequest
    ): Response<BuyPlanResponse>

    @POST("/api/subscriber/plan")
    suspend fun managePlan(
        @Header("Authorization") authHeader: String,
        @Body body: ManagePlanRequest
    ): Response<ManagePlanResponse>

    @POST("/api/subscriber/update-profile")
    suspend fun updateProfile(
        @Header("Authorization") authHeader: String,
        @Body body: UpdateProfileRequest
    ): Response<UpdateProfileResponse>

    @POST("/api/subscriber/payment-request")
    suspend fun sendPaymentRequest(
        @Header("Authorization") authHeader: String,
        @Body body: PaymentRequest
    ): Response<PaymentRequestResponse>

    @GET("/api/subscriber/addons")
    suspend fun getAddons(
        @Header("Authorization") authHeader: String
    ): Response<List<AddonPlan>>

    @POST("/api/subscriber/addon")
    suspend fun buyAddon(
        @Header("Authorization") authHeader: String,
        @Body body: BuyAddonRequest
    ): Response<BuyAddonResponse>

    @GET("/api/v1/admin/available-gateways")
    suspend fun getAvailableGateways(): Response<okhttp3.ResponseBody>

    @GET("/api/subscriber/available-gateways")
    suspend fun getSubscriberAvailableGateways(
        @Header("Authorization") authHeader: String
    ): Response<okhttp3.ResponseBody>

    companion object {
        fun create(): ApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(AppConfig.BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

            return retrofit.create(ApiService::class.java)
        }
    }
}
