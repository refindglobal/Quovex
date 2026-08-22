package com.quovex.ui.notes

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexButtonVariant
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexChip
import com.quovex.ui.components.QuovexTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNoteBottomSheet(
    availableSubjects: List<String>,
    isProcessing: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onQuickNoteCreate: (title: String, subject: String, content: String, summarizeWithAi: Boolean) -> Unit,
    onUrlImport: (url: String, subject: String) -> Unit,
    onScanDocumentClick: () -> Unit
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Quick Note", "Upload PDF", "Import Link")

    // Quick Note fields
    var quickTitle by remember { mutableStateOf("") }
    var quickSubject by remember { mutableStateOf(availableSubjects.firstOrNull() ?: "Physics") }
    var quickContent by remember { mutableStateOf("") }
    var summarizeWithAi by remember { mutableStateOf(false) }

    // PDF fields
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var pdfTitle by remember { mutableStateOf("") }
    var pdfSubject by remember { mutableStateOf(availableSubjects.firstOrNull() ?: "Physics") }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedPdfUri = uri
        if (uri != null && pdfTitle.isBlank()) {
            pdfTitle = uri.lastPathSegment?.substringAfterLast('/')?.removeSuffix(".pdf") ?: "Uploaded PDF Note"
        }
    }

    // URL fields
    var importUrl by remember { mutableStateOf("") }
    var urlSubject by remember { mutableStateOf(availableSubjects.firstOrNull() ?: "Physics") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        contentColor = colors.textPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.lg)
                .padding(bottom = spacing.xxl)
        ) {
            Text(
                text = "Add Study Note",
                style = QuovexTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(Modifier.height(spacing.sm))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = colors.surfaceVariant,
                contentColor = colors.primary
            ) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                label,
                                style = QuovexTheme.typography.labelMedium,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) colors.primary else colors.textSecondary
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(spacing.base))

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    style = QuovexTheme.typography.bodySmall,
                    color = colors.error,
                    modifier = Modifier.padding(bottom = spacing.sm)
                )
            }

            when (selectedTab) {
                0 -> {
                    // ── QUICK NOTE ──────────────────────────────────────────
                    QuovexTextField(
                        value = quickTitle,
                        onValueChange = { quickTitle = it },
                        label = "Note Title",
                        placeholder = "e.g. Wave Optics Core Formulas"
                    )

                    Spacer(Modifier.height(spacing.sm))

                    Text("Subject", style = QuovexTheme.typography.labelSmall, color = colors.textSecondary)
                    Spacer(Modifier.height(spacing.xxs))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        items(availableSubjects) { subj ->
                            QuovexChip(
                                label = subj,
                                isSelected = quickSubject == subj,
                                onClick = { quickSubject = subj }
                            )
                        }
                    }

                    Spacer(Modifier.height(spacing.sm))

                    QuovexTextField(
                        value = quickContent,
                        onValueChange = { quickContent = it },
                        label = "Content",
                        placeholder = "Type your notes, equations, or concepts here...",
                        singleLine = false,
                        minLines = 4
                    )

                    Spacer(Modifier.height(spacing.sm))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = if (summarizeWithAi) colors.primary else colors.textTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.size(spacing.xs))
                            Text(
                                "Polish & Key Points with AI",
                                style = QuovexTheme.typography.bodySmall,
                                color = if (summarizeWithAi) colors.primary else colors.textSecondary
                            )
                        }
                        androidx.compose.material3.Switch(
                            checked = summarizeWithAi,
                            onCheckedChange = { summarizeWithAi = it }
                        )
                    }

                    Spacer(Modifier.height(spacing.base))

                    QuovexButton(
                        text = if (isProcessing) "Saving Note..." else "Save Note",
                        onClick = {
                            onQuickNoteCreate(quickTitle, quickSubject, quickContent, summarizeWithAi)
                        },
                        enabled = quickTitle.isNotBlank() && quickContent.isNotBlank() && !isProcessing,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                1 -> {
                    // ── UPLOAD PDF ──────────────────────────────────────────
                    QuovexCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = colors.surfaceVariant,
                        onClick = { pdfPickerLauncher.launch("application/pdf") }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.lg),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.UploadFile,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.height(spacing.sm))
                            Text(
                                text = if (selectedPdfUri != null) "Selected: ${selectedPdfUri?.lastPathSegment?.substringAfterLast('/') ?: "PDF Document"}"
                                else "Choose PDF Document",
                                style = QuovexTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "PDF is uploaded to cloud storage and parsed by AI backend",
                                style = QuovexTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Spacer(Modifier.height(spacing.sm))

                    if (selectedPdfUri != null) {
                        QuovexTextField(
                            value = pdfTitle,
                            onValueChange = { pdfTitle = it },
                            label = "Note Title"
                        )
                        Spacer(Modifier.height(spacing.sm))
                        Text("Subject", style = QuovexTheme.typography.labelSmall, color = colors.textSecondary)
                        Spacer(Modifier.height(spacing.xxs))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                            items(availableSubjects) { subj ->
                                QuovexChip(
                                    label = subj,
                                    isSelected = pdfSubject == subj,
                                    onClick = { pdfSubject = subj }
                                )
                            }
                        }
                        Spacer(Modifier.height(spacing.base))
                        QuovexButton(
                            text = if (isProcessing) "Uploading & Processing..." else "Upload & Summarize",
                            onClick = {
                                onQuickNoteCreate(pdfTitle, pdfSubject, "Uploaded PDF source: ${selectedPdfUri?.lastPathSegment}", true)
                            },
                            enabled = !isProcessing && pdfTitle.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                2 -> {
                    // ── IMPORT LINK ─────────────────────────────────────────
                    QuovexTextField(
                        value = importUrl,
                        onValueChange = { importUrl = it },
                        label = "Article or YouTube URL",
                        placeholder = "https://en.wikipedia.org/... or https://youtube.com/..."
                    )

                    Spacer(Modifier.height(spacing.sm))

                    Text("Subject", style = QuovexTheme.typography.labelSmall, color = colors.textSecondary)
                    Spacer(Modifier.height(spacing.xxs))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        items(availableSubjects) { subj ->
                            QuovexChip(
                                label = subj,
                                isSelected = urlSubject == subj,
                                onClick = { urlSubject = subj }
                            )
                        }
                    }

                    Spacer(Modifier.height(spacing.base))

                    QuovexButton(
                        text = if (isProcessing) "Extracting & Summarizing..." else "Import & Summarize Note",
                        onClick = {
                            onUrlImport(importUrl, urlSubject)
                        },
                        enabled = importUrl.isNotBlank() && !isProcessing,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
