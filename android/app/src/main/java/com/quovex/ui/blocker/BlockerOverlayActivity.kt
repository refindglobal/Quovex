package com.quovex.ui.blocker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.MainActivity
import com.quovex.domain.util.TimerFormatter
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexButtonVariant
import com.quovex.ui.components.QuovexCard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlockerOverlayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: "This application"
        val subject = intent.getStringExtra(EXTRA_SUBJECT) ?: "Deep Focus"
        val remainingSeconds = intent.getIntExtra(EXTRA_REMAINING_SECONDS, 1500)
        val attempts = intent.getIntExtra(EXTRA_ATTEMPTS, 1)

        setContent {
            QuovexTheme {
                BlockerOverlayScreen(
                    blockedPackage = blockedPackage,
                    subject = subject,
                    remainingSeconds = remainingSeconds,
                    attemptsCount = attempts,
                    onReturnToFocus = {
                        val mainIntent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        }
                        startActivity(mainIntent)
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"
        const val EXTRA_SUBJECT = "extra_subject"
        const val EXTRA_REMAINING_SECONDS = "extra_remaining_seconds"
        const val EXTRA_ATTEMPTS = "extra_attempts"
    }
}

@Composable
fun BlockerOverlayScreen(
    blockedPackage: String,
    subject: String,
    remainingSeconds: Int,
    attemptsCount: Int,
    onReturnToFocus: () -> Unit
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing
    val formattedTime = TimerFormatter.formatRemainingTime(remainingSeconds)

    Scaffold(
        containerColor = colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = spacing.xl, vertical = spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Shield Glow
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(colors.primaryGlow, CircleShape)
                        .border(2.dp, colors.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(Modifier.height(spacing.lg))

                Text(
                    text = "Distraction Blocked!",
                    style = QuovexTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(spacing.xs))

                Text(
                    text = "You are in an active deep work session for $subject.",
                    style = QuovexTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // Session Countdown Timer Card
            QuovexCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surfaceElevated,
                borderColor = colors.primary,
                borderWidth = 1.5.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(spacing.xs))
                        Text(
                            text = "ACTIVE STUDY LOCK",
                            style = QuovexTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Spacer(Modifier.height(spacing.sm))

                    Text(
                        text = formattedTime,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = colors.textPrimary
                    )

                    Spacer(Modifier.height(spacing.xs))

                    Text(
                        text = "REMAINING FOR $subject",
                        style = QuovexTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
            }

            // Resisted Counter & Motivational Quote
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .padding(horizontal = spacing.base, vertical = spacing.sm)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = colors.warning,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(spacing.xs))
                        Text(
                            text = "Resisted $attemptsCount distraction ${if (attemptsCount == 1) "attempt" else "attempts"} today",
                            style = QuovexTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                }

                Spacer(Modifier.height(spacing.lg))

                Text(
                    text = "“The pain of discipline is far less than the pain of regret.”",
                    style = QuovexTheme.typography.bodyMedium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // Return to Focus Zone Primary CTA
            QuovexButton(
                text = "Return to Focus Zone",
                onClick = onReturnToFocus,
                variant = QuovexButtonVariant.Primary,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = colors.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
