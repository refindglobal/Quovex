package com.quovex.ui.material

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.SubjectInference
import com.quovex.theme.BrandEmerald
import com.quovex.theme.BrandEmeraldDim
import com.quovex.theme.SurfaceDark
import com.quovex.theme.SurfaceGlass
import com.quovex.theme.TextPrimary
import com.quovex.theme.TextSecondary
import com.quovex.ui.components.QuovexButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SubjectInferenceScreen(
    inference: SubjectInference,
    initialTitle: String,
    onConfirm: (confirmedSubject: String, confirmedTopic: String, confirmedTitle: String) -> Unit
) {
    var subject by remember { mutableStateOf(inference.subject) }
    var topic by remember { mutableStateOf(inference.topic) }
    var title by remember { mutableStateOf(initialTitle.ifBlank { inference.topic }) }

    val confidencePercent = (inference.confidence * 100).toInt().coerceIn(50, 99)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Confirm Classification",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Confidence Banner
            Surface(
                color = BrandEmeraldDim.copy(alpha = 0.15f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(BrandEmerald, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Quovex AI Inferred: $confidencePercent% Match",
                            fontWeight = FontWeight.Bold,
                            color = BrandEmerald,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Confirm or customize the subject categorization below.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Material Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandEmerald,
                    unfocusedBorderColor = SurfaceGlass,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            // Subject Field
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject (e.g. Physics, Chemistry, Maths)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandEmerald,
                    unfocusedBorderColor = SurfaceGlass,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            // Topic Field
            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Topic / Chapter (e.g. Laws of Motion)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandEmerald,
                    unfocusedBorderColor = SurfaceGlass,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            // Exam Relevance Tags
            if (inference.examRelevance.isNotEmpty()) {
                Text(
                    text = "Target Exam Alignment",
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    fontSize = 14.sp
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    inference.examRelevance.forEach { exam ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SurfaceGlass,
                            modifier = Modifier.border(1.dp, BrandEmeraldDim, RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = exam,
                                color = BrandEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            QuovexButton(
                text = "Confirm & Generate Study Pack",
                onClick = {
                    onConfirm(subject.trim(), topic.trim(), title.trim())
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = subject.isNotBlank() && topic.isNotBlank()
            )
        }
    }
}
