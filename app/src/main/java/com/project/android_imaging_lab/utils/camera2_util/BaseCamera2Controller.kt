package com.project.android_imaging_lab.utils.camera2_util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.MeteringRectangle
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.annotation.RequiresPermission
import androidx.camera.viewfinder.core.TransformationInfo
import androidx.compose.runtime.Stable
import androidx.camera.viewfinder.core.ViewfinderSurfaceRequest
import androidx.camera.viewfinder.core.camera2.Camera2TransformationInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor
import kotlin.coroutines.resume

private const val TAG = "BaseCamera2Controller"


@Stable
abstract class BaseCamera2Controller(
    protected val context: Context,
    val isFrontCamera: Boolean
) {

    var surfaceRequest: ViewfinderSurfaceRequest? by mutableStateOf(null)
        private set

    var transformationInfo: TransformationInfo by mutableStateOf(TransformationInfo.DEFAULT)
        private set

    protected val cameraManager: CameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private val backgroundThread = HandlerThread("CameraBackground").apply { start() }
    protected val backgroundHandler = Handler(backgroundThread.looper)

    protected var cameraDevice: CameraDevice? = null
    protected var captureSession: CameraCaptureSession? = null
    protected var previewRequestBuilder: CaptureRequest.Builder? = null

    protected var cameraId: String = ""
    protected var currentCharacteristics: CameraCharacteristics? = null
    protected var currentDisplayRotation: Int = Surface.ROTATION_0

    private var previewSurface: Surface? = null
    private var isCameraOpeningOrOpen: Boolean = false

    protected open val previewSize: Size = Size(1920, 1080)

    private fun findCameraId(): String? {
        val targetFacing = if (isFrontCamera) {
            CameraCharacteristics.LENS_FACING_FRONT
        } else {
            CameraCharacteristics.LENS_FACING_BACK
        }
        return cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.LENS_FACING) == targetFacing
        }
    }

    fun updateDisplayRotation(displayRotation: Int) {
        currentDisplayRotation = displayRotation
        recomputeTransformation()
    }

    private fun recomputeTransformation() {
        val characteristics = currentCharacteristics ?: return
        val sensorRotation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
        val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
            ?: CameraCharacteristics.LENS_FACING_BACK
        val sign = if (lensFacing == CameraCharacteristics.LENS_FACING_FRONT) 1 else -1

        val rotationDegrees = currentDisplayRotation.toRotationDegrees()
        val relativeRotation = (sensorRotation - rotationDegrees * sign + 360) % 360

        val baseInfo = Camera2TransformationInfo.createFromCharacteristics(characteristics)
        transformationInfo =
            TransformationInfo(
                sourceRotation = relativeRotation,
                isSourceMirroredHorizontally = baseInfo.isSourceMirroredHorizontally,
                isSourceMirroredVertically = baseInfo.isSourceMirroredVertically,
                cropRectLeft = baseInfo.cropRectLeft,
                cropRectTop = baseInfo.cropRectTop,
                cropRectRight = baseInfo.cropRectRight,
                cropRectBottom = baseInfo.cropRectBottom,
            )
    }

    fun openCamera() {
        backgroundHandler.post { openCameraLocked() }
    }

    @SuppressLint("MissingPermission")
    private fun openCameraLocked() {
        if (cameraDevice != null || isCameraOpeningOrOpen) return
        try {
            val id = findCameraId() ?: return
            cameraId = id
            val characteristics = cameraManager.getCameraCharacteristics(id)
            currentCharacteristics = characteristics
            onCameraPrepared(characteristics)
            recomputeTransformation()
            isCameraOpeningOrOpen = true

            cameraManager.openCamera(
                id,
                object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        isCameraOpeningOrOpen = false
                        cameraDevice = camera

                        surfaceRequest =
                            ViewfinderSurfaceRequest(previewSize.width, previewSize.height)
                        maybeStartSession()
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        isCameraOpeningOrOpen = false
                        camera.close()
                        cameraDevice = null
                    }

                    override fun onError(
                        camera: CameraDevice,
                        error: Int,
                    ) {
                        isCameraOpeningOrOpen = false
                        camera.close()
                        cameraDevice = null
                    }
                },
                backgroundHandler,
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Failed to open camera", e)
        }
    }

    suspend fun runViewfinderSession(surface: Surface) {
        backgroundHandler.post {
            previewSurface = surface
            maybeStartSession()
        }
        try {
            awaitCancellation()
        } finally {
            withContext(NonCancellable) {
                suspendCancellableCoroutine { continuation ->
                    backgroundHandler.post {
                        try {
                            captureSession?.close()
                        } catch (e: Exception) {
                            Log.w(TAG, "Exception closing capture session", e)
                        }
                        captureSession = null
                        if (previewSurface === surface) previewSurface = null
                        continuation.resume(Unit)
                    }
                }
            }
        }
    }

    private fun maybeStartSession() {
        val camera = cameraDevice ?: return
        val surface = previewSurface ?: return
        try {
            captureSession?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Exception closing previous session", e)
        }
        captureSession = null
        onCameraOpened(camera, surface)
    }


    protected open fun onCameraPrepared(characteristics: CameraCharacteristics) {}

    protected abstract fun onCameraOpened(
        camera: CameraDevice,
        surface: Surface,
    )

    protected open fun onCameraClosed() {}

    open fun closeCamera() {
        backgroundHandler.post { closeCameraLocked() }
    }

    private fun closeCameraLocked() {
        isCameraOpeningOrOpen = false
        try {
            captureSession?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Exception closing capture session", e)
        }
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
        previewSurface = null
        surfaceRequest = null
        onCameraClosed()
    }

    open fun release() {
        backgroundHandler.post { closeCameraLocked() }
        backgroundThread.quitSafely()
    }

    protected fun createCaptureSession(
        camera: CameraDevice,
        surfaces: List<Surface>,
        onConfigured: (CameraCaptureSession) -> Unit,
    ) {
        val stateCallback =
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    if (cameraDevice == null) return
                    captureSession = session
                    onConfigured(session)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e(TAG, "Failed to configure capture session")
                }
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val executor = Executor { command -> backgroundHandler.post(command) }
            val outputConfigurations =
                surfaces.map { surface ->
                    OutputConfiguration(surface).also { configureOutput(it, surface) }
                }
            val sessionConfiguration =
                SessionConfiguration(
                    SessionConfiguration.SESSION_REGULAR,
                    outputConfigurations,
                    executor,
                    stateCallback,
                )
            camera.createCaptureSession(sessionConfiguration)
        } else {
            @Suppress("DEPRECATION")
            camera.createCaptureSession(surfaces, stateCallback, null)
        }
    }

    protected open fun configureOutput(
        output: OutputConfiguration,
        surface: Surface,
    ) {
    }

    fun focus(
        offset: Offset,
        viewWidth: Float,
        viewHeight: Float,
    ) {
        val session = captureSession ?: return
        val builder = previewRequestBuilder ?: return

        try {
            val characteristics =
                currentCharacteristics ?: cameraManager.getCameraCharacteristics(cameraId)
            val sensorArraySize =
                characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE) ?: return

            val y0 = (offset.x / viewWidth) * sensorArraySize.height()
            val x0 = (offset.y / viewHeight) * sensorArraySize.width()
            val halfTouchWidth = 150
            val halfTouchHeight = 150

            val focusArea =
                Rect(
                    (x0 - halfTouchWidth).toInt().coerceAtLeast(0),
                    (y0 - halfTouchHeight).toInt().coerceAtLeast(0),
                    (x0 + halfTouchWidth).toInt().coerceAtMost(sensorArraySize.width()),
                    (y0 + halfTouchHeight).toInt().coerceAtMost(sensorArraySize.height()),
                )

            try {
                session.stopRepeating()
            } catch (e: CameraAccessException) {
                Log.w(TAG, "Exception while stopping repeating", e)
            }

            val rectangle = MeteringRectangle(focusArea, MeteringRectangle.METERING_WEIGHT_MAX)
            builder.set(CaptureRequest.CONTROL_AF_REGIONS, arrayOf(rectangle))
            builder.set(CaptureRequest.CONTROL_AE_REGIONS, arrayOf(rectangle))
            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
            builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)

            session.capture(builder.build(), null, backgroundHandler)

            builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE)
            session.setRepeatingRequest(builder.build(), null, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Focus error", e)
        }
    }

    protected companion object {
        fun Int.toRotationDegrees(): Int =
            when (this) {
                Surface.ROTATION_0 -> 0
                Surface.ROTATION_90 -> 90
                Surface.ROTATION_180 -> 180
                Surface.ROTATION_270 -> 270
                else -> 0
            }
    }


}