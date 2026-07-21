package com.shieldai.app.ui.screens.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shieldai.app.data.local.TokenManager
import com.shieldai.app.data.model.ScanResult
import com.shieldai.app.data.model.ThreatHistoryItem
import com.shieldai.app.data.repository.ShieldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val recentScans: List<ScanResult> = emptyList(),
    val recentThreats: List<ThreatHistoryItem> = emptyList(),
    val totalScans: Int = 0,
    val threatsBlocked: Int = 0,
    val safeScore: Int = 95, // Calculated safety percentage
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: ShieldRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    var uiState by mutableStateOf(DashboardUiState())
        private set

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            // Load user name
            val name = tokenManager.userName.first() ?: "User"
            uiState = uiState.copy(userName = name)

            // Load recent scans
            val scanResult = repository.getScanHistory(page = 0)
            scanResult.onSuccess { page ->
                val threats = page.content.count { it.riskLabel != "SAFE" }
                uiState = uiState.copy(
                    recentScans = page.content.take(5),
                    totalScans = page.totalElements.toInt(),
                    threatsBlocked = threats,
                    safeScore = if (page.totalElements > 0) {
                        ((page.totalElements - threats) * 100 / page.totalElements).toInt()
                    } else 100
                )
            }

            // Load threat history
            val historyResult = repository.getThreatHistory(page = 0)
            historyResult.onSuccess { page ->
                uiState = uiState.copy(recentThreats = page.content.take(5))
            }

            uiState = uiState.copy(isLoading = false)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
