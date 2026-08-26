package com.quovex.domain.usecase

import android.app.Activity
import com.quovex.domain.manager.AdManager
import com.quovex.domain.model.AdRewardResult
import com.quovex.domain.repository.BillingRepository
import javax.inject.Inject

class ShowRewardedAdUseCase @Inject constructor(
    private val adManager: AdManager,
    private val billingRepository: BillingRepository
) {
    operator fun invoke(
        activity: Activity,
        onResult: (AdRewardResult) -> Unit
    ) {
        adManager.showRewarded(activity) { result ->
            if (result is AdRewardResult.Success) {
                billingRepository.grantBonusAiQueries(result.bonusQueries)
            }
            onResult(result)
        }
    }

    fun preload() {
        adManager.preloadRewarded()
    }
}
