package com.zdmgold.orlumen.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.core.viewmodel.AppPickerTarget
import com.zdmgold.orlumen.core.viewmodel.AppPickerViewModel
import com.zdmgold.orlumen.ui.components.ScreenHeader

@Composable
fun AppPickerScreen(
    target: AppPickerTarget,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppPickerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val selected = if (target == AppPickerTarget.VISUALIZER_APPS) state.visualizerApps else state.musicSources
    val titleRes = if (target == AppPickerTarget.VISUALIZER_APPS) {
        R.string.settings_visualizer_apps
    } else {
        R.string.settings_music_sources
    }
    Column(modifier = modifier.fillMaxSize().padding(horizontal = Spacing.lg)) {
        ScreenHeader(title = stringResource(titleRes), onBack = onBack)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (selected.isEmpty()) {
                    stringResource(R.string.picker_all_apps)
                } else {
                    pluralStringResource(R.plurals.picker_selected, selected.size, selected.size)
                },
                style = OrlumenTheme.typography.body,
                color = OrlumenTheme.colors.textSecondary,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { viewModel.clear(target) }, enabled = selected.isNotEmpty()) {
                Text(text = stringResource(R.string.picker_clear))
            }
        }
        if (state.loading) {
            Text(
                text = stringResource(R.string.picker_loading),
                style = OrlumenTheme.typography.body,
                color = OrlumenTheme.colors.textTertiary
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.apps, key = { it.packageName }) { app ->
                    val isSelected = app.packageName in selected
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = isSelected,
                                role = Role.Checkbox,
                                onValueChange = { viewModel.toggle(target, app.packageName) }
                            )
                            .padding(vertical = Spacing.md)
                    ) {
                        Text(
                            text = app.label,
                            style = OrlumenTheme.typography.body,
                            color = OrlumenTheme.colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(
                                checkedColor = OrlumenTheme.colors.accent,
                                checkmarkColor = OrlumenTheme.colors.onAccent,
                                uncheckedColor = OrlumenTheme.colors.border
                            )
                        )
                    }
                }
            }
        }
    }
}
