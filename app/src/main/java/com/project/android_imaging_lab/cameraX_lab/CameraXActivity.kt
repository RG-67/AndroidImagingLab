package com.project.android_imaging_lab.cameraX_lab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
    fun ViewCamera(modifier: Modifier) {
        AndroidView(
            modifier = modifier.fillMaxSize(),
            factory = { context ->
                val preview = PreviewView(context)
                preview
            }
        )
    }


}