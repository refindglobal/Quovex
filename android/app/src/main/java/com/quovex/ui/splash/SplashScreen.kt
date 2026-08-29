package com.quovex.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.quovex.R
import com.quovex.data.local.UserPreferencesManager
import com.quovex.theme.QuovexTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    userPreferencesManager: UserPreferencesManager?,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val colors = QuovexTheme.colors

    // Breathing glow & pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "SplashPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    LaunchedEffect(Unit) {
        delay(1800) // Rich branded delay
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            onNavigateToDashboard()
        } else {
            val isOnboardingCompleted = userPreferencesManager?.isOnboardingCompleted() ?: false
            if (isOnboardingCompleted) {
                onNavigateToAuth()
            } else {
                onNavigateToOnboarding()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF003828).copy(alpha = glowAlpha),
                        Color(0xFF0A0F0D),
                        Color(0xFF060908)
                    ),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 3D "Q" Stopwatch Emblem
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_brand_emblem),
                    contentDescription = "Quovex Emblem",
                    modifier = Modifier.size(130.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3D Chrome "QUOVEX" Wordmark
            Image(
                painter = painterResource(id = R.drawable.ic_brand_wordmark),
                contentDescription = "QUOVEX",
                modifier = Modifier
                    .width(220.dp)
                    .height(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle Tagline
            Text(
                text = "FOCUS • LEARN • MASTER",
                color = colors.primary.copy(alpha = 0.9f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                modifier = Modifier.alpha(0.85f)
            )
        }
    }
}
