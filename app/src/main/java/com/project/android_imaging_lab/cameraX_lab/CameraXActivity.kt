package com.project.android_imaging_lab.cameraX_lab

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.project.android_imaging_lab.ui.theme.Android_Imaging_LabTheme

class CameraXActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Android_Imaging_LabTheme {
                CameraScreen(Modifier)
            }
        }
    }


    @Composable
    fun CameraScreen(modifier: Modifier) {

        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        var isGranted by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            isGranted = granted
        }

        LaunchedEffect(Unit) {
            if (!isGranted) {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        if (isGranted) {
            AndroidView(
                modifier = modifier.fillMaxSize(),
                factory = { context ->

                    val previewView = PreviewView(context)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build()

                        preview.surfaceProvider = previewView.surfaceProvider
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)

                    }, ContextCompat.getMainExecutor(context))
                    previewView
                }
            )
        } else {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Camera permission is required")
            }
        }

    }


    /*@Composable
    fun ViewCamera(
        modifier: Modifier,
        onTakePhoto: () -> Unit = {},
        onCapturePhoto: () -> Unit = {}
    ) {
        Box(modifier.fillMaxSize()) {
            AndroidView(
                modifier = modifier.fillMaxSize(),
                factory = { context ->
                    PreviewView(context)
                }
            )

            Row(
                modifier.align(Alignment.BottomCenter)
                    .padding(bottom = 50.dp),
                horizontalArrangement = Arrangement.spacedBy(100.dp)
            ) {
                Button(
                    onClick = onTakePhoto,
                    modifier.size(110.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text("Take Photo", textAlign = TextAlign.Center)
                }

                Button(
                    onClick = onCapturePhoto,
                    modifier.size(110.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text("Start Capture", textAlign = TextAlign.Center)
                }

            }

        }

    }*/


}