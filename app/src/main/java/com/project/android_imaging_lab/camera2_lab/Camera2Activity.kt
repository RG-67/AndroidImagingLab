package com.project.android_imaging_lab.camera2_lab

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.project.android_imaging_lab.ui.theme.Android_Imaging_LabTheme
import com.project.android_imaging_lab.utils.camera2_util.Camera2Inspector
import com.project.android_imaging_lab.utils.camera2_util.Camera2Preview
import com.project.android_imaging_lab.utils.camera2_util.Camera2PreviewController
import com.project.android_imaging_lab.utils.camera2_util.CameraCapabilities
import com.project.android_imaging_lab.utils.camera2_util.CameraOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

class Camera2Activity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            Android_Imaging_LabTheme {
                Camera2Screen()
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Camera2Screen(
        modifier: Modifier = Modifier
    ) {

        val context = LocalContext.current

        val coroutineScope = rememberCoroutineScope()

        val inspector = remember {
            Camera2Inspector(context)
        }

        var cameraOptions by remember {
            mutableStateOf<List<CameraOption>>(emptyList())
        }

        var selectedCameraId by remember {
            mutableStateOf<String?>(null)
        }

        var capabilities by remember {
            mutableStateOf(CameraCapabilities())
        }

        var isLoading by remember {
            mutableStateOf(false)
        }

        var expandedCamera by remember {
            mutableStateOf(false)
        }

        var expandedFocus by remember {
            mutableStateOf(false)
        }

        var hasCameraPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        var previewEnabled by remember {
            mutableStateOf(true)
        }

        var isCapturing by remember {
            mutableStateOf(false)
        }

        var captureMessage by remember {
            mutableStateOf<String?>(null)
        }

        var capturedPhoto by remember {
            mutableStateOf<File?>(null)
        }

        val previewController = remember(selectedCameraId) {

            selectedCameraId?.let { id ->

                Camera2PreviewController(
                    context,
                    id
                )
            }
        }

        val permissionLauncher =
            rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->

                hasCameraPermission = granted
            }

        /*
         * LOAD CAMERAS
         */

        LaunchedEffect(hasCameraPermission) {

            if (!hasCameraPermission) return@LaunchedEffect

            try {

                isLoading = true

                val cameras = withContext(Dispatchers.IO) {

                    inspector.getAvailableCameras()
                }

                cameraOptions = cameras

                val initialCamera = cameras.firstOrNull {
                    it.name.startsWith("Back Camera")
                } ?: cameras.firstOrNull()

                selectedCameraId = initialCamera?.id

            } catch (e: Exception) {

                Log.e("Camera2Screen", "Camera discovery failed", e)

            } finally {

                isLoading = false
            }
        }

        /*
         * LOAD SELECTED CAMERA CAPABILITIES
         */

        LaunchedEffect(selectedCameraId) {

            val id = selectedCameraId ?: return@LaunchedEffect

            isLoading = true

            try {

                capabilities = withContext(Dispatchers.IO) {

                    inspector.getCapabilities(id)
                }

            } catch (e: Exception) {

                Log.e("Camera2Screen", "Capability loading failed", e)

            } finally {

                isLoading = false
            }
        }

        /*
         * RELEASE CONTROLLER
         */

        DisposableEffect(previewController) {

            onDispose {

                previewController?.release()
            }
        }

