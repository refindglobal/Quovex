package com.quovex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.theme.QuovexTheme

/**
 * High-performance, rich Math and Markdown renderer for Quovex.
 * Converts raw LaTeX to clean Unicode math, highlights formulas, and formats headings,
 * bold, italic, bullet lists, numbered steps, and equation blocks.
 */
@Composable
fun QuovexMathText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = QuovexTheme.typography.bodyMedium,
    color: Color = QuovexTheme.colors.textPrimary,
    lineHeight: TextUnit = 22.sp
) {
    val colors = QuovexTheme.colors
    val formattedContent = remember(text) {
        QuovexMathFormatter.formatMathAndLatex(text)
    }

    val blocks = remember(formattedContent) {
        parseMarkdownBlocks(formattedContent)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.xs)
    ) {
        blocks.forEach { block ->
            when (block) {
                is Block.Heading -> {
                    val headingStyle = when (block.level) {
                        1 -> QuovexTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        2 -> QuovexTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        else -> QuovexTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = buildStyledInline(block.content, colors.primary),
                        style = headingStyle,
                        color = colors.primary,
                        modifier = Modifier.padding(top = QuovexTheme.spacing.xs, bottom = 2.dp)
                    )
                }
                is Block.Divider -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = QuovexTheme.spacing.xs),
                        thickness = 1.dp,
                        color = colors.border
                    )
                }
                is Block.BulletItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = style.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary,
                            modifier = Modifier.padding(end = QuovexTheme.spacing.xs, start = QuovexTheme.spacing.xxs)
                        )
                        Text(
                            text = buildStyledInline(block.content, colors.textPrimary),
                            style = style,
                            color = color,
                            lineHeight = lineHeight,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is Block.NumberedItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}.",
                            style = style.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary,
                            modifier = Modifier.width(24.dp)
                        )
                        Text(
                            text = buildStyledInline(block.content, colors.textPrimary),
                            style = style,
                            color = color,
                            lineHeight = lineHeight,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is Block.EquationCard -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(QuovexTheme.shapes.small)
                            .background(colors.surfaceElevated)
                            .border(1.dp, colors.border, QuovexTheme.shapes.small)
                            .padding(horizontal = QuovexTheme.spacing.md, vertical = QuovexTheme.spacing.sm),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = block.formula,
                            style = QuovexTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = colors.primary
                        )
                    }
                }
                is Block.Paragraph -> {
                    Text(
                        text = buildStyledInline(block.content, colors.textPrimary),
                        style = style,
                        color = color,
                        lineHeight = lineHeight
                    )
                }
            }
        }
    }
}

private sealed interface Block {
    data class Heading(val level: Int, val content: String) : Block
    object Divider : Block
    data class BulletItem(val content: String) : Block
    data class NumberedItem(val number: String, val content: String) : Block
    data class EquationCard(val formula: String) : Block
    data class Paragraph(val content: String) : Block
}

private fun parseMarkdownBlocks(text: String): List<Block> {
    val lines = text.split("\n")
    val blocks = mutableListOf<Block>()

    for (rawLine in lines) {
        val line = rawLine.trim()
        if (line.isEmpty()) continue

        when {
            line.startsWith("### ") -> {
                blocks.add(Block.Heading(3, line.removePrefix("### ").trim()))
            }
            line.startsWith("## ") -> {
                blocks.add(Block.Heading(2, line.removePrefix("## ").trim()))
            }
            line.startsWith("# ") -> {
                blocks.add(Block.Heading(1, line.removePrefix("# ").trim()))
            }
            line == "---" || line == "***" || line == "___" -> {
                blocks.add(Block.Divider)
            }
            line.startsWith("- ") || line.startsWith("* ") -> {
                val content = if (line.startsWith("- ")) line.removePrefix("- ") else line.removePrefix("* ")
                blocks.add(Block.BulletItem(content.trim()))
            }
            line.matches(Regex("""^\d+\.\s+.*""")) -> {
                val match = Regex("""^(\d+)\.\s+(.*)""").find(line)
                if (match != null) {
                    val num = match.groupValues[1]
                    val content = match.groupValues[2]
                    blocks.add(Block.NumberedItem(num, content.trim()))
                } else {
                    blocks.add(Block.Paragraph(line))
                }
            }
            isStandaloneFormula(line) -> {
                blocks.add(Block.EquationCard(line))
            }
            else -> {
                blocks.add(Block.Paragraph(line))
            }
        }
    }
    return blocks
}

private fun isStandaloneFormula(line: String): Boolean {
    // Isolated equations like "ε = -(dΦ/dt)", "W_enter = ∫₀ʷ F dx = F·w", "I = |ε| / R = (N·B·l·v) / R"
    if (line.length in 5..80 && (line.contains("=") || line.contains("≈") || line.contains("→") || line.contains("≤") || line.contains("≥"))) {
        val wordCount = line.split(" ").size
        val hasMathTokens = line.contains("∫") || line.contains("∑") || line.contains("√") || line.contains("²") || line.contains("³") || line.contains("⁻") || line.contains("ε") || line.contains("Φ") || line.contains("θ") || line.contains("/")
        if (wordCount <= 8 && hasMathTokens && !line.startsWith("When") && !line.startsWith("This") && !line.startsWith("Where")) {
            return true
        }
    }
    return false
}

private fun buildStyledInline(content: String, baseColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val pattern = Regex("""(\*\*([^*]+)\*\*)|(\*([^*]+)\*)|(`([^`]+)`)""")
        val matches = pattern.findAll(content)

        for (match in matches) {
            val start = match.range.first
            val end = match.range.last + 1

            if (start > cursor) {
                append(content.substring(cursor, start))
            }

            when {
                // **bold**
                match.value.startsWith("**") && match.value.endsWith("**") -> {
                    val boldText = match.value.removeSurrounding("**")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = baseColor)) {
                        append(boldText)
                    }
                }
                // `code`
                match.value.startsWith("`") && match.value.endsWith("`") -> {
                    val codeText = match.value.removeSurrounding("`")
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)) {
                        append(codeText)
                    }
                }
                // *italic*
                match.value.startsWith("*") && match.value.endsWith("*") -> {
                    val italicText = match.value.removeSurrounding("*")
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(italicText)
                    }
                }
            }
            cursor = end
        }

        if (cursor < content.length) {
            append(content.substring(cursor))
        }
    }
}
