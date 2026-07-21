package com.shieldai.app.ui.navigation

import androidx.lifecycle.ViewModel
import com.shieldai.app.data.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    val tokenManager: TokenManager
) : ViewModel()
