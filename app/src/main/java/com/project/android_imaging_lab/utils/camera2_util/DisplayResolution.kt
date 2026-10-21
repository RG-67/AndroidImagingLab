package com.project.android_imaging_lab.utils.camera2_util

import android.content.Context
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext


@Composable
fun rememberDisplayRotation(): Int {

    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    return remember(configuration) {
        val display =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display
            } else {
                @Suppress("DEPRECATION")
                (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            }
        display?.rotation ?: Surface.ROTATION_0
    }

}