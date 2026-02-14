package com.wallshift.app.ui.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wallshift.app.ui.components.CategoryChipGrid
import com.wallshift.app.ui.components.FrequencySelector
import com.wallshift.app.ui.components.WallpaperTargetSelector
import com.wallshift.app.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
            )
            viewModel.clearMessage()
        }
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear Cache") },
            text = {
                Text(
                    "This will delete all cached wallpapers " +
                        "(${uiState.cacheSizeMb} MB). Are you sure?",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearCacheDialog = false
                        viewModel.clearCache()
                    },
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .animateContentSize(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessLow,
                    ),
                ),
        ) {
            // --- Categories ---
            SectionHeader("Categories")
            Spacer(modifier = Modifier.height(8.dp))
            CategoryChipGrid(
                categories = Constants.AVAILABLE_CATEGORIES,
                selectedCategories = uiState.selectedCategories,
                onCategoryToggle = viewModel::toggleCategory,
                customCategories = uiState.customCategories,
                onAddCustomCategory = viewModel::addCustomCategory,
                onRemoveCustomCategory = viewModel::removeCustomCategory,
            )

            SettingsDivider()

            // --- Frequency ---
            SectionHeader("Change Frequency")
            Spacer(modifier = Modifier.height(4.dp))
            FrequencySelector(
                selectedMinutes = uiState.frequencyMinutes,
                onFrequencySelected = viewModel::setFrequency,
            )

            SettingsDivider()

            // --- Wallpaper Target ---
            SectionHeader("Apply To")
            Spacer(modifier = Modifier.height(4.dp))
            WallpaperTargetSelector(
                selectedTarget = uiState.wallpaperTarget,
                onTargetSelected = viewModel::setWallpaperTarget,
            )

            SettingsDivider()

            // --- Auto Change Toggle ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto-Change",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "Automatically change wallpaper on schedule",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = uiState.autoChangeEnabled,
                    onCheckedChange = viewModel::setAutoChangeEnabled,
                )
            }

            SettingsDivider()

            // --- Actions ---
            SectionHeader("Actions")
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.applyNow() },
                enabled = !uiState.isApplying,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.isApplying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Apply Now")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showClearCacheDialog = true },
                enabled = !uiState.isClearing,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Clear Cache (${uiState.cacheSizeMb} MB)")
            }

            SettingsDivider()

            // --- About ---
            SectionHeader("About")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "WallShift v1.0.0",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Images provided by Unsplash, Pexels, Pixabay, and Wallhaven. " +
                    "All photos are licensed for free use.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}
