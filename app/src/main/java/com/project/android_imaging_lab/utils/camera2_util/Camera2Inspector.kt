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

        /*
         * HARDWARE LEVEL
         */

        val hardwareValue = characteristics.get(
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL
        )

        val hardwareLevel = when (hardwareValue) {

            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY ->
                "LEGACY"

            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED ->
                "LIMITED"

            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL ->
                "FULL"

            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 ->
                "LEVEL_3"

            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL ->
                "EXTERNAL"

            else -> "UNKNOWN"
        }

        /*
         * LENS FACING
         */

        val lensFacingValue = characteristics.get(
            CameraCharacteristics.LENS_FACING
        )

        val lensFacing = when (lensFacingValue) {

            CameraCharacteristics.LENS_FACING_BACK ->
                "Back"

            CameraCharacteristics.LENS_FACING_FRONT ->
                "Front"

            CameraCharacteristics.LENS_FACING_EXTERNAL ->
                "External"

            else -> "Unknown"
        }

        /*
         * SENSOR RESOLUTION
         */

        val pixelArraySize = characteristics.get(
            CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE
        )

        val sensorResolution = pixelArraySize?.let {

            "${it.width} × ${it.height}"

        } ?: "Unavailable"

        /*
         * PHYSICAL SENSOR SIZE
         */

        val physicalSize = characteristics.get(
            CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE
        )

        val physicalSensorSize = physicalSize?.let {

            "${"%.2f".format(it.width)} × " +
                    "${"%.2f".format(it.height)} mm"

        } ?: "Unavailable"

        /*
         * SENSOR ORIENTATION
         */

        val sensorOrientation = characteristics.get(
            CameraCharacteristics.SENSOR_ORIENTATION
        )

        /*
         * AVAILABLE CAPABILITIES
         */

        val availableCapabilities = characteristics.get(
            CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES
        )?.toList() ?: emptyList()

        /*
         * MANUAL SENSOR SUPPORT
         */

        val manualSensorSupported =
            CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR in availableCapabilities

        /*
         * RAW SUPPORT
         */

        val rawSupported =
            CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW in availableCapabilities

        /*
         * MANUAL POST PROCESSING
         */

        val manualPostProcessingSupported =
            CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING in availableCapabilities

        /*
         * ISO RANGE
         */

        val sensitivityRange = characteristics.get(
            CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE
        )

        val isoRange = sensitivityRange?.let {
            it.lower..it.upper
        }

        /*
         * EXPOSURE RANGE
         */

        val exposureRangeValue = characteristics.get(
            CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE
        )

        val exposureRange = exposureRangeValue?.let {
            it.lower..it.upper
        }

        /*
         * FRAME DURATION RANGE
         */

        val frameDurationValue =
            characteristics.get(CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION)

        /*
        * MAXIMUM FRAME DURATION
        */

        val maximumFrameDuration = characteristics.get(
            CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION
        )

        /*
         * MINIMUM FOCUS DISTANCE
         */

        val minimumFocusDistance = characteristics.get(
            CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE
        ) ?: 0f

        val manualFocusSupported =
            minimumFocusDistance > 0f

        /*
         * AUTOFOCUS MODES
         */

        val autofocusModes = characteristics.get(
            CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES
        )?.toList() ?: emptyList()

        val autofocusSupported = autofocusModes.any {

            it != CaptureRequest.CONTROL_AF_MODE_OFF
        }

        /*
         * AUTO WHITE BALANCE MODES
         */

        val awbModes = characteristics.get(
            CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES
        )?.toList() ?: emptyList()

        /*
         * AUTO EXPOSURE MODES
         */

        val aeModes = characteristics.get(
            CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES
        )?.toList() ?: emptyList()

        /*
         * EXPOSURE COMPENSATION
         */

        val compensationRange = characteristics.get(
            CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE
        )

        val compensationStep = characteristics.get(
            CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP
        )

        val compensationSupported =
            compensationRange != null &&
                    compensationStep != null &&
                    compensationStep.toFloat() > 0f

        /*
         * DEBUG INFORMATION
         */

        Log.d("Camera2Inspector", "Camera ID: $cameraId")

        Log.d("Camera2Inspector", "Lens Facing: $lensFacing")

        Log.d("Camera2Inspector", "Hardware Level: $hardwareLevel")

        Log.d("Camera2Inspector", "Sensor Resolution: $sensorResolution")

        Log.d("Camera2Inspector", "Physical Sensor Size: $physicalSensorSize")

        Log.d("Camera2Inspector", "ISO Range: $isoRange")

        Log.d("Camera2Inspector", "Exposure Range: $exposureRange")

        Log.d("Camera2Inspector", "Frame Duration: $frameDurationValue")

        Log.d("Camera2Inspector", "RAW Supported: $rawSupported")

        Log.d(
            "Camera2Inspector",
            "Manual Sensor Supported: $manualSensorSupported"
        )

        Log.d(
            "Camera2Inspector",
            "Manual Post Processing: $manualPostProcessingSupported"
        )

        Log.d(
            "Camera2Inspector",
            "Available Capabilities: ${availableCapabilities}"
        )

        /*
         * RETURN CAMERA CAPABILITIES
         */

        return CameraCapabilities(

            cameraId = cameraId,

            lensFacing = lensFacing,

            hardwareLevel = hardwareLevel,

            sensorResolution = sensorResolution,

            physicalSensorSize = physicalSensorSize,

            sensorOrientation = sensorOrientation,

            supportsManualSensor = manualSensorSupported,

            isoRange = isoRange,

            exposureRange = exposureRange,

            supportsManualFocus = manualFocusSupported,

            minimumFocusDistance = minimumFocusDistance,

            autoFocus = autofocusSupported,

            autofocusModes = autofocusModes,

            aeModes = aeModes,

            awbModes = awbModes,

            exposureCompensationRange = compensationRange?.let {
                it.lower..it.upper
            },

            exposureCompensationStep =
                compensationStep?.toFloat() ?: 0f,

            supportsExposureCompensation =
                compensationSupported,

            supportsRaw = rawSupported,

            supportsManualPostProcessing =
                manualPostProcessingSupported,

            availableCapabilities = availableCapabilities
        )



    }


}