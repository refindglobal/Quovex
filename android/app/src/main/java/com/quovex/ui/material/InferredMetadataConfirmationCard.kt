package com.quovex.ui.material

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.SubjectCatalog
import com.quovex.domain.model.SubjectInference
import com.quovex.theme.BrandEmerald
import com.quovex.theme.BrandEmeraldDim
import com.quovex.theme.SurfaceGlass
import com.quovex.theme.TextPrimary
import com.quovex.theme.TextSecondary
import com.quovex.theme.TextTertiary
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexCard

/**
 * Interactive Inferred Metadata Confirmation Card (Module B: L-010 to L-016).
 *
 * Displays AI detected Subject, Topic, Subtopic, and Exam Relevance with
 * single-tap [Confirm] or [Change] controls.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InferredMetadataConfirmationCard(
    inference: SubjectInference,
    initialTitle: String,
    onConfirm: (confirmedSubject: String, confirmedTopic: String, confirmedTitle: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var selectedSubject by remember { mutableStateOf(inference.subject) }
    var enteredTopic by remember { mutableStateOf(inference.topic) }
    var enteredTitle by remember { mutableStateOf(initialTitle.ifBlank { "${inference.subject} - ${inference.topic}" }) }

    val confidencePercent = (inference.confidence * 100).toInt().coerceIn(60, 99)

    QuovexCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with AI Sparkle and Confidence Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BrandEmeraldDim),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = BrandEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "AI Classification Detected",
                            fontSize = 12.sp,
                            color = BrandEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Review & Confirm",
                            fontSize = 14.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Confidence pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BrandEmerald.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "$confidencePercent% Match",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandEmerald,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Inferred Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                BrandEmerald.copy(alpha = 0.08f),
                                Color(0xFF141F1A)
                            )
                        )
                    )
                    .border(1.dp, BrandEmerald.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Looks like:",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Text(
                        text = "${selectedSubject} · ${enteredTopic}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    if (!inference.subtopic.isNullOrBlank()) {
                        Text(
                            text = "Subtopic: ${inference.subtopic}",
                            fontSize = 12.sp,
                            color = TextTertiary
                        )
                    }
                }
            }

            // Exam Relevance Tags
            if (inference.examRelevance.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    inference.examRelevance.forEach { exam ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SurfaceGlass,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF26332B))
                        ) {
                            Text(
                                text = "🎯 $exam",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Expandable Edit Section
            AnimatedVisibility(
                visible = isEditing,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Customize Subject & Topic",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    // Subject Selector Chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val commonSubjects = listOf("Physics", "Chemistry", "Mathematics", "Biology", "Economics", "Accountancy", "History", "General")
                        commonSubjects.forEach { subj ->
                            val isSelected = selectedSubject.equals(subj, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) BrandEmerald else SurfaceGlass,
                                modifier = Modifier.clickable { selectedSubject = subj }
                            ) {
                                Text(
                                    text = subj,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.Black else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = enteredTopic,
                        onValueChange = { enteredTopic = it },
                        label = { Text("Chapter / Topic") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandEmerald,
                            unfocusedBorderColor = Color(0xFF26332B),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = enteredTitle,
                        onValueChange = { enteredTitle = it },
                        label = { Text("Material Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandEmerald,
                            unfocusedBorderColor = Color(0xFF26332B),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons (Confirm and Change)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceGlass,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF26332B)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isEditing = !isEditing }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEditing) "Close" else "Change",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                QuovexButton(
                    text = "Confirm & Synthesize",
                    onClick = {
                        onConfirm(selectedSubject, enteredTopic, enteredTitle)
                    },
                    modifier = Modifier.weight(1.8f)
                )
            }
        }
    }
}
