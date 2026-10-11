package com.zdmgold.orlumen.platform.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.RoundedCorner
import android.view.View
import android.view.WindowManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val DIM_AMOUNT = 0.6f
private const val FALLBACK_CORNER_RADIUS_DP = 36f

@Singleton
class WindowManagerHelper @Inject constructor(
    private val windowManager: WindowManager,
    @ApplicationContext private val context: Context
) {
    fun overlayParams(keepScreenOn: Boolean, dimBackground: Boolean): WindowManager.LayoutParams {
        var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        if (keepScreenOn) flags = flags or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        if (dimBackground) flags = flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            dimAmount = DIM_AMOUNT
            layoutInDisplayCutoutMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }

    fun addOverlay(view: View, params: WindowManager.LayoutParams): Boolean = runCatching {
        windowManager.addView(view, params)
    }.isSuccess

    fun updateOverlay(view: View, params: WindowManager.LayoutParams): Boolean = runCatching {
        windowManager.updateViewLayout(view, params)
    }.isSuccess

    fun removeOverlay(view: View): Boolean = runCatching { windowManager.removeViewImmediate(view) }.isSuccess

    fun measureGeometry(): EdgeGeometry {
        val density = context.resources.displayMetrics.density
        val fallback = FALLBACK_CORNER_RADIUS_DP * density
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val metrics = windowManager.maximumWindowMetrics
            val insets = metrics.windowInsets
            val corners = CornerRadii(
                topLeft = insets.radiusOf(RoundedCorner.POSITION_TOP_LEFT, fallback),
                topRight = insets.radiusOf(RoundedCorner.POSITION_TOP_RIGHT, fallback),
                bottomRight = insets.radiusOf(RoundedCorner.POSITION_BOTTOM_RIGHT, fallback),
                bottomLeft = insets.radiusOf(RoundedCorner.POSITION_BOTTOM_LEFT, fallback)
            )
            return geometryOf(metrics.bounds.width().toFloat(), metrics.bounds.height().toFloat(), corners)
        }
        val real = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(real)
        return geometryOf(real.widthPixels.toFloat(), real.heightPixels.toFloat(), CornerRadii.uniform(fallback))
    }

    private fun geometryOf(width: Float, height: Float, corners: CornerRadii): EdgeGeometry =
        EdgeGeometry(width, height, corners.coercedTo(minOf(width, height) / 2f))

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.S)
    private fun android.view.WindowInsets.radiusOf(position: Int, fallback: Float): Float =
        getRoundedCorner(position)?.radius?.toFloat() ?: fallback
}
