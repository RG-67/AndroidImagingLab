package com.project.android_imaging_lab.utils.camera2_util

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureFailure
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.media.ImageReader
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Camera2PreviewController(context: Context, cameraId: String) :
    BaseCamera2Controller(context = context, selectedCameraId = cameraId) {

    private var imageReader: ImageReader? = null

    private var captureInProgress = false

    private val mainHandler = Handler(Looper.getMainLooper())

    private var photoCallback: ((File) -> Unit)? = null

    private var errorCallback: ((String) -> Unit)? = null

    override fun onCameraOpened(
        camera: CameraDevice,
        surface: Surface
    ) {

        try {

            val characteristics = currentCharacteristics
                ?: return

            val streamMap = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
            ) ?: return

            val jpegSize = streamMap.getOutputSizes(ImageFormat.JPEG)
                ?.maxByOrNull {
                    it.width.toLong() * it.height.toLong()
                } ?: return

            // Create JPEG ImageReader.
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

                        val directory = context.getExternalFilesDir(null)
                            ?: throw IOException("Storage directory unavailable.")

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

            // Configure preview request.
            val previewBuilder = camera.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            ).apply {

                addTarget(surface)

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

            // IMPORTANT:
            // Both preview and JPEG surfaces must be included.
            createCaptureSession(
                camera = camera,
                surfaces = listOf(
                    surface,
                    imageReader!!.surface
                )
            ) { session ->

                try {

                    session.setRepeatingRequest(
                        previewBuilder.build(),
                        null,
                        backgroundHandler
                    )

                    Log.d(
                        "Camera2PreviewController",
                        "Preview started successfully."
                    )

                } catch (e: CameraAccessException) {

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

                                val builder = previewRequestBuilder
                                    ?: return

                                session.setRepeatingRequest(
                                    builder.build(),
                                    null,
                                    backgroundHandler
                                )

                            } catch (e: CameraAccessException) {

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
                                callback?.invoke("Photo capture failed.")
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
    }


}