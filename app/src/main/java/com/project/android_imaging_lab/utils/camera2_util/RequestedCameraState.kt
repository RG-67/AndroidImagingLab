package com.project.android_imaging_lab.utils.camera2_util

data class RequestedCameraState(
    val requestedIso: Int? = null,

    val requestedExposureTime: Long? = null,

    val requestedFocusDistance: Float? = null,

    val requestedAfMode: Int? = null,

    val requestedAeMode: Int? = null
)
