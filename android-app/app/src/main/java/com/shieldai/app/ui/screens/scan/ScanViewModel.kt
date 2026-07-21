package com.shieldai.app.ui.screens.scan

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shieldai.app.data.model.ScanRequest
import com.shieldai.app.data.model.ScanResult
import com.shieldai.app.data.repository.ShieldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanUiState(
    val isScanning: Boolean = false,
    val result: ScanResult? = null,
    val error: String? = null,
    val selectedChannel: String = "SMS"
)

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val repository: ShieldRepository
) : ViewModel() {

    var uiState by mutableStateOf(ScanUiState())
        private set

    val channels = listOf("SMS", "EMAIL", "URL", "QR", "CALL", "NOTIFICATION", "MANUAL")

    fun selectChannel(channel: String) {
        uiState = uiState.copy(selectedChannel = channel)
    }

    fun scan(content: String, senderInfo: String? = null) {
        if (content.isBlank()) {
            uiState = uiState.copy(error = "Please enter content to scan")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isScanning = true, error = null, result = null)

            val result = repository.scanContent(
                ScanRequest(
                    content = content,
                    channel = uiState.selectedChannel,
                    senderInfo = senderInfo
                )
            )

            uiState = result.fold(
                onSuccess = { scanResult ->
                    uiState.copy(isScanning = false, result = scanResult)
                },
                onFailure = { error ->
                    uiState.copy(isScanning = false, error = error.message ?: "Scan failed")
                }
            )
        }
    }

    fun resetScan() {
        uiState = ScanUiState()
    }

    fun clearError() {
        uiState = uiState.copy(error = null)
    }
}
