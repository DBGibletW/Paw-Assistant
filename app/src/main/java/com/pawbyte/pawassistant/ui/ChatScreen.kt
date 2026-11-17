package com.pawbyte.pawassistant.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
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
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                Text(
                    text = message.text,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun LoadingBubble() {
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Yanıt hazırlanıyor...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SuggestionChipRow(
    onSuggestionClick: (String) -> Unit
) {
    val suggestions = listOf(
        "Bir şey öğren",
        "Fikir üret",
        "Metin yaz",
        "Özet çıkar"
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(suggestions) { suggestion ->
            Surface(
                onClick = { onSuggestionClick(suggestion) },
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(
                    text = suggestion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                )
            }
        }
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
