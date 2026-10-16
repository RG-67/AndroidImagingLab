package com.project.android_imaging_lab.cameraX_lab

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.project.android_imaging_lab.ui.theme.Android_Imaging_LabTheme
import java.security.Permissions

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
    fun CameraScreen(
        modifier: Modifier
    ) {

        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        var allPermissionsGranted by remember {
            mutableStateOf(
                REQUEST_ALL_PERMISSION.all {
                    ContextCompat.checkSelfPermission(
                        context,
                        it
                    ) == PackageManager.PERMISSION_GRANTED
                }
            )
        }

        var isCameraClick by remember { mutableStateOf(false) }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            allPermissionsGranted = REQUEST_ALL_PERMISSION.all { permission ->
                permissions[permission] == true || ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

        LaunchedEffect(Unit) {
            if (!allPermissionsGranted) {
                permissionLauncher.launch(REQUEST_ALL_PERMISSION)
            }
        }

        if (allPermissionsGranted) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(50.dp)
            ) {

                if (isCameraClick) {
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
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview
                                )

                            }, ContextCompat.getMainExecutor(context))
                            previewView
                        }
                    )
                }

                if (!isCameraClick) {
                    Row(
                        modifier = modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { isCameraClick = true }
                        ) {
                            Text("Camera")
                        }
                        Button(
                            onClick = {}
                        ) {
                            Text("Video")
                        }
                    }
                } else {
                    Row(
                        modifier = modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { isCameraClick = false }
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                takePhoto()
                            }
                        ) {
                            Text("Take Picture")
                        }
                    }
                }


            }
        } else {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("All permissions are required")
            }
        }

    }

    private fun takePhoto() {
//        val imageCapture = imageCapture ?: return
    }


    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUEST_ALL_PERMISSION = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

    }


}