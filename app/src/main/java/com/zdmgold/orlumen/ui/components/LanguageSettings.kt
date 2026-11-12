package com.zdmgold.orlumen.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log

private const val TAG = "LanguageSettings"

/** Opens Android's per-app language picker (13+), or the phone's language settings on older versions. */
fun openLanguageSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Intent(Settings.ACTION_APP_LOCALE_SETTINGS, Uri.fromParts("package", context.packageName, null))
    } else {
        Intent(Settings.ACTION_LOCALE_SETTINGS)
    }
    try {
        context.startActivity(intent)
    } catch (notFound: ActivityNotFoundException) {
        Log.w(TAG, "No language settings screen available", notFound)
    }
}
