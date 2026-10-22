package com.project.android_imaging_lab.camera2_lab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.android_imaging_lab.ui.theme.Android_Imaging_LabTheme

class Camera2Activity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Android_Imaging_LabTheme {
                Camera2Screen(Modifier)
            }
        }

    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Camera2Screen(modifier: Modifier) {

        var expanded by remember { mutableStateOf(false) }
        var selectedOption by remember { mutableStateOf("Back Camera") }

        val options = listOf("Back Camera", "Front Camera")

        Column(
            modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(20.dp)
        ) {

            Row(
                modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 10.dp)
            ) {
                Text(
                    text = "Camera2 Lab",
                    modifier.fillMaxWidth(),
                    fontSize = TextUnit(value = 20f, type = TextUnitType.Sp),
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(color = Color.White)
            )

            /*CAMERA SELECTION*/
            Text(
                text = "CAMERA SELECTION",
                fontSize = TextUnit(value = 16f, type = TextUnitType.Sp),
                textAlign = TextAlign.Start,
                color = Color.White,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                modifier = modifier.padding(top = 50.dp)
            )

            Text(
                text = "Select Camera",
                fontSize = TextUnit(value = 15f, type = TextUnitType.Sp),
                textAlign = TextAlign.Start,
                color = Color.White,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal,
                modifier = modifier.padding(top = 20.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = modifier.padding(top = 10.dp, bottom = 20.dp)
            ) {
                TextField(
                    value = selectedOption,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                selectedOption = option
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Spacer(
                modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(color = Color.White)
            )

            /*CAMERA CAPABILITIES*/

            /*HARDWARE LEVEL*/
            Text(
                text = "CAMERA CAPABILITIES",
                fontSize = TextUnit(value = 16f, type = TextUnitType.Sp),
                textAlign = TextAlign.Start,
                color = Color.White,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                modifier = modifier.padding(top = 50.dp)
            )

            Text(
                text = "Hardware Level",
                fontSize = TextUnit(value = 15f, type = TextUnitType.Sp),
                textAlign = TextAlign.Start,
                color = Color.White,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal,
                modifier = modifier.padding(top = 20.dp)
            )

            Text(
                text = "Full",
                fontSize = TextUnit(value = 15f, type = TextUnitType.Sp),
                textAlign = TextAlign.Start,
                color = Color.White,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal,
                modifier = modifier.padding(top = 5.dp, bottom = 10.dp)
            )

            Spacer(
                modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(color = Color.White)
            )

            /*MANUAL SENSOR*/
            Text(
                text = "Manual Sensor",
                fontSize = TextUnit(value = 15f, type = TextUnitType.Sp),
                textAlign = TextAlign.Start,
                color = Color.White,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal,
                modifier = modifier.padding(top = 20.dp)
            )

            Text(
                text = "Supported",
                fontSize = TextUnit(value = 15f, type = TextUnitType.Sp),
                textAlign = TextAlign.Start,
                color = Color.White,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal,
                modifier = modifier.padding(top = 5.dp, bottom = 10.dp)
            )

            Spacer(
                modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(color = Color.White)
            )

            /*AUTOFOCUS*/
            Text(
                text = "Autofocus",
                fontSize = TextUnit(value = 15f, type = TextUnitType.Sp),
                textAlign = TextAlign.Start,
                color = Color.White,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal,
                modifier = modifier.padding(top = 20.dp)
            )

            Text(
                text = "Supported",
                fontSize = TextUnit(value = 15f, type = TextUnitType.Sp),
                textAlign = TextAlign.Start,
                color = Color.White,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal,
                modifier = modifier.padding(top = 5.dp, bottom = 10.dp)
            )

            Spacer(
                modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(color = Color.White)
            )

            /*MANUAL FOCUS*/
            Text(
                text = "Manual Focus",
                fontSize = TextUnit(value = 15f, type = TextUnitType.Sp),
                textAlign = TextAlign.Start,
                color = Color.White,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal,
                modifier = modifier.padding(top = 20.dp)
            )

            Text(
                text = "Supported",
                fontSize = TextUnit(value = 15f, type = TextUnitType.Sp),
                textAlign = TextAlign.Start,
                color = Color.White,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal,
                modifier = modifier.padding(top = 5.dp, bottom = 10.dp)
            )


            Spacer(
                modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(color = Color.White)
            )

            /*CAMERA INFORMATION*/
            Text(
                text = "CAMERA INFORMATION",
                fontSize = TextUnit(value = 16f, type = TextUnitType.Sp),
                textAlign = TextAlign.Start,
                color = Color.White,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                modifier = modifier.padding(top = 30.dp)
            )

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .align(Alignment.Start)
            ) {
                Text(
                    text = "Camera ID:",
                    fontSize = TextUnit(value = 15f, type = TextUnitType.Sp),
                    textAlign = TextAlign.Start,
                    color = Color.White,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Normal,
                    modifier = modifier.padding(end = 10.dp)
                )
                Text(
                    text = "0",
                    fontSize = TextUnit(value = 15f, type = TextUnitType.Sp),
                    textAlign = TextAlign.Start,
                    color = Color.White,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Normal,
                )
            }

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .align(Alignment.Start)
            ) {
                Text(
                    text = "ISO Range:",
                    fontSize = TextUnit(value = 15f, type = TextUnitType.Sp),
                    textAlign = TextAlign.Start,
                    color = Color.White,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Normal,
                    modifier = modifier.padding(end = 10.dp)
                )
                Text(
                    text = "100–3200",
                    fontSize = TextUnit(value = 15f, type = TextUnitType.Sp),
                    textAlign = TextAlign.Start,
                    color = Color.White,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Normal,
                )
            }

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .align(Alignment.Start)
            ) {
                Text(
                    text = "Exposure Range:",
                    fontSize = TextUnit(value = 15f, type = TextUnitType.Sp),
                    textAlign = TextAlign.Start,
                    color = Color.White,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Normal,
                    modifier = modifier.padding(end = 10.dp)
                )
                Text(
                    text = "100000–1000000000 ns",
                    fontSize = TextUnit(value = 15f, type = TextUnitType.Sp),
                    textAlign = TextAlign.Start,
                    color = Color.White,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Normal,
                )
            }

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .align(Alignment.Start)
            ) {
                Text(
                    text = "Minimum Focus Distance:",
                    fontSize = TextUnit(value = 15f, type = TextUnitType.Sp),
                    textAlign = TextAlign.Start,
                    color = Color.White,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Normal,
                    modifier = modifier.padding(end = 10.dp)
                )
                Text(
                    text = "10.0 diopters",
                    fontSize = TextUnit(value = 15f, type = TextUnitType.Sp),
                    textAlign = TextAlign.Start,
                    color = Color.White,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Normal,
                )
            }

            Button(
                onClick = {},
                modifier = modifier.fillMaxWidth()
                    .height(40.dp)
                    .padding(top = 10.dp)
            ) {
                Text("Refresh Capabilities")
            }


        }
    }


}