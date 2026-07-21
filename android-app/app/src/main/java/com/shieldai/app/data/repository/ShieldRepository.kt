package com.shieldai.app.data.repository

import com.shieldai.app.data.local.TokenManager
import com.shieldai.app.data.model.*
import com.shieldai.app.data.remote.ShieldAIApi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central repository — abstracts the API and local data sources.
 * All network calls wrapped in Result<T> for clean error handling.
 */
@Singleton
class ShieldRepository @Inject constructor(
    private val api: ShieldAIApi,
    private val tokenManager: TokenManager
) {

    // ============================================
    // Auth
    // ============================================

    suspend fun register(request: RegisterRequest): Result<AuthResponse> = safeApiCall {
        api.register(request)
    }.also { result ->
        result.getOrNull()?.let { auth ->
            tokenManager.saveTokens(auth.accessToken, auth.refreshToken)
            auth.user?.let { tokenManager.saveUserInfo(it.email, it.fullName) }
        }
    }

    suspend fun login(request: LoginRequest): Result<AuthResponse> = safeApiCall {
        api.login(request)
    }.also { result ->
        result.getOrNull()?.let { auth ->
            tokenManager.saveTokens(auth.accessToken, auth.refreshToken)
            auth.user?.let { tokenManager.saveUserInfo(it.email, it.fullName) }
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            api.logout()
            tokenManager.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            tokenManager.clearSession() // Clear local session even if API fails
            Result.success(Unit)
        }
    }

    // ============================================
    // User Profile
    // ============================================

    suspend fun getProfile(): Result<UserProfile> = safeApiCall {
        api.getProfile()
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Result<UserProfile> = safeApiCall {
        api.updateProfile(request)
    }

    // ============================================
    // Scan
    // ============================================

    suspend fun scanContent(request: ScanRequest): Result<ScanResult> = safeApiCall {
        api.scanContent(request)
    }

    suspend fun getScanHistory(page: Int = 0): Result<PageResponse<ScanResult>> = safeApiCall {
        api.getScanHistory(page)
    }

    suspend fun submitFeedback(scanId: String, feedback: String): Result<Unit> {
        return try {
            api.submitFeedback(scanId, feedback)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // Threat History
    // ============================================

    suspend fun getThreatHistory(page: Int = 0): Result<PageResponse<ThreatHistoryItem>> = safeApiCall {
        api.getThreatHistory(page)
    }

    // ============================================
    // Threat Reports
    // ============================================

    suspend fun submitReport(request: ThreatReportRequest): Result<ThreatReportResponse> = safeApiCall {
        api.submitReport(request)
    }

    suspend fun getMyReports(page: Int = 0): Result<PageResponse<ThreatReportResponse>> = safeApiCall {
        api.getMyReports(page)
    }

    // ============================================
    // Helper
    // ============================================

    private suspend fun <T> safeApiCall(call: suspend () -> retrofit2.Response<T>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "API Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
