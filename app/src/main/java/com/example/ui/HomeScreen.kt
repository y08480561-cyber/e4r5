package com.example.ui

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val scrollState = rememberScrollState()
    var showSpeedSlider by remember { mutableStateOf(false) }

    val isWorking = viewModel.isSynthesizing || viewModel.isSpeaking || viewModel.isSaving

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // High-end frosted glass background gradient
    val glassBgBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        )
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.AutoStories,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "صدایار - گوینده داستانی",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                        }
                    },
                    actions = {
                        val context = LocalContext.current

                        // Share App Button
                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "سلام! برنامه‌ی صدایار (گوینده داستانی پیشرفته با هوش مصنوعی) را دانلود کنید و از تبدیل متن به صوت طبیعی لذت ببرید.")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "اشتراک‌گذاری صدایار")
                                context.startActivity(shareIntent)
                            }
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "اشتراک‌گذاری برنامه",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Coin Balance Corner Badge (موجودی سکه در گوشه)
                        Surface(
                            onClick = { viewModel.showStoreDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.MonetizationOn,
                                    contentDescription = "سکه",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "${viewModel.coinBalance} سکه",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(glassBgBrush)
            ) {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Low Coin Balance Warning Banner (هشدار کاهش موجودی سکه به ۲ یا کمتر)
                    AnimatedVisibility(visible = viewModel.coinBalance <= 2) {
                        Surface(
                            onClick = { viewModel.showStoreDialog = true },
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "موجودی سکه بسیار کم است (${viewModel.coinBalance} سکه)! برای افزایش اعتبار کلیک کنید.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                TextButton(onClick = { viewModel.showStoreDialog = true }) {
                                    Text("فروشگاه", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Daily Reward Banner
                    AnimatedVisibility(visible = viewModel.canClaimDaily) {
                        Surface(
                            onClick = { viewModel.claimDailyReward() },
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "۲ سکه پاداش خوش‌آمدگویی منتظر شماست!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Button(
                                    onClick = { viewModel.claimDailyReward() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                ) {
                                    Text("دریافت", color = MaterialTheme.colorScheme.onTertiary)
                                }
                            }
                        }
                    }

                    // Advanced Frosted Glass Text Input Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.4f),
                                        Color.White.copy(alpha = 0.05f)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            OutlinedTextField(
                                value = viewModel.text,
                                onValueChange = { viewModel.text = it },
                                placeholder = {
                                    Text(
                                        "داستان یا متن خود را اینجا بنویسید...\n(هر تولید صدا ۲ سکه از موجودی شما کسر می‌کند)",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f))
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 150.dp, max = 250.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )

                            // Bottom utility row inside frosted box
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${if (viewModel.text.isBlank()) 0 else viewModel.text.trim().split("\\s+".toRegex()).size} کلمه (هزینه: ۲ سکه)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )

                                if (viewModel.text.isNotBlank() && !isWorking) {
                                    IconButton(
                                        onClick = { viewModel.text = "" },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "پاک کردن",
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Frosted Voice Selector & Speed Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Voice Profile Dropdown Selector
                        var expandedVoiceMenu by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expandedVoiceMenu,
                            onExpandedChange = { if (!isWorking) expandedVoiceMenu = it },
                            modifier = Modifier.width(190.dp)
                        ) {
                            OutlinedTextField(
                                value = viewModel.selectedVoice.label,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("پروفایل صدا", style = MaterialTheme.typography.labelSmall) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVoiceMenu) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedVoiceMenu,
                                onDismissRequest = { expandedVoiceMenu = false }
                            ) {
                                VoiceOption.values().forEach { voice ->
                                    DropdownMenuItem(
                                        text = { Text(voice.label) },
                                        onClick = {
                                            viewModel.selectedVoice = voice
                                            expandedVoiceMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                if (voice == VoiceOption.FEMALE) Icons.Default.Female else Icons.Default.Male,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        // Speed Toggle Glass Chip
                        FilterChip(
                            selected = showSpeedSlider,
                            onClick = { showSpeedSlider = !showSpeedSlider },
                            label = { Text("${"%.2f".format(viewModel.speed)}x") },
                            leadingIcon = { Icon(Icons.Default.Speed, contentDescription = "سرعت", modifier = Modifier.size(16.dp)) },
                            shape = RoundedCornerShape(24.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }

                    // Animated Frosted Speed Slider
                    AnimatedVisibility(visible = showSpeedSlider) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("تنظیم سرعت روایت داستان", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    Text("${"%.2f".format(viewModel.speed)}x", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = viewModel.speed,
                                    onValueChange = { viewModel.onSpeedChanged(it) },
                                    valueRange = 0.70f..1.40f,
                                    steps = 6
                                )
                            }
                        }
                    }

                    // Frosted Visual Progress Indicator Card
                    AnimatedVisibility(
                        visible = isWorking,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.GraphicEq,
                                            contentDescription = "در حال پردازش",
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = alphaAnim),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = if (viewModel.isSaving) "در حال ذخیره‌سازی فایل صوتی..."
                                            else if (viewModel.isSynthesizing) "در حال آماده‌سازی صوت..."
                                            else "در حال روایت داستان...",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }

                                    if (viewModel.totalChunks > 0) {
                                        Text(
                                            text = "بخش ${viewModel.currentChunk} از ${viewModel.totalChunks}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                LinearProgressIndicator(
                                    progress = { if (viewModel.totalChunks > 0) viewModel.progress else 0f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                WaveformVisualizer(
                                    isAnimating = isWorking,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f, fill = false))

                    // Action Bar (High-end Frosted Buttons)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Main Play/Stop Button
                        Button(
                            onClick = {
                                if (viewModel.isSpeaking || viewModel.isSynthesizing) viewModel.stop() else viewModel.speak()
                            },
                            enabled = !viewModel.isSaving && viewModel.text.isNotBlank(),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewModel.isSpeaking || viewModel.isSynthesizing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            if (viewModel.isSynthesizing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("در حال آماده‌سازی...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            } else if (viewModel.isSpeaking) {
                                Icon(Icons.Default.Stop, contentDescription = "توقف")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("توقف روایت", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = "روایت")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("روایت با صدای انسان (۲ سکه)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }

                        // Save / Download Button
                        FilledTonalIconButton(
                            onClick = { viewModel.saveAudio() },
                            enabled = !isWorking && viewModel.text.isNotBlank(),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.size(56.dp)
                        ) {
                            if (viewModel.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = "ذخیره در دانلودها",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Minimal Subtle Status Bar
                    AnimatedVisibility(visible = viewModel.statusMessage.isNotBlank()) {
                        Text(
                            text = viewModel.statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    }
                }
            }

            // Store Dialog (فروشگاه سکه)
            if (viewModel.showStoreDialog) {
                StoreDialog(
                    coinBalance = viewModel.coinBalance,
                    coinPacks = viewModel.coinPacks,
                    transactions = viewModel.transactionHistory,
                    onDismiss = { viewModel.showStoreDialog = false },
                    onBuyPack = { pack -> viewModel.buyPack(pack) }
                )
            }
        }
    }
}
