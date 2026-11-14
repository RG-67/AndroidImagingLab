package com.project.android_imaging_lab.utils.camera2_util

data class CameraCaptureState(

    // Actual sensor values

    val actualIso: Int? = null,

    val actualExposureTime: Long? = null,

    val actualFocusDistance: Float? = null,

    // Camera states

    val afState: Int? = null,

    val aeState: Int? = null,

    val awbState: Int? = null,

    // Active camera modes

    val afMode: Int? = null,

    val aeMode: Int? = null,

    // Frame metadata

    val frameNumber: Long? = null,

    val sensorTimestamp: Long? = null,

    val frameDuration: Long? = null,

    val frameInterval: Long? = null,

    val estimatedFps: Float? = null

)