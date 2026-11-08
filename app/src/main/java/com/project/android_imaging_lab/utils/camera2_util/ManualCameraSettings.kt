package com.project.android_imaging_lab.utils.camera2_util

import android.hardware.camera2.CaptureRequest

data class ManualCameraSettings(
    val autofocusMode: Int =
        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE,

    val manualFocusDistance: Float? = null,

    val iso: Int? = null,

    val exposureTime: Long? = null,

    val exposureCompensation: Int = 0
)
