package com.project.android_imaging_lab.utils.camera2_util

data class CameraCapabilities(

    val cameraId: String = "",

    val hardwareLevel: String = "Unknown",

    val manualSensor: Boolean = false,

    val autoFocus: Boolean = false,

    val manualFocus: Boolean = false,

    val isoRange: IntRange? = null,

    val exposureRange: LongRange? = null,

    val minimumFocusDistance: Float = 0f,

    val autofocusModes: List<Int> = emptyList(),

    val exposureCompensationRange: IntRange? = null,

    val exposureCompensationStep: Float = 0f,

    val supportsExposureCompensation: Boolean = false,

    val supportsManualSensor: Boolean = false,

    val supportsManualFocus: Boolean = false
)