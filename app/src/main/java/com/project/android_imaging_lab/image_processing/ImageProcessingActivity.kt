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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.project.android_imaging_lab.ui.theme.Android_Imaging_LabTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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


@Composable
fun ImageProcessScreen(modifier: Modifier) {

    val context = LocalContext.current

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
            // TODO Camera Permission Checking and Live Preview

            Text(
                text = "LIVE",
                color = Color.Green,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            )

        }

        // TODO Set Dropdown For Camera Selection

        Spacer(modifier = modifier.height(10.dp))

        Text(
            text = "Image Format: YUV_420_888", // TODO
            color = Color.White
        )

        Button(
            onClick = {},
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
                .height(50.dp)
        ) {
            Text(text = "Capture YUV Frame")
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