        /*
         * MAIN UI
         */

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF101114))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // HEADER

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),

                horizontalArrangement = Arrangement.SpaceBetween,

                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Camera2 Lab",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "MANUAL MODE",
                    color = Color(0xFF80CBC4),
                    fontSize = 12.sp
                )
            }

            DividerLine()

            Spacer(Modifier.height(16.dp))

            // LIVE PREVIEW

            SectionTitle("LIVE PREVIEW")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color.Black),

                contentAlignment = Alignment.Center
            ) {

                if (hasCameraPermission &&
                    previewEnabled &&
                    previewController != null
                ) {

                    Camera2Preview(
                        controller = previewController,
                        modifier = Modifier.fillMaxSize()
                    )

                } else {

                    Text(
                        text = "CAMERA NOT OPEN",
                        color = Color.White
                    )
                }

                Text(
                    text = "LIVE",
                    color = Color.Green,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // CAMERA SELECTION

            SectionTitle("CAMERA")

            ExposedDropdownMenuBox(
                expanded = expandedCamera,
                onExpandedChange = {
                    expandedCamera = !expandedCamera
                }
            ) {

                OutlinedTextField(
                    value = cameraOptions.firstOrNull {
                        it.id == selectedCameraId
                    }?.name ?: "Select camera",

                    onValueChange = {},

                    readOnly = true,

                    singleLine = true,

                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 15.sp
                    ),

                    label = {
                        Text(
                            "Select Camera",
                            color = Color.LightGray
                        )
                    },

                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expandedCamera
                        )
                    },

                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF80CBC4),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF80CBC4),
                        unfocusedLabelColor = Color.LightGray,
                        focusedTrailingIconColor = Color.White,
                        unfocusedTrailingIconColor = Color.White
                    ),

                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            ExposedDropdownMenuAnchorType.PrimaryNotEditable
                        )
                )


                ExposedDropdownMenu(
                    expanded = expandedCamera,
                    onDismissRequest = {
                        expandedCamera = false
                    }
                ) {

                    cameraOptions.forEach { camera ->

                        DropdownMenuItem(

                            text = {
                                Text(camera.name, color = Color.Black)
                            },

                            onClick = {
                                selectedCameraId = camera.id
                                expandedCamera = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = "Preview status",
                    color = Color.LightGray
                )

                Text(
                    text = if (previewController != null &&
                        previewController.surfaceRequest != null
                    ) {
                        "Preview running"
                    } else {
                        "Initializing"
                    },
                    color = Color(0xFF80CBC4)
                )
            }

            DividerLine()

            /*
             * FOCUS CONTROLS
             */

            Spacer(Modifier.height(20.dp))

            SectionTitle("FOCUS")

            val controller = previewController

            val autofocusNames = mapOf(

                CaptureRequest.CONTROL_AF_MODE_OFF to "Manual Focus",

                CaptureRequest.CONTROL_AF_MODE_AUTO to "Auto",

                CaptureRequest.CONTROL_AF_MODE_MACRO to "Macro",

                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO to
                        "Continuous Video",

                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE to
                        "Continuous Picture",

                CaptureRequest.CONTROL_AF_MODE_EDOF to "EDOF"
            )

            val availableFocusModes = capabilities.autofocusModes

            val selectedFocusMode =
                controller?.cameraSettings?.autofocusMode
                    ?: CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE

            ExposedDropdownMenuBox(
                expanded = expandedFocus,
                onExpandedChange = {
                    expandedFocus = !expandedFocus
                }
            ) {

                OutlinedTextField(
                    value = autofocusNames[selectedFocusMode] ?: "Unknown",

                    onValueChange = {},

                    readOnly = true,

                    singleLine = true,

                    enabled = availableFocusModes.isNotEmpty(),

                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 15.sp
                    ),

                    label = {
                        Text(
                            "Autofocus Mode",
                            color = Color.LightGray
                        )
                    },

                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expandedFocus
                        )
                    },

                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.Gray,
                        focusedBorderColor = Color(0xFF80CBC4),
                        unfocusedBorderColor = Color.Gray,
                        disabledBorderColor = Color.DarkGray,
                        focusedLabelColor = Color(0xFF80CBC4),
                        unfocusedLabelColor = Color.LightGray,
                        focusedTrailingIconColor = Color.White,
                        unfocusedTrailingIconColor = Color.White
                    ),

                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            ExposedDropdownMenuAnchorType.PrimaryNotEditable
                        )
                )

                ExposedDropdownMenu(
                    expanded = expandedFocus,
                    onDismissRequest = {
                        expandedFocus = false
                    }
                ) {

                    availableFocusModes.forEach { mode ->

                        DropdownMenuItem(

                            text = {
                                Text(
                                    autofocusNames[mode] ?: "Unknown",
                                    color = Color.Black
                                )
                            },

                            onClick = {
                                controller?.setAutofocusMode(mode)
                                expandedFocus = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // MANUAL FOCUS DISTANCE

            val manualFocusSupported =
                capabilities.supportsManualFocus

            var focusDistance by remember(selectedCameraId) {
                mutableFloatStateOf(0f)
            }

            Text(
                text = if (manualFocusSupported) {
                    "Focus Distance: ${"%.2f".format(focusDistance)} D"
                } else {
                    "Manual focus not supported"
                },
                color = Color.White
            )

            Slider(

                value = focusDistance,

                onValueChange = {
                    focusDistance = it
                },

                onValueChangeFinished = {
                    controller?.setManualFocusDistance(focusDistance)
                },

                valueRange = 0f..capabilities.minimumFocusDistance
                    .coerceAtLeast(0.01f),

                enabled = manualFocusSupported
            )

            DividerLine()

            /*
             * ISO CONTROLS
             */

            Spacer(Modifier.height(20.dp))

            SectionTitle("ISO")

            val isoRange = capabilities.isoRange

            val isoOptions = remember(isoRange) {

                val presets = listOf(
                    100,
                    200,
                    400,
                    800,
                    1600,
                    3200,
                    6400
                )

                presets.filter { value ->

                    isoRange != null &&
                            value in isoRange.first..isoRange.last
                }
            }

            Text(
                text = "Current ISO: ${
                    controller?.cameraSettings?.iso ?: "Auto"
                }",
                color = Color.White
            )

            Row(
                modifier = Modifier.horizontalScroll(
                    rememberScrollState()
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                FilterChip(

                    selected = controller?.cameraSettings?.iso == null,

                    onClick = {
                        controller?.setIso(null)
                    },

                    label = {
                        Text("AUTO")
                    },

                    enabled = capabilities.isoRange != null &&
                            capabilities.supportsManualSensor
                )

                isoOptions.forEach { iso ->

                    FilterChip(

                        selected =
                            controller?.cameraSettings?.iso == iso,

                        onClick = {
                            controller?.setIso(iso)
                        },

                        label = {
                            Text(iso.toString())
                        },

                        enabled = capabilities.isoRange != null &&
                                capabilities.supportsManualSensor
                    )
                }
            }

            if (!capabilities.supportsManualSensor) {

                UnsupportedMessage(
                    "Manual ISO is not supported by this camera."
                )
            }

            DividerLine()

            /*
             * SHUTTER SPEED
             */

            Spacer(Modifier.height(20.dp))

            SectionTitle("SHUTTER SPEED")

            val shutterOptions = listOf(
                1_000_000L,
                2_000_000L,
                4_000_000L,
                8_000_000L,
                10_000_000L,
                20_000_000L,
                40_000_000L,
                100_000_000L
            )

            val exposureRange = capabilities.exposureRange

            val supportedShutters = shutterOptions.filter { value ->

                exposureRange != null &&
                        value in exposureRange.first..exposureRange.last
            }

            Text(
                text = "Current Shutter: ${
                    controller?.cameraSettings?.exposureTime?.let {
                        formatShutterSpeed(it)
                    } ?: "Auto"
                }",
                color = Color.White
            )

            Column {

                Row(
                    modifier = Modifier.horizontalScroll(
                        rememberScrollState()
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    FilterChip(

                        selected =
                            controller?.cameraSettings?.exposureTime == null,

                        onClick = {
                            controller?.setExposureTime(null)
                        },

                        label = {
                            Text("AUTO")
                        },

                        enabled = capabilities.supportsManualSensor
                    )

                    supportedShutters.forEach { exposure ->

                        FilterChip(

                            selected =
                                controller?.cameraSettings?.exposureTime == exposure,

                            onClick = {
                                controller?.setExposureTime(exposure)
                            },

                            label = {
                                Text(formatShutterSpeed(exposure))
                            },

                            enabled = capabilities.supportsManualSensor
                        )
                    }
                }
            }

            if (!capabilities.supportsManualSensor) {

                UnsupportedMessage(
                    "Manual shutter speed is not supported."
                )
            }

            DividerLine()

            /*
             * EXPOSURE COMPENSATION
             */

            Spacer(Modifier.height(20.dp))

            SectionTitle("EXPOSURE COMPENSATION")

            val compensationRange =
                capabilities.exposureCompensationRange

            val manualExposureActive =
                controller?.cameraSettings?.iso != null ||
                        controller?.cameraSettings?.exposureTime != null

            val compensationEnabled =
                capabilities.supportsExposureCompensation &&
                        !manualExposureActive

            val compensationValue =
                controller?.cameraSettings?.exposureCompensation ?: 0

            val step = capabilities.exposureCompensationStep

            val compensationEv = compensationValue * step

            Text(
                text = "Exposure: ${
                    "%+.1f".format(compensationEv)
                } EV",
                color = Color.White
            )

            if (compensationRange != null) {

                Slider(

                    value = compensationValue.toFloat(),

                    onValueChange = {

                        controller?.setExposureCompensation(
                            it.roundToInt()
                        )
                    },

                    valueRange = compensationRange.first.toFloat()..
                            compensationRange.last.toFloat(),

                    enabled = compensationEnabled
                )
            }

            if (manualExposureActive) {

                UnsupportedMessage(
                    "Exposure compensation is unavailable during manual exposure."
                )
            }

            DividerLine()

            /*
             * CAPTURE PHOTO
             */

            Spacer(Modifier.height(24.dp))

            Button(

                onClick = {

                    if (controller != null && !isCapturing) {

                        isCapturing = true

                        captureMessage = null

                        controller.capturePhoto(

                            onSuccess = { file ->

                                isCapturing = false

                                capturedPhoto = file

                                captureMessage =
                                    "Photo saved: ${file.name}"
                            },

                            onError = { message ->

                                isCapturing = false

                                captureMessage = message
                            }
                        )
                    }
                },

                enabled = controller != null &&
                        hasCameraPermission &&
                        !isCapturing,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {

                Text(
                    text = if (isCapturing) {
                        "CAPTURING..."
                    } else {
                        "CAPTURE PHOTO"
                    }
                )
            }

            captureMessage?.let {

                Text(
                    text = it,
                    color = Color(0xFF80CBC4),
                    modifier = Modifier.padding(12.dp)
                )
            }

            DividerLine()

            /*
             * ACTUAL CAMERA STATUS
             */

            Spacer(Modifier.height(20.dp))

            SectionTitle("ACTUAL CAMERA VALUES")

            val actualState = controller?.captureState

            InformationRow(
                "Actual ISO:",
                actualState?.actualIso?.toString() ?: "Waiting..."
            )

            InformationRow(
                "Actual Exposure:",
                actualState?.actualExposureTime?.let {
                    formatShutterSpeed(it)
                } ?: "Waiting..."
            )

            InformationRow(
                "Focus Distance:",
                actualState?.actualFocusDistance?.let {
                    "%.2f D".format(it)
                } ?: "Unavailable"
            )

            InformationRow(
                "AF State:",
                actualState?.afState?.let {
                    formatAfState(it)
                } ?: "Unavailable"
            )

            InformationRow(
                "AE State:",
                actualState?.aeState?.let {
                    formatAeState(it)
                } ?: "Unavailable"
            )

            DividerLine()

            /*
             * CAMERA CAPABILITIES
             */

            Spacer(Modifier.height(20.dp))

            SectionTitle("CAMERA CAPABILITIES")

            InformationRow(
                "Hardware Level:",
                capabilities.hardwareLevel
            )

            InformationRow(
                "Manual Sensor:",
                if (capabilities.supportsManualSensor) {
                    "Supported"
                } else {
                    "Not supported"
                }
            )

            InformationRow(
                "Autofocus:",
                if (capabilities.autoFocus) {
                    "Supported"
                } else {
                    "Not supported"
                }
            )

            InformationRow(
                "Manual Focus:",
                if (capabilities.supportsManualFocus) {
                    "Supported"
                } else {
                    "Not supported"
                }
            )

            InformationRow(
                "ISO Range:",
                capabilities.isoRange?.let {
                    "${it.first} - ${it.last}"
                } ?: "Unavailable"
            )

            InformationRow(
                "Exposure Range:",
                capabilities.exposureRange?.let {
                    "${formatShutterSpeed(it.first)} - " +
                            "${formatShutterSpeed(it.last)}"
                } ?: "Unavailable"
            )

            InformationRow(
                "Focus Distance:",
                "${capabilities.minimumFocusDistance} D"
            )

            Spacer(Modifier.height(20.dp))

            Button(

                onClick = {

                    val id = selectedCameraId ?: return@Button

                    coroutineScope.launch {

                        isLoading = true

                        try {

                            capabilities = withContext(Dispatchers.IO) {

                                inspector.getCapabilities(id)
                            }

                        } finally {

                            isLoading = false
                        }
                    }
                },

                enabled = selectedCameraId != null && !isLoading,

                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    if (isLoading) {
                        "Refreshing..."
                    } else {
                        "REFRESH CAPABILITIES"
                    }
                )
            }

            if (isLoading) {

                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
            }

            if (!hasCameraPermission) {

                Button(
                    onClick = {
                        permissionLauncher.launch(
                            Manifest.permission.CAMERA
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text("Grant Camera Permission")
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }

}

/*
 * REUSABLE UI COMPONENTS
 */

@Composable
private fun DividerLine() {

    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White)
    )
}

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {

    Text(
        text = text,
        fontSize = 16.sp,
        textAlign = TextAlign.Start,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(bottom = 5.dp)
    )
}

@Composable
private fun InformationItem(
    title: String,
    value: String
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 15.dp,
                bottom = 12.dp
            )
    ) {

        Text(
            text = title,
            fontSize = 15.sp,
            color = Color.White,
            fontWeight = FontWeight.Normal
        )

        Text(
            text = value,
            fontSize = 15.sp,
            color = Color.White,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
}

@Composable
fun InformationRow(
    title: String,
    value: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),

        horizontalArrangement = Arrangement.SpaceBetween,

        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = title,
            color = Color.LightGray,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun UnsupportedMessage(message: String) {

    Text(
        text = message,
        color = Color(0xFFFFAB91),
        fontSize = 12.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

fun formatShutterSpeed(exposureTime: Long): String {

    val seconds = exposureTime / 1_000_000_000.0

    return when {

        seconds >= 1.0 -> {

            val formatted = "%.1f".format(seconds)
                .removeSuffix(".0")

            "${formatted}s"
        }

        seconds > 0.0 -> {

            val denominator = (1.0 / seconds).roundToInt()

            "1/$denominator"
        }

        else -> "Unknown"
    }
}

fun formatAfState(state: Int): String {
    return when (state) {
        CaptureResult.CONTROL_AF_STATE_INACTIVE ->
            "Inactive"

        CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN ->
            "Passive Scanning"

        CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED ->
            "Focused"

        CaptureResult.CONTROL_AF_STATE_ACTIVE_SCAN ->
            "Scanning"

        CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ->
            "Focused Locked"

        CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED ->
            "Not Focused"

        CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED ->
            "Unfocused"

        else -> "Unknown"
    }
}

fun formatAeState(state: Int): String {
    return when (state) {
        CaptureResult.CONTROL_AE_STATE_INACTIVE ->
            "Inactive"

        CaptureResult.CONTROL_AE_STATE_SEARCHING ->
            "Searching"

        CaptureResult.CONTROL_AE_STATE_CONVERGED ->
            "Converged"

        CaptureResult.CONTROL_AE_STATE_LOCKED ->
            "Locked"

        CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED ->
            "Flash Required"

        CaptureResult.CONTROL_AE_STATE_PRECAPTURE ->
            "Precapture"

        else -> "Unknown"
    }
}