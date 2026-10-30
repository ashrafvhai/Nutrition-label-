package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.components.GlassCard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ChatViewModel

@Composable
fun ChatbotScreen(
    viewModel: ChatViewModel
) {
    val messages by viewModel.messages.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val thinkingEnabled by viewModel.thinkingModeEnabled.collectAsState()

    var inputPrompt by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickQuestions = listOf(
        "How can I hit 30g more protein today?",
        "Is chicken biryani healthy post-workout?",
        "Suggest a low-carb dinner recipe",
        "What are high-fiber Bengali foods?"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070B11),
                        Color(0xFF0C141F),
                        Color(0xFF080D14)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp)
        ) {
            // Header: Title & Model Selector
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NutriAI Coach",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "Dietary intelligence powered by Gemini",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Model Selection Glass Bar
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = Color(0x18FFFFFF)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "gemini-4-argon" to "Argon (Thinking)",
                            "gemini-3.8-flash" to "Flash (Balanced)",
                            "gemini-3.5-flash-lite" to "Lite (Fast)"
                        ).forEach { (modelKey, label) ->
                            val isSelected = selectedModel == modelKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) EmeraldPrimary else Color.Transparent)
                                    .clickable { viewModel.setModel(modelKey) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color(0xFF042F2E) else TextSecondary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Thinking Mode Toggle if Argon is selected
                if (selectedModel == "gemini-4-argon") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x1206B6D4))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Deep Nutritional Thinking", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                        }

                        Switch(
                            checked = thinkingEnabled,
                            onCheckedChange = { viewModel.toggleThinkingMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF042F2E),
                                checkedTrackColor = CyanAccent
                            ),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chat Messages Thread
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🥗", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Ask your AI Nutritionist",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Get advice on food swaps, macro splits, Bangladeshi dishes, and recovery meals.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                items(messages) { message ->
                    ChatMessageBubble(message = message)
                }

                if (isSending) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = EmeraldPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (thinkingEnabled && selectedModel == "gemini-4-argon") "Thinking through nutrition science..." else "NutriAI is responding...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CyanAccent
                            )
                        }
                    }
                }
            }

            // Quick suggestion chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickQuestions) { question ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x18FFFFFF))
                            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                inputPrompt = question
                                viewModel.sendMessage(question)
                                inputPrompt = ""
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Input Row at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputPrompt,
                    onValueChange = { inputPrompt = it },
                    placeholder = { Text("Ask about any food, macro or recipe...", color = TextMuted) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = GlassBorder,
                        focusedContainerColor = Color(0x1AFFFFFF),
                        unfocusedContainerColor = Color(0x14FFFFFF)
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(10.dp))

                IconButton(
                    onClick = {
                        val text = inputPrompt.trim()
                        if (text.isNotBlank()) {
                            viewModel.sendMessage(text)
                            inputPrompt = ""
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary)
                        .testTag("chat_send_button"),
                    enabled = !isSending && inputPrompt.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color(0xFF042F2E)
                    )
                }
            }

            // Spacer for floating bottom navigation
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    var isThoughtExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Thinking Process Box if available
        if (!message.thought.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x1A06B6D4))
                    .border(1.dp, CyanAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .clickable { isThoughtExpanded = !isThoughtExpanded }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Thinking Process (High)",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (isThoughtExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    AnimatedVisibility(visible = isThoughtExpanded) {
                        Text(
                            text = message.thought ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
                .background(
                    if (isUser) Brush.linearGradient(listOf(EmeraldPrimary, CyanAccent))
                    else Brush.linearGradient(listOf(Color(0x22FFFFFF), Color(0x14FFFFFF)))
                )
                .border(1.dp, if (isUser) Color.Transparent else GlassBorder, RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Text(
                text = message.content,
                color = if (isUser) Color(0xFF03221C) else TextPrimary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}
