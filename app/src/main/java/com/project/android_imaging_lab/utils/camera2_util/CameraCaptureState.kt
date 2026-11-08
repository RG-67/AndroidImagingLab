package com.project.android_imaging_lab.utils.camera2_util

data class CameraCaptureState(
    val actualIso: Int? = null,

    val actualExposureTime: Long? = null,

    val actualFocusDistance: Float? = null,

    val afState: Int? = null,

    val aeState: Int? = null
)
