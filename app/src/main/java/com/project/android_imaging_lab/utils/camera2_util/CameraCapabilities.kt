package com.project.android_imaging_lab.utils.camera2_util

data class CameraCapabilities(
    val cameraId: String = "",
    val hardwareLevel: String = "",
    val manualSensor: String = "Unknown",
    val autoFocus: String = "Unknown",
    val manualFocus: String = "Unknown",
    val isoRange: String = "Unavailable",
    val exposureRange: String = "Unavailable",
    val minimumFocusDistance: String = "Unavailable"
)
