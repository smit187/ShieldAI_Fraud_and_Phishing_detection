package com.shieldai.app.data.remote

import com.shieldai.app.data.local.TokenManager
import com.shieldai.app.data.model.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that automatically attaches the JWT access token
 * to every outgoing request as a Bearer token.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth header for public endpoints
        val path = originalRequest.url.encodedPath
        if (path.contains("/auth/register") ||
            path.contains("/auth/login") ||
            path.contains("/auth/refresh")) {
            return chain.proceed(originalRequest)
        }

        // Attach bearer token
        val token = runBlocking { tokenManager.getAccessTokenSync() }

        val authenticatedRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(authenticatedRequest)
    }
}
