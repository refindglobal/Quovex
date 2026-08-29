package com.quovex.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.BuildConfig
import com.quovex.R
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexTextField

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: (isNewUser: Boolean) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val colors = QuovexTheme.colors

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
                .padding(horizontal = QuovexTheme.spacing.xl, vertical = QuovexTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))

                // ── 3D Stopwatch Q Emblem & Wordmark ─────────────────────────
                Image(
                    painter = painterResource(id = R.drawable.ic_brand_emblem),
                    contentDescription = "Quovex Emblem",
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))

                Image(
                    painter = painterResource(id = R.drawable.ic_brand_wordmark),
                    contentDescription = "QUOVEX",
                    modifier = Modifier
                        .width(180.dp)
                        .height(38.dp)
                )

                Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))

                Text(
                    text = if (state.isSignUpMode) "Create your student account" else "Sign in to your study space",
                    style = QuovexTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(QuovexTheme.spacing.xl))

                // ── Mode Switcher (Sign In vs Register) ───────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(QuovexTheme.shapes.medium)
                        .background(colors.surfaceVariant)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(QuovexTheme.shapes.small)
                            .background(if (!state.isSignUpMode) colors.surface else Color.Transparent)
                            .clickable { if (state.isSignUpMode) viewModel.toggleAuthMode() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign In",
                            style = QuovexTheme.typography.labelMedium,
                            fontWeight = if (!state.isSignUpMode) FontWeight.Bold else FontWeight.Medium,
                            color = if (!state.isSignUpMode) colors.primary else colors.textSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(QuovexTheme.shapes.small)
                            .background(if (state.isSignUpMode) colors.surface else Color.Transparent)
                            .clickable { if (!state.isSignUpMode) viewModel.toggleAuthMode() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Register",
                            style = QuovexTheme.typography.labelMedium,
                            fontWeight = if (state.isSignUpMode) FontWeight.Bold else FontWeight.Medium,
                            color = if (state.isSignUpMode) colors.primary else colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(QuovexTheme.spacing.lg))

                // ── Email & Password Form ────────────────────────────────────
                QuovexCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surface,
                    borderColor = colors.border,
                    elevation = QuovexTheme.elevation.card
                ) {
                    Column(modifier = Modifier.padding(QuovexTheme.spacing.base)) {
                        QuovexTextField(
                            value = state.emailInput,
                            onValueChange = { viewModel.onEmailChanged(it) },
                            label = "Email Address",
                            placeholder = "student@example.com",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Email,
                                    contentDescription = null,
                                    tint = colors.textSecondary
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))

                        QuovexTextField(
                            value = state.passwordInput,
                            onValueChange = { viewModel.onPasswordChanged(it) },
                            label = "Password",
                            placeholder = "••••••••",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = colors.textSecondary
                                )
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.lg))

                        QuovexButton(
                            text = if (state.isSignUpMode) "Create Account" else "Sign In",
                            onClick = { viewModel.submitEmailAuth(onAuthSuccess) },
                            isLoading = state.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // ── Error Message ─────────────────────────────────────────
                AnimatedVisibility(visible = state.errorMessage != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))
                        Text(
                            text = state.errorMessage ?: "",
                            style = QuovexTheme.typography.bodySmall,
                            color = colors.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(QuovexTheme.spacing.lg))

                // ── OR Divider ───────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = colors.border)
                    Text(
                        text = "OR",
                        style = QuovexTheme.typography.labelSmall,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(horizontal = QuovexTheme.spacing.md)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = colors.border)
                }

                Spacer(modifier = Modifier.height(QuovexTheme.spacing.lg))

                // ── 1-Tap Google Sign-In Card ────────────────────────────────
                QuovexCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = QuovexTheme.shapes.card,
                    backgroundColor = colors.surfaceElevated,
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
                            .padding(vertical = 14.dp, horizontal = 20.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color.White, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "G",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF4285F4)
                            )
                        }
                        Spacer(modifier = Modifier.width(QuovexTheme.spacing.md))
                        Text(
                            text = "1-Tap Google Sign-In",
                            style = QuovexTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            // ── Footer ───────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = QuovexTheme.spacing.xl, bottom = QuovexTheme.spacing.sm)
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = "Encrypted",
                    tint = colors.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(QuovexTheme.spacing.xs))
                Text(
                    text = "100% Encrypted • Zero Guest Mode • Powered by Firebase",
                    style = QuovexTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}
