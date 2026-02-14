package com.wallshift.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wallshift.app.util.Constants

@Composable
fun FrequencySelector(
    selectedMinutes: Int,
    onFrequencySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Track whether the user is in "custom" mode
    val isCustom = Constants.FREQUENCY_OPTIONS
        .none {
            it.first == selectedMinutes &&
                it.first != Constants.CUSTOM_FREQUENCY_SENTINEL
        }
    var customHoursText by remember {
        mutableStateOf(
            if (isCustom && selectedMinutes > 0) {
                (selectedMinutes / 60).toString()
            } else {
                ""
            },
        )
    }

    Column(
        modifier = modifier.selectableGroup(),
    ) {
        Constants.FREQUENCY_OPTIONS.forEach { (minutes, label) ->
            val isSelected = if (minutes == Constants.CUSTOM_FREQUENCY_SENTINEL) {
                isCustom
            } else {
                selectedMinutes == minutes
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isSelected,
                        onClick = {
                            if (minutes == Constants.CUSTOM_FREQUENCY_SENTINEL) {
                                val hours = customHoursText.toIntOrNull()
                                if (hours != null && hours > 0) {
                                    onFrequencySelected(hours * 60)
                                } else {
                                    customHoursText = "2"
                                    onFrequencySelected(120)
                                }
                            } else {
                                onFrequencySelected(minutes)
                            }
                        },
                        role = Role.RadioButton,
                    )
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = null,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }

        // Custom hours input with smooth expand/collapse
        AnimatedVisibility(
            visible = isCustom,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            ) + fadeIn(
                animationSpec = spring(stiffness = Spring.StiffnessLow),
            ),
            exit = shrinkVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
            ) + fadeOut(
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 48.dp,
                        top = 4.dp,
                        bottom = 8.dp,
                        end = 4.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = customHoursText,
                    onValueChange = { value ->
                        val filtered = value.filter { it.isDigit() }
                        customHoursText = filtered
                        val hours = filtered.toIntOrNull()
                        if (hours != null && hours > 0) {
                            onFrequencySelected(hours * 60)
                        }
                    },
                    label = { Text("Hours") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                    modifier = Modifier.width(100.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "hours between changes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
