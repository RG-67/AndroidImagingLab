package com.project.android_imaging_lab.utils.camera2_util

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import androidx.compose.ui.window.Popup


class Camera2Inspector(context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    fun getAvailableCameras(): List<CameraOption> {
        return cameraManager.cameraIdList.map { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            val name = when (facing) {
                CameraCharacteristics.LENS_FACING_BACK -> "Back Camera"
                CameraCharacteristics.LENS_FACING_FRONT -> "Front Camera"
                CameraCharacteristics.LENS_FACING_EXTERNAL -> "External Camera"
                else -> "Unknown Camera"
            }
            CameraOption(id = id, name = "$name (ID: $id)")
        }
    }

    fun getCapabilities(cameraId: String): CameraCapabilities {
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)

        // Hardware level

        val hardwareLevelValue =
            characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
        val hardwareLevel = when (hardwareLevelValue) {
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "LEGACY"
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "LIMITED"
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "FULL"
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "LEVEL_3"
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> "EXTERNAL"
            else -> "Unknown"
        }

        // Manual sensor support

        val availableCapabilities =
            characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)

        val manualSensorSupported =
            availableCapabilities?.contains(
                CameraCharacteristics
                    .REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR
            ) == true

        val manualSensor =
            if (manualSensorSupported) "Supported"
            else "Not supported"

        // Autofocus support

        val afModes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)

        val autoFocusSupported = afModes?.any { mode ->
            mode != CaptureRequest.CONTROL_AF_MODE_OFF
        } == true

        val autoFocus =
            if (autoFocusSupported) "Supported"
            else "Not supported"

        // Manual focus support

        val minimumFocusDistance =
            characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE)

        val manualFocus =
            when {
                minimumFocusDistance == null ->
                    "Unknown"

                minimumFocusDistance > 0f ->
                    "Supported"

                else ->
                    "Not supported (fixed focus)"
            }

        // ISO range

        val sensitivityRange =
            characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)

        val isoRange = sensitivityRange?.let {
            "${it.lower}-${it.upper}"
        } ?: "Unavailable"

        // Exposure-time range

        val exposureTimeRange =
            characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)

        val exposureRange = exposureTimeRange?.let {
            "${it.lower}-${it.upper} ns"
        } ?: "Unavailable"

        // Minimum focus distance

        val minimumFocusDistanceText =
            minimumFocusDistance?.let {
                "$it diopters"
            } ?: "Unavailable"

        return CameraCapabilities(
            cameraId = cameraId,
            hardwareLevel = hardwareLevel,
            manualSensor = manualSensor,
            autoFocus = autoFocus,
            manualFocus = manualFocus,
            isoRange = isoRange,
            exposureRange = exposureRange,
            minimumFocusDistance = minimumFocusDistanceText
        )


    }

}