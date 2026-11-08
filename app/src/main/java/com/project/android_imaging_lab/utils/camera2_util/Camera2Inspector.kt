package com.project.android_imaging_lab.utils.camera2_util

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.util.Log
import androidx.compose.ui.window.Popup


class Camera2Inspector(context: Context) {

    private val cameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    fun getAvailableCameras(): List<CameraOption> {

        return cameraManager.cameraIdList.map { id ->

            val characteristics =
                cameraManager.getCameraCharacteristics(id)

            val facing =
                characteristics.get(CameraCharacteristics.LENS_FACING)

            val name = when (facing) {

                CameraCharacteristics.LENS_FACING_BACK ->
                    "Back Camera"

                CameraCharacteristics.LENS_FACING_FRONT ->
                    "Front Camera"

                CameraCharacteristics.LENS_FACING_EXTERNAL ->
                    "External Camera"

                else -> "Unknown Camera"
            }

            CameraOption(
                id = id,
                name = "$name (ID: $id)"
            )
        }
    }

    fun getCapabilities(cameraId: String): CameraCapabilities {

        val characteristics =
            cameraManager.getCameraCharacteristics(cameraId)

        val hardwareValue = characteristics.get(
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL
        )

        val hardwareLevel = when (hardwareValue) {
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "LEGACY"
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "LIMITED"
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "FULL"
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "LEVEL_3"
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> "EXTERNAL"
            else -> "UNKNOWN"
        }

        // Available capabilities

        val availableCapabilities =
            characteristics.get(
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES
            ) ?: intArrayOf()

        val manualSensorSupported =
            CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR in availableCapabilities

        // ISO

        val sensitivityRange =
            characteristics.get(
                CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE
            )

        val isoRange = sensitivityRange?.let {
            it.lower..it.upper
        }

        val supportsManualIso =
            manualSensorSupported && sensitivityRange != null

        // Exposure time

        val exposureRangeValue =
            characteristics.get(
                CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE
            )

        val exposureRange = exposureRangeValue?.let {
            it.lower..it.upper
        }

        val supportsManualExposure =
            manualSensorSupported && exposureRangeValue != null

        // Autofocus

        val autofocusModes =
            characteristics.get(
                CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES
            )?.toList() ?: emptyList()

        val autofocusSupported =
            autofocusModes.any {
                it != CaptureRequest.CONTROL_AF_MODE_OFF
            }

        // Manual focus

        val minimumFocusDistance =
            characteristics.get(
                CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE
            ) ?: 0f

        val manualFocusSupported =
            minimumFocusDistance > 0f

        // Exposure compensation

        val compensationRange =
            characteristics.get(
                CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE
            )

        val compensationStep =
            characteristics.get(
                CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP
            )

        val compensationSupported =
            compensationRange != null &&
                    compensationStep != null &&
                    compensationStep.toFloat() > 0f

        // Debug information

        Log.d("CameraCapabilities", "Camera ID: $cameraId")
        Log.d("CameraCapabilities", "Hardware: $hardwareLevel")
        Log.d("CameraCapabilities", "Manual Sensor: $manualSensorSupported")
        Log.d("CameraCapabilities", "ISO Range: $isoRange")
        Log.d("CameraCapabilities", "Exposure Range: $exposureRange")
        Log.d("CameraCapabilities", "ISO Supported: $supportsManualIso")
        Log.d("CameraCapabilities", "Exposure Supported: $supportsManualExposure")
        Log.d(
            "CameraCapabilities",
            "Available capabilities: ${availableCapabilities.contentToString()}"
        )

        return CameraCapabilities(

            cameraId = cameraId,

            hardwareLevel = hardwareLevel,

            manualSensor = manualSensorSupported,

            autoFocus = autofocusSupported,

            manualFocus = manualFocusSupported,

            isoRange = isoRange,

            exposureRange = exposureRange,

            minimumFocusDistance = minimumFocusDistance,

            autofocusModes = autofocusModes,

            exposureCompensationRange = compensationRange?.let {
                it.lower..it.upper
            },

            exposureCompensationStep =
                compensationStep?.toFloat() ?: 0f,

            supportsExposureCompensation =
                compensationSupported,

            supportsManualSensor =
                manualSensorSupported,

            supportsManualFocus =
                manualFocusSupported
        )
    }


}