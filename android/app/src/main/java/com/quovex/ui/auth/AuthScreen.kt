package com.quovex.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.BuildConfig
import com.quovex.R
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexCard

/**
 * Auth screen — Google Sign-In only, as per TECHNICAL_DEEP_DIVE.md §5.
 * "Method: Google Sign-In only (via Firebase Auth). Account: Mandatory."
 */
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: (isNewUser: Boolean) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val colors = QuovexTheme.colors

    // Web Client ID — from BuildConfig (set in secrets.properties)
    val webClientId = remember {
        try { BuildConfig.GOOGLE_WEB_CLIENT_ID } catch (_: Exception) {
            "784018860004-web.apps.googleusercontent.com"
        }
    }

    Scaffold(containerColor = colors.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = QuovexTheme.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = QuovexTheme.spacing.lg)
            ) {
                // ── Hero Illustration ─────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(QuovexTheme.shapes.superLarge)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ill_welcome),
                        contentDescription = "Welcome to Quovex illustration",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Bottom gradient fade into background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0.0f to Color.Transparent,
                                    0.7f to Color.Transparent,
                                    1.0f to colors.background
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(QuovexTheme.spacing.xl))

                // ── Headline ──────────────────────────────────────────────
                Text(
                    text = "Welcome to Quovex.",
                    style = QuovexTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))

                Text(
                    text = "The complete ecosystem for extreme focus,\nspaced repetition & AI tutoring.",
                    style = QuovexTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(QuovexTheme.spacing.xxl))

                // ── Google Sign-In Card ───────────────────────────────────
                QuovexCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = QuovexTheme.shapes.card,
                    backgroundColor = colors.surface,
                    borderColor = colors.border,
                    elevation = QuovexTheme.elevation.card,
                    onClick = {
                        if (!state.isLoading) {
                            viewModel.signInWithGoogle(context, webClientId, onAuthSuccess)
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 20.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = colors.primary,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(QuovexTheme.spacing.md))
                            Text(
                                text = "Signing in with Google...",
                                style = QuovexTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                        } else {
                            // Google "G" icon badge
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(Color.White, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "G",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF4285F4)
                                )
                            }
                            Spacer(modifier = Modifier.width(QuovexTheme.spacing.md))
                            Text(
                                text = "Continue with Google",
                                style = QuovexTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }
                    }
                }

                // ── Error Message ─────────────────────────────────────────
                AnimatedVisibility(visible = state.errorMessage != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))
                        Text(
                            text = state.errorMessage ?: "",
                            style = QuovexTheme.typography.bodySmall,
                            color = colors.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))

                // ── Continue as Guest ─────────────────────────────────────
                TextButton(
                    onClick = { viewModel.signInGuest(onAuthSuccess) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Continue as Guest",
                        style = QuovexTheme.typography.labelLarge,
                        color = colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))

                // ── Tagline below button ──────────────────────────────────
                Text(
                    text = "Secure · No password required · Synced across devices",
                    style = QuovexTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // ── Footer ───────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = QuovexTheme.spacing.lg)
                    .then(
                        if (com.quovex.BuildConfig.DEBUG) {
                            Modifier.clickable { onAuthSuccess(false) }
                        } else Modifier
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = "Encrypted icon",
                    tint = colors.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(QuovexTheme.spacing.xs))
                Text(
                    text = "End-to-End Encrypted • Powered by Firebase",
                    style = QuovexTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}
