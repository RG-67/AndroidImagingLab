package com.project.android_imaging_lab.utils.camera2_util

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureFailure
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.media.ImageReader
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Camera2PreviewController(
    context: Context,
    cameraId: String
) : BaseCamera2Controller(context, cameraId) {

    private var imageReader: ImageReader? = null

    private var captureInProgress = false

    private val mainHandler = Handler(Looper.getMainLooper())

    private var photoCallback: ((File) -> Unit)? = null

    private var errorCallback: ((String) -> Unit)? = null

    var cameraSettings by mutableStateOf(ManualCameraSettings())
        private set

    var captureState by mutableStateOf(CameraCaptureState())
        private set

    var capabilities by mutableStateOf(CameraCapabilities())
        private set

    private var currentIso: Int? = null

    private var currentExposureTime: Long? = null

    private var currentPreviewSurface: Surface? = null

    private var currentCamera: CameraDevice? = null

    private val captureResultCallback =
        object : CameraCaptureSession.CaptureCallback() {

            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {

                val iso = result.get(
                    CaptureResult.SENSOR_SENSITIVITY
                )

                val exposure = result.get(
                    CaptureResult.SENSOR_EXPOSURE_TIME
                )

                val focus = result.get(
                    CaptureResult.LENS_FOCUS_DISTANCE
                )

                val afState = result.get(
                    CaptureResult.CONTROL_AF_STATE
                )

                val aeState = result.get(
                    CaptureResult.CONTROL_AE_STATE
                )

                iso?.let {
                    currentIso = it
                }

                exposure?.let {
                    currentExposureTime = it
                }

                mainHandler.post {

                    captureState = CameraCaptureState(

                        actualIso = iso,

                        actualExposureTime = exposure,

                        actualFocusDistance = focus,

                        afState = afState,

                        aeState = aeState
                    )
                }
            }
        }

    override fun onCameraPrepared(
        characteristics: CameraCharacteristics
    ) {

//        val cameraId = characteristics

        // The actual capabilities are loaded by Camera2Inspector.

        capabilities = Camera2Inspector(context)
            .getCapabilities(cameraId)

    }

    override fun onCameraOpened(
        camera: CameraDevice,
        surface: Surface
    ) {

        try {

            currentCamera = camera

            currentPreviewSurface = surface

            val characteristics =
                currentCharacteristics ?: return

            val inspector = Camera2Inspector(context)

            capabilities = inspector.getCapabilities(this.cameraId)

            val streamMap = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
            ) ?: return

            val jpegSize = streamMap.getOutputSizes(
                ImageFormat.JPEG
            )?.maxByOrNull {
                it.width.toLong() * it.height.toLong()
            } ?: return

            imageReader?.close()

            imageReader = ImageReader.newInstance(
                jpegSize.width,
                jpegSize.height,
                ImageFormat.JPEG,
                2
            )

            imageReader?.setOnImageAvailableListener(
                { reader ->

                    val image = reader.acquireNextImage()
                        ?: return@setOnImageAvailableListener

                    try {

                        val buffer = image.planes[0].buffer

                        val bytes = ByteArray(buffer.remaining())

                        buffer.get(bytes)

                        val directory =
                            context.getExternalFilesDir(null)
                                ?: throw IOException(
                                    "Storage directory unavailable."
                                )

                        val photoFile = File(
                            directory,
                            "Camera_${System.currentTimeMillis()}.jpg"
                        )

                        FileOutputStream(photoFile).use {
                            it.write(bytes)
                        }

                        val callback = photoCallback

                        photoCallback = null
                        errorCallback = null
                        captureInProgress = false

                        mainHandler.post {
                            callback?.invoke(photoFile)
                        }

                    } catch (e: Exception) {

                        val callback = errorCallback

                        photoCallback = null
                        errorCallback = null
                        captureInProgress = false

                        mainHandler.post {
                            callback?.invoke(
                                e.message ?: "Unable to save photo."
                            )
                        }

                    } finally {

                        image.close()
                    }

                },
                backgroundHandler
            )

            val previewBuilder = camera.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            ).apply {

                addTarget(surface)

//                addTarget(imageReader!!.surface)

                set(
                    CaptureRequest.CONTROL_MODE,
                    CaptureRequest.CONTROL_MODE_AUTO
                )

                set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON
                )

                set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
            }

            previewRequestBuilder = previewBuilder

            createCaptureSession(
                camera = camera,
                surfaces = listOf(
                    surface,
                    imageReader!!.surface
                )
            ) { session ->

                try {

                    applySettingsToBuilder(previewBuilder)

                    session.setRepeatingRequest(
                        previewBuilder.build(),
                        captureResultCallback,
                        backgroundHandler
                    )

                } catch (e: Exception) {

                    Log.e(
                        "Camera2PreviewController",
                        "Unable to start preview.",
                        e
                    )
                }
            }

        } catch (e: Exception) {

            Log.e(
                "Camera2PreviewController",
                "Preview initialization failed.",
                e
            )
        }
    }

    /*
     * APPLY SETTINGS TO CAPTURE REQUEST
     */

    private fun applySettingsToBuilder(
        builder: CaptureRequest.Builder
    ) {

        val settings = cameraSettings

        builder.set(
            CaptureRequest.CONTROL_MODE,
            CaptureRequest.CONTROL_MODE_AUTO
        )

        val manualExposure =
            settings.iso != null ||
                    settings.exposureTime != null

        if (manualExposure && capabilities.supportsManualSensor) {

            val iso = settings.iso
                ?: currentIso
                ?: capabilities.isoRange?.first
                ?: 100

            val exposure = settings.exposureTime
                ?: currentExposureTime
                ?: capabilities.exposureRange?.first
                ?: 10_000_000L

            builder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_OFF
            )

            builder.set(
                CaptureRequest.SENSOR_SENSITIVITY,
                iso
            )

            builder.set(
                CaptureRequest.SENSOR_EXPOSURE_TIME,
                exposure
            )

        } else {

            builder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON
            )

            builder.set(
                CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION,
                settings.exposureCompensation
            )
        }

        // Autofocus

        if (settings.manualFocusDistance != null &&
            capabilities.supportsManualFocus
        ) {

            builder.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_OFF
            )

            builder.set(
                CaptureRequest.LENS_FOCUS_DISTANCE,
                settings.manualFocusDistance
            )

        } else {

            builder.set(
                CaptureRequest.CONTROL_AF_MODE,
                settings.autofocusMode
            )
        }
    }

    /*
     * UPDATE CAMERA SETTINGS
     */

    fun updateSettings(settings: ManualCameraSettings) {

        val updatedSettings = settings.copy(

            iso = settings.iso?.let { value ->
                capabilities.isoRange?.let {
                    value.coerceIn(it.first, it.last)
                } ?: value
            },

            exposureTime = settings.exposureTime?.let { value ->
                capabilities.exposureRange?.let {
                    value.coerceIn(it.first, it.last)
                } ?: value
            },

            manualFocusDistance =
                settings.manualFocusDistance?.coerceIn(
                    0f,
                    capabilities.minimumFocusDistance
                )
        )

        cameraSettings = updatedSettings

        backgroundHandler.post {

            val builder = previewRequestBuilder ?: return@post

            val session = captureSession ?: return@post

            try {

                applySettingsToBuilder(builder)

                session.setRepeatingRequest(
                    builder.build(),
                    captureResultCallback,
                    backgroundHandler
                )

            } catch (e: Exception) {

                Log.e(TAG, "Unable to update camera settings.", e)
            }
        }
    }


    /*
     * UPDATE ISO
     */

    fun setIso(iso: Int?) {

        updateSettings(
            cameraSettings.copy(iso = iso)
        )
    }

    /*
     * UPDATE SHUTTER SPEED
     */

    fun setExposureTime(exposureTime: Long?) {

        updateSettings(
            cameraSettings.copy(
                exposureTime = exposureTime
            )
        )
    }

    /*
     * UPDATE AUTOFOCUS MODE
     */

    fun setAutofocusMode(mode: Int) {

        if (mode !in capabilities.autofocusModes) return

        updateSettings(
            cameraSettings.copy(
                autofocusMode = mode,
                manualFocusDistance = null
            )
        )
    }

    /*
     * UPDATE MANUAL FOCUS
     */

    fun setManualFocusDistance(distance: Float) {

        if (!capabilities.supportsManualFocus) return

        updateSettings(
            cameraSettings.copy(
                manualFocusDistance = distance
            )
        )
    }

    /*
     * UPDATE EXPOSURE COMPENSATION
     */

    fun setExposureCompensation(value: Int) {

        if (!capabilities.supportsExposureCompensation) return

        if (cameraSettings.iso != null ||
            cameraSettings.exposureTime != null
        ) return

        val range =
            capabilities.exposureCompensationRange ?: return

        updateSettings(
            cameraSettings.copy(
                exposureCompensation = value.coerceIn(
                    range.first,
                    range.last
                )
            )
        )
    }

    /*
     * CAPTURE PHOTO
     */

    fun capturePhoto(
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit
    ) {

        backgroundHandler.post {

            val camera = cameraDevice
            val session = captureSession
            val reader = imageReader

            if (camera == null || session == null || reader == null) {

                mainHandler.post {
                    onError("Camera preview is not ready.")
                }

                return@post
            }

            if (captureInProgress) {

                mainHandler.post {
                    onError("A photo is already being captured.")
                }

                return@post
            }

            try {

                captureInProgress = true

                photoCallback = onSuccess
                errorCallback = onError

                val captureBuilder = camera.createCaptureRequest(
                    CameraDevice.TEMPLATE_STILL_CAPTURE
                ).apply {

                    addTarget(reader.surface)

                    applySettingsToBuilder(this)

                    set(
                        CaptureRequest.JPEG_ORIENTATION,
                        0
                    )
                }

                session.capture(
                    captureBuilder.build(),

                    object : CameraCaptureSession.CaptureCallback() {

                        override fun onCaptureCompleted(
                            session: CameraCaptureSession,
                            request: CaptureRequest,
                            result: TotalCaptureResult
                        ) {

                            try {

                                val builder =
                                    previewRequestBuilder ?: return

                                applySettingsToBuilder(builder)

                                session.setRepeatingRequest(
                                    builder.build(),
                                    captureResultCallback,
                                    backgroundHandler
                                )

                            } catch (e: Exception) {

                                Log.e(
                                    "Camera2PreviewController",
                                    "Unable to resume preview.",
                                    e
                                )
                            }
                        }

                        override fun onCaptureFailed(
                            session: CameraCaptureSession,
                            request: CaptureRequest,
                            failure: CaptureFailure
                        ) {

                            val callback = errorCallback

                            photoCallback = null
                            errorCallback = null
                            captureInProgress = false

                            mainHandler.post {
                                callback?.invoke(
                                    "Photo capture failed."
                                )
                            }
                        }
                    },

                    backgroundHandler
                )

            } catch (e: Exception) {

                val callback = errorCallback

                photoCallback = null
                errorCallback = null
                captureInProgress = false

                mainHandler.post {
                    callback?.invoke(
                        e.message ?: "Unable to capture photo."
                    )
                }
            }
        }
    }

    override fun onCameraClosed() {

        imageReader?.close()

        imageReader = null

        captureInProgress = false

        photoCallback = null

        errorCallback = null

        currentCamera = null

        currentPreviewSurface = null

        currentIso = null

        currentExposureTime = null

        mainHandler.post {
            captureState = CameraCaptureState()
        }
    }
}