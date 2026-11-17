package com.pawbyte.pawassistant.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pawbyte.pawassistant.ChatMessage
import com.pawbyte.pawassistant.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel = viewModel(),
    onSignOut: () -> Unit = {}
) {
    val uiState by chatViewModel.uiState.collectAsState()
    var inputText by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val isKeyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    fun handleSend() {
        val trimmed = inputText.trim()
        if (trimmed.isEmpty() || uiState.isLoading) return
        chatViewModel.sendMessage(trimmed)
        inputText = ""
    }

    LaunchedEffect(uiState.messages.size, uiState.isLoading, isKeyboardOpen) {
        if (uiState.messages.isNotEmpty()) {
            val targetIndex = if (uiState.isLoading) {
                uiState.messages.size
            } else {
                uiState.messages.lastIndex
            }
            listState.animateScrollToItem(targetIndex)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistantLogo(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Paw Assistant",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            chatViewModel.startNewChat()
                            inputText = ""
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Yeni sohbet",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    TextButton(
                        onClick = onSignOut,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = "Çıkış",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.messages.isEmpty()) {
                    item(key = "welcome_greeting") {
                        WelcomeGreetingBubble()
                    }
                } else {
                    items(uiState.messages, key = { it.id }) { message ->
                        MessageBubble(message = message)
                    }

                    if (uiState.isLoading) {
                        item(key = "loading_indicator") {
                            LoadingBubble()
                        }
                    }
                }
            }

            uiState.errorMessage?.let { error ->
                ErrorBanner(
                    message = error,
                    onDismiss = { chatViewModel.dismissError() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (!isKeyboardOpen) {
                SuggestionChipRow(
                    onSuggestionClick = { suggestion ->
                        inputText = suggestion
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            HorizontalDivider(
                thickness = 0.7.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )

            MessageInputBar(
                value = inputText,
                enabled = !uiState.isLoading,
                onValueChange = { inputText = it },
                onSendClick = { handleSend() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WelcomeGreetingBubble() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            ),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Text(
                text = "Merhaba, ben Paw Assistant. Nasıl yardımcı olabilirim?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
            )
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.isFromUser

    if (isUser) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(22.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    UserAvatarIcon(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 4.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                ),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Text(
                    text = message.text,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp)
                )
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 4.dp,
                    topEnd = 20.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                ),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.widthIn(max = 340.dp)
            ) {
                FormattedAssistantText(
                    text = message.text,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

private sealed interface MarkdownBlock {
    data class TextBlock(val text: String) : MarkdownBlock
    data class TableBlock(
        val headers: List<String>,
        val rows: List<List<String>>
    ) : MarkdownBlock
}

@Composable
private fun FormattedAssistantText(
    text: String,
    modifier: Modifier = Modifier
) {
    val blocks = remember(text) { parseMarkdownBlocks(text) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.TextBlock -> {
                    Text(
                        text = parseInlineMarkdown(block.text),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is MarkdownBlock.TableBlock -> {
                    MarkdownTable(
                        headers = block.headers,
                        rows = block.rows
                    )
                }
            }
        }
    }
}

@Composable
private fun MarkdownTable(
    headers: List<String>,
    rows: List<List<String>>
) {
    val columnCount = headers.size
    if (columnCount == 0) return

    val columnWidths: List<Dp> = remember(headers, rows) {
        List(columnCount) { col ->
            val maxChars = maxOf(
                headers[col].length,
                rows.maxOfOrNull { row -> row.getOrElse(col) { "" }.length } ?: 0
            )
            (maxChars * 8 + 24).coerceIn(92, 188).dp
        }
    }

    val borderColor = MaterialTheme.colorScheme.outline

    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Max)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                headers.forEachIndexed { index, header ->
                    Box(
                        modifier = Modifier
                            .width(columnWidths[index])
                            .fillMaxHeight()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = parseInlineMarkdown(header),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (index < columnCount - 1) {
                        VerticalDivider(
                            modifier = Modifier.fillMaxHeight(),
                            thickness = 1.dp,
                            color = borderColor
                        )
                    }
                }
            }

            rows.forEach { row ->
                HorizontalDivider(thickness = 1.dp, color = borderColor)
                Row(
                    modifier = Modifier.height(IntrinsicSize.Max)
                ) {
                    for (col in 0 until columnCount) {
                        val cellText = row.getOrElse(col) { "" }
                        Box(
                            modifier = Modifier
                                .width(columnWidths[col])
                                .fillMaxHeight()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = parseInlineMarkdown(cellText),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (col < columnCount - 1) {
                            VerticalDivider(
                                modifier = Modifier.fillMaxHeight(),
                                thickness = 1.dp,
                                color = borderColor
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun parseMarkdownBlocks(text: String): List<MarkdownBlock> {
    val lines = text.lines()
    val blocks = mutableListOf<MarkdownBlock>()
    val paragraphBuffer = mutableListOf<String>()

    fun flushParagraph() {
        val joined = paragraphBuffer.joinToString("\n").trim()
        if (joined.isNotEmpty()) {
            blocks.add(MarkdownBlock.TextBlock(joined))
        }
        paragraphBuffer.clear()
    }

    var idx = 0
    while (idx < lines.size) {
        if (idx + 1 < lines.size && lines[idx].contains('|') && isTableSeparatorLine(lines[idx + 1])) {
            flushParagraph()
            val headers = parseTableRow(lines[idx])
            idx += 2
            val rows = mutableListOf<List<String>>()
            while (idx < lines.size && lines[idx].contains('|') && lines[idx].trim().isNotEmpty()) {
                rows.add(parseTableRow(lines[idx]))
                idx++
            }
            if (headers.isNotEmpty()) {
                blocks.add(MarkdownBlock.TableBlock(headers = headers, rows = rows))
            }
        } else {
            paragraphBuffer.add(lines[idx])
            idx++
        }
    }
    flushParagraph()
    return blocks
}

private fun isTableSeparatorLine(line: String): Boolean {
    val trimmed = line.trim()
    if (!trimmed.contains('|') || !trimmed.contains('-')) return false
    val cells = parseTableRow(trimmed)
    return cells.isNotEmpty() && cells.all { cell ->
        cell.isNotEmpty() && cell.all { ch -> ch == '-' || ch == ':' || ch == ' ' }
    }
}

private fun parseTableRow(line: String): List<String> {
    var trimmed = line.trim()
    if (trimmed.startsWith('|')) trimmed = trimmed.drop(1)
    if (trimmed.endsWith('|')) trimmed = trimmed.dropLast(1)
    return trimmed.split('|').map { it.trim() }
}

private fun parseInlineMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    val n = text.length
    while (i < n) {
        when {
            text.startsWith("***", i) -> {
                val end = text.indexOf("***", i + 3)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                        append(text.substring(i + 3, end))
                    }
                    i = end + 3
                } else {
                    append("***")
                    i += 3
                }
            }

            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append("**")
                    i += 2
                }
            }

            text[i] == '*' -> {
                val end = text.indexOf('*', i + 1)
                if (end != -1 && end > i + 1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append('*')
                    i += 1
                }
            }

            text[i] == '_' -> {
                val end = text.indexOf('_', i + 1)
                val prevIsSpaceOrStart = i == 0 || text[i - 1].isWhitespace()
                if (prevIsSpaceOrStart && end != -1 && end > i + 1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append('_')
                    i += 1
                }
            }

            else -> {
                append(text[i])
                i += 1
            }
        }
    }
}

@Composable
private fun LoadingBubble() {
    val dotColors = listOf(
        Color(0xFF4285F4), // Mavi
        Color(0xFFEA4335), // Kırmızı
        Color(0xFFFBBC04), // Sarı
        Color(0xFF34A853)  // Yeşil
    )

    val transition = rememberInfiniteTransition(label = "assistant_typing")
    val dotOffsets = dotColors.indices.map { index ->
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1200
                    0f at 0 using FastOutSlowInEasing
                    -7f at 220 using FastOutSlowInEasing
                    0f at 440 using FastOutSlowInEasing
                    0f at 1200
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(index * 160)
            ),
            label = "typing_dot_$index"
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            ),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                dotColors.forEachIndexed { index, color ->
                    val offsetY by dotOffsets[index]
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .offset(y = offsetY.dp)
                            .background(color = color, shape = CircleShape)
                    )
                }
            }
        }
    }
}

private enum class SuggestionIconType {
    NONE,
    LEARN,
    IDEA
}

private data class SuggestionItem(
    val text: String,
    val iconType: SuggestionIconType = SuggestionIconType.NONE
)

@Composable
private fun SuggestionChipRow(
    onSuggestionClick: (String) -> Unit
) {
    val suggestions = listOf(
        SuggestionItem("Bir şey öğren", SuggestionIconType.LEARN),
        SuggestionItem("Fikir üret", SuggestionIconType.IDEA),
        SuggestionItem("Metin yaz"),
        SuggestionItem("Özet çıkar")
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(suggestions) { item ->
            Surface(
                onClick = { onSuggestionClick(item.text) },
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (item.iconType) {
                        SuggestionIconType.LEARN -> {
                            LearnBookIcon(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        SuggestionIconType.IDEA -> {
                            IdeaSparkleIcon(
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        SuggestionIconType.NONE -> Unit
                    }

                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun LearnBookIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = size.minDimension * 0.11f

        val bookPath = Path().apply {
            moveTo(w * 0.50f, h * 0.28f)
            cubicTo(w * 0.36f, h * 0.20f, w * 0.18f, h * 0.20f, w * 0.10f, h * 0.26f)
            lineTo(w * 0.10f, h * 0.78f)
            cubicTo(w * 0.18f, h * 0.72f, w * 0.36f, h * 0.72f, w * 0.50f, h * 0.80f)
            cubicTo(w * 0.64f, h * 0.72f, w * 0.82f, h * 0.72f, w * 0.90f, h * 0.78f)
            lineTo(w * 0.90f, h * 0.26f)
            cubicTo(w * 0.82f, h * 0.20f, w * 0.64f, h * 0.20f, w * 0.50f, h * 0.28f)
            close()
        }
        drawPath(
            path = bookPath,
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawLine(
            color = color,
            start = Offset(w * 0.50f, h * 0.28f),
            end = Offset(w * 0.50f, h * 0.80f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun IdeaSparkleIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.50f
        val cy = h * 0.50f
        val outerR = w * 0.40f
        val innerR = w * 0.13f

        val sparkle = Path().apply {
            moveTo(cx, cy - outerR)
            lineTo(cx + innerR, cy - innerR)
            lineTo(cx + outerR, cy)
            lineTo(cx + innerR, cy + innerR)
            lineTo(cx, cy + outerR)
            lineTo(cx - innerR, cy + innerR)
            lineTo(cx - outerR, cy)
            lineTo(cx - innerR, cy - innerR)
            close()
        }
        drawPath(path = sparkle, color = color)
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Kapat",
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun MessageInputBar(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canSend = enabled && value.isNotBlank()

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = if (enabled) "Mesaj yazın" else "Yanıt bekleniyor...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = { if (canSend) onSendClick() }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )

            IconButton(
                onClick = onSendClick,
                enabled = canSend,
                modifier = Modifier
                    .size(44.dp)
                    .semantics { contentDescription = "Gönder" }
            ) {
                SendArrowIcon(
                    color = if (canSend) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    },
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun AssistantLogo(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.50f
        val cy = h * 0.50f

        val topPetal = Path().apply {
            moveTo(cx, h * 0.08f)
            cubicTo(w * 0.65f, h * 0.08f, w * 0.68f, h * 0.24f, w * 0.61f, h * 0.38f)
            lineTo(cx, h * 0.46f)
            lineTo(w * 0.39f, h * 0.38f)
            cubicTo(w * 0.32f, h * 0.24f, w * 0.35f, h * 0.08f, cx, h * 0.08f)
            close()
        }
        drawPath(topPetal, color = Color(0xFF4285F4))

        val rightPetal = Path().apply {
            moveTo(w * 0.92f, cy)
            cubicTo(w * 0.92f, h * 0.65f, w * 0.76f, h * 0.68f, w * 0.62f, h * 0.61f)
            lineTo(w * 0.54f, cy)
            lineTo(w * 0.62f, h * 0.39f)
            cubicTo(w * 0.76f, h * 0.32f, w * 0.92f, h * 0.35f, w * 0.92f, cy)
            close()
        }
        drawPath(rightPetal, color = Color(0xFF34A853))

        val bottomPetal = Path().apply {
            moveTo(cx, h * 0.92f)
            cubicTo(w * 0.35f, h * 0.92f, w * 0.32f, h * 0.76f, w * 0.39f, h * 0.62f)
            lineTo(cx, h * 0.54f)
            lineTo(w * 0.61f, h * 0.62f)
            cubicTo(w * 0.68f, h * 0.76f, w * 0.65f, h * 0.92f, cx, h * 0.92f)
            close()
        }
        drawPath(bottomPetal, color = Color(0xFFFBBC04))

        val leftPetal = Path().apply {
            moveTo(w * 0.08f, cy)
            cubicTo(w * 0.08f, h * 0.35f, w * 0.24f, h * 0.32f, w * 0.38f, h * 0.39f)
            lineTo(w * 0.46f, cy)
            lineTo(w * 0.38f, h * 0.61f)
            cubicTo(w * 0.24f, h * 0.68f, w * 0.08f, h * 0.65f, w * 0.08f, cy)
            close()
        }
        drawPath(leftPetal, color = Color(0xFFEA4335))

        drawCircle(
            color = Color.White,
            radius = w * 0.09f,
            center = Offset(cx, cy)
        )
    }
}

@Composable
private fun UserAvatarIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(
            color = color,
            radius = w * 0.22f,
            center = Offset(w * 0.50f, h * 0.34f)
        )

        val shoulders = Path().apply {
            moveTo(w * 0.16f, h * 0.88f)
            cubicTo(w * 0.16f, h * 0.62f, w * 0.84f, h * 0.62f, w * 0.84f, h * 0.88f)
            close()
        }
        drawPath(path = shoulders, color = color)
    }
}

@Composable
private fun SendArrowIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.16f, size.height * 0.20f)
            lineTo(size.width * 0.88f, size.height * 0.50f)
            lineTo(size.width * 0.16f, size.height * 0.80f)
            lineTo(size.width * 0.32f, size.height * 0.50f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = size.minDimension * 0.10f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
