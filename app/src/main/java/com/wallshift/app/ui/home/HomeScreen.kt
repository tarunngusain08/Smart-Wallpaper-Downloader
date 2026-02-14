package com.wallshift.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.wallshift.app.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSettings: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAdjustSheet by remember { mutableStateOf(false) }

    // Handle error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
            )
            viewModel.clearError()
        }
    }

    // Handle save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar(
                message = "Wallpaper saved to Pictures/WallShift",
                duration = SnackbarDuration.Short,
            )
            viewModel.clearSaveSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "WallShift",
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.nextWallpaper() },
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                // Wallpaper preview with crossfade
                val wallpaper = uiState.currentWallpaper

                AnimatedContent(
                    targetState = wallpaper?.id,
                    transitionSpec = {
                        fadeIn(
                            animationSpec = tween(600),
                        ) togetherWith fadeOut(
                            animationSpec = tween(400),
                        )
                    },
                    label = "wallpaper_crossfade",
                    modifier = Modifier.fillMaxSize(),
                ) { wallpaperId ->
                    if (wallpaperId != null && wallpaper != null) {
                        val imageModel = wallpaper.localPath
                            ?: wallpaper.thumbnailUrl
                        AsyncImage(
                            model = imageModel,
                            contentDescription = "Current wallpaper",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        // Placeholder when no wallpaper is set
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme
                                        .onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Pull down or tap Next to get " +
                                        "your first wallpaper",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme
                                        .onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                // Shimmer overlay while refreshing
                if (uiState.isRefreshing) {
                    ShimmerOverlay(
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                // Bottom gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.8f),
                                ),
                            ),
                        ),
                )

                // Bottom content with slide-in animation
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow,
                        ),
                    ) + fadeIn(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(padding)
                            .padding(
                                horizontal = 24.dp,
                                vertical = 16.dp,
                            ),
                    ) {
                        // Photographer attribution
                        if (wallpaper != null) {
                            Text(
                                text = "Photo by ${wallpaper.photographer}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f),
                            )
                            Text(
                                text = "via ${wallpaper.source}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f),
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Status chip
                        val statusText = if (uiState.isAutoChangeEnabled) {
                            val freq = uiState.frequencyMinutes
                            val label = Constants.FREQUENCY_OPTIONS
                                .firstOrNull {
                                    it.first == freq &&
                                        it.first !=
                                        Constants.CUSTOM_FREQUENCY_SENTINEL
                                }
                                ?.second ?: if (freq >= 60) {
                                "${freq / 60} hour${
                                    if (freq >= 120) "s" else ""
                                }"
                            } else {
                                "$freq min"
                            }
                            "Auto-changing every $label"
                        } else {
                            "Auto-change paused"
                        }

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Color.White.copy(alpha = 0.15f),
                                )
                                .padding(
                                    horizontal = 12.dp,
                                    vertical = 6.dp,
                                ),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement
                                .spacedBy(12.dp),
                        ) {
                            FilledTonalButton(
                                onClick = { viewModel.nextWallpaper() },
                                enabled = !uiState.isLoading &&
                                    !uiState.isRefreshing,
                                modifier = Modifier.weight(1f),
                            ) {
                                if (uiState.isRefreshing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.SkipNext,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                                Spacer(
                                    modifier = Modifier.width(8.dp),
                                )
                                Text("Next")
                            }

                            FilledTonalButton(
                                onClick = {
                                    viewModel.saveCurrentWallpaper()
                                },
                                enabled = wallpaper?.localPath != null,
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(
                                    modifier = Modifier.width(8.dp),
                                )
                                Text("Save")
                            }

                            FilledTonalButton(
                                onClick = { showAdjustSheet = true },
                                enabled = wallpaper?.localPath != null,
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(
                                    Icons.Default.CropFree,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(
                                    modifier = Modifier.width(8.dp),
                                )
                                Text("Adjust")
                            }
                        }
                    }
                }

                // Loading overlay
                AnimatedVisibility(
                    visible = uiState.isLoading,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300)),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.Black.copy(alpha = 0.4f),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        }
    }

    // Adjust wallpaper bottom sheet
    if (showAdjustSheet) {
        val localPath = uiState.currentWallpaper?.localPath
        if (localPath != null) {
            AdjustWallpaperSheet(
                imagePath = localPath,
                onApply = { offsetX, offsetY, scale ->
                    showAdjustSheet = false
                    viewModel.reapplyWithCrop(offsetX, offsetY, scale)
                },
                onDismiss = { showAdjustSheet = false },
            )
        }
    }
}

/**
 * A shimmer overlay that shows a sweeping light gradient while loading.
 */
@Composable
private fun ShimmerOverlay(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_translate",
    )

    Box(
        modifier = modifier.background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.12f),
                    Color.Transparent,
                ),
                start = Offset(translateAnim * 1000f, 0f),
                end = Offset(
                    (translateAnim + 0.5f) * 1000f,
                    1000f,
                ),
            ),
        ),
    )
}
