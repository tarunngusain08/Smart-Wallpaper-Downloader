package com.wallshift.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.BubbleChart
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Castle
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.EmojiNature
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FilterHdr
import androidx.compose.material.icons.outlined.FilterVintage
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.Gradient
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Sailing
import androidx.compose.material.icons.outlined.Pool
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.Texture
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material.icons.outlined.Tsunami
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.ViewCompact
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

private val categoryIcons: Map<String, ImageVector> = mapOf(
    // Nature & Landscapes
    "nature" to Icons.Outlined.Landscape,
    "mountains" to Icons.Outlined.FilterHdr,
    "ocean" to Icons.Outlined.Tsunami,
    "sunset" to Icons.Outlined.WbTwilight,
    "sunrise" to Icons.Outlined.WbSunny,
    "forest" to Icons.Outlined.Forest,
    "beach" to Icons.Outlined.Sailing,
    "waterfall" to Icons.Outlined.WaterDrop,
    "desert" to Icons.Outlined.Terrain,
    "lake" to Icons.Outlined.WaterDrop,
    "sky" to Icons.Outlined.Air,
    "flowers" to Icons.Outlined.LocalFlorist,
    "autumn" to Icons.Outlined.EmojiNature,
    "winter" to Icons.Outlined.AcUnit,
    "tropical" to Icons.Outlined.Park,
    // Urban & Architecture
    "cityscapes" to Icons.Outlined.LocationCity,
    "architecture" to Icons.Outlined.Apartment,
    "street" to Icons.Outlined.PhotoCamera,
    "skyline" to Icons.Outlined.LocationCity,
    "neon" to Icons.Outlined.Bolt,
    "urban" to Icons.Outlined.LocationCity,
    // Space & Science
    "space" to Icons.Outlined.RocketLaunch,
    "galaxy" to Icons.Outlined.AutoAwesome,
    "planets" to Icons.Outlined.RocketLaunch,
    "nebula" to Icons.Outlined.BubbleChart,
    "aurora" to Icons.Outlined.Nightlight,
    // Art & Design
    "abstract" to Icons.Outlined.Texture,
    "minimal" to Icons.Outlined.ViewCompact,
    "geometric" to Icons.Outlined.GridView,
    "gradient" to Icons.Outlined.Gradient,
    "watercolor" to Icons.Outlined.Brush,
    "digital art" to Icons.Outlined.Palette,
    "3d render" to Icons.Outlined.ViewInAr,
    "typography" to Icons.Outlined.TextFields,
    "pattern" to Icons.Outlined.GridView,
    // Dark & Moody
    "dark" to Icons.Outlined.DarkMode,
    "black" to Icons.Outlined.DarkMode,
    "gothic" to Icons.Outlined.Castle,
    "moody" to Icons.Outlined.Nightlight,
    // Lifestyle & Culture
    "vintage" to Icons.Outlined.FilterVintage,
    "retro" to Icons.Outlined.FilterVintage,
    "aesthetic" to Icons.Outlined.Colorize,
    "pastel" to Icons.Outlined.Palette,
    "boho" to Icons.Outlined.EmojiNature,
    "japanese" to Icons.Outlined.Explore,
    "cyberpunk" to Icons.Outlined.Bolt,
    "steampunk" to Icons.Outlined.Diamond,
    // Vehicles & Machines
    "cars" to Icons.Outlined.DirectionsCar,
    "motorcycles" to Icons.AutoMirrored.Outlined.DirectionsBike,
    "aviation" to Icons.Outlined.Flight,
    // Animals & Wildlife
    "animals" to Icons.Outlined.Pets,
    "cats" to Icons.Outlined.Pets,
    "dogs" to Icons.Outlined.Pets,
    "wildlife" to Icons.Outlined.Pets,
    "underwater" to Icons.Outlined.Pool,
    // Pop Culture
    "anime" to Icons.Outlined.SentimentSatisfied,
    "gaming" to Icons.Outlined.SportsEsports,
    "superhero" to Icons.Outlined.Bolt,
    "fantasy" to Icons.Outlined.AutoAwesome,
    "sci-fi" to Icons.Outlined.RocketLaunch,
    // Food & Drinks
    "food" to Icons.Outlined.Cake,
    "coffee" to Icons.Outlined.Coffee,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryChipGrid(
    categories: List<String>,
    selectedCategories: Set<String>,
    onCategoryToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
    customCategories: Set<String> = emptySet(),
    onAddCustomCategory: ((String) -> Unit)? = null,
    onRemoveCustomCategory: ((String) -> Unit)? = null,
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(false) }
    var customText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val filteredCategories = remember(categories, searchQuery) {
        if (searchQuery.isBlank()) categories
        else categories.filter {
            it.contains(searchQuery.trim(), ignoreCase = true)
        }
    }

    Column(modifier = modifier) {
        // Search / filter field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search categories...") },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(20.dp),
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            textStyle = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Custom categories shown first as InputChips with delete
            customCategories.forEach { custom ->
                val isSelected = custom in selectedCategories
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1f,
                    animationSpec = spring(
                        dampingRatio = 0.6f,
                        stiffness = Spring.StiffnessLow,
                    ),
                    label = "chip_scale",
                )
                val chipColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    label = "chip_color",
                )

                InputChip(
                    selected = isSelected,
                    onClick = { onCategoryToggle(custom) },
                    label = {
                        Text(
                            text = custom.replaceFirstChar { it.titlecase() },
                        )
                    },
                    trailingIcon = {
                        if (onRemoveCustomCategory != null) {
                            IconButton(
                                onClick = { onRemoveCustomCategory(custom) },
                                modifier = Modifier.size(
                                    InputChipDefaults.IconSize,
                                ),
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    },
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = chipColor,
                    ),
                    modifier = Modifier.scale(scale),
                )
            }

            // Built-in categories
            filteredCategories.forEach { category ->
                val isSelected = category in selectedCategories
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1f,
                    animationSpec = spring(
                        dampingRatio = 0.6f,
                        stiffness = Spring.StiffnessLow,
                    ),
                    label = "chip_scale",
                )

                val icon = categoryIcons[category]

                FilterChip(
                    selected = isSelected,
                    onClick = { onCategoryToggle(category) },
                    label = {
                        Text(
                            text = category.replaceFirstChar { it.titlecase() },
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = icon ?: Icons.Outlined.Category,
                            contentDescription = null,
                            modifier = Modifier.size(
                                FilterChipDefaults.IconSize,
                            ),
                        )
                    },
                    modifier = Modifier.scale(scale),
                )
            }

            // "Add Custom" chip
            if (onAddCustomCategory != null) {
                FilterChip(
                    selected = showCustomInput,
                    onClick = { showCustomInput = !showCustomInput },
                    label = { Text("Add Custom") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(
                                FilterChipDefaults.IconSize,
                            ),
                        )
                    },
                )
            }
        }

        // Custom category input
        if (showCustomInput && onAddCustomCategory != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = customText,
                    onValueChange = { customText = it },
                    placeholder = {
                        Text("e.g. sakura, lofi, vaporwave...")
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val trimmed = customText.trim()
                                .lowercase()
                            if (trimmed.isNotEmpty()) {
                                onAddCustomCategory(trimmed)
                                customText = ""
                                focusManager.clearFocus()
                            }
                        },
                    ),
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val trimmed = customText.trim().lowercase()
                        if (trimmed.isNotEmpty()) {
                            onAddCustomCategory(trimmed)
                            customText = ""
                            focusManager.clearFocus()
                        }
                    },
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
