package com.quovex.ui.premium

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.SubscriptionPlan
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexButtonVariant
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexTopAppBar

@Composable
fun PremiumPaywallScreen(
    viewModel: PremiumPaywallViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            QuovexTopAppBar(
                title = "Quovex Pro",
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.lg, vertical = spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glowing Crown Hero Header
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(colors.primaryGlow, CircleShape)
                    .border(2.dp, colors.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.height(spacing.md))

            Text(
                text = "Unlock Ultimate Focus & AI Tutoring",
                style = QuovexTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(spacing.xs))

            Text(
                text = "Supercharge your exam preparation with unlimited Groq/Cerebras AI tutoring, offline soundscapes & camera focus.",
                style = QuovexTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(spacing.xl))

            // Active Entitlement Banner if user already has Pro
            if (state.entitlement.isPremiumActive) {
                QuovexCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surfaceElevated,
                    borderColor = colors.primary,
                    borderWidth = 1.5.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.base),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(spacing.md))
                        Column {
                            Text(
                                text = "👑 ${state.entitlement.tier.title} Active",
                                style = QuovexTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (state.entitlement.expiryDateFormatted != null)
                                    "Renews / Expires: ${state.entitlement.expiryDateFormatted}"
                                else
                                    "Permanent VIP Access",
                                style = QuovexTheme.typography.bodySmall,
                                color = colors.primary
                            )
                        }
                    }
                }
                Spacer(Modifier.height(spacing.xl))
            }

            // Pro Features Comparison Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                FeatureRow(
                    icon = Icons.Default.AutoAwesome,
                    title = "Unlimited 24/7 AI Tutoring & Doubts",
                    description = "Groq & Cerebras deep explanations (vs 10/day on Free)"
                )
                FeatureRow(
                    icon = Icons.Default.MenuBook,
                    title = "Unlimited NCERT PDF Textbook Scanning",
                    description = "OCR question extraction & automatic flashcard generator"
                )
                FeatureRow(
                    icon = Icons.Default.Headphones,
                    title = "All 9 Binaural Beats & Ambient Soundscapes",
                    description = "Alpha, Gamma, Beta, Theta binaural beats & Rain synthesizers"
                )
                FeatureRow(
                    icon = Icons.Default.Videocam,
                    title = "Real-time AI Camera Focus & Drowsiness",
                    description = "100% on-device ML eye openness & posture tracking"
                )
                FeatureRow(
                    icon = Icons.Default.Shield,
                    title = "100% Ad-Free Deep Work Zone",
                    description = "Zero banners, zero interstitials, zero distractions"
                )
            }

            Spacer(Modifier.height(spacing.xl))

            // Plan Selection Cards
            Text(
                text = "CHOOSE YOUR PLAN",
                style = QuovexTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
                letterSpacing = 1.2.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(spacing.xs))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                state.plans.forEach { plan ->
                    PlanCard(
                        plan = plan,
                        isSelected = state.selectedPlan.id == plan.id,
                        onClick = { viewModel.selectPlan(plan) }
                    )
                }
            }

            Spacer(Modifier.height(spacing.xl))

            // Checkout / Start Free Trial Button
            QuovexButton(
                text = if (state.isPurchasing) "Processing..." else if (state.selectedPlan.isBestValue)
                    "Start 7-Day Free Trial • ${state.selectedPlan.formattedPrice}/yr"
                else
                    "Upgrade to Pro — ${state.selectedPlan.formattedPrice}",
                onClick = { viewModel.purchaseSelectedPlan() },
                variant = QuovexButtonVariant.Primary,
                enabled = !state.isPurchasing,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Upgrade to Quovex Pro ${state.selectedPlan.title}" }
            )

            Spacer(Modifier.height(spacing.sm))

            // Restore Purchases Action
            TextButton(
                onClick = { viewModel.restorePurchases() },
                enabled = !state.isRestoring
            ) {
                if (state.isRestoring) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = colors.primary
                    )
                    Spacer(Modifier.width(spacing.xs))
                }
                Text(
                    text = "Restore Purchases",
                    style = QuovexTheme.typography.labelMedium,
                    color = colors.textSecondary
                )
            }

            // Trust & Policy Text
            Text(
                text = "Recurring billing • Cancel anytime in Google Play Store\nProtected by 256-bit encryption",
                style = QuovexTheme.typography.labelSmall,
                color = colors.textTertiary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(spacing.base))
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .padding(horizontal = spacing.base, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(colors.primaryGlow, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = QuovexTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = description,
                style = QuovexTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    QuovexCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = if (isSelected) colors.surfaceElevated else colors.surface,
        borderColor = if (isSelected) colors.primary else colors.border,
        borderWidth = if (isSelected) 2.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.base)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Badge if any
                    if (plan.badge != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (plan.isBestValue) colors.primary else colors.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = plan.badge,
                                style = QuovexTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (plan.isBestValue) colors.onPrimary else colors.primary,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }

                    Text(
                        text = plan.title,
                        style = QuovexTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) colors.primary else colors.textPrimary
                    )
                    Text(
                        text = plan.subtitle,
                        style = QuovexTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = plan.formattedPrice,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) colors.primary else colors.textPrimary
                    )
                    Text(
                        text = plan.periodDescription,
                        style = QuovexTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}
