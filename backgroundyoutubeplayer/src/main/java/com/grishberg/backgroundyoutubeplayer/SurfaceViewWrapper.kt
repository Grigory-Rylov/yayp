package com.grishberg.backgroundyoutubeplayer

import android.content.Context
import android.view.SurfaceHolder
import android.view.SurfaceView

/**
 * Simple wrapper for Android SurfaceView.
 */
class SurfaceViewWrapper(
    context: Context
) {
    val view = SurfaceView(context)
    val holder: SurfaceHolder = view.holder
}