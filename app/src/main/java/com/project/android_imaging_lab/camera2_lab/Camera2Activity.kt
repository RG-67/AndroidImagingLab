package com.project.android_imaging_lab.camera2_lab

import android.Manifest
import android.R
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.project.android_imaging_lab.ui.theme.Android_Imaging_LabTheme
import com.project.android_imaging_lab.utils.camera2_util.Camera2Inspector
import com.project.android_imaging_lab.utils.camera2_util.CameraCapabilities
import com.project.android_imaging_lab.utils.camera2_util.CameraOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

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

        /* CAMERA SELECTION */

        var expanded by remember {
            mutableStateOf(false)
        }

        var cameraOptions by remember {
            mutableStateOf<List<CameraOption>>(emptyList())
        }

        var selectedCameraId by remember {
            mutableStateOf<String?>(null)
        }

        /* CAMERA CAPABILITIES */

        var cameraCapabilities by remember {
            mutableStateOf(CameraCapabilities())
        }

        /* STAGE 2.2 — PREVIEW STATE */

        var isPreviewRequested by remember { mutableStateOf(false) }
        var isPreviewRunning by remember { mutableStateOf(false) }
        var previewError by remember { mutableStateOf<String?>(null) }

        /* UI STATE */

        var errorMessage by remember {
            mutableStateOf<String?>(null)
        }

        var isLoading by remember {
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

        /*
         * CAMERA PERMISSION
         */

        val permissionLauncher =
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { granted ->

                hasCameraPermission = granted

                if (granted) {
                    errorMessage = null
                } else {
                    errorMessage =
                        "Camera permission is required."
                }
            }

        /*
         * LOAD CAPABILITIES
         *
         * Camera characteristics are retrieved on the IO dispatcher.
         */

        suspend fun loadCapabilities(cameraId: String) {

            isLoading = true
            errorMessage = null

            try {

                val capabilities = withContext(Dispatchers.IO) {
                    inspector.getCapabilities(cameraId)
                }

                cameraCapabilities = capabilities

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                errorMessage =
                    e.message ?: "Unable to retrieve camera capabilities."

            } finally {

                isLoading = false
            }
        }

        /*
         * REQUEST CAMERA PERMISSION
         */

        LaunchedEffect(Unit) {

            if (!hasCameraPermission) {
                permissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }

        /*
         * DISCOVER CAMERAS
         */

        LaunchedEffect(hasCameraPermission) {

            if (!hasCameraPermission) {
                return@LaunchedEffect
            }

            isLoading = true
            errorMessage = null

            try {

                val cameras = withContext(Dispatchers.IO) {
                    inspector.getAvailableCameras()
                }

                cameraOptions = cameras

                val initialCamera = cameras.firstOrNull {
                    it.name.startsWith("Back Camera")
                } ?: cameras.firstOrNull()

                selectedCameraId = initialCamera?.id

                if (initialCamera != null) {

                    loadCapabilities(initialCamera.id)

                } else {

                    cameraCapabilities = CameraCapabilities()

                    errorMessage = "No cameras available."
                }

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                errorMessage =
                    e.message ?: "Unable to discover cameras."

            } finally {

                isLoading = false
            }
        }

        /*
         * MAIN SCREEN
         */

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {

            /*
             * APP TITLE
             */

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "Camera2 Lab",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold
                )
            }

            DividerLine()

            /*
             * CAMERA SELECTION
             */

            SectionTitle(
                text = "CAMERA SELECTION",
                modifier = Modifier.padding(top = 30.dp)
            )

            Text(
                text = "Select Camera",
                fontSize = 15.sp,
                color = Color.White,
                modifier = Modifier.padding(top = 20.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    if (cameraOptions.isNotEmpty()) {
                        expanded = !expanded
                    }
                },
                modifier = Modifier.padding(
                    top = 10.dp,
                    bottom = 20.dp
                )
            ) {

                TextField(
                    value = cameraOptions.firstOrNull {
                        it.id == selectedCameraId
                    }?.name ?: "No camera selected",

                    onValueChange = {},

                    readOnly = true,

                    singleLine = true,

                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expanded
                        )
                    },

                    colors =
                        ExposedDropdownMenuDefaults.textFieldColors(),

                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            ExposedDropdownMenuAnchorType.PrimaryNotEditable
                        )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    }
                ) {

                    cameraOptions.forEach { camera ->

                        DropdownMenuItem(
                            text = {
                                Text(camera.name)
                            },

                            onClick = {

                                expanded = false

                                selectedCameraId = camera.id

                                coroutineScope.launch {
                                    loadCapabilities(camera.id)
                                }
                            },

                            contentPadding =
                                ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            /*
             * LOADING INDICATOR
             */

            if (isLoading) {

                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 12.dp),
                    color = Color.White
                )
            }

            /*
             * ERROR MESSAGE
             */

            errorMessage?.let { message ->

                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            DividerLine()

            /*
            * STAGE 2.2 — CAMERA PREVIEW
            */

            SectionTitle("CAMERA PREVIEW", modifier = Modifier.padding(top = 25.dp))

            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(top = 15.dp)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                if (isPreviewRunning) {
                    // TODO
                    // Display existing Camera2Preview
                } else {
                    Text(
                        text = if (isPreviewRequested) {
                            "OPENING CAMERA....."
                        } else {
                            "CAMERA NOT OPEN"
                        },
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }

            }

            Text(
                text = "[AUTO]",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.End,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp)
            )

            DividerLine()


            /*
             * CAMERA CAPABILITIES
             */

            SectionTitle(
                text = "CAMERA CAPABILITIES",
                modifier = Modifier.padding(top = 30.dp)
            )

            /*
             * HARDWARE LEVEL
             */

            InformationItem(
                title = "Hardware Level",
                value = cameraCapabilities.hardwareLevel
            )

            DividerLine()

            /*
             * MANUAL SENSOR
             */

            InformationItem(
                title = "Manual Sensor",
                value = cameraCapabilities.manualSensor
            )

            DividerLine()

            /*
             * AUTOFOCUS
             */

            InformationItem(
                title = "Autofocus",
                value = cameraCapabilities.autoFocus
            )

            DividerLine()

            /*
             * MANUAL FOCUS
             */

            InformationItem(
                title = "Manual Focus",
                value = cameraCapabilities.manualFocus
            )

            DividerLine()

            /*
             * CAMERA INFORMATION
             */

            SectionTitle(
                text = "CAMERA INFORMATION",
                modifier = Modifier.padding(top = 30.dp)
            )

            /*
             * CAMERA ID
             */

            InformationRow(
                title = "Camera ID:",
                value = cameraCapabilities.cameraId
            )

            /*
             * ISO RANGE
             */

            InformationRow(
                title = "ISO Range:",
                value = cameraCapabilities.isoRange
            )

            /*
             * EXPOSURE RANGE
             */

            InformationRow(
                title = "Exposure Range:",
                value = cameraCapabilities.exposureRange
            )

            /*
             * MINIMUM FOCUS DISTANCE
             */

            InformationRow(
                title = "Minimum Focus Distance:",
                value = cameraCapabilities.minimumFocusDistance
            )

            /*
             * REFRESH CAPABILITIES
             */

            Row(
                modifier = modifier.fillMaxWidth()
            ) {

                Button(
                    onClick = {

                        val cameraId = selectedCameraId

                        if (
                            cameraId != null &&
                            hasCameraPermission
                        ) {

                            coroutineScope.launch {
                                loadCapabilities(cameraId)
                            }
                        }
                    },

                    enabled = selectedCameraId != null &&
                            hasCameraPermission &&
                            !isLoading,

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.Black
                    ),

                    modifier = Modifier
                        .padding(top = 30.dp)
                        .height(55.dp)
                        .weight(weight = 1f)
                ) {

                    Text(
                        text = if (isLoading) {
                            "Refreshing..."
                        } else {
                            "Refresh Capabilities"
                        }
                    )
                }

                Spacer(modifier = modifier.padding(horizontal = 5.dp))

                Button(
                    onClick = {

                    },

                    enabled = selectedCameraId != null &&
                            hasCameraPermission &&
                            !isLoading,

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.Black
                    ),

                    modifier = Modifier
                        .padding(top = 30.dp)
                        .height(55.dp)
                        .weight(weight = 1f)
                ) {

                    Text(
                        text = "Capture Photo"
                    )
                }

            }

            /*
             * PERMISSION RETRY
             */

            if (!hasCameraPermission) {

                Button(
                    onClick = {
                        permissionLauncher.launch(
                            Manifest.permission.CAMERA
                        )
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {

                    Text("Grant Camera Permission")
                }
            }

            Spacer(
                modifier = Modifier.height(30.dp)
            )
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
    private fun InformationRow(
        title: String,
        value: String
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.Top
        ) {

            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = value,
                fontSize = 14.sp,
                color = Color.White,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
    }
}