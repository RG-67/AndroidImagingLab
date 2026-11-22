package com.project.android_imaging_lab.image_processing

import android.Manifest
import android.R
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.project.android_imaging_lab.ui.theme.Android_Imaging_LabTheme
import com.project.android_imaging_lab.utils.camera2_util.Camera2Inspector
import com.project.android_imaging_lab.utils.camera2_util.Camera2Preview
import com.project.android_imaging_lab.utils.camera2_util.Camera2PreviewController
import com.project.android_imaging_lab.utils.camera2_util.CameraOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.Permission
import java.security.Permissions

/*
    Process:
    1) When "Capture YUV Frame" clicked the live image frame converted to YUV to RGB and set to "CAPTURED RGB IMAGE"
    2) When "Capture Again" clicked the live camera view can change and again clicked on "Capture YUV Frame" to capture the another frame and convert it to RGB
    3) When "Back to Camera" clicked then the captured RGB image should hide and set to default as Activity start
*/



class ImageProcessingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Android_Imaging_LabTheme {
                ImageProcessScreen(modifier = Modifier)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageProcessScreen(modifier: Modifier) {

    val context = LocalContext.current

    var originalFormat by remember { mutableStateOf("YUV_420_888") }
    var convertedFormat by remember { mutableStateOf("RGB_888") }
    var conversion by remember { mutableStateOf("SUCCESS") }
    var imageFormat by remember { mutableStateOf("YUV_420_888") }


    var isLoading by remember { mutableStateOf(false) }

    var expandedCamera by remember { mutableStateOf(false) }

    var cameraOptions by remember { mutableStateOf<List<CameraOption>>(emptyList()) }

    var selectedCameraId by remember { mutableStateOf<String?>(null) }

    val inspector = remember { Camera2Inspector(context) }

    var previewEnable by remember { mutableStateOf(true) }

    val previewController = remember(selectedCameraId) {
        selectedCameraId?.let { id ->
            Camera2PreviewController(context, id)
        }
    }

    DisposableEffect(previewController) {
        onDispose {
            previewController?.release()
        }
    }


    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            hasCameraPermission = granted
        }


    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
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

            Log.e("Image Processing", "Camera discovery failed", e)

        } finally {

            isLoading = false
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
            .padding(all = 16.dp)
    ) {

        // HEADER

        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Image Processing Lab",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        DividerLine()

        /*
        * YUV → RGB
        */

        Spacer(modifier = modifier.height(16.dp))

        SectionTitle("YUV to RGB")

        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {

            if (hasCameraPermission && previewEnable && previewController != null) {
                Camera2Preview(
                    controller = previewController,
                    modifier = modifier.fillMaxSize()
                )
            } else {
                Text(text = "CAMERA NOT OPEN", color = Color.White)
            }


            Text(
                text = "LIVE",
                color = Color.Green,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            )

        }

        Spacer(modifier = modifier.height(10.dp))

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


        Spacer(modifier = modifier.height(10.dp))


        Text(
            text = "Image Format: $imageFormat",
            color = Color.White
        )

        Button(
            onClick = {
                if (!hasCameraPermission) {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 20.dp)
                .height(50.dp)
        ) {
            Text(text = "Capture YUV Frame")
        }

        DividerLine()

        /*
        * RGB IMAGE CAPTURED
        */

        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(top = 20.dp)
                .background(Color.Black)
        ) {
            // TODO
        }

        Spacer(modifier.height(10.dp))

        Text(text = "Original Format: $originalFormat", color = Color.White)

        Text(text = "Converted Format: $convertedFormat", color = Color.White)

        Text(text = "Conversion: $conversion", color = Color.White)

        Row(
            modifier = modifier.fillMaxWidth()
        ) {

            Button(
                onClick = {}, // TODO
                modifier = modifier
                    .weight(1f)
                    .padding(vertical = 20.dp)
                    .height(50.dp)
            ) {
                Text(text = "Capture Again", color = Color.White)
            }

            Spacer(modifier.width(5.dp))

            Button(
                onClick = {}, // TODO
                modifier = modifier
                    .weight(1f)
                    .padding(vertical = 20.dp)
                    .height(50.dp)
            ) {
                Text(text = "Back to Camera", color = Color.White)
            }

        }


    }

}


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
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        textAlign = TextAlign.Start,
        modifier = Modifier.padding(bottom = 5.dp)
    )
}