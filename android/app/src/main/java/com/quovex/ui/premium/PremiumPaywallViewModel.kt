package com.quovex.ui.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.domain.model.DefaultSubscriptionPlans
import com.quovex.domain.model.PurchaseResult
import com.quovex.domain.model.SubscriptionPlan
import com.quovex.domain.model.UserEntitlement
import com.quovex.domain.usecase.GetSubscriptionPlansUseCase
import com.quovex.domain.usecase.ObserveUserEntitlementUseCase
import com.quovex.domain.usecase.PurchaseSubscriptionUseCase
import com.quovex.domain.usecase.RestorePurchasesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PremiumPaywallUiState(
    val plans: List<SubscriptionPlan> = DefaultSubscriptionPlans.ALL,
    val selectedPlan: SubscriptionPlan = DefaultSubscriptionPlans.ANNUAL,
    val entitlement: UserEntitlement = UserEntitlement.createFreeTier(),
    val isPurchasing: Boolean = false,
    val isRestoring: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class PremiumPaywallViewModel @Inject constructor(
    private val observeUserEntitlementUseCase: ObserveUserEntitlementUseCase,
    private val getSubscriptionPlansUseCase: GetSubscriptionPlansUseCase,
    private val purchaseSubscriptionUseCase: PurchaseSubscriptionUseCase,
    private val restorePurchasesUseCase: RestorePurchasesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumPaywallUiState())
    val uiState: StateFlow<PremiumPaywallUiState> = _uiState.asStateFlow()

    init {
        observeEntitlements()
        observePlans()
    }

    private fun observeEntitlements() {
        viewModelScope.launch {
            observeUserEntitlementUseCase().collect { entitlement ->
                _uiState.update { it.copy(entitlement = entitlement) }
            }
        }
    }

    private fun observePlans() {
        viewModelScope.launch {
            getSubscriptionPlansUseCase().collect { plans ->
                if (plans.isNotEmpty()) {
                    _uiState.update { state ->
                        val selected = plans.find { it.id == state.selectedPlan.id } ?: plans.first()
                        state.copy(plans = plans, selectedPlan = selected)
                    }
                }
            }
        }
    }

    fun selectPlan(plan: SubscriptionPlan) {
        _uiState.update { it.copy(selectedPlan = plan) }
    }

    fun purchaseSelectedPlan() {
        val plan = _uiState.value.selectedPlan
        _uiState.update { it.copy(isPurchasing = true, errorMessage = null) }

        viewModelScope.launch {
            when (val result = purchaseSubscriptionUseCase(plan)) {
                is PurchaseResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isPurchasing = false,
                            successMessage = "Welcome to Quovex Pro! All VIP features unlocked."
                        )
                    }
                }
                is PurchaseResult.UserCanceled -> {
                    _uiState.update { it.copy(isPurchasing = false) }
                }
                is PurchaseResult.Pending -> {
                    _uiState.update {
                        it.copy(
                            isPurchasing = false,
                            successMessage = "Purchase pending. Your Pro status will update shortly."
                        )
                    }
                }
                is PurchaseResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isPurchasing = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun restorePurchases() {
        _uiState.update { it.copy(isRestoring = true, errorMessage = null) }

        viewModelScope.launch {
            val result = restorePurchasesUseCase()
            result.onSuccess { entitlement ->
                _uiState.update {
                    it.copy(
                        isRestoring = false,
                        successMessage = if (entitlement.isPremiumActive)
                            "Purchases restored successfully! You have active ${entitlement.tier.title} access."
                        else
                            "No active Pro subscription found for this Google account."
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isRestoring = false,
                        errorMessage = error.message ?: "Failed to restore purchases."
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
