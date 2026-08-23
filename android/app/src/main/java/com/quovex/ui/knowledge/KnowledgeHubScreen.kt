package com.quovex.ui.knowledge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.quovex.ui.components.QuovexButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.LearningMaterial
import com.quovex.theme.BrandEmerald
import com.quovex.theme.BrandEmeraldDim
import com.quovex.theme.SurfaceDark
import com.quovex.theme.SurfaceGlass
import com.quovex.theme.TextPrimary
import com.quovex.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeHubScreen(
    viewModel: KnowledgeHubViewModel,
    onNavigateToAddMaterial: () -> Unit,
    onNavigateToMaterialDetail: (materialId: Long) -> Unit,
    onNavigateToFlashcards: (deckId: Long) -> Unit,
    onNavigateToNcert: () -> Unit = {},
    onNavigateToOriginals: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Knowledge Hub",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Study materials, concepts & active recall",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddMaterial,
                containerColor = BrandEmerald,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Learning Material",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = SurfaceDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── QUOVEX ORIGINALS BANNER ──────────────────────────────────────
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onNavigateToOriginals),
                    color = SurfaceGlass,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BrandEmerald.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = BrandEmerald,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Quovex Originals",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 16.sp
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        color = BrandEmerald.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = "ORIGINALS",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandEmerald,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "High-Yield Conceptual Educational Books",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Quovex Originals",
                            tint = BrandEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ── OFFICIAL RESOURCES (NCERT LIBRARY) BANNER ────────────────────
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onNavigateToNcert),
                    color = SurfaceGlass,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoStories,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "NCERT Official Library",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 16.sp
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        color = Color.White.copy(alpha = 0.1f),
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = "OFFICIAL",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextSecondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Class 9–12 CBSE Textbooks & AI Study",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open NCERT",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Subject Filter Bar
            item {
                SubjectFilterRow(
                    subjects = uiState.subjects,
                    selectedSubject = uiState.selectedSubject,
                    onSelect = { viewModel.selectSubject(it) }
                )
            }

            if (uiState.materials.isEmpty()) {
                item {
                    EmptyKnowledgeHubState(onAddClick = onNavigateToAddMaterial)
                }
            } else {
                items(uiState.materials, key = { it.id }) { material ->
                    MaterialItemCard(
                        material = material,
                        onClick = { onNavigateToMaterialDetail(material.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectFilterRow(
    subjects: List<String>,
    selectedSubject: String?,
    onSelect: (String?) -> Unit
) {
    val allSubjects = listOf("All") + subjects
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(allSubjects) { subject ->
            val isSelected = (subject == "All" && selectedSubject == null) || subject == selectedSubject
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) BrandEmerald else SurfaceGlass,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        if (subject == "All") onSelect(null) else onSelect(subject)
                    }
            ) {
                Text(
                    text = subject,
                    color = if (isSelected) Color.Black else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MaterialItemCard(
    material: LearningMaterial,
    onClick: () -> Unit
) {
    val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(material.createdAt))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = SurfaceGlass,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = BrandEmeraldDim.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = material.subject.ifBlank { "General" },
                        color = BrandEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = formattedDate,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Text(
                text = material.title.ifBlank { "Untitled Material" },
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            if (material.topic.isNotBlank()) {
                Text(
                    text = material.topic,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            // Badges
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                if (material.needsProcessing) {
                    BadgeChip(
                        label = "Needs Processing",
                        color = Color(0xFFFFB74D),
                        icon = Icons.Default.Refresh
                    )
                } else {
                    if (material.summary.isNotBlank()) {
                        BadgeChip(
                            label = "Summary Ready",
                            color = BrandEmerald,
                            icon = Icons.Default.CheckCircle
                        )
                    }
                    if (material.keyPoints.isNotEmpty()) {
                        BadgeChip(
                            label = "${material.keyPoints.size} Key Concepts",
                            color = Color(0xFF64B5F6),
                            icon = Icons.Default.AutoAwesome
                        )
                    }
                    if (material.flashcardCount > 0) {
                        BadgeChip(
                            label = "${material.flashcardCount} Flashcards",
                            color = Color(0xFFBA68C8),
                            icon = Icons.Default.Style
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeChip(
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EmptyKnowledgeHubState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            tint = TextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No study materials yet",
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Scan notes, import articles or YouTube lectures to start transforming your study routine.",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        QuovexButton(
            text = "Add Learning Material",
            onClick = onAddClick,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
