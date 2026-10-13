package com.zdmgold.orlumen.core.viewmodel

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.orlumen.core.datastore.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val STOP_TIMEOUT_MS = 5_000L

enum class AppPickerTarget {
    VISUALIZER_APPS,
    MUSIC_SOURCES
}

data class InstalledApp(val packageName: String, val label: String)

data class AppPickerUiState(
    val apps: List<InstalledApp>,
    val visualizerApps: Set<String>,
    val musicSources: Set<String>,
    val loading: Boolean
)

internal fun Set<String>.toggled(packageName: String): Set<String> =
    if (packageName in this) this - packageName else this + packageName

@HiltViewModel
class AppPickerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val packageManager: PackageManager,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    private val apps = MutableStateFlow<List<InstalledApp>?>(null)

    val uiState: StateFlow<AppPickerUiState> = combine(apps, preferencesRepository.preferences) { installed, prefs ->
        AppPickerUiState(
            apps = installed.orEmpty(),
            visualizerApps = prefs.visualizerAppPackages,
            musicSources = prefs.musicSourcePackages,
            loading = installed == null
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        AppPickerUiState(emptyList(), emptySet(), emptySet(), loading = true)
    )

    init {
        viewModelScope.launch { apps.value = withContext(Dispatchers.IO) { loadLaunchableApps() } }
    }

    fun toggle(target: AppPickerTarget, packageName: String) {
        viewModelScope.launch {
            preferencesRepository.update { prefs ->
                when (target) {
                    AppPickerTarget.VISUALIZER_APPS ->
                        prefs.copy(visualizerAppPackages = prefs.visualizerAppPackages.toggled(packageName))
                    AppPickerTarget.MUSIC_SOURCES ->
                        prefs.copy(musicSourcePackages = prefs.musicSourcePackages.toggled(packageName))
                }
            }
        }
    }

    /** An empty selection means every app, so clearing is how the user goes back to the default. */
    fun clear(target: AppPickerTarget) {
        viewModelScope.launch {
            preferencesRepository.update { prefs ->
                when (target) {
                    AppPickerTarget.VISUALIZER_APPS -> prefs.copy(visualizerAppPackages = emptySet())
                    AppPickerTarget.MUSIC_SOURCES -> prefs.copy(musicSourcePackages = emptySet())
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun loadLaunchableApps(): List<InstalledApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            packageManager.queryIntentActivities(intent, 0)
        }
        return resolved
            .map { InstalledApp(it.activityInfo.packageName, it.loadLabel(packageManager).toString()) }
            .filterNot { it.packageName == context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }
}
