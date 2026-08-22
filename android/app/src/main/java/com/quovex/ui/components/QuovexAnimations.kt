package com.quovex.ui.components

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically

object QuovexAnimations {
    const val DURATION_FAST = 150
    const val DURATION_NORMAL = 300
    const val DURATION_SLOW = 500
    const val DURATION_CARD_FLIP = 400

    val fastTween = tween<Float>(durationMillis = DURATION_FAST, easing = FastOutSlowInEasing)
    val normalTween = tween<Float>(durationMillis = DURATION_NORMAL, easing = FastOutSlowInEasing)
    val slowTween = tween<Float>(durationMillis = DURATION_SLOW, easing = LinearOutSlowInEasing)

    fun fadeInTransition(durationMillis: Int = DURATION_NORMAL): EnterTransition {
        return fadeIn(animationSpec = tween(durationMillis))
    }

    fun fadeOutTransition(durationMillis: Int = DURATION_NORMAL): ExitTransition {
        return fadeOut(animationSpec = tween(durationMillis))
    }

    fun expandCollapseTransition(): Pair<EnterTransition, ExitTransition> {
        val enter = fadeIn(animationSpec = normalTween) + expandVertically(animationSpec = tween(DURATION_NORMAL))
        val exit = fadeOut(animationSpec = normalTween) + shrinkVertically(animationSpec = tween(DURATION_NORMAL))
        return enter to exit
    }

    fun scaleInOutTransition(): Pair<EnterTransition, ExitTransition> {
        val enter = fadeIn(animationSpec = fastTween) + scaleIn(animationSpec = fastTween, initialScale = 0.92f)
        val exit = fadeOut(animationSpec = fastTween) + scaleOut(animationSpec = fastTween, targetScale = 0.92f)
        return enter to exit
    }
}
