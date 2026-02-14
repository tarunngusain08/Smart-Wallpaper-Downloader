package com.wallshift.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onOnboardingComplete: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WallShift") },
                navigationIcon = {
                    if (uiState.step > 1) {
                        IconButton(onClick = { viewModel.previousStep() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = { uiState.step / 2f },
                modifier = Modifier.fillMaxWidth(),
            )

            AnimatedContent(
                targetState = uiState.step,
                transitionSpec = {
                    val springSpec = spring<Int>(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow,
                    )
                    if (targetState > initialState) {
                        (slideInHorizontally(
                            animationSpec = spring(
                                stiffness = Spring.StiffnessLow,
                            ),
                        ) { it / 3 } + fadeIn()) togetherWith
                            (slideOutHorizontally(
                                animationSpec = spring(
                                    stiffness = Spring.StiffnessMedium,
                                ),
                            ) { -it / 3 } + fadeOut())
                    } else {
                        (slideInHorizontally(
                            animationSpec = spring(
                                stiffness = Spring.StiffnessLow,
                            ),
                        ) { -it / 3 } + fadeIn()) togetherWith
                            (slideOutHorizontally(
                                animationSpec = spring(
                                    stiffness = Spring.StiffnessMedium,
                                ),
                            ) { it / 3 } + fadeOut())
                    }
                },
                label = "onboarding_step",
            ) { step ->
                when (step) {
                    1 -> CategorySelectionStep(
                        selectedCategories = uiState.selectedCategories,
                        customCategories = uiState.customCategories,
                        onCategoryToggle = viewModel::toggleCategory,
                        onAddCustomCategory = viewModel::addCustomCategory,
                        onRemoveCustomCategory = viewModel::removeCustomCategory,
                        onNext = { viewModel.nextStep() },
                        canProceed = uiState.selectedCategories.isNotEmpty(),
                        error = uiState.error,
                    )
                    2 -> FrequencyTargetStep(
                        frequencyMinutes = uiState.frequencyMinutes,
                        onFrequencySelected = viewModel::setFrequency,
                        wallpaperTarget = uiState.wallpaperTarget,
                        onTargetSelected = viewModel::setWallpaperTarget,
                        isLoading = uiState.isLoading,
                        error = uiState.error,
                        onStart = { viewModel.completeOnboarding(onOnboardingComplete) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySelectionStep(
    selectedCategories: Set<String>,
    customCategories: Set<String>,
    onCategoryToggle: (String) -> Unit,
    onAddCustomCategory: (String) -> Unit,
    onRemoveCustomCategory: (String) -> Unit,
    onNext: () -> Unit,
    canProceed: Boolean,
    error: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "Choose Your Style",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select the categories you love, or add your own custom search terms.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        CategoryChipGrid(
            categories = Constants.AVAILABLE_CATEGORIES,
            selectedCategories = selectedCategories,
            onCategoryToggle = onCategoryToggle,
            customCategories = customCategories,
            onAddCustomCategory = onAddCustomCategory,
            onRemoveCustomCategory = onRemoveCustomCategory,
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            enabled = canProceed,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun FrequencyTargetStep(
    frequencyMinutes: Int,
    onFrequencySelected: (Int) -> Unit,
    wallpaperTarget: com.wallshift.app.domain.model.WallpaperTarget,
    onTargetSelected: (com.wallshift.app.domain.model.WallpaperTarget) -> Unit,
    isLoading: Boolean,
    error: String?,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "Set Your Schedule",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "How often should we refresh your wallpaper?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        FrequencySelector(
            selectedMinutes = frequencyMinutes,
            onFrequencySelected = onFrequencySelected,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Apply wallpaper to:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        WallpaperTargetSelector(
            selectedTarget = wallpaperTarget,
            onTargetSelected = onTargetSelected,
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onStart,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text("Start WallShift")
            }
        }
    }
}
