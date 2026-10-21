package com.project.android_imaging_lab.utils.camera2_util

import androidx.camera.viewfinder.compose.Viewfinder
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner


@Composable
fun Camera2Preview(
    controller: BaseCamera2Controller,
    modifier: Modifier = Modifier,
    onFocusTop: ((Offset) -> Unit)? = null
) {

    val lifecycleOwner = LocalLifecycleOwner.current
    val displayRotation = rememberDisplayRotation()

    LaunchedEffect(displayRotation, controller) {
        controller.updateDisplayRotation(displayRotation)
    }

    DisposableEffect(lifecycleOwner, controller) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {

                Lifecycle.Event.ON_CREATE, Lifecycle.Event.ON_RESUME -> {
                    controller.openCamera()
                }

                Lifecycle.Event.ON_PAUSE -> {
                    controller.closeCamera()
                }

                else -> {}

            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            controller.closeCamera()
        }
    }


    val request = controller.surfaceRequest
    if (request != null) {
        Viewfinder(
            surfaceRequest = request,
            transformationInfo = controller.transformationInfo,
            modifier = modifier
                .fillMaxSize()
                .pointerInput(controller) {
                    detectTapGestures(
                        onTap = { offset ->
                            controller.focus(offset, size.width.toFloat(), size.height.toFloat())
                            onFocusTop?.invoke(offset)
                        }
                    )
                }
        ) {
            onSurfaceSession {
                controller.runViewfinderSession(surface)
            }
        }
    }


}