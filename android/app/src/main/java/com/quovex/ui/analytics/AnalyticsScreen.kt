package com.quovex.ui.analytics

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.quovex.domain.model.HourlyProductivity
import com.quovex.domain.model.PerformanceInsights
import com.quovex.domain.model.SubjectStudyTime
import com.quovex.theme.BrandEmerald
import com.quovex.theme.BrandEmeraldDim
import com.quovex.theme.SurfaceDark
import com.quovex.theme.SurfaceGlass
import com.quovex.theme.TextPrimary
import com.quovex.theme.TextSecondary
import com.quovex.ui.components.QuovexButton
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPaywall: () -> Unit = {}
) {
    val context = LocalContext.current
    val hourlyProductivity by viewModel.hourlyProductivity.collectAsState()
    val subjects by viewModel.subjectBreakdown.collectAsState()
    val insights by viewModel.performanceInsights.collectAsState()
    val pdfState by viewModel.pdfExportState.collectAsState()
    val entitlement by viewModel.userEntitlement.collectAsState()

    // Handle PDF Export Share Sheet
    LaunchedEffect(pdfState) {
        when (val state = pdfState) {
            is PdfExportState.Success -> {
                sharePdfReport(context, state.file)
                viewModel.clearPdfExportState()
            }
            is PdfExportState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.clearPdfExportState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Study Analytics & Insights",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        },
        containerColor = SurfaceDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. AI Cognitive Coach Insights Card
            insights?.let { insightData ->
                AiCoachInsightsCard(insightData = insightData)
            }

            // 2. 24-Hour Peak Productivity Curve
            HourlyProductivityCard(hourlyData = hourlyProductivity)

            // 3. Subject Time & Mastery Distribution
            SubjectBreakdownCard(subjects = subjects)

            // 4. Export Weekly PDF Report Action
            ExportPdfCard(
                pdfState = pdfState,
                isPro = entitlement.isAdvancedAnalyticsUnlocked,
                onExportClick = {
                    if (entitlement.isAdvancedAnalyticsUnlocked) {
                        viewModel.exportWeeklyPdfReport()
                    } else {
                        onNavigateToPaywall()
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AiCoachInsightsCard(insightData: PerformanceInsights) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceGlass
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BrandEmeraldDim),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = BrandEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AI Study Coach Telemetry",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Computed from your real study sessions",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = insightData.aiInsight,
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricPill(
                    label = "Peak Window",
                    value = insightData.bestHourWindow,
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = "Best Study Day",
                    value = insightData.bestDayOfWeek,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricPill(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = SurfaceDark.copy(alpha = 0.6f)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = label, color = TextSecondary, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = BrandEmerald,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun HourlyProductivityCard(hourlyData: List<HourlyProductivity>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceGlass
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = BrandEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "24-Hour Productivity Curve",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 15.sp
                )
            }

            Text(
                text = "Discover the hours when your focus score and duration peak.",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            val maxMinutes = (hourlyData.maxOfOrNull { it.totalMinutes } ?: 1).coerceAtLeast(30)

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(hourlyData) { item ->
                    val barHeightFraction = (item.totalMinutes.toFloat() / maxMinutes).coerceIn(0.08f, 1f)
                    val isPeak = item.totalMinutes == maxMinutes && item.totalMinutes > 0

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(28.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .height(80.dp)
                                .width(14.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // Track
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceDark.copy(alpha = 0.7f))
                            )
                            // Filled bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((80 * barHeightFraction).dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isPeak) Color(0xFFFFD54F) else BrandEmerald)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = String.format(java.util.Locale.US, "%02d", item.hourOfDay),
                            color = if (isPeak) BrandEmerald else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = if (isPeak) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectBreakdownCard(subjects: List<SubjectStudyTime>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceGlass
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = null,
                    tint = BrandEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Subject Mastery & Time Distribution",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (subjects.isEmpty()) {
                Text(
                    text = "No study sessions recorded yet. Start a focus timer to track subject distribution.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    subjects.take(5).forEach { subj ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = subj.subject,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${subj.totalMinutes / 60}h ${subj.totalMinutes % 60}m (${(subj.percentage * 100).toInt()}%)",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(SurfaceDark)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(subj.percentage)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(BrandEmerald)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportPdfCard(
    pdfState: PdfExportState,
    isPro: Boolean,
    onExportClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceGlass
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = BrandEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Executive Study Analytics PDF",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 15.sp
                )
            }

            Text(
                text = "Generate and share a clean, high-resolution A4 study summary report for parents, teachers, or personal progress archives.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
            )

            QuovexButton(
                text = if (pdfState is PdfExportState.Generating) "Generating Report..." else if (!isPro) "Unlock PDF Export (Quovex Pro)" else "Export & Share Weekly PDF Report",
                onClick = onExportClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = pdfState !is PdfExportState.Generating
            )
        }
    }
}

private fun sharePdfReport(context: android.content.Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "com.quovex.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Quovex Weekly Study Report")
            putExtra(Intent.EXTRA_TEXT, "Here is my latest weekly cognitive performance and study report from Quovex.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Study Report PDF via"))
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open share sheet: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
