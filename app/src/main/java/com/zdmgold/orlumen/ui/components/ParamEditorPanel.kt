package com.zdmgold.orlumen.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.zdmgold.orlumen.core.model.ParamSpec
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Spacing
import java.util.Locale

private const val TOGGLE_ON_THRESHOLD = 0.5f
private const val WHOLE_NUMBER_FROM = 10f
private const val ONE_DECIMAL_FROM = 2f

internal fun isToggleOn(value: Float): Boolean = value >= TOGGLE_ON_THRESHOLD

internal fun toggleValue(checked: Boolean): Float = if (checked) 1f else 0f

internal fun formatSliderValue(value: Float, max: Float, locale: Locale = Locale.getDefault()): String {
    val decimals = when {
        max >= WHOLE_NUMBER_FROM -> 0
        max >= ONE_DECIMAL_FROM -> 1
        else -> 2
    }
    return String.format(locale, "%.${decimals}f", value)
}

/**
 * Renders a design's [ParamSpec] list as controls. Numbers and toggles are reported through
 * [onNumberChange] (toggles as 1f or 0f) and choices through [onChoiceChange].
 */
@Composable
fun ParamEditorPanel(
    specs: List<ParamSpec>,
    numbers: Map<String, Float>,
    choices: Map<String, String>,
    onNumberChange: (key: String, value: Float) -> Unit,
    onChoiceChange: (key: String, optionId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        specs.forEach { spec ->
            when (spec) {
                is ParamSpec.Slider -> {
                    val value = numbers[spec.key] ?: spec.defaultValue
                    AppSlider(
                        label = stringResource(spec.labelRes),
                        value = value,
                        onValueChange = { onNumberChange(spec.key, it) },
                        valueRange = spec.min..spec.max,
                        valueLabel = formatSliderValue(value, spec.max)
                    )
                }
                is ParamSpec.Toggle -> {
                    val value = numbers[spec.key] ?: toggleValue(spec.defaultValue)
                    AppSwitchRow(
                        title = stringResource(spec.labelRes),
                        checked = isToggleOn(value),
                        onCheckedChange = { onNumberChange(spec.key, toggleValue(it)) }
                    )
                }
                is ParamSpec.Choice -> {
                    val current = choices[spec.key] ?: spec.defaultOptionId
                    Column {
                        Text(
                            text = stringResource(spec.labelRes),
                            style = OrlumenTheme.typography.caption,
                            color = OrlumenTheme.colors.textSecondary,
                            modifier = Modifier.padding(bottom = Spacing.xs)
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            spec.options.forEach { option ->
                                AppChip(
                                    label = stringResource(option.labelRes),
                                    selected = option.id == current,
                                    onClick = { onChoiceChange(spec.key, option.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
