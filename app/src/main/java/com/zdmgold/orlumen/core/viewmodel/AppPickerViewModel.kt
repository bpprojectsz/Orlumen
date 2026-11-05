package com.zdmgold.orlumen.core.viewmodel

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
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
private const val TAG = "AppPickerViewModel"
private const val MEDIA_BROWSER_SERVICE = "android.media.browse.MediaBrowserService"

enum class AppPickerTarget {
    VISUALIZER_APPS,
    MUSIC_SOURCES
}

data class InstalledApp(val packageName: String, val label: String)

data class AppPickerUiState(
    val apps: List<InstalledApp>,
    val musicApps: List<InstalledApp>,
    val visualizerApps: Set<String>,
    val musicSources: Set<String>,
    val loading: Boolean
)

/** Two apps can share a name, so repeated labels get their package appended to tell them apart. */
internal fun disambiguate(apps: List<InstalledApp>): List<InstalledApp> {
    val repeated = apps.groupBy { it.label.lowercase() }.filterValues { it.size > 1 }.keys
    return apps.map { app ->
        if (app.label.lowercase() in repeated) app.copy(label = "${app.label} (${app.packageName})") else app
    }
}

internal fun Set<String>.toggled(packageName: String): Set<String> =
    if (packageName in this) this - packageName else this + packageName

@HiltViewModel
class AppPickerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val packageManager: PackageManager,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    private val apps = MutableStateFlow<List<InstalledApp>?>(null)
    private val mediaApps = MutableStateFlow<List<InstalledApp>?>(null)

    val uiState: StateFlow<AppPickerUiState> = combine(
        apps,
        mediaApps,
        preferencesRepository.preferences
    ) { installed, media, prefs ->
        AppPickerUiState(
            apps = installed.orEmpty(),
            musicApps = media?.takeIf { it.isNotEmpty() } ?: installed.orEmpty(),
            visualizerApps = prefs.visualizerAppPackages,
            musicSources = prefs.musicSourcePackages,
            loading = installed == null
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        AppPickerUiState(emptyList(), emptyList(), emptySet(), emptySet(), loading = true)
    )

    init {
        viewModelScope.launch {
            apps.value = withContext(Dispatchers.IO) { loadLaunchableApps() }
            mediaApps.value = withContext(Dispatchers.IO) { loadMediaApps() }
        }
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
            .let(::disambiguate)
            .sortedBy { it.label.lowercase() }
    }

    /** Apps that publish a media browser or take media buttons, which is how music apps show up. */
    @Suppress("DEPRECATION")
    private fun loadMediaApps(): List<InstalledApp> {
        val browse = Intent(MEDIA_BROWSER_SERVICE)
        val button = Intent(Intent.ACTION_MEDIA_BUTTON)
        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val flags = PackageManager.ResolveInfoFlags.of(0)
            packageManager.queryIntentServices(browse, flags).map { it.serviceInfo.packageName } +
                packageManager.queryBroadcastReceivers(button, flags).map { it.activityInfo.packageName }
        } else {
            packageManager.queryIntentServices(browse, 0).map { it.serviceInfo.packageName } +
                packageManager.queryBroadcastReceivers(button, 0).map { it.activityInfo.packageName }
        }
        return packages
            .distinct()
            .filterNot { it == context.packageName }
            .mapNotNull { pkg ->
                try {
                    InstalledApp(pkg, packageManager.getApplicationInfo(pkg, 0).loadLabel(packageManager).toString())
                } catch (missing: PackageManager.NameNotFoundException) {
                    Log.w(TAG, "Package vanished while listing media apps: $pkg", missing)
                    null
                }
            }
            .let(::disambiguate)
            .sortedBy { it.label.lowercase() }
    }
}
