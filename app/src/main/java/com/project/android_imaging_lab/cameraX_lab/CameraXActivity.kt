package com.project.android_imaging_lab.cameraX_lab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.project.android_imaging_lab.ui.theme.Android_Imaging_LabTheme

class CameraXActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Android_Imaging_LabTheme {
                ViewCamera(Modifier)
            }
        }
    }


    @Composable
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

    }


}