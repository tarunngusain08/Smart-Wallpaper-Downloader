package com.wallshift.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.wallshift.app.domain.model.WallpaperTarget
import androidx.compose.foundation.layout.Column

@Composable
fun WallpaperTargetSelector(
    selectedTarget: WallpaperTarget,
    onTargetSelected: (WallpaperTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(
        WallpaperTarget.HOME to "Home Screen",
        WallpaperTarget.LOCK to "Lock Screen",
        WallpaperTarget.BOTH to "Both",
    )

    Column(
        modifier = modifier.selectableGroup(),
    ) {
        options.forEach { (target, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedTarget == target,
                        onClick = { onTargetSelected(target) },
                        role = Role.RadioButton,
                    )
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = selectedTarget == target,
                    onClick = null,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
    }
}
