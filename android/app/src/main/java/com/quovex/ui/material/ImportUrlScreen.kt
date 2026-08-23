package com.quovex.ui.material

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.NoteInputType
import com.quovex.theme.BrandEmerald
import com.quovex.theme.SurfaceDark
import com.quovex.theme.SurfaceGlass
import com.quovex.theme.TextPrimary
import com.quovex.theme.TextSecondary
import com.quovex.ui.components.QuovexButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportUrlScreen(
    onNavigateBack: () -> Unit,
    onImportUrl: (url: String, inputType: NoteInputType) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var inputUrl by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Import Study Content",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceGlass,
                contentColor = BrandEmerald,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = BrandEmerald
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; errorMessage = null },
                    text = { Text("Web Article", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; errorMessage = null },
                    text = { Text("YouTube Video", fontWeight = FontWeight.SemiBold) }
                )
            }

            Text(
                text = if (selectedTab == 0) {
                    "Enter any academic article, Wikipedia link, or blog URL. Quovex AI will extract the text and generate a structured study pack."
                } else {
                    "Enter a YouTube lecture or educational video URL. Quovex AI will extract the transcript, core formulas, and key concepts."
                },
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            OutlinedTextField(
                value = inputUrl,
                onValueChange = {
                    inputUrl = it
                    errorMessage = null
                },
                label = {
                    Text(if (selectedTab == 0) "https://en.wikipedia.org/..." else "https://youtube.com/watch?v=...")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = errorMessage != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandEmerald,
                    unfocusedBorderColor = SurfaceGlass,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            errorMessage?.let { err ->
                Text(
                    text = err,
                    color = androidx.compose.ui.graphics.Color(0xFFEF5350),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            QuovexButton(
                text = "Extract & Transform",
                onClick = {
                    val trimmed = inputUrl.trim()
                    if (trimmed.isBlank() || !trimmed.startsWith("http")) {
                        errorMessage = "Please enter a valid URL starting with http:// or https://"
                        return@QuovexButton
                    }

                    val inputType = if (selectedTab == 0) NoteInputType.URL else NoteInputType.YOUTUBE
                    onImportUrl(trimmed, inputType)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = inputUrl.isNotBlank()
            )
        }
    }
}
