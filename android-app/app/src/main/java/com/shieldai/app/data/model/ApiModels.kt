package com.shieldai.app.data.model

import com.google.gson.annotations.SerializedName

// ============================================
// Auth Models
// ============================================

data class RegisterRequest(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phoneNumber") val phoneNumber: String? = null,
    @SerializedName("password") val password: String,
    @SerializedName("locale") val locale: String = "en"
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("deviceInfo") val deviceInfo: String? = null
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class AuthResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("tokenType") val tokenType: String,
    @SerializedName("expiresIn") val expiresIn: Long,
    @SerializedName("user") val user: UserProfile?
)

// ============================================
// User Models
// ============================================

data class UserProfile(
    @SerializedName("userId") val userId: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phoneNumber") val phoneNumber: String?,
    @SerializedName("role") val role: String,
    @SerializedName("locale") val locale: String?,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("isEmailVerified") val isEmailVerified: Boolean?,
    @SerializedName("registrationDate") val registrationDate: String?,
    @SerializedName("lastLoginAt") val lastLoginAt: String?
)

data class UpdateProfileRequest(
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("phoneNumber") val phoneNumber: String? = null,
    @SerializedName("locale") val locale: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null
)

// ============================================
// Scan Models
// ============================================

data class ScanRequest(
    @SerializedName("content") val content: String,
    @SerializedName("channel") val channel: String,
    @SerializedName("senderInfo") val senderInfo: String? = null
)

data class ScanResult(
    @SerializedName("scanId") val scanId: String,
    @SerializedName("channel") val channel: String,
    @SerializedName("riskScore") val riskScore: Int,
    @SerializedName("riskLabel") val riskLabel: String,
    @SerializedName("aiConfidence") val aiConfidence: Double?,
    @SerializedName("explanation") val explanation: String?,
    @SerializedName("aiModelVersion") val aiModelVersion: String?,
    @SerializedName("scannedAt") val scannedAt: String?
)

// ============================================
// Threat History Models
// ============================================

data class ThreatHistoryItem(
    @SerializedName("historyId") val historyId: String,
    @SerializedName("sourceChannel") val sourceChannel: String,
    @SerializedName("scanId") val scanId: String?,
    @SerializedName("riskScore") val riskScore: Int,
    @SerializedName("riskLabel") val riskLabel: String,
    @SerializedName("summary") val summary: String?,
    @SerializedName("timestamp") val timestamp: String?
)

// ============================================
// Threat Report Models
// ============================================

data class ThreatReportRequest(
    @SerializedName("category") val category: String,
    @SerializedName("channel") val channel: String? = null,
    @SerializedName("contentReference") val contentReference: String,
    @SerializedName("senderInfo") val senderInfo: String? = null,
    @SerializedName("description") val description: String? = null
)

data class ThreatReportResponse(
    @SerializedName("reportId") val reportId: String,
    @SerializedName("userId") val userId: String?,
    @SerializedName("category") val category: String,
    @SerializedName("channel") val channel: String?,
    @SerializedName("contentReference") val contentReference: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("status") val status: String,
    @SerializedName("upvoteCount") val upvoteCount: Int?,
    @SerializedName("submittedAt") val submittedAt: String?
)

// ============================================
// Pagination Wrapper (Spring Boot Page<T>)
// ============================================

data class PageResponse<T>(
    @SerializedName("content") val content: List<T>,
    @SerializedName("totalElements") val totalElements: Long,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("number") val number: Int,
    @SerializedName("first") val first: Boolean,
    @SerializedName("last") val last: Boolean
)

// ============================================
// Error Response
// ============================================

data class ApiError(
    @SerializedName("status") val status: Int,
    @SerializedName("error") val error: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("path") val path: String?
)
