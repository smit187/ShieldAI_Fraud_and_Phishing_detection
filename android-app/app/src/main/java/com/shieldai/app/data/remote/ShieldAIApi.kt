package com.shieldai.app.data.remote

import com.shieldai.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service interface — maps to all ShieldAI backend endpoints.
 */
interface ShieldAIApi {

    // ============================================
    // Auth Endpoints (Public)
    // ============================================

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<Void>

    // ============================================
    // User Endpoints (Authenticated)
    // ============================================

    @GET("api/v1/users/me")
    suspend fun getProfile(): Response<UserProfile>

    @PUT("api/v1/users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserProfile>

    @DELETE("api/v1/users/me")
    suspend fun deleteAccount(): Response<Void>

    // ============================================
    // Scan Endpoints (Authenticated)
    // ============================================

    @POST("api/v1/scan")
    suspend fun scanContent(@Body request: ScanRequest): Response<ScanResult>

    @GET("api/v1/scan/history")
    suspend fun getScanHistory(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PageResponse<ScanResult>>

    @POST("api/v1/scan/{scanId}/feedback")
    suspend fun submitFeedback(
        @Path("scanId") scanId: String,
        @Query("feedback") feedback: String
    ): Response<Void>

    // ============================================
    // Threat History Endpoints (Authenticated)
    // ============================================

    @GET("api/v1/history")
    suspend fun getThreatHistory(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PageResponse<ThreatHistoryItem>>

    // ============================================
    // Threat Report Endpoints (Authenticated)
    // ============================================

    @POST("api/v1/reports")
    suspend fun submitReport(@Body request: ThreatReportRequest): Response<ThreatReportResponse>

    @GET("api/v1/reports/my")
    suspend fun getMyReports(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PageResponse<ThreatReportResponse>>

    @POST("api/v1/reports/{reportId}/upvote")
    suspend fun upvoteReport(@Path("reportId") reportId: String): Response<Void>
}
