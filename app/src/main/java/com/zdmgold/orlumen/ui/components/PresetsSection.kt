package com.zdmgold.orlumen.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Spacing

/** Saved presets for an editor: tap a name to load it, delete with the bin, save the current look below. */
@Composable
fun PresetsSection(
    presets: List<Pair<Long, String>>,
    onLoad: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    AppCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            if (presets.isEmpty()) {
                Text(
                    text = stringResource(R.string.preset_empty),
                    style = OrlumenTheme.typography.caption,
                    color = OrlumenTheme.colors.textTertiary
                )
            }
            presets.forEach { (id, presetName) ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = presetName,
                        style = OrlumenTheme.typography.body,
                        color = OrlumenTheme.colors.textPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .selectable(selected = false, role = Role.Button, onClick = { onLoad(id) })
                            .padding(vertical = Spacing.sm)
                    )
                    IconButton(onClick = { onDelete(id) }) {
                        IconWrapper(
                            imageVector = iconDelete,
                            contentDescription = stringResource(R.string.preset_delete)
                        )
                    }
                }
            }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(text = stringResource(R.string.preset_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            AppChip(
                label = stringResource(R.string.preset_save),
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim())
                        name = ""
                    }
                }
            )
        }
    }
}
