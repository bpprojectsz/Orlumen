package com.zdmgold.orlumen.ui.screens.permissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.PermissionKind
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.core.viewmodel.PermissionsViewModel
import com.zdmgold.orlumen.core.viewmodel.requiredMissing
import com.zdmgold.orlumen.ui.components.AppPrimaryButton
import com.zdmgold.orlumen.ui.components.IconWrapper
import com.zdmgold.orlumen.ui.components.iconBack
import com.zdmgold.orlumen.ui.components.rationaleFor

private const val REQUIRED_STEPS = 2

/**
 * A two-step guided setup for the only permissions Orlumen cannot start without. It moves to the
 * next step by itself when the user comes back from Android, and finishes without a final tap.
 */
@Composable
fun PermissionHubScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    viewModel: PermissionsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val requester = rememberPermissionRequester { viewModel.refresh() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }
    var awaiting by remember { mutableStateOf<PermissionKind?>(null) }
    val missing = requiredMissing(state)
    LaunchedEffect(missing.isEmpty()) { if (missing.isEmpty()) onDone() }
    val colors = OrlumenTheme.colors
    Column(modifier = modifier.fillMaxSize().padding(Spacing.xl)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    IconWrapper(imageVector = iconBack, contentDescription = stringResource(R.string.action_back))
                }
            }
            Text(
                text = stringResource(R.string.setup_title),
                style = OrlumenTheme.typography.headline,
                color = colors.textPrimary
            )
        }
        val current = missing.firstOrNull()
        if (current != null) {
            val rationale = rationaleFor(current)
            val stillOff = awaiting == current && missing.contains(current)
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.setup_step, REQUIRED_STEPS - missing.size + 1, REQUIRED_STEPS),
                    style = OrlumenTheme.typography.chrome,
                    color = colors.accent
                )
                Spacer(modifier = Modifier.height(Spacing.lg))
                Text(
                    text = stringResource(rationale.first),
                    style = OrlumenTheme.typography.headline,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                Text(
                    text = stringResource(rationale.second),
                    style = OrlumenTheme.typography.body,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
                if (current == PermissionKind.OVERLAY) {
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Text(
                        text = stringResource(R.string.setup_overlay_hint),
                        style = OrlumenTheme.typography.body,
                        color = colors.accent,
                        textAlign = TextAlign.Center
                    )
                }
                if (current == PermissionKind.OVERLAY && !requester.installedFromPlayStore) {
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Text(
                        text = stringResource(R.string.perm_access_help),
                        style = OrlumenTheme.typography.caption,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    TextButton(onClick = requester::openAppInfo) {
                        Text(text = stringResource(R.string.perm_open_app_info), color = colors.accent)
                    }
                }
                if (stillOff) {
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    Text(
                        text = stringResource(
                            if (current ==
                                PermissionKind.OVERLAY
                            ) {
                                R.string.setup_still_off_overlay
                            } else {
                                R.string.setup_still_off_audio
                            }
                        ),
                        style = OrlumenTheme.typography.body,
                        color = colors.accentSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            AppPrimaryButton(
                text = stringResource(if (stillOff) R.string.setup_open_again else R.string.setup_allow),
                onClick = {
                    awaiting = current
                    requester.request(current, askFirst = false)
                }
            )
        }
    }
    PermissionRequesterDialogs(requester)
}
