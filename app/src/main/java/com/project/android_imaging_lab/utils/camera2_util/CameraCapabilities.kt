package com.project.android_imaging_lab.utils.camera2_util

data class CameraCapabilities(

    // Camera identification

    val cameraId: String = "",

    val lensFacing: String = "Unknown",

    val hardwareLevel: String = "Unknown",

    // Sensor information

    val sensorResolution: String = "Unavailable",

    val physicalSensorSize: String = "Unavailable",

    val sensorOrientation: Int? = null,

    // Manual sensor capabilities

    val supportsManualSensor: Boolean = false,

    val isoRange: IntRange? = null,

    val exposureRange: LongRange? = null,

    // Focus capabilities

    val supportsManualFocus: Boolean = false,

    val minimumFocusDistance: Float = 0f,

    val autoFocus: Boolean = false,

    val autofocusModes: List<Int> = emptyList(),

    // Camera modes

    val aeModes: List<Int> = emptyList(),

    val awbModes: List<Int> = emptyList(),

    // Exposure compensation

    val exposureCompensationRange: IntRange? = null,

    val exposureCompensationStep: Float = 0f,

    val supportsExposureCompensation: Boolean = false,

    // Advanced capabilities

    val supportsRaw: Boolean = false,

    val supportsManualPostProcessing: Boolean = false,

    // Original Camera2 capability values

    val availableCapabilities: List<Int> = emptyList()
) {

    val manualSensor: Boolean
        get() = supportsManualSensor

    val supportsIso: Boolean
        get() = supportsManualSensor && isoRange != null

    val supportsManualExposure: Boolean
        get() = supportsManualSensor && exposureRange != null
